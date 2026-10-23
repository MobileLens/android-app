package com.mobilelens.mobilelens.phones.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.core.ui.UiStateCrossfade
import com.mobilelens.mobilelens.core.ui.localizedDate
import com.mobilelens.mobilelens.core.ui.theme.rememberSlideDistance
import com.mobilelens.mobilelens.core.ui.theme.sharedAxisXIn
import com.mobilelens.mobilelens.core.ui.theme.sharedAxisXOut
import com.mobilelens.mobilelens.phones.data.LensPair
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.data.pairLenses
import com.mobilelens.mobilelens.phones.ui.components.CompareLensSpecs
import com.mobilelens.mobilelens.phones.ui.components.DeviceImageOrPlaceholder
import com.mobilelens.mobilelens.phones.ui.lensKindLabel
import com.mobilelens.mobilelens.phones.viewmodel.ComparedPhone
import com.mobilelens.mobilelens.phones.viewmodel.CompareUiState

/**
 * Two phones side by side, with a floating toolbar to swap them or pick another right-hand phone.
 * Has its own top bar, so MainApp hides the search bar here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    uiState: CompareUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onChangePhone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.compare_title)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
            },
            // Insets are handled by the parent Scaffold
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        UiStateCrossfade(state = uiState, modifier = Modifier.weight(1f)) { state ->
            when (state) {
                CompareUiState.Loading -> LoadingContent()
                is CompareUiState.Error -> ErrorContent(messageRes = state.messageRes, onRetry = onRetry)
                is CompareUiState.Success -> CompareContent(
                    first = state.first,
                    second = state.second,
                    onChangePhone = onChangePhone,
                )
            }
        }
    }
}

/**
 * The phones' headers, then a tab for each kind of lens either phone has, comparing that lens on
 * both. A floating toolbar at the bottom swaps the sides or picks another phone for the right.
 */
@Composable
fun CompareContent(
    first: ComparedPhone,
    second: ComparedPhone,
    onChangePhone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var swapped by rememberSaveable { mutableStateOf(false) }
    // Half a turn per swap, so the arrows point the way the phones just moved
    val swapRotation by animateFloatAsState(
        targetValue = if (swapped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "CompareSwapRotation",
    )
    // Hides the toolbar while scrolling down, brings it back on the way up
    val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom,
    )
    val left = if (swapped) second else first
    val right = if (swapped) first else second
    val pairs = remember(left, right) { pairLenses(left.phone.lenses, right.phone.lenses) }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val safeTabIndex = selectedTabIndex.coerceIn(0, (pairs.size - 1).coerceAtLeast(0))
    val slideDistance = rememberSlideDistance()

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(toolbarScrollBehavior),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CompareHeader(left = left, right = right)

            if (pairs.isNotEmpty()) {
                PrimaryScrollableTabRow(
                    selectedTabIndex = safeTabIndex,
                    edgePadding = 16.dp,
                ) {
                    pairs.forEachIndexed { index, pair ->
                        Tab(
                            selected = safeTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(pair.tabLabel()) },
                        )
                    }
                }

                // Slides the way the tabs are laid out, as on the phone screen
                AnimatedContent(
                    targetState = safeTabIndex,
                    transitionSpec = {
                        val forward = targetState > initialState
                        sharedAxisXIn(forward, slideDistance) togetherWith sharedAxisXOut(forward, slideDistance)
                    },
                    label = "CompareLensSpecs",
                ) { index ->
                    // Swapping sides changes the pairs under an unchanged tab
                    pairs.getOrNull(index)?.let { CompareLensSpecs(it) }
                }
            }

            // Keeps the last specs clear of the toolbar
            Spacer(modifier = Modifier.height(96.dp))
        }

        HorizontalFloatingToolbar(
            expanded = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            scrollBehavior = toolbarScrollBehavior,
        ) {
            IconButton(onClick = { swapped = !swapped }, shapes = IconButtonDefaults.shapes()) {
                Icon(
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = stringResource(R.string.compare_swap),
                    modifier = Modifier.rotate(swapRotation),
                )
            }
            IconButton(onClick = onChangePhone, shapes = IconButtonDefaults.shapes()) {
                Icon(
                    imageVector = Icons.Filled.PhoneAndroid,
                    contentDescription = stringResource(R.string.compare_change_phone),
                )
            }
        }
    }
}

/** e.g. "Back telephoto", and "Back telephoto 2" for a phone's second lens of that kind. */
@Composable
private fun LensPair.tabLabel(): String {
    val kind = lensKindLabel(facing, type)
    return if (ordinal == 0) kind else stringResource(R.string.compare_lens_numbered, kind, ordinal + 1)
}

@Composable
private fun CompareHeader(
    left: ComparedPhone,
    right: ComparedPhone,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PhoneHeader(left, modifier = Modifier.weight(1f))
        PhoneHeader(right, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PhoneHeader(
    compared: ComparedPhone,
    modifier: Modifier = Modifier,
) {
    val deviceInfo = compared.phone.deviceInfo
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DeviceImageOrPlaceholder(deviceInfo)
        Spacer(modifier = Modifier.height(12.dp))
        if (compared.isThisDevice) {
            Text(
                text = stringResource(R.string.compare_this_device),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = deviceInfo.brand,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = deviceInfo.model,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (deviceInfo.releaseDate != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.phone_released, localizedDate(deviceInfo.releaseDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (compared.phone.lenses.isEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.compare_no_cameras),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompareContentPreview() {
    MaterialTheme {
        CompareContent(
            first = ComparedPhone(PhoneCatalogue[0]),
            second = ComparedPhone(PhoneCatalogue[1]),
            onChangePhone = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompareScreenThisDevicePreview() {
    MaterialTheme {
        CompareScreen(
            uiState = CompareUiState.Success(
                first = ComparedPhone(PhoneCatalogue[1], isThisDevice = true),
                second = ComparedPhone(PhoneCatalogue[0]),
            ),
            onBackClick = {},
            onRetry = {},
            onChangePhone = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompareScreenNoCamerasPreview() {
    MaterialTheme {
        CompareScreen(
            uiState = CompareUiState.Success(
                first = ComparedPhone(PhoneCatalogue[0]),
                second = ComparedPhone(PhoneCatalogue[1].copy(lenses = emptyList())),
            ),
            onBackClick = {},
            onRetry = {},
            onChangePhone = {},
        )
    }
}
