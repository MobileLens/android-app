package com.mobilelens.mobilelens.phones.model

data class VideoResolution(
    val width: Int,
    val height: Int,
    val fps: Int
) {
    override fun toString() : String {
        return "${width}x${height}@$fps"
    }
}