package io.github.janmalch.woroboro.ui.components.tags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.components.common.rememberClearFocus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.launch

@Composable
fun TagSelectors(
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    value: List<Tag>,
    isCounterVisible: Boolean,
    onValueChange: (List<Tag>) -> Unit,
    enabled: Boolean = true,
) {
    availableTags.forEach { (type, labels) ->
        val valuesForType =
            remember(type, value) { value.mapNotNull { it.takeIf { it.type == type }?.label } }

        TagTypeMultiDropdown(
            type = type,
            availableLabels = labels,
            value = valuesForType,
            isCounterVisible = isCounterVisible,
            onValueChange = { newValuesForType ->
                onValueChange(
                    // remove old values for this type
                    value.filter { it.type != type } +
                        // and append new ones
                        (newValuesForType.map { Tag(label = it, type = type) })
                )
            },
            enabled = enabled,
        )
    }
}

@Composable
fun TagTypeMultiDropdown(
    type: String,
    availableLabels: ImmutableList<String>,
    value: List<String>,
    isCounterVisible: Boolean,
    onValueChange: (List<String>) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val clearFocus = rememberClearFocus()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    FilterChip(
        onClick = {
            clearFocus()
            expanded = true
        },
        label = { Text(text = type, maxLines = 1) },
        selected = value.isNotEmpty(),
        leadingIcon =
            if (isCounterVisible && value.isNotEmpty()) {
                { Badge { Text(text = value.size.toString()) } }
            } else null,
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        enabled = enabled,
    )

    if (expanded) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { expanded = false },
            dragHandle = null,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier.padding(
                        start = 12.dp,
                        top = 12.dp,
                        end = 12.dp,
                        bottom = 8.dp,
                    ),
            ) {
                IconButton(
                    onClick = {
                        scope
                            .launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    expanded = false
                                }
                            }
                    }
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close))
                }
                Text(text = type, style = MaterialTheme.typography.titleMedium)
            }
            availableLabels.forEach { availableLabel ->
                var isSelected by remember { mutableStateOf(availableLabel in value) }
                val mis = remember { MutableInteractionSource() }
                val onClick: () -> Unit = {
                    if (isSelected) {
                        isSelected = false
                        onValueChange(value - availableLabel)
                    } else {
                        isSelected = true
                        onValueChange(value + availableLabel)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable(
                                interactionSource = mis,
                                indication = null,
                                onClick = onClick
                            )
                            .padding(horizontal = 12.dp),
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        interactionSource = mis
                    )
                    Text(availableLabel)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
