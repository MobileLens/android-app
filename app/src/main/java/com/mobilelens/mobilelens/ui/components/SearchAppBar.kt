package com.mobilelens.mobilelens.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults

import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopSearchBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.model.Phone
import kotlinx.coroutines.launch

private val DockedSearchBreakpoint = 600.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    searchResults: List<Phone>,
    onSearch: (String) -> Unit,
    onResultSelected: (Phone) -> Unit,
    onClear: () -> Unit,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
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
            placeholder = {
                Text(
                    text = stringResource(R.string.search_phones_hint),
                    style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            leadingIcon = if (searchBarState.currentValue == SearchBarValue.Expanded) {
                {
                    IconButton(onClick = collapseSearch) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_back),
                        )
                    }
                }
            } else {
                {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_phones_hint),
                    )
                }
            },
            trailingIcon = if (textFieldState.text.isNotEmpty()) {
                {
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
            } else {
                {
                    if (searchBarState.currentValue == SearchBarValue.Expanded) {
                        Box(modifier = Modifier.size(48.dp))
                    } else {
                        Box(modifier = Modifier.size(24.dp))
                    }
                }
            },
        )
    }

    val results: @Composable ColumnScope.() -> Unit = {
        SearchResultsList(
            searchResults = searchResults,
            onResultSelected = { phone ->
                onResultSelected(phone)
                collapseSearch()
            },
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isCollapsed = searchBarState.currentValue == SearchBarValue.Collapsed

            if (isCollapsed) {
                if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_back),
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(48.dp))
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                TopSearchBar(
                    state = searchBarState,
                    inputField = inputField,
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                )
            }

            if (isCollapsed) {
                IconButton(
                    onClick = { /* Future menu */ }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "U", // User placeholder
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (searchBarState.currentValue == SearchBarValue.Expanded) {
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
}

