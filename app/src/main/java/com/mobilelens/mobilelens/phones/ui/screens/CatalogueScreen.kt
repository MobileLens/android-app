package com.mobilelens.mobilelens.phones.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.mobilelens.mobilelens.phones.ui.components.PhoneListItem

@Composable
fun CatalogueScreen(
    phones: List<Phone>,
    selectedPhoneId: String?,
    onPhoneClick: (Phone) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedPhoneId, phones) {
        val selectedIndex = phones.indexOfFirst { it.id == selectedPhoneId }
        if (selectedIndex >= 0) listState.animateScrollToItem(selectedIndex)
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (phones.isEmpty()) {
            Text(
                text = stringResource(R.string.no_phones_found),
                modifier = Modifier.padding(24.dp),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(phones, key = { _, phone -> phone.id }) { _, phone ->
                    PhoneListItem(
                        phone = phone,
                        onClick = { onPhoneClick(phone) },
                        isSelected = phone.id == selectedPhoneId,
                    )
                }
            }
        }

        // Overlaid so the list doesn't shift while a new search loads
        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            )
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
            onPhoneClick = {}
        )
    }
}

