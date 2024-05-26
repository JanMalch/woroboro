package io.github.janmalch.woroboro.ui.exercise.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.janmalch.woroboro.models.EditedMedia
import io.github.janmalch.woroboro.models.isEmpty
import io.github.janmalch.woroboro.ui.components.common.clickableWithClearFocus
import io.github.janmalch.woroboro.ui.components.common.rememberClearFocus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

@Composable
fun MediaPicker(
    value: EditedMedia,
    onValueChange: (EditedMedia) -> Unit,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
    title: @Composable RowScope.() -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val clearFocus = rememberClearFocus()
    val currentValue by rememberUpdatedState(value)
    val pickMedia = rememberMediaPicker { uris ->
        onValueChange(currentValue.copy(added = (uris + currentValue.added).toSet()))
    }

    Column(modifier = modifier) {
        Row(
            modifier = headerModifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            title()
            IconButton(
                onClick = {
                    clearFocus()
                    pickMedia()
                }
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        }

        AnimatedVisibility(visible = !value.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = contentPadding,
            ) {
                items(
                    items = value.added.toImmutableList(),
                    key = { it },
                    contentType = { "Added" }
                ) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier.size(100.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickableWithClearFocus {
                                    onValueChange(
                                        currentValue.copy(added = currentValue.added - uri)
                                    )
                                },
                    )
                }

                items(items = value.existing, key = { it }, contentType = { "Existing" }) { uri ->
                    AsyncImage(
                        model = uri.thumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier.size(100.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickableWithClearFocus {
                                    onValueChange(
                                        currentValue.copy(
                                            existing =
                                                (currentValue.existing - uri).toPersistentList()
                                        )
                                    )
                                },
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberMediaPicker(onMediaSelected: (List<Uri>) -> Unit): () -> Unit {
    val pickMultipleMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris
            ->
            if (uris.isNotEmpty()) {
                onMediaSelected(uris)
            }
        }

    return {
        // TODO: support video
        pickMultipleMedia.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}
