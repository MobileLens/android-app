package com.mobilelens.mobilelens.model

import java.net.URL
import java.util.Date

data class DeviceInfo(
    val brand: String,
    val model: String,
    val imageURL: URL?,
    val releaseDate: Date?,
)