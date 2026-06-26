package com.mobilelens.mobilelens

import android.hardware.camera2.CameraManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobilelens.mobilelens.data.BuildInfoRepository
import com.mobilelens.mobilelens.data.CameraHardwareRepository
import com.mobilelens.mobilelens.ui.MainApp
import com.mobilelens.mobilelens.ui.theme.MobileLensTheme
import com.mobilelens.mobilelens.viewmodel.CameraViewModel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileLensTheme {
                MainApp(cameraViewModel = cameraViewModel)
            }
        }
    }
}
