package com.mobilelens.mobilelens.model

data class Phone(
    val id: Int,
    val deviceInfo: DeviceInfo,
    val lenses: List<Lens>,
)
