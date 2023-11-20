package io.github.janmalch.woroboro.ui.exercise.tageditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.components.common.BackIconButton
import io.github.janmalch.woroboro.ui.components.common.TextFieldSheet
import io.github.janmalch.woroboro.ui.components.common.TextWithEditSheet
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.launch

@Composable
fun TagEditorScreen(
    value: ImmutableMap<String, ImmutableList<Tag>>,
    onAddTag: (Tag) -> Unit,
    onUpdateTag: (Tag, String) -> Unit,
    onDeleteTag: (Tag) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val newTypes = remember { mutableStateListOf<String>() }
    var isSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        newTypes.removeIf { it in value }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = { BackIconButton(onClick = onBackClick) },
                title = { Text("Tags") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isSheetVisible = true }) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        }
    ) { padding ->
        TagEditor(
            listState = listState,
            value = value,
            newTypes = newTypes,
            onAddTag = onAddTag,
            onUpdateTag = onUpdateTag,
            onDeleteTag = onDeleteTag,
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
        )


        if (isSheetVisible) {
            TextFieldSheet(
                value = "",
                placeholder = "Neuer Typ",
                buttonText = "Erstellen",
                onValueChange = {
                    newTypes.add(it.trim())
                    coroutineScope.launch {
                        listState.scrollToItem(0)
                    }
                },
                onDismissRequest = { isSheetVisible = false })
        }
    }
}

@Composable
fun TagEditor(
    listState: LazyListState,
    value: ImmutableMap<String, ImmutableList<Tag>>,
    newTypes: List<String>,
    onAddTag: (Tag) -> Unit,
    onUpdateTag: (Tag, String) -> Unit,
    onDeleteTag: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
    ) {
        items(newTypes, key = { it }, contentType = { "TypeCard" }) { type ->
            TypeCard(
                type = type,
                tags = emptyList(),
                isExpanded = true,
                onAddTag = onAddTag,
                onUpdateTag = onUpdateTag,
                onDeleteTag = onDeleteTag,
                modifier = Modifier.animateItemPlacement(),
            )
        }

        if (newTypes.isNotEmpty()) {
            item(key = "Divider", contentType = "Divider") { HorizontalDivider() }
        }

        items(
            value.entries.toList(),
            key = { it.key },
            contentType = { "TypeCard" }) { (type, tags) ->
            TypeCard(
                type = type,
                tags = tags,
                onAddTag = onAddTag,
                onUpdateTag = onUpdateTag,
                onDeleteTag = onDeleteTag,
                modifier = Modifier
                    .animateItemPlacement(),
            )
        }
        item(key = "FabSpacer", contentType = "FabSpacer") {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun TypeCard(
    type: String,
    tags: List<Tag>,
    onAddTag: (Tag) -> Unit,
    onUpdateTag: (Tag, String) -> Unit,
    onDeleteTag: (Tag) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    var isSheetVisible by remember { mutableStateOf(false) }
    var areLabelsVisible by remember(isExpanded) { mutableStateOf(isExpanded) }

    Column(modifier = modifier.fillMaxWidth()) {

        ListItem(
            headlineContent = {
                Text(
                    text = type,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = if (areLabelsVisible) FontWeight.Medium else null
                )
            },
            trailingContent = {
                val rotation by animateFloatAsState(
                    targetValue = if (areLabelsVisible) 180f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "ChevronSpin:$type",
                )
                IconButton(onClick = { areLabelsVisible = !areLabelsVisible }) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown, contentDescription = null,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        )

        AnimatedVisibility(visible = areLabelsVisible) {
            Column {
                tags.forEach { tag ->
                    val label = tag.label

                    ListItem(
                        headlineContent = {

                            TextWithEditSheet(
                                value = label,
                                placeholder = "Aktualisiertes Label",
                                buttonText = "Speichern",
                                onValueChange = {
                                    onUpdateTag(
                                        Tag(label = it, type = tag.type),
                                        label,
                                    )
                                },
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 16.dp),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { onDeleteTag(tag) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null)
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 24.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { isSheetVisible = true }) {
                        Text(text = "Neuer Tag")
                    }
                }
            }
        }

        HorizontalDivider()
    }

    if (isSheetVisible) {
        TextFieldSheet(
            value = "",
            placeholder = "Neuer Tag für $type",
            buttonText = "Erstellen",
            onValueChange = { onAddTag(Tag(it, type)) },
            onDismissRequest = { isSheetVisible = false },
        )
    }
}

