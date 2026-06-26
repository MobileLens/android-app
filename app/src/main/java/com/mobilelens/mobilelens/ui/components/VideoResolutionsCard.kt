package com.mobilelens.mobilelens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobilelens.mobilelens.model.VideoResolution

@Composable
fun VideoResolutionsCard(
    resolutions: List<VideoResolution>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
           text = "Video resolutions",
           color = MaterialTheme.colorScheme.onSecondaryContainer,
           fontSize = 16.sp,
           fontWeight = FontWeight.Normal
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            resolutions.forEach { videoResolution ->
                Text(
                    text = "${videoResolution.width}x${videoResolution.height}@${videoResolution.fps}",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

    }
}