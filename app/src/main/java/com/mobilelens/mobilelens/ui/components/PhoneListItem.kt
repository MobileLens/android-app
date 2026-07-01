package com.mobilelens.mobilelens.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.data.displayName

@Composable
fun PhoneListItem(
    phone: Phone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    supportingText: String? = null,
) {
    ListItem(
        headlineContent = { Text(phone.displayName) },
        supportingContent = supportingText?.let { { Text(it) } },
        leadingContent = {
            Icon(Icons.Default.PhoneAndroid, contentDescription = null)
        },
        colors = ListItemDefaults.colors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                else -> Color.Transparent
            },
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
