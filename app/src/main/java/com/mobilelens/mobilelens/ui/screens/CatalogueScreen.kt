package com.mobilelens.mobilelens.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.ui.components.PhoneListItem

@Composable
fun CatalogueScreen(
    phones: List<Phone>,
    selectedPhoneId: Int?,
    onPhoneClick: (Phone) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedPhoneId, phones) {
        val selectedIndex = phones.indexOfFirst { it.id == selectedPhoneId }
        if (selectedIndex >= 0) listState.animateScrollToItem(selectedIndex)
    }

    if (phones.isEmpty()) {
        Text(
            text = stringResource(R.string.no_phones_found),
            modifier = modifier.padding(24.dp),
        )
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
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

