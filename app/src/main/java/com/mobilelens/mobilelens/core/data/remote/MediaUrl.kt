package com.mobilelens.mobilelens.core.data.remote

private val STORAGE_URL = Regex("^(?:storage|minio)://([^/]+)/(.+)$")

/**
 * The permanent public address of a stored file, from its `storage://<bucket>/<objectKey>` reference
 * (legacy `minio://` references too). The reverse proxy serves public files under `/media/<bucket>/`.
 *
 * A file only resolves there once it is public: review media stays private until its review is
 * published, and until then only the short-lived signed `url` from the upload response works.
 * Returns null when [storageUrl] isn't a storage reference.
 */
internal fun publicMediaUrl(storageUrl: String, baseUrl: String = ApiClient.BASE_URL): String? {
    val match = STORAGE_URL.matchEntire(storageUrl) ?: return null
    val (bucket, objectKey) = match.destructured
    return "${baseUrl.trimEnd('/')}/media/$bucket/$objectKey"
}
