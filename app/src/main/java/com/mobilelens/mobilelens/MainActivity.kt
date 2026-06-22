package com.mobilelens.mobilelens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mobilelens.mobilelens.ui.MainApp
import com.mobilelens.mobilelens.ui.theme.MobileLensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileLensTheme {
                MainApp()
            }
        }
    }
}
