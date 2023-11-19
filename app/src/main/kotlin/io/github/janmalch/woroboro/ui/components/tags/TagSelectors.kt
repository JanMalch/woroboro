package io.github.janmalch.woroboro.ui.components.tags

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.models.Tag
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
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(text = type, softWrap = false) },
            colors = if (value.isNotEmpty()) AssistChipDefaults.elevatedAssistChipColors() else AssistChipDefaults.assistChipColors(),
            elevation = if (value.isNotEmpty()) AssistChipDefaults.assistChipElevation(
                elevation = 12.dp,
            ) else null,
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
                                painterResource(
                                    id =
                                    if (it) R.drawable.round_check_box_24
                                    else R.drawable.round_check_box_outline_blank_24
                                ),
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    }
}
