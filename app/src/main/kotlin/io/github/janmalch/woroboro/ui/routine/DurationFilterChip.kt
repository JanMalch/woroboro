package io.github.janmalch.woroboro.ui.routine

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.models.DurationFilter


@Composable
fun DurationFilterChip(
    value: DurationFilter,
    onValueChange: (DurationFilter) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val emitAndClose = remember<(DurationFilter) -> Unit>(onValueChange) {
        {
            onValueChange(it)
            expanded = false
        }
    }

    Box {
        FilterChip(
            label = {
                Icon(
                    Icons.Rounded.Timelapse,
                    contentDescription = null,
                    tint = if (value != DurationFilter.Any) MaterialTheme.colorScheme.primary
                    else LocalContentColor.current,
                    modifier = Modifier.size(18.dp)
                )
            },
            selected = value != DurationFilter.Any,
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DurationFilterItem(
                selected = value,
                value = DurationFilter.VeryShort,
                text = "< ${DurationFilter.VeryShort.range.endExclusive.inWholeMinutes} Minuten",
                onValueChange = emitAndClose,
            )
            DurationFilterItem(
                selected = value,
                value = DurationFilter.Short,
                onValueChange = emitAndClose,
            )
            DurationFilterItem(
                selected = value,
                value = DurationFilter.Medium,
                onValueChange = emitAndClose,
            )
            DurationFilterItem(
                selected = value,
                value = DurationFilter.Long,
                onValueChange = emitAndClose,
                text = "≥ ${DurationFilter.Long.range.start.inWholeMinutes} Minuten"
            )
            DurationFilterItem(
                selected = value,
                value = DurationFilter.Any,
                onValueChange = emitAndClose,
                text = "Beliebige Dauer",
            )
        }
    }

}

@Composable
private fun DurationFilterItem(
    selected: DurationFilter,
    value: DurationFilter,
    text: String = "${value.range.start.inWholeMinutes}–${value.range.endExclusive.inWholeMinutes} Minuten",
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
                    if (it) Icons.Rounded.RadioButtonChecked
                    else Icons.Rounded.RadioButtonUnchecked,
                    tint = if (it) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                    contentDescription = null
                )
            }
        }
    )
}
