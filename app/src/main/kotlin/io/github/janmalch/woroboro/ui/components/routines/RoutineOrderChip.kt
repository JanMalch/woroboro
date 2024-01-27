package io.github.janmalch.woroboro.ui.components.routines

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.models.RoutinesOrder


@Composable
fun RoutineOrderChip(
    value: RoutinesOrder,
    onValueChange: (RoutinesOrder) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val emitAndClose = remember<(RoutinesOrder) -> Unit>(onValueChange) {
        {
            onValueChange(it)
            expanded = false
        }
    }

    Box {
        FilterChip(
            label = {
                Icon(
                    Icons.AutoMirrored.Rounded.Sort,
                    contentDescription = null,
                    tint = if (value != RoutinesOrder.NameAsc) MaterialTheme.colorScheme.primary
                    else LocalContentColor.current,
                    modifier = Modifier.size(18.dp)
                )
            },
            selected = value != RoutinesOrder.NameAsc,
            onClick = { expanded = true },
            enabled = enabled,
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            RoutinesOrderItem(
                selected = value,
                value = RoutinesOrder.NameAsc,
                onValueChange = emitAndClose,
            )
            RoutinesOrderItem(
                selected = value,
                value = RoutinesOrder.NameDesc,
                onValueChange = emitAndClose,
            )
            RoutinesOrderItem(
                selected = value,
                value = RoutinesOrder.LastRunRecently,
                onValueChange = emitAndClose,
            )
            RoutinesOrderItem(
                selected = value,
                value = RoutinesOrder.LastRunLongAgo,
                onValueChange = emitAndClose,
            )
        }
    }

}

@Composable
private fun RoutinesOrderItem(
    selected: RoutinesOrder,
    value: RoutinesOrder,
    onValueChange: (RoutinesOrder) -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(
                    when (value) {
                        RoutinesOrder.NameAsc -> R.string.order_by_name_asc
                        RoutinesOrder.NameDesc -> R.string.order_by_name_desc
                        RoutinesOrder.LastRunRecently -> R.string.order_by_last_run_recently
                        RoutinesOrder.LastRunLongAgo -> R.string.order_by_last_run_long_ago
                    }
                )
            )
        },
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
