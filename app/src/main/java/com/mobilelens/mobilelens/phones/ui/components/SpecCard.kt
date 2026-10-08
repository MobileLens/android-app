package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpecCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Shrinks for long labels such as "Focal length (35mm)"
        BasicText(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 10.sp,
                maxFontSize = 16.sp,
            ),
        )
        // Shrinks in narrow rows (e.g. "12.34x" next to OIS and AF zones)
        BasicText(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
            ),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 10.sp,
                maxFontSize = 22.sp,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SpecCardPreview() {
    MaterialTheme {
        SpecCard(
            title = "Crop factor",
            value = "12.34x"
        )
    }
}
