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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.rounded.Timelapse
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
import io.github.janmalch.woroboro.models.DurationFilter
import kotlinx.coroutines.launch

@Composable
fun DurationFilterChip(
    value: DurationFilter,
    onValueChange: (DurationFilter) -> Unit,
    enabled: Boolean = true,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val emitAndClose =
        remember<(DurationFilter) -> Unit>(onValueChange) {
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
                Icons.Rounded.Timelapse,
                contentDescription = null,
                tint =
                    if (value != DurationFilter.Any) MaterialTheme.colorScheme.primary
                    else LocalContentColor.current,
                modifier = Modifier.size(18.dp)
            )
        },
        selected = value != DurationFilter.Any,
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
                    text = stringResource(R.string.duration),
                    style = MaterialTheme.typography.titleMedium
                )
            }

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
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DurationFilterItem(
    selected: DurationFilter,
    value: DurationFilter,
    text: String =
        "${value.range.start.inWholeMinutes}–${value.range.endExclusive.inWholeMinutes} Minuten",
    onValueChange: (DurationFilter) -> Unit,
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
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
