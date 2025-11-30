package com.app.core.tvui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A horizontal row component for displaying a list of focusable cards
 */
@Composable
fun TvContentRow(
    title: String,
    items: List<TvContentItem>,
    modifier: Modifier = Modifier,
    onItemClick: (TvContentItem) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 48.dp, bottom = 20.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            items(items) { item ->
                TvFocusableCard(
                    title = item.title,
                    subtitle = item.subtitle,
                    imageUrl = item.imageUrl,
                    onClick = { onItemClick(item) }
                ) {
                    // Content placeholder - can be enhanced with images later
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class TvContentItem(
    val id: Int,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null
)

