package com.mobilelens.mobilelens.model

import java.net.URL
import java.util.Date

data class Phone(
    val id: Int,
    val brand: String,
    val model: String,
    val releaseDate: Date?,
    val lenses: List<Lens>,
    val imageURL: URL?,
)