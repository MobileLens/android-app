package com.mobilelens.mobilelens.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.data.focalLengthSummary

@Composable
fun SearchResultsList(
    searchResults: List<Phone>,
    onResultSelected: (Phone) -> Unit,
) {
    if (searchResults.isEmpty()) {
        Text(
            text = stringResource(R.string.no_phones_found),
            modifier = Modifier.padding(24.dp),
        )
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(searchResults, key = Phone::id) { phone ->
            PhoneListItem(
                phone = phone,
                onClick = { onResultSelected(phone) },
                supportingText = stringResource(
                    R.string.phone_camera_summary,
                    phone.lenses.size,
                    phone.focalLengthSummary,
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
