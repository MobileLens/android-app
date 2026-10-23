package com.mobilelens.mobilelens.phones.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.ui.components.PhoneCard
import com.mobilelens.mobilelens.phones.ui.components.PhoneCardListArrangement
import com.mobilelens.mobilelens.phones.ui.components.PhoneCardListContentPadding

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CatalogueScreen(
    phones: List<Phone>,
    selectedPhoneId: String?,
    onPhoneClick: (Phone) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    isPullRefreshing: Boolean = false,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedPhoneId, phones) {
        val selectedIndex = phones.indexOfFirst { it.id == selectedPhoneId }
        if (selectedIndex >= 0) listState.animateScrollToItem(selectedIndex)
    }

    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isPullRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullToRefreshState,
                isRefreshing = isPullRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        // Also holds the empty message, so the pull gesture has a scrollable child to work with
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PhoneCardListContentPadding,
            verticalArrangement = PhoneCardListArrangement,
        ) {
            if (phones.isEmpty()) {
                item {
                    // The list's margins already take 16dp horizontally and 8dp vertically of the 24dp
                    Text(
                        text = stringResource(R.string.no_phones_found),
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                    )
                }
            } else {
                itemsIndexed(phones, key = { _, phone -> phone.id }) { _, phone ->
                    // New results fade in and the ones that stay glide to their new place
                    PhoneCard(
                        phone = phone,
                        onClick = { onPhoneClick(phone) },
                        isSelected = phone.id == selectedPhoneId,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }

        // Overlaid so the list doesn't shift while a new search loads
        AnimatedVisibility(
            visible = isRefreshing,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun CatalogueScreenPreview() {
    MaterialTheme {
        CatalogueScreen(
            phones = PhoneCatalogue,
            selectedPhoneId = "1",
            onPhoneClick = {},
            onRefresh = {},
        )
    }
}

