package com.mobilelens.mobilelens.phones.model

data class Phone(
    val id: Int,
    val deviceInfo: DeviceInfo,
    val lenses: List<Lens>,
)
