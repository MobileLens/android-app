package com.mobilelens.mobilelens.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import java.util.Locale

/**
 * Picks one of [languages] (language tags), or the system language (null). Choosing one closes
 * the dialog through [onSelect].
 */
@Composable
fun LanguageDialog(
    languages: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.app_settings_language)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                LanguageOption(
                    label = stringResource(R.string.app_settings_language_system),
                    selected = selected == null,
                    onClick = { onSelect(null) },
                )
                languages.forEach { tag ->
                    LanguageOption(
                        label = languageName(tag),
                        selected = tag == selected,
                        onClick = { onSelect(tag) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

/** A language's name in that language, e.g. "English" or "Polski", so anyone can find their own. */
fun languageName(tag: String): String {
    val locale = Locale.forLanguageTag(tag)
    return locale.getDisplayName(locale).replaceFirstChar { it.titlecase(locale) }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The row handles the click and announces the state
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Preview
@Composable
private fun LanguageDialogPreview() {
    MaterialTheme {
        LanguageDialog(
            languages = listOf("en", "pl"),
            selected = "pl",
            onSelect = {},
            onDismiss = {},
        )
    }
}
