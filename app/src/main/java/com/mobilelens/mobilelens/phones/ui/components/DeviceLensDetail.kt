package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.core.ui.theme.rememberSlideDistance
import com.mobilelens.mobilelens.core.ui.theme.sharedAxisXIn
import com.mobilelens.mobilelens.core.ui.theme.sharedAxisXOut
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.DeviceInfo
import com.mobilelens.mobilelens.phones.model.Lens
import com.mobilelens.mobilelens.phones.ui.tabLabel

@Composable
fun DeviceLensDetail(
    lenses: List<Lens>,
    deviceInfo: DeviceInfo,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    // Shown below the specs, scrolling with them
    footer: @Composable () -> Unit = {},
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val safeTabIndex = selectedTabIndex.coerceIn(0, (lenses.size - 1).coerceAtLeast(0))
    val slideDistance = rememberSlideDistance()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        DeviceInfoSection(
            deviceInfo = deviceInfo,
            isFavorite = isFavorite,
            onFavoriteClick = onFavoriteClick
        )

        if (lenses.isNotEmpty()) {
            PrimaryScrollableTabRow(
                selectedTabIndex = safeTabIndex,
                edgePadding = 16.dp,
            ) {
                lenses.forEachIndexed { index, _ ->
                    Tab(
                        selected = safeTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(lenses[index].tabLabel()) }
                    )
                }
            }

            // Slides the way the tabs are laid out, so a lens to the right comes in from the right
            AnimatedContent(
                targetState = safeTabIndex,
                transitionSpec = {
                    val forward = targetState > initialState
                    sharedAxisXIn(forward, slideDistance) togetherWith sharedAxisXOut(forward, slideDistance)
                },
                label = "LensSpecs",
            ) { index ->
                // The lens list may have changed since this tab was left
                lenses.getOrNull(index)?.let { LensSpecs(it) }
            }
        }

        footer()
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceLensDetailPreview() {
    com.mobilelens.mobilelens.core.ui.theme.MobileLensTheme {
        DeviceLensDetail(
            lenses = PhoneCatalogue[0].lenses,
            deviceInfo = PhoneCatalogue[0].deviceInfo,
            isFavorite = true,
            onFavoriteClick = {}
        )
    }
}
