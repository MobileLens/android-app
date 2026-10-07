package com.mobilelens.mobilelens.reviews.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.phones.data.displayName
import com.mobilelens.mobilelens.reviews.ui.MarkdownStyle
import com.mobilelens.mobilelens.reviews.ui.components.EditorToolbar
import com.mobilelens.mobilelens.reviews.ui.markdownStyles
import com.mobilelens.mobilelens.reviews.ui.toggleMarkdownStyle
import com.mobilelens.mobilelens.reviews.viewmodel.ReviewTargetUiState

/**
 * Markdown editor for a new review: title and content fields with a formatting toolbar, and a
 * rendered preview tab. Closing with unsaved text asks for confirmation first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    title: TextFieldState,
    content: TextFieldState,
    target: ReviewTargetUiState,
    isUploadingImage: Boolean,
    isPublishing: Boolean,
    onClose: () -> Unit,
    onPublish: () -> Unit,
    onInsertImage: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showPreview by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    val hasDraft by remember(title, content) {
        derivedStateOf { title.text.isNotBlank() || content.text.isNotBlank() }
    }
    val canPublish = target is ReviewTargetUiState.Found &&
            title.text.isNotBlank() &&
            content.text.isNotBlank() &&
            !isUploadingImage &&
            !isPublishing

    fun close() {
        if (hasDraft) showDiscardDialog = true else onClose()
    }

    // Publishing leaves the screen without asking, so only guard while there's something to lose
    BackHandler(enabled = hasDraft && !isPublishing) { showDiscardDialog = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            // The parent Scaffold already pads for the navigation bar
            .windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(stringResource(R.string.review_editor_title)) },
                navigationIcon = {
                    IconButton(onClick = ::close) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.review_editor_close),
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onPublish,
                        enabled = canPublish,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(stringResource(R.string.review_editor_publish))
                        }
                    }
                },
                // Insets are handled by the parent Scaffold
                windowInsets = WindowInsets(0, 0, 0, 0),
            )

            if (isUploadingImage) {
                val uploadingDescription = stringResource(R.string.review_editor_uploading_image)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = uploadingDescription },
                )
            }

            ReviewTarget(
                target = target,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            PrimaryTabRow(selectedTabIndex = if (showPreview) 1 else 0) {
                Tab(
                    selected = !showPreview,
                    onClick = { showPreview = false },
                    text = { Text(stringResource(R.string.review_editor_tab_write)) },
                )
                Tab(
                    selected = showPreview,
                    onClick = { showPreview = true },
                    text = { Text(stringResource(R.string.review_editor_tab_preview)) },
                )
            }

            if (showPreview) {
                ReviewPreview(
                    title = title.text.toString(),
                    content = content.text.toString(),
                    modifier = Modifier.weight(1f),
                )
            } else {
                ReviewEditor(
                    title = title,
                    content = content,
                    onInsertImage = onInsertImage,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.review_editor_discard_title)) },
            text = { Text(stringResource(R.string.review_editor_discard_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onClose()
                    }
                ) {
                    Text(stringResource(R.string.review_editor_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.review_editor_keep_editing))
                }
            },
        )
    }
}

@Composable
private fun ReviewTarget(
    target: ReviewTargetUiState,
    modifier: Modifier = Modifier,
) {
    val (text, color) = when (target) {
        is ReviewTargetUiState.Loading ->
            stringResource(R.string.review_editor_finding_phone) to MaterialTheme.colorScheme.onSurfaceVariant
        is ReviewTargetUiState.Found ->
            stringResource(R.string.review_editor_reviewing, target.phone.displayName) to
                    MaterialTheme.colorScheme.onSurfaceVariant
        is ReviewTargetUiState.Error ->
            stringResource(target.messageRes) to MaterialTheme.colorScheme.error
    }
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        color = color,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReviewEditor(
    title: TextFieldState,
    content: TextFieldState,
    onInsertImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Re-derived as the cursor moves, so the toolbar shows the styles around it
    val styles by remember(content) { derivedStateOf { content.markdownStyles() } }

    Column(modifier = modifier.fillMaxWidth()) {
        EditorTextField(
            state = title,
            placeholder = stringResource(R.string.review_editor_title_hint),
            textStyle = MaterialTheme.typography.titleLarge,
            lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        EditorTextField(
            state = content,
            placeholder = stringResource(R.string.review_editor_content_hint),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
        )

        EditorToolbar(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp),
            bold = styles.bold,
            italic = styles.italic,
            underline = styles.underline,
            onBoldClick = { content.toggleMarkdownStyle(MarkdownStyle.Bold) },
            onItalicClick = { content.toggleMarkdownStyle(MarkdownStyle.Italic) },
            onUnderlineClick = { content.toggleMarkdownStyle(MarkdownStyle.Underline) },
            onImageClick = onInsertImage,
            onUndoClick = { content.undoState.undo() },
        )
    }
}

@Composable
private fun EditorTextField(
    state: TextFieldState,
    placeholder: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.MultiLine(),
) {
    BasicTextField(
        state = state,
        modifier = modifier,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        lineLimits = lineLimits,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorator = { innerTextField ->
            Box {
                if (state.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun ReviewPreview(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        if (title.isBlank() && content.isBlank()) {
            Text(
                text = stringResource(R.string.review_editor_preview_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        RichText {
            Markdown(content)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WriteReviewScreenPreview() {
    MaterialTheme {
        WriteReviewScreen(
            title = rememberTextFieldState("Night mode tested"),
            content = rememberTextFieldState(
                "The **main camera** keeps detail in *very* dark scenes.\n\n" +
                        "The telephoto struggles below 10 lux."
            ),
            target = ReviewTargetUiState.Found(PhoneCatalogue[0]),
            isUploadingImage = false,
            isPublishing = false,
            onClose = {},
            onPublish = {},
            onInsertImage = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WriteReviewScreenEmptyPreview() {
    MaterialTheme {
        WriteReviewScreen(
            title = rememberTextFieldState(),
            content = rememberTextFieldState(),
            target = ReviewTargetUiState.Error(R.string.review_editor_device_not_in_catalogue),
            isUploadingImage = true,
            isPublishing = false,
            onClose = {},
            onPublish = {},
            onInsertImage = {},
        )
    }
}
