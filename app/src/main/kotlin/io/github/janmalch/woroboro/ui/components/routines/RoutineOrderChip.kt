package io.github.janmalch.woroboro.ui.components.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
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
import io.github.janmalch.woroboro.models.RoutinesOrder
import kotlinx.coroutines.launch

@Composable
fun RoutineOrderChip(
    value: RoutinesOrder,
    onValueChange: (RoutinesOrder) -> Unit,
    enabled: Boolean = true,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val emitAndClose =
        remember<(RoutinesOrder) -> Unit>(onValueChange) {
            {
                onValueChange(it)
                scope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
            }
        }

    FilterChip(
        label = {
            Icon(
                Icons.AutoMirrored.Rounded.Sort,
                contentDescription = null,
                tint =
                    if (value != RoutinesOrder.NameAsc) MaterialTheme.colorScheme.primary
                    else LocalContentColor.current,
                modifier = Modifier.size(18.dp)
            )
        },
        selected = value != RoutinesOrder.NameAsc,
        onClick = { showBottomSheet = true },
        enabled = enabled,
    )

    if (showBottomSheet) {

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false },
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
                IconButton(onClick = { emitAndClose(value) }) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close))
                }
                Text(
                    text = stringResource(R.string.order),
                    style = MaterialTheme.typography.titleMedium
                )
            }

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
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RoutinesOrderItem(
    selected: RoutinesOrder,
    value: RoutinesOrder,
    onValueChange: (RoutinesOrder) -> Unit,
) {
    val mis = remember { MutableInteractionSource() }
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier.fillMaxWidth()
                .clickable(
                    interactionSource = mis,
                    indication = null,
                    onClick = { onValueChange(value) },
                )
                .padding(horizontal = 12.dp),
    ) {
        RadioButton(
            selected = selected == value,
            onClick = { onValueChange(value) },
            interactionSource = mis
        )
        Text(
            text =
                stringResource(
                    when (value) {
                        RoutinesOrder.NameAsc -> R.string.order_by_name_asc
                        RoutinesOrder.NameDesc -> R.string.order_by_name_desc
                        RoutinesOrder.LastRunRecently -> R.string.order_by_last_run_recently
                        RoutinesOrder.LastRunLongAgo -> R.string.order_by_last_run_long_ago
                    }
                ),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
