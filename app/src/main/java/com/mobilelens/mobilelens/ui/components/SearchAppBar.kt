package com.mobilelens.mobilelens.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopSearchBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.model.Phone
import com.mobilelens.mobilelens.data.displayName
import com.mobilelens.mobilelens.data.focalLengthSummary
import kotlinx.coroutines.launch

private val DockedSearchBreakpoint = 600.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    textFieldState: TextFieldState,
    searchResults: List<Phone>,
    onSearch: (String) -> Unit,
    onResultSelected: (Phone) -> Unit,
    onClear: () -> Unit,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val searchBarState = rememberSearchBarState()
    val coroutineScope = rememberCoroutineScope()

    val collapseSearch: () -> Unit = fun() {
        coroutineScope.launch { searchBarState.animateToCollapsed() }
    }

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = { query ->
                onSearch(query)
                collapseSearch()
            },
            placeholder = { Text(stringResource(R.string.search_phones_hint)) },
            leadingIcon = {
                if (searchBarState.currentValue == SearchBarValue.Expanded) {
                    IconButton(onClick = collapseSearch) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_back),
                        )
                    }
                } else if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_back),
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_phones),
                    )
                }
            },
            trailingIcon = {
                if (textFieldState.text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            textFieldState.edit { replace(0, length, "") }
                            onClear()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search),
                        )
                    }
                }
            },
        )
    }

    val results: @Composable ColumnScope.() -> Unit = {
        SearchResults(
            searchResults = searchResults,
            onResultSelected = { phone ->
                onResultSelected(phone)
                collapseSearch()
            },
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        TopSearchBar(
            state = searchBarState,
            inputField = inputField,
        )

        if (maxWidth < DockedSearchBreakpoint) {
            ExpandedFullScreenSearchBar(
                state = searchBarState,
                inputField = inputField,
                modifier = Modifier.testTag("expanded_search_full_screen"),
                content = results,
            )
        } else {
            ExpandedDockedSearchBar(
                state = searchBarState,
                inputField = inputField,
                modifier = Modifier.testTag("expanded_search_docked"),
                content = results,
            )
        }
    }
}

@Composable
private fun SearchResults(
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
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier
                    .clickable { onResultSelected(phone) }
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
