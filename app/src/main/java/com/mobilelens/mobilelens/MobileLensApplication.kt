package com.mobilelens.mobilelens

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.mobilelens.mobilelens.core.data.AuthTokenStore
import com.mobilelens.mobilelens.core.data.remote.ApiClient
import com.mobilelens.mobilelens.core.ui.theme.Motion

class MobileLensApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(AuthTokenStore.getInstance(this))
    }

    // Images fade in once downloaded; ones already in memory show straight away
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(Motion.DURATION_MEDIUM)
        .build()
}
