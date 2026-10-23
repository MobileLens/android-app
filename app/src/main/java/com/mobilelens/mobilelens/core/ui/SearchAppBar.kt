package com.mobilelens.mobilelens.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.theme.MobileLensTheme
import com.mobilelens.mobilelens.core.ui.theme.Motion
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.ui.components.SearchResultsList
import kotlinx.coroutines.launch

private val DockedSearchBreakpoint = 600.dp

// Added to AppBarWithSearch's own 8dp around the pill, for 16dp side margins in all
private val AppBarPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)

// Leading and trailing slots match, so a centered hint stays in the middle of the pill.
private val SearchBarIconSlot = 48.dp

// The back button and the avatar mirror each other at the two ends of the pill
private val PillButtonSize = 40.dp

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
    // Hides the bar as the content scrolls up and brings it back on the way down
    scrollBehavior: SearchBarScrollBehavior? = null,
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
            // AppBarWithSearch sizes the pill to its content, so a short query would shrink it
            modifier = Modifier.fillMaxWidth(),
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
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {},
                )
            },
            leadingIcon = {
                val leading = when {
                    searchExpanded -> LeadingSlot.Collapse
                    showBackButton -> LeadingSlot.Back
                    else -> LeadingSlot.Search
                }
                // The search icon and the back button swap in place with a springy scale
                AnimatedContent(
                    targetState = leading,
                    transitionSpec = {
                        (scaleIn(Motion.fastSpatialSpring(), initialScale = 0.6f) + fadeIn()) togetherWith
                                (scaleOut(targetScale = 0.6f) + fadeOut())
                    },
                    contentAlignment = Alignment.Center,
                    label = "leadingSlot",
                ) { slot ->
                    Box(
                        modifier = Modifier.size(SearchBarIconSlot),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (slot) {
                            LeadingSlot.Collapse -> IconButton(onClick = collapseSearch) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.search_back),
                                )
                            }
                            LeadingSlot.Back -> BackButton(onClick = onBackClick)
                            LeadingSlot.Search -> Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                            )
                        }
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
                    if (!searchExpanded) {
                        AccountMenuButton(
                            accountInitial = accountInitial,
                            onAccountClick = onAccountClick,
                            onSettingsClick = onSettingsClick,
                        )
                    } else if (!hasQuery) {
                        // Balances the collapse arrow, so the hint stays centered
                        Spacer(Modifier.size(SearchBarIconSlot))
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
        AppBarWithSearch(
            state = searchBarState,
            inputField = inputField,
            colors = SearchBarDefaults.appBarWithSearchColors(
                searchBarColors = SearchBarDefaults.containedColors(state = searchBarState),
            ),
            contentPadding = AppBarPadding,
            scrollBehavior = scrollBehavior,
        )

        if (searchExpanded) {
            if (maxWidth < DockedSearchBreakpoint) {
                ExpandedFullScreenContainedSearchBar(
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

private enum class LeadingSlot { Collapse, Back, Search }

/** A tonal back button that morphs its shape while pressed. Mirrors the avatar. */
@Composable
private fun BackButton(onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(),
        // Same colours as the avatar at the other end
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = Modifier.size(PillButtonSize),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.common_back),
        )
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    // Shrinks a touch under the finger and springs back, and stays raised while its menu is open
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.88f
            expanded -> 1.06f
            else -> 1f
        },
        animationSpec = Motion.fastSpatialSpring(),
        label = "avatarScale",
    )

    // The menu drops down from the avatar
    Box {
        val menuDescription = stringResource(R.string.account_menu)
        IconButton(
            onClick = { expanded = true },
            interactionSource = interactionSource,
            modifier = Modifier
                .size(SearchBarIconSlot)
                .semantics { contentDescription = menuDescription }
        ) {
            UserAvatar(
                initial = accountInitial,
                size = PillButtonSize,
                textStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.large,
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
