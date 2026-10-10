package com.mobilelens.mobilelens.phones.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.model.Brand
import com.mobilelens.mobilelens.phones.model.CatalogueFilters
import com.mobilelens.mobilelens.phones.model.LensType
import com.mobilelens.mobilelens.phones.model.Stabilization
import com.mobilelens.mobilelens.phones.ui.labelRes

// "Other" lenses and "no stabilization" aren't something people filter the catalogue by
private val FilterLensTypes = listOf(LensType.WIDE, LensType.ULTRAWIDE, LensType.TELEPHOTO, LensType.MACRO)
private val FilterStabilizations = listOf(Stabilization.OIS, Stabilization.SENSORSHIFT)

@Composable
fun CatalogueFilterChips(
    filters: CatalogueFilters,
    brands: List<Brand>,
    onFiltersChange: (CatalogueFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (filters.isActive) {
            AssistChip(
                onClick = { onFiltersChange(CatalogueFilters()) },
                label = { Text(stringResource(R.string.filter_clear)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
            )
        }
        if (brands.isNotEmpty()) {
            DropdownFilterChip(
                title = stringResource(R.string.filter_brand),
                options = brands,
                selected = brands.firstOrNull { it.id == filters.brandId },
                optionLabel = { it.name },
                onSelect = { onFiltersChange(filters.copy(brandId = it?.id)) },
            )
        }
        DropdownFilterChip(
            title = stringResource(R.string.filter_lens),
            options = FilterLensTypes,
            selected = filters.lensType,
            optionLabel = { stringResource(it.labelRes()) },
            onSelect = { onFiltersChange(filters.copy(lensType = it)) },
        )
        DropdownFilterChip(
            title = stringResource(R.string.filter_stabilization),
            options = FilterStabilizations,
            selected = filters.stabilization,
            optionLabel = { stringResource(it.labelRes()) },
            onSelect = { onFiltersChange(filters.copy(stabilization = it)) },
        )
        ToggleFilterChip(
            label = stringResource(R.string.filter_optical_zoom),
            selected = filters.opticalZoom,
            onClick = { onFiltersChange(filters.copy(opticalZoom = !filters.opticalZoom)) },
        )
        ToggleFilterChip(
            label = stringResource(R.string.filter_verified),
            selected = filters.verifiedOnly,
            onClick = { onFiltersChange(filters.copy(verifiedOnly = !filters.verifiedOnly)) },
        )
    }
}

@Composable
private fun ToggleFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
    )
}

/** A chip that opens a menu of [options]; shows the chosen one in place of [title]. */
@Composable
private fun <T> DropdownFilterChip(
    title: String,
    options: List<T>,
    selected: T?,
    optionLabel: @Composable (T) -> String,
    onSelect: (T?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { Text(if (selected != null) optionLabel(selected) else title) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_any)) },
                onClick = {
                    expanded = false
                    onSelect(null)
                },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogueFilterChipsPreview() {
    MaterialTheme {
        CatalogueFilterChips(
            filters = CatalogueFilters(lensType = LensType.TELEPHOTO, verifiedOnly = true),
            brands = listOf(Brand("1", "Samsung"), Brand("2", "Google")),
            onFiltersChange = {},
        )
    }
}
