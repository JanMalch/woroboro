package io.github.janmalch.woroboro.ui.routine

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.janmalch.woroboro.models.DurationFilter


@Composable
fun DurationFilterChip(
    value: DurationFilter,
    onValueChange: (DurationFilter) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        FilterChip(
            label = { Text(text = "Dauer", maxLines = 1) },
            selected = value != DurationFilter.Any,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DurationFilterItem(
                selected = value,
                value = DurationFilter.VeryShort,
                onValueChange = onValueChange,
            )
            DurationFilterItem(
                selected = value,
                value = DurationFilter.Short,
                onValueChange = onValueChange,
            )
            DurationFilterItem(
                selected = value,
                value = DurationFilter.Medium,
                onValueChange = onValueChange,
            )
            DurationFilterItem(
                selected = value,
                value = DurationFilter.Any,
                onValueChange = onValueChange,
                text = "Beliebige Dauer",
            )
        }
    }

}

@Composable
private fun DurationFilterItem(
    selected: DurationFilter,
    value: DurationFilter,
    text: String = "≤ ${value.duration.inWholeMinutes} Minuten",
    onValueChange: (DurationFilter) -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = { onValueChange(value) },
        leadingIcon = {
            Crossfade(
                targetState = selected == value,
                label = "Crossfade:Icon:${value}",
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
