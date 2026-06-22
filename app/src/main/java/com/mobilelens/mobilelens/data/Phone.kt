package com.mobilelens.mobilelens.data

import java.util.Date

data class Phone(
    val id: Int,
    val brand: String,
    val model: String,
    val releaseDate: Date?,
    val lenses: List<Lens>,
)