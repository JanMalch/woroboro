package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.ui.theme.LoveRed

@Composable
fun FavoriteIcon(
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    crossfadeLabel: String = "Crossfade:Icon:IsFavorite",
) {
    Crossfade(
        targetState = isFavorite,
        label = crossfadeLabel,
    ) {
        Icon(
            if (it) Icons.Rounded.Favorite
            else Icons.Rounded.FavoriteBorder,
            contentDescription = null,
            tint = if (it) LoveRed else LocalContentColor.current,
            modifier = modifier,
        )
    }
}

@Composable
fun OnlyFavoritesChip(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    FilterChip(
        label = { Text(text = "Favoriten", maxLines = 1) },
        selected = value,
        trailingIcon = { FavoriteIcon(isFavorite = value, modifier = Modifier.size(18.dp)) },
        onClick = { onValueChange(!value) }
    )
}

@Composable
fun IsFavoriteCheckbox(
    text: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onValueChange(!value) }
    ) {
        FavoriteIcon(
            isFavorite = value,
            crossfadeLabel = "Crossfade:Icon:IsFavoriteCheckbox",
        )
        Text(text = text)
    }
}


