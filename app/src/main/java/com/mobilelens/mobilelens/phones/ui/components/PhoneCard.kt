package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Phone

/** Margins around a list of [PhoneCard]s, shared so every such list lines up the same way. */
val PhoneCardListContentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

/** The gap between [PhoneCard]s in a list. */
val PhoneCardListArrangement = Arrangement.spacedBy(8.dp)

private val CardPadding = 12.dp

/**
 * A phone in the catalogue or favorites list, as a Material 3 elevated card: a tonal surface with
 * large corners that sits slightly above the screen, holding the photo and the name.
 *
 * The corners are nested: the card's corner (`extraLarge`, 28dp) is the photo's (`large`, 16dp)
 * plus [CardPadding], so the gap around the photo stays even all the way round.
 */
@Composable
fun PhoneCard(
    phone: Phone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "PhoneCardContainer",
    )
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier.padding(CardPadding),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DeviceImageOrPlaceholder(
                deviceInfo = phone.deviceInfo,
                shape = MaterialTheme.shapes.large,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = phone.deviceInfo.model,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = phone.deviceInfo.brand,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(PhoneCardListContentPadding),
            verticalArrangement = PhoneCardListArrangement,
        ) {
            PhoneCard(
                phone = PhoneCatalogue[0],
                onClick = {},
            )
            PhoneCard(
                phone = PhoneCatalogue[1],
                onClick = {},
                isSelected = true,
            )
        }
    }
}
