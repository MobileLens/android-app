package com.mobilelens.mobilelens.phones.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.core.ui.ErrorContent
import com.mobilelens.mobilelens.core.ui.LoadingContent
import com.mobilelens.mobilelens.core.ui.UiStateCrossfade
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.model.Phone
import com.mobilelens.mobilelens.phones.ui.components.SearchResultsList
import com.mobilelens.mobilelens.phones.viewmodel.ComparePickerUiState

/**
 * Search for a catalogue phone: the one to compare with, or (with [titleRes] changed) the one to
 * review. Lists every phone until something is typed, leaving out [excludedPhoneId], the phone
 * being compared. A sheet, so it draws its own opaque background and top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparePickerScreen(
    searchState: TextFieldState,
    uiState: ComparePickerUiState,
    excludedPhoneId: String?,
    onClose: () -> Unit,
    onPhoneSelected: (Phone) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int = R.string.compare_picker_title,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    // Opens ready to type, but leaves the keyboard closed after a rotation if it was closed
    var focusedOnOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!focusedOnOpen) {
            focusedOnOpen = true
            focusRequester.requestFocus()
        }
    }

    // Opaque, since it rises over the screen that opened it
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = { Text(stringResource(titleRes)) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.compare_picker_close),
                    )
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        TextField(
            state = searchState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            placeholder = { Text(stringResource(R.string.compare_picker_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = if (searchState.text.isNotEmpty()) {
                {
                    IconButton(onClick = { searchState.clearText() }, shapes = IconButtonDefaults.shapes()) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.clear_search),
                        )
                    }
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            // Results follow the typing, so the search key only puts the keyboard away
            onKeyboardAction = { focusManager.clearFocus() },
            lineLimits = TextFieldLineLimits.SingleLine,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
        )

        PickerResults(
            uiState = uiState,
            excludedPhoneId = excludedPhoneId,
            onPhoneSelected = onPhoneSelected,
            onRetry = onRetry,
            // Stays above the keyboard, so the last results can still be scrolled to
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
                .imePadding(),
        )
    }
}

@Composable
private fun PickerResults(
    uiState: ComparePickerUiState,
    excludedPhoneId: String?,
    onPhoneSelected: (Phone) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        UiStateCrossfade(state = uiState) { state ->
            when (state) {
                ComparePickerUiState.Loading -> LoadingContent()
                is ComparePickerUiState.Error -> ErrorContent(
                    messageRes = state.messageRes,
                    onRetry = onRetry,
                )
                is ComparePickerUiState.Success -> SearchResultsList(
                    searchResults = state.phones.filter { it.id != excludedPhoneId },
                    onResultSelected = onPhoneSelected,
                )
            }
        }

        // A newer search is loading while the older results stay up
        AnimatedVisibility(
            visible = (uiState as? ComparePickerUiState.Success)?.isRefreshing == true,
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
private fun ComparePickerScreenPreview() {
    MaterialTheme {
        ComparePickerScreen(
            searchState = rememberTextFieldState(),
            uiState = ComparePickerUiState.Success(PhoneCatalogue),
            excludedPhoneId = PhoneCatalogue[0].id,
            onClose = {},
            onPhoneSelected = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ComparePickerScreenEmptyPreview() {
    MaterialTheme {
        ComparePickerScreen(
            searchState = rememberTextFieldState("Nokia"),
            uiState = ComparePickerUiState.Success(emptyList()),
            excludedPhoneId = null,
            onClose = {},
            onPhoneSelected = {},
            onRetry = {},
        )
    }
}
