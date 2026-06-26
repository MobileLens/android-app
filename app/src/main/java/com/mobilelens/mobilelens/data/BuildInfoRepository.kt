package com.mobilelens.mobilelens.data

import android.os.Build
import com.mobilelens.mobilelens.model.DeviceInfo
import java.net.URL
import java.util.Date

class BuildInfoRepository {
    fun getBrand(): String = Build.MANUFACTURER

    fun getModel(): String = Build.MODEL

    fun getReleaseDate(): Date? {
        // Placeholder
        return null
    }

    fun getImageURL(): URL? {
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