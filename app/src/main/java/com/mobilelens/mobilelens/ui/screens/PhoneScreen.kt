package com.mobilelens.mobilelens.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.ui.components.DeviceLensDetail

@Composable
fun PhoneScreen(
    phone: Phone,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fabExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        DeviceLensDetail(
            lenses = phone.lenses,
            deviceInfo = phone.deviceInfo,
            isFavorited = isFavorited,
            onFavoriteClick = onFavoriteClick,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Menu items
            val items = listOf(
                Triple(Icons.Filled.Star, "Reviews", 0),
                Triple(Icons.AutoMirrored.Filled.CompareArrows, "Compare", 1),
            )
            items.forEach { (icon, label, index) ->
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn(tween(delayMillis = index * 40)) +
                            slideInVertically(tween(delayMillis = index * 40)) { it / 2 },
                    exit = fadeOut(tween(durationMillis = 100)) +
                            slideOutVertically(tween(durationMillis = 100)) { it / 2 },
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { fabExpanded = false },
                        icon = { Icon(icon, contentDescription = null) },
                        text = { Text(label) },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            // Main toggle FAB
            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                modifier = Modifier.semantics {
                    contentDescription = if (fabExpanded) "Close menu" else "Open menu"
                },
            ) {
                Icon(
                    imageVector = if (fabExpanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = null,
                )
            }
        }
    }
}