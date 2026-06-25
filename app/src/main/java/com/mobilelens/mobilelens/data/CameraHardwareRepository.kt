package com.mobilelens.mobilelens.data

import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.util.Log
import android.util.Size
import android.util.SizeF
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

const val TAG = "CameraHardwareRepository"

class CameraHardwareRepository(private val cameraManager: CameraManager) {
    fun getAllCamerasSpecs() : List<Lens> {
        cameraManager.cameraIdList.forEach { cameraId ->
            Log.i(TAG, "Processing cameraID = $cameraId")

            val cameraSpecs = cameraManager.getCameraCharacteristics(cameraId)
            val physicalCameraIds = cameraSpecs.physicalCameraIds


            if (physicalCameraIds.isNotEmpty()) {
                // Processing the data
                physicalCameraIds.forEach { physicalCameraId ->
                    Log.i(TAG, "Processing physical camera $physicalCameraId")

                    try {
                        val lens = cameraManager.getCameraCharacteristics(physicalCameraId)

                        // Lens facing
                        // Defaulting to Facing.OTHER in case Camera2 returns null
                        val facing: Facing = when (lens.get(CameraCharacteristics.LENS_FACING)) {
                            CameraCharacteristics.LENS_FACING_FRONT -> Facing.FRONT
                            CameraCharacteristics.LENS_FACING_BACK -> Facing.BACK
                            CameraCharacteristics.LENS_FACING_EXTERNAL -> Facing.OTHER
                            else -> Facing.OTHER // Default to OTHER
                        }

                        // Physical focal lengths [mm]
                        // List of focal length in case the phone has variable focal length (e.g. Xiaomi 17 Ultra, Sony Xperia 1 VI)
                        // This should be physical focal length (ranging from 1 to 8mm for wide-angle lenses, for periscope telephotos much more, not sure how much)
                        // TODO: check more manufacturers if they aren't providing 35mm equivalents here
                        // TODO: check what focal lengths are available on phones with variable focal length
                        val focalLengths: List<Float> =
                            lens.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.map { it }
                                ?: listOf()

                        // Physical apertures [f-stop?]
                        // List of apertures in case the phone has variable apertures (e.g. Samsung Galaxy S10, Xiaomi 17 Ultra)
                        // This should be physical aperture (ranging from f/1.2 to f/3.0 usually, equivalent should be above f/5.0)
                        // TODO: check, more manufacturers if they aren't providing 35mm equivalents here
                        // TODO: check how it look on phones with variable aperture
                        val apertures: List<Float> =
                            lens.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.map { it }
                                ?: listOf()

                        // Skipping cameras with no apertures/focal lengths
                        if (focalLengths.isEmpty() || apertures.isEmpty()) {
                            Log.i(TAG, "Skipping camera $physicalCameraId - No apertures/focal lengths")
                            return@forEach
                        }

                        // Getting active array size and calculating the resolution
                        val activeArraySize: Rect =
                            lens.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?:
                            Rect(0, 0, 0, 0)

                        val activeArrayWidth = activeArraySize.width()
                        val activeArrayHeight = activeArraySize.height()

                        // Active resolution in MP
                        var activeResolution: Float = (activeArrayWidth * activeArrayHeight) / 1000000f
                        activeResolution = "%.1f".format(activeResolution).toFloat() // Fantastic rounding

                        // TODO: implement ultra high resolution camera detection (48MP+)
                        val resolution = activeResolution

                        // Video resolutions
                        var resolutionList = mutableListOf<String>()

                        val map = lens.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                            ?: return emptyList()

                        val videoSizes: Array<Size> = map.getOutputSizes(MediaRecorder::class.java)
                            ?: map.getOutputSizes(SurfaceTexture::class.java)
                            ?: return emptyList()

                        for (size in videoSizes) {
                            val minFrameDurationNs = map.getOutputMinFrameDuration(MediaRecorder::class.java, size)

                            if (minFrameDurationNs > 0) {
                                val maxFps = (1_000_000_000L / minFrameDurationNs).toInt()
                                val roundedFps = roundToStandardFps(maxFps)

                                resolutionList.add("${size.width}x${size.height}@$roundedFps")
                            } else {
                                // TODO: implement fallback
                            }
                        }

                        // Sort resolutions (highest first)
                        resolutionList.sortByDescending {
                            val width = it.split("x").firstOrNull()?.toIntOrNull() ?: 0
                            width
                        }

                        // Image stabilization
                        // There's no way to distinguish standard OIS from Sensor-shift through Camera2 API
                        // Therefore we are defaulting to OIS if any stablization is detected
                        val imageStabilization: Stabilization =
                            when (lens.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                                ?.firstOrNull {
                                    it == CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON
                                }
                            ) {
                                CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON -> Stabilization.OIS
                                else -> Stabilization.NONE
                            }

                        // Physical sensor size [mm]
                        val physicalSensorSize: SizeF =
                            lens.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE) ?: SizeF(0f,0f)

                        // Diagonal of sensor
                        val diag: Float = sqrt(physicalSensorSize.width.pow(2) + physicalSensorSize.height.pow(2))
                        val diag35mm: Float = sqrt(35f.pow(2) + 24f.pow(2)) // Diagonal of standard 35mm sensor

                        // Pixel pitch [um]
                        // Assuming the pixels are square
                        // Pixel pitch = sensorWidth / pixelArrayWidth
                        val pixelPitch = (physicalSensorSize.width / activeArraySize.width().toFloat()) * 1000f

                        // Crop factor
                        // Crop factor = 35mm diagonal / sensor diagonal
                        val cropFactor = diag35mm / diag

                        // Sensor type [1/x"]
                        // Calculating and storing only the denominator
                        val sensorTypeDenominator = 16f / diag

                        // TODO: create a new lens

                    } catch (e: Exception) {
                        Log.e(TAG, "Error during processing physical camera $physicalCameraId: ${e.message}")
                    }
                }
            } else {
                // No ids found, skipping for now (brute-forcing ids might be necessary for some brands) <- TODO
                Log.i(TAG, "No physical IDs found for cameraID = $cameraId. Skipping...")
            }
        }

        return emptyList() // Temporary
    }

    private fun roundToStandardFps(fps: Int): Int {
        return when (fps) {
            in 28..31 -> 30
            in 58..62 -> 60
            in 118..122 -> 120
            in 238..242 -> 240
            else -> fps
        }
    }
}