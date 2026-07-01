package com.mobilelens.mobilelens.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.ui.components.PhoneListItem

@Composable
fun FavoritesScreen(
    favoritePhones: List<Phone>,
    onPhoneClick: (Phone) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (favoritePhones.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "No favorites yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            items(favoritePhones, key = { it.id }) { phone ->
                PhoneListItem(
                    phone = phone,
                    onClick = { onPhoneClick(phone) }
                )
            }
        }
    }
}
