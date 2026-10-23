package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.data.ComparedSide

// Shown in place of a value the phone doesn't have
internal const val MISSING_VALUE = "—"

/**
 * One spec of two phones, [first] on the left and [second] on the right. A null value means that
 * phone has no such lens. The [better] side's value is picked out in the primary color.
 */
@Composable
fun CompareSpecCard(
    title: String,
    first: String?,
    second: String?,
    modifier: Modifier = Modifier,
    better: ComparedSide? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(CardShadowElevation, MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        CompareRow(
            first = { CompareValue(first, highlighted = better == ComparedSide.FIRST, modifier = it) },
            second = { CompareValue(second, highlighted = better == ComparedSide.SECOND, modifier = it) },
        )
    }
}

/** Two halves of equal width with a divider between them, as tall as the taller half. */
@Composable
fun CompareRow(
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        first(Modifier.weight(1f))
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        second(Modifier.weight(1f))
    }
}

@Composable
private fun CompareValue(
    value: String?,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val description = when {
        value == null -> stringResource(R.string.compare_not_available)
        highlighted -> stringResource(R.string.compare_better_value, value)
        else -> value
    }
    // Shrinks in narrow halves, e.g. a zoom lens' "2.22 – 15.66 mm"
    BasicText(
        text = value ?: MISSING_VALUE,
        modifier = modifier
            .padding(horizontal = 8.dp)
            .semantics { contentDescription = description },
        style = MaterialTheme.typography.titleLarge.copy(
            color = when {
                value == null -> MaterialTheme.colorScheme.onSurfaceVariant
                highlighted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontFamily = FontFamily.Monospace,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
        ),
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 10.sp,
            maxFontSize = 22.sp,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun CompareSpecCardPreview() {
    MaterialTheme {
        CompareSpecCard(
            title = "Resolution",
            first = "48 MP",
            second = "50 MP",
            better = ComparedSide.SECOND,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompareSpecCardMissingPreview() {
    MaterialTheme {
        CompareSpecCard(
            title = "Focal length",
            first = "15.66 mm",
            second = null,
        )
    }
}
