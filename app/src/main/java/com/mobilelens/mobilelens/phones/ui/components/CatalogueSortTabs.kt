package com.mobilelens.mobilelens.phones.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mobilelens.mobilelens.R
import com.mobilelens.mobilelens.phones.model.CatalogueSort

@StringRes
private fun CatalogueSort.labelRes(): Int = when (this) {
    CatalogueSort.ALL -> R.string.catalogue_tab_all
    CatalogueSort.NEW -> R.string.catalogue_tab_new
    CatalogueSort.TRENDING -> R.string.catalogue_tab_trending
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueSortTabs(
    selected: CatalogueSort,
    onSelect: (CatalogueSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryTabRow(
        selectedTabIndex = CatalogueSort.entries.indexOf(selected),
        modifier = modifier,
    ) {
        CatalogueSort.entries.forEach { sort ->
            Tab(
                selected = sort == selected,
                onClick = { onSelect(sort) },
                text = { Text(stringResource(sort.labelRes())) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogueSortTabsPreview() {
    MaterialTheme {
        CatalogueSortTabs(selected = CatalogueSort.NEW, onSelect = {})
    }
}
