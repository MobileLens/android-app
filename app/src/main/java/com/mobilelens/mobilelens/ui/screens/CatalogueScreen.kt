package com.mobilelens.mobilelens.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.data.displayName
import com.mobilelens.mobilelens.data.focalLengthSummary

@Composable
fun CatalogueScreen(
    phones: List<Phone>,
    selectedPhoneId: Int?,
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
            val isSelected = phone.id == selectedPhoneId
            ListItem(
                headlineContent = { Text(phone.displayName) },
                supportingContent = {
                    Text(
                        stringResource(
                            R.string.phone_camera_summary,
                            phone.lenses.size,
                            phone.focalLengthSummary,
                        ),
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                },
                colors = ListItemDefaults.colors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
