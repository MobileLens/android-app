package com.mobilelens.mobilelens.phones.model

data class Brand(
    val id: String,
    val name: String,
)

/**
 * Catalogue filters. The backend requires the lens filters ([lensType], [stabilization],
 * [opticalZoom]) to be met by one and the same camera of a phone.
 */
data class CatalogueFilters(
    val brandId: String? = null,
    val lensType: LensType? = null,
    val stabilization: Stabilization? = null,
    val verifiedOnly: Boolean = false,
    val opticalZoom: Boolean = false,
) {
    val isActive: Boolean
        get() = this != CatalogueFilters()
}
