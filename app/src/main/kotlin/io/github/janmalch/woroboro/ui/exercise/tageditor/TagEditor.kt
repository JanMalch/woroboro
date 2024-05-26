package io.github.janmalch.woroboro.ui.exercise.tageditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.components.common.BackIconButton
import io.github.janmalch.woroboro.ui.components.common.SimpleTextField
import io.github.janmalch.woroboro.ui.components.common.TextFieldSheet
import io.github.janmalch.woroboro.ui.components.common.clearFocusAsOutsideClick
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.launch

@Composable
fun TagEditorScreen(
    value: ImmutableMap<String, ImmutableList<Tag>>,
    onAddTag: (Tag) -> Unit,
    onUpdateTag: (Tag, String) -> Unit,
    onDeleteTag: (Tag) -> Unit,
    onRenameType: (Pair<String, String>) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val newTypes = remember { mutableStateListOf<String>() }
    var isSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(value) { newTypes.removeIf { it in value } }

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
            onRenameType = onRenameType,
            modifier = modifier.fillMaxSize().clearFocusAsOutsideClick().padding(padding),
        )

        if (isSheetVisible) {
            TextFieldSheet(
                value = "",
                placeholder = "Neuer Typ",
                buttonText = "Erstellen",
                onValueChange = {
                    newTypes.add(it.trim())
                    coroutineScope.launch { listState.scrollToItem(0) }
                },
                onDismissRequest = { isSheetVisible = false }
            )
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
    onRenameType: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        items(newTypes, key = { "_new_$it" }, contentType = { "TypeCard" }) { type ->
            TypeCard(
                type = type,
                tags = emptyList(),
                onAddTag = onAddTag,
                onUpdateTag = onUpdateTag,
                onDeleteTag = onDeleteTag,
                onRenameType = onRenameType,
            )
        }

        if (newTypes.isNotEmpty()) {
            item(key = "Divider", contentType = "Divider") { HorizontalDivider() }
        }

        items(value.entries.toList(), key = { it.key }, contentType = { "TypeCard" }) { (type, tags)
            ->
            TypeCard(
                type = type,
                tags = tags,
                onAddTag = onAddTag,
                onUpdateTag = onUpdateTag,
                onDeleteTag = onDeleteTag,
                onRenameType = onRenameType,
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
    onRenameType: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var labelInputValue by rememberSaveable { mutableStateOf("") }
    var editedType by remember(type) { mutableStateOf(type) }
    var editingLabel by remember { mutableStateOf<String?>(null) }

    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = editedType,
            onValueChange = { editedType = it },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (editedType != type) {
                    IconButton(
                        onClick = { onRenameType(type to editedType) },
                        enabled = editedType.isNotBlank(),
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                    }
                }
            }
        )

        AnimatedVisibility(
            visible = tags.isNotEmpty(),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = editingLabel == tag.label,
                        onClick = {
                            editingLabel = if (editingLabel == tag.label) null else tag.label
                        },
                        label = { Text(text = tag.label) },
                        trailingIcon = {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = null,
                                modifier = Modifier.clickable { onDeleteTag(tag) },
                            )
                        }
                    )
                }
            }
        }

        SimpleTextField(
            value = labelInputValue,
            onValueChange = { labelInputValue = it },
            placeholder =
                if (editingLabel == null) "Neuer Tag für $type…" else "$editingLabel umbenennen…",
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        val tag = Tag(label = labelInputValue.trim(), type = type)
                        val currentEditingLabel = editingLabel
                        if (currentEditingLabel == null) {
                            onAddTag(tag)
                        } else {
                            onUpdateTag(tag, currentEditingLabel)
                        }
                        labelInputValue = ""
                    }
                )
        )
    }
}
