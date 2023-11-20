package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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