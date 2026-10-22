package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Phone

@Composable
fun PhoneListItem(
    phone: Phone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(phone.deviceInfo.model) },
        supportingContent = {
            Text(
                text = phone.deviceInfo.brand,
                fontWeight = FontWeight.Bold
            ) },
        leadingContent = {
            DeviceImageOrPlaceholder(phone.deviceInfo)
        },
        // See-through, so the row takes the colour of whatever it sits on
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun PhoneListItemPreview() {
    MaterialTheme {
        PhoneListItem(
            phone = PhoneCatalogue[0],
            onClick = {}
        )
    }
}
