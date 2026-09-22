package com.mobilelens.mobilelens.phones.data

import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Phone

val PhoneCatalogue = listOf(
    Phone(
        id = "1",
        deviceInfo = DeviceInfo(
            brand = "Apple",
            model = "iPhone 17 Pro",
            releaseDate = "2025-09-19",
            imageURL = null
        ),
        lenses = emptyList()
    )
)

val Phone.displayName: String
    get() = "${deviceInfo.brand} ${deviceInfo.model}"

