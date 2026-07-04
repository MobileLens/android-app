package com.mobilelens.mobilelens.core.data

import android.os.Build
import com.mobilelens.mobilelens.phones.model.DeviceInfo

class BuildInfoRepository {
    fun getBrand(): String = Build.MANUFACTURER

    fun getModel(): String = Build.MODEL

    fun getReleaseDate(): String? {
        // Placeholder
        return null
    }

    fun getImageURL(): String? {
        // Placeholder
        return null
    }

    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            brand = getBrand(),
            model = getModel(),
            releaseDate = getReleaseDate(),
            imageURL = getImageURL()
        )
    }
}