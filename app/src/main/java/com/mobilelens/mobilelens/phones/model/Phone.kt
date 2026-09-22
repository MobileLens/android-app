package com.mobilelens.mobilelens.phones.model

data class Phone(
    val id: String,
    val deviceInfo: DeviceInfo,
    val lenses: List<Lens>,
)
