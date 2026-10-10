package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.mobilelens.mobilelens.core.ui.localizedDate
import com.mobilelens.mobilelens.core.ui.theme.Motion
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.DeviceInfo

// The heart grows from this size as it fills, or shrinks to it as it empties
private const val HEART_SMALL_SCALE = 0.4f
private val HeartPopSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium,
)

@Composable
fun DeviceInfoSection(
    deviceInfo: DeviceInfo,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DeviceImageOrPlaceholder(deviceInfo)

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = deviceInfo.brand,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = deviceInfo.model,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (deviceInfo.releaseDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.phone_released, localizedDate(deviceInfo.releaseDate)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (onFavoriteClick != null) {
                IconButton(onClick = onFavoriteClick) {
                    AnimatedContent(
                        targetState = isFavorite,
                        transitionSpec = {
                            if (targetState) {
                                // The filled heart pops in with a bounce over the fading outline
                                (scaleIn(HeartPopSpec, HEART_SMALL_SCALE) + fadeIn(tween(Motion.DURATION_SHORT)))
                                    .togetherWith(fadeOut(tween(Motion.DURATION_SHORT)))
                            } else {
                                fadeIn(tween(Motion.DURATION_SHORT))
                                    .togetherWith(
                                        scaleOut(tween(Motion.DURATION_SHORT), HEART_SMALL_SCALE) +
                                                fadeOut(tween(Motion.DURATION_SHORT))
                                    )
                            }
                        },
                        label = "FavoriteIcon",
                    ) { favorite ->
                        Icon(
                            imageVector = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(
                                if (favorite) R.string.phone_remove_favorite else R.string.phone_add_favorite
                            ),
                            tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceInfoSectionPreview() {
    MaterialTheme {
        DeviceInfoSection(
            deviceInfo = PhoneCatalogue[0].deviceInfo,
            isFavorite = true,
            onFavoriteClick = {}
        )
    }
}
