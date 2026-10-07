package com.mobilelens.mobilelens.phones.data

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.util.Log
import com.mobilelens.mobilelens.phones.model.Facing
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.model.VideoResolution

private const val TAG = "CameraHardwareRepository"

// Highest numeric camera ID probed when looking for cameras missing from cameraIdList
private const val MAX_PROBED_CAMERA_ID = 99

// Tab order: back cameras first
private val FACING_ORDER = listOf(Facing.BACK, Facing.FRONT, Facing.OTHER)

class CameraHardwareRepository(private val cameraManager: CameraManager) {
    fun getAllCamerasSpecs(): List<Lens> {
        val lenses = mutableListOf<Lens>()

        for (cameraId in cameraIdsToRead()) {
            val lens = readLens(cameraId) ?: continue

            if (lenses.any { it.isSameCameraAs(lens) }) {
                Log.i(TAG, "Skipping duplicate camera $cameraId")
                continue
            }
            Log.i(TAG, "Adding camera $cameraId")
            lenses += lens
        }

        // Stable sort, so cameras keep their discovery order within each facing (main camera usually first)
        return lenses.sortedBy { FACING_ORDER.indexOf(it.facing) }
    }

    /**
     * IDs of the cameras to read: the physical cameras behind each logical camera, or the logical
     * camera itself when it doesn't list any. In the latter case numeric IDs missing from
     * cameraIdList are also probed (once), since some OEMs hide auxiliary cameras there.
     */
    private fun cameraIdsToRead(): Set<String> {
        val publicIds = cameraManager.cameraIdList.toList()
        val ids = linkedSetOf<String>()
        var probeHiddenIds = false

        for (cameraId in publicIds) {
            Log.i(TAG, "Processing cameraID = $cameraId")

            val physicalIds = try {
                cameraManager.getCameraCharacteristics(cameraId).physicalCameraIds
            } catch (e: Exception) {
                Log.e(TAG, "Error reading camera $cameraId: ${e.message}")
                continue
            }

            if (physicalIds.isNotEmpty()) {
                Log.i(TAG, "Found ${physicalIds.size} physical cameras for logical camera $cameraId")
                ids += physicalIds
            } else {
                Log.i(TAG, "No physical cameras found for logical camera $cameraId, will probe hidden IDs")
                ids += cameraId
                probeHiddenIds = true
            }
        }

        if (probeHiddenIds) {
            ids += probeHiddenCameraIds(publicIds)
        }
        return ids
    }

    private fun probeHiddenCameraIds(publicIds: List<String>): List<String> =
        (0..MAX_PROBED_CAMERA_ID)
            .map { it.toString() }
            .filter { it !in publicIds }
            .filter { cameraId ->
                try {
                    cameraManager.getCameraCharacteristics(cameraId)
                    Log.i(TAG, "Discovered camera: $cameraId")
                    true
                } catch (_: Exception) {
                    false
                }
            }

    private fun readLens(cameraId: String): Lens? = try {
        buildLens(cameraId, cameraManager.getCameraCharacteristics(cameraId))
    } catch (e: Exception) {
        Log.e(TAG, "Error during processing camera $cameraId: ${e.message}")
        null
    }

    private fun buildLens(cameraId: String, specs: CameraCharacteristics): Lens? {
        // Physical focal lengths [mm] and apertures [f-number], not 35 mm equivalents.
        // Lists, because some phones have variable focal length (e.g. Xiaomi 17 Ultra, Sony Xperia 1 VI)
        // or variable aperture (e.g. Samsung Galaxy S10, Xiaomi 17 Ultra)
        // TODO: check more manufacturers if they aren't providing 35mm equivalents here
        // TODO: check what focal lengths/apertures look like on phones with variable ones
        val focalLengths = specs.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.toList().orEmpty()
        val apertures = specs.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.toList().orEmpty()

        if (focalLengths.isEmpty() || apertures.isEmpty()) {
            Log.i(TAG, "Skipping camera $cameraId - No apertures/focal lengths")
            return null
        }

        // Physical size [mm] covers the full pixel array
        val sensorSize = specs.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        val pixelArray = specs.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        val activeArray = specs.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)

        // Every derived value divides by these, so a camera without them can't be described
        if (sensorSize == null || sensorSize.width <= 0f || sensorSize.height <= 0f ||
            pixelArray == null || pixelArray.width <= 0 ||
            activeArray == null || activeArray.width() <= 0 || activeArray.height() <= 0
        ) {
            Log.i(TAG, "Skipping camera $cameraId - Incomplete sensor info")
            return null
        }

        // Ultra high resolution (e.g. 48 MP quad-Bayer) sensors output binned frames by default
        // and report their full resolution only through the *_MAXIMUM_RESOLUTION keys
        val maxResActiveArray = specs.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE_MAXIMUM_RESOLUTION)
        val maxResPixelArray = specs.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE_MAXIMUM_RESOLUTION)

        val activeResolution = megapixels(activeArray.width(), activeArray.height())
        val resolution = maxResActiveArray?.let { megapixels(it.width(), it.height()) } ?: activeResolution
        val cropFactor = cropFactor(sensorSize.width, sensorSize.height)

        return Lens(
            focalLength = focalLengths,
            aperture = apertures,
            cropFactor = cropFactor,
            sensorTypeDenominator = sensorTypeDenominator(sensorSize.width, sensorSize.height),
            facing = facingOf(specs),
            // Native (unbinned) pitch when the sensor reports its full-resolution pixel array
            pixelPitchUm = pixelPitchUm(sensorSize.width, maxResPixelArray?.width ?: pixelArray.width),
            resolution = resolution,
            activeResolution = activeResolution,
            afZones = 1,
            stabilization = stabilizationOf(specs),
            videoResolutions = readVideoResolutions(specs),
            type = lensTypeFor(focalLengths.first() * cropFactor),
        )
    }

    private fun facingOf(specs: CameraCharacteristics): Facing =
        when (specs.get(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_FRONT -> Facing.FRONT
            CameraCharacteristics.LENS_FACING_BACK -> Facing.BACK
            else -> Facing.OTHER // External, or null from Camera2
        }

    // There's no way to distinguish standard OIS from sensor-shift through Camera2 API,
    // therefore we are defaulting to OIS if any stabilization is detected
    private fun stabilizationOf(specs: CameraCharacteristics): Stabilization {
        val modes = specs.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
        return if (modes?.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) == true) {
            Stabilization.OIS
        } else {
            Stabilization.NONE
        }
    }

    private fun readVideoResolutions(specs: CameraCharacteristics): List<VideoResolution> {
        val map = specs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return emptyList()

        // Frame durations must be queried for the same output class the sizes came from
        val modes = map.getOutputSizes(MediaRecorder::class.java)?.map {
            StreamMode(it.width, it.height, map.getOutputMinFrameDuration(MediaRecorder::class.java, it))
        } ?: map.getOutputSizes(SurfaceTexture::class.java)?.map {
            StreamMode(it.width, it.height, map.getOutputMinFrameDuration(SurfaceTexture::class.java, it))
        } ?: emptyList()

        return videoResolutionsFrom(modes)
    }
}
