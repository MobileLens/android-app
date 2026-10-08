package com.mobilelens.mobilelens

import android.app.Application
import com.mobilelens.mobilelens.core.data.AuthTokenStore
import com.mobilelens.mobilelens.core.data.remote.ApiClient

class MobileLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(AuthTokenStore.getInstance(this))
    }
}
