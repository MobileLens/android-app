package com.mobilelens.mobilelens.ui.components

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

@Composable
fun DeviceLensDetail(
    lenses: List<Lens>,
    deviceInfo: DeviceInfo,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        DeviceInfoSection(deviceInfo)

        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
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
