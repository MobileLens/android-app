package com.mobilelens.mobilelens.phones.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughIn
import com.mobilelens.mobilelens.core.ui.theme.fadeThroughOut
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.ui.components.PhoneListItem

@Composable
fun FavoritesScreen(
    favoritePhones: List<Phone>,
    onPhoneClick: (Phone) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Only switching between the empty state and the list fades; the list animates its own items
    AnimatedContent(
        targetState = favoritePhones,
        modifier = modifier,
        transitionSpec = { fadeThroughIn() togetherWith fadeThroughOut() },
        contentKey = { it.isEmpty() },
        label = "Favorites",
    ) { phones ->
        if (phones.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
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
                        text = stringResource(R.string.favorites_empty),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(phones, key = { it.id }) { phone ->
                    PhoneListItem(
                        phone = phone,
                        onClick = { onPhoneClick(phone) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    MaterialTheme {
        FavoritesScreen(
            favoritePhones = PhoneCatalogue.take(2),
            onPhoneClick = {}
        )
    }
}
