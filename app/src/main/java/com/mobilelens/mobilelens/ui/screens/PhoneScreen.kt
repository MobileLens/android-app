package com.mobilelens.mobilelens.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.ui.components.DeviceLensDetail

@Composable
fun PhoneScreen(
    phone: Phone,
    modifier: Modifier = Modifier
) {
    DeviceLensDetail(
        lenses = phone.lenses,
        deviceInfo = phone.deviceInfo,
        modifier = modifier,
    )
}