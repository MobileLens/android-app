package com.mobilelens.mobilelens.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mobilelens.mobilelens.model.DeviceInfo
import com.mobilelens.mobilelens.model.Lens
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.ui.components.DeviceInfoSection
import com.mobilelens.mobilelens.ui.components.LensSpecs

@Composable
fun PhoneScreen(
    phone: Phone,
    modifier: Modifier = Modifier
) {
    val deviceInfo: DeviceInfo = phone.deviceInfo
    val lenses: List<Lens> = phone.lenses

    val scrollState = rememberScrollState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        DeviceInfoSection(deviceInfo)

        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex
        ) {
            lenses.forEachIndexed { index, _ ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text("Camera ${index + 1}") }
                )
            }
        }

        if (lenses.isNotEmpty()) {
            LensSpecs(lenses[selectedTabIndex])
        }
    }
}