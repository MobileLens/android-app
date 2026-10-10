package com.mobilelens.mobilelens.phones.model

/** Catalogue orderings, with the `sort` query values of `GET api/smartphones`. */
enum class CatalogueSort(val apiValue: String) {
    /** Alphabetical by model name, the backend default. */
    ALL("name"),

    /** Newest release date first. */
    NEW("new"),

    /** Most viewed first. */
    TRENDING("trending"),
}
