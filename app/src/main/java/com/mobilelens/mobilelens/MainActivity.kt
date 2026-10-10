package com.mobilelens.mobilelens

import android.content.Context
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobilelens.mobilelens.core.data.BuildInfoRepository
import com.mobilelens.mobilelens.phones.data.CameraHardwareRepository
import com.mobilelens.mobilelens.core.ui.theme.MobileLensTheme
import com.mobilelens.mobilelens.phones.viewmodel.CameraViewModel
import com.mobilelens.mobilelens.settings.data.AppLanguages
import com.mobilelens.mobilelens.settings.data.AppSettingsStore
import com.mobilelens.mobilelens.settings.model.isDark

class MainActivity : ComponentActivity() {
    private val cameraViewModel: CameraViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val cameraManager = getSystemService(CameraManager::class.java)
                return CameraViewModel(
                    CameraHardwareRepository(cameraManager),
                    BuildInfoRepository()
                ) as T
            }
        }
    }

    // Before Android 13 the app's language has to be applied to each activity as it's created
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        AppLanguages.configurationOverride(newBase)?.let { applyOverrideConfiguration(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsStore = AppSettingsStore.getInstance(this)
        setContent {
            val settings by settingsStore.settings.collectAsState()
            val darkTheme = settings.themeMode.isDark(systemIsDark = isSystemInDarkTheme())

            // The system bars' icons follow the app's theme, which can differ from the system's
            DisposableEffect(darkTheme) {
                val barStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                onDispose {}
            }

            MobileLensTheme(darkTheme = darkTheme, dynamicColor = settings.dynamicColor) {
                MainApp(cameraViewModel = cameraViewModel)
            }
        }
    }
}
