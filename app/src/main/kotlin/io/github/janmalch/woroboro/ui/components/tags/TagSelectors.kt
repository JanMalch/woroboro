package io.github.janmalch.woroboro.ui.components.tags

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.components.common.rememberClearFocus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap


@Composable
fun TagSelectors(
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    value: List<Tag>,
    isCounterVisible: Boolean,
    onValueChange: (List<Tag>) -> Unit,
) {
    availableTags.forEach { (type, labels) ->
        val valuesForType = remember(type, value) {
            value.mapNotNull { it.takeIf { it.type == type }?.label }
        }

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
            }
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
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val clearFocus = rememberClearFocus()
    Box {
        FilterChip(
            onClick = {
                clearFocus()
                expanded = true
            },
            label = { Text(text = type, maxLines = 1) },
            selected = value.isNotEmpty(),
            leadingIcon = if (isCounterVisible && value.isNotEmpty()) {
                {
                    Badge {
                        Text(text = value.size.toString())
                    }
                }
            } else null,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            availableLabels.forEach { availableLabel ->
                var isSelected by remember { mutableStateOf(availableLabel in value) }
                DropdownMenuItem(
                    text = { Text(availableLabel) },
                    onClick = {
                        if (isSelected) {
                            isSelected = false
                            onValueChange(value - availableLabel)
                        } else {
                            isSelected = true
                            onValueChange(value + availableLabel)
                        }
                    },
                    leadingIcon = {
                        Crossfade(
                            targetState = isSelected,
                            label = "Crossfade:Icon:$availableLabel",
                        ) {
                            Icon(
                                if (it) Icons.Rounded.CheckBox
                                else Icons.Rounded.CheckBoxOutlineBlank,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    }
}
