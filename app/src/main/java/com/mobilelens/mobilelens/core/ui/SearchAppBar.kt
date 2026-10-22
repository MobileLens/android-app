package com.mobilelens.mobilelens.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.theme.MobileLensTheme
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.ui.components.SearchResultsList
import kotlinx.coroutines.launch

private val DockedSearchBreakpoint = 600.dp

// Equal side margins keep the pill centered. The cap stops it stretching on wide screens.
private val SearchBarSideMargin = 32.dp
private val SearchBarMaxWidth = 340.dp

// Leading and trailing slots match, so a centered hint stays in the middle of the pill.
private val SearchBarIconSlot = 48.dp

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
    onAccountClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    // Initial of the logged-in user; a generic person icon is shown when null
    accountInitial: String? = null,
) {
    val searchBarState = rememberSearchBarState()
    val coroutineScope = rememberCoroutineScope()
    val searchExpanded = searchBarState.currentValue == SearchBarValue.Expanded
    val hasQuery = textFieldState.text.isNotEmpty()

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
            // A query starts at the leading icon. The empty hint stays centered between the icons.
            textStyle = LocalTextStyle.current.copy(
                textAlign = if (hasQuery) TextAlign.Start else TextAlign.Center,
            ),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_phones_hint),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {},
                )
            },
            leadingIcon = {
                when {
                    searchExpanded -> IconButton(onClick = collapseSearch) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_back),
                        )
                    }
                    // Same slot as the search icon, so the pill keeps its width and its center.
                    showBackButton -> IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                    else -> Box(
                        modifier = Modifier.size(SearchBarIconSlot),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                        )
                    }
                }
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasQuery) {
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
                    AccountMenuButton(
                        accountInitial = accountInitial,
                        onAccountClick = onAccountClick,
                        onSettingsClick = onSettingsClick,
                    )
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
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            SearchBar(
                state = searchBarState,
                inputField = inputField,
                modifier = Modifier
                    .padding(horizontal = SearchBarSideMargin)
                    .widthIn(max = SearchBarMaxWidth)
                    .fillMaxWidth(),
            )
        }

        if (searchExpanded) {
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

/** The user's avatar, opening a menu that leads to their account or to the app's settings. */
@Composable
private fun AccountMenuButton(
    accountInitial: String?,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    // The menu drops down from the avatar
    Box {
        val menuDescription = stringResource(R.string.account_menu)
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.semantics { contentDescription = menuDescription }
        ) {
            UserAvatar(initial = accountInitial)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.account)) },
                onClick = {
                    expanded = false
                    onAccountClick()
                },
                leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.app_settings_title)) },
                onClick = {
                    expanded = false
                    onSettingsClick()
                },
                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SearchAppBarPreview() {
    MobileLensTheme {
        SearchAppBar(
            textFieldState = rememberTextFieldState(),
            searchResults = emptyList(),
            onSearch = {},
            onResultSelected = {},
            onClear = {},
            accountInitial = "M",
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SearchAppBarWithBackPreview() {
    MobileLensTheme {
        SearchAppBar(
            textFieldState = rememberTextFieldState(),
            searchResults = emptyList(),
            onSearch = {},
            onResultSelected = {},
            onClear = {},
            showBackButton = true,
            accountInitial = "M",
        )
    }
}
