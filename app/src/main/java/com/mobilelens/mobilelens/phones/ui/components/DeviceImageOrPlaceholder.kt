package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobilelens.mobilelens.phones.model.DeviceInfo

private val DeviceImageSize = 96.dp

@Composable
fun DeviceImageOrPlaceholder(
    deviceInfo: DeviceInfo,
) {
    val imageModifier = Modifier
        .size(DeviceImageSize)
        .clip(MaterialTheme.shapes.extraLarge)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)

    if (deviceInfo.imageURL != null) {
        AsyncImage(
            model = deviceInfo.imageURL,
            contentDescription = "${deviceInfo.brand} ${deviceInfo.model}",
            modifier = imageModifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = imageModifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceImageOrPlaceholderPreview() {
    MaterialTheme {
        DeviceImageOrPlaceholder(
            deviceInfo = DeviceInfo("Google", "Pixel 8 Pro", null, "2023")
        )
    }
}
