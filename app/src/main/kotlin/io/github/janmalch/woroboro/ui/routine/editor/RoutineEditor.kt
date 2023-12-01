package io.github.janmalch.woroboro.ui.routine.editor

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.janmalch.woroboro.models.CustomExerciseExecution
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.ExerciseExecution
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.models.RoutineStep
import io.github.janmalch.woroboro.models.asCustomExecution
import io.github.janmalch.woroboro.models.asExerciseExecutionOrNull
import io.github.janmalch.woroboro.ui.components.DurationTextField
import io.github.janmalch.woroboro.ui.components.ExerciseListItem
import io.github.janmalch.woroboro.ui.components.ExerciseListItemDefaults
import io.github.janmalch.woroboro.ui.components.ExerciseListItemDefaults.ImageCornerSize
import io.github.janmalch.woroboro.ui.components.NumberTextField
import io.github.janmalch.woroboro.ui.components.common.ButtonLoading
import io.github.janmalch.woroboro.ui.components.common.CloseIconButton
import io.github.janmalch.woroboro.ui.components.common.HapticFeedback
import io.github.janmalch.woroboro.ui.components.common.IsFavoriteCheckbox
import io.github.janmalch.woroboro.ui.components.common.MoreMenu
import io.github.janmalch.woroboro.ui.components.common.clearFocusAsOutsideClick
import io.github.janmalch.woroboro.ui.components.common.clickableWithClearFocus
import io.github.janmalch.woroboro.ui.components.common.rememberClearFocus
import io.github.janmalch.woroboro.ui.components.common.rememberHapticFeedback
import io.github.janmalch.woroboro.ui.components.exerciseExecution
import io.github.janmalch.woroboro.ui.exercise.editor.DurationSaver
import io.github.janmalch.woroboro.ui.theme.Success
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun List<RoutineStep>.updateSortIndices(): ImmutableList<RoutineStep> =
    mapIndexed { index, routineStep ->
        when (routineStep) {
            is RoutineStep.ExerciseStep -> routineStep.copy(sortIndex = index)
            is RoutineStep.PauseStep -> routineStep.copy(sortIndex = index)
        }
    }.toImmutableList()

@Composable
fun RoutineEditorScreen(
    routine: FullRoutine?,
    isLoading: Boolean,
    allExercises: ImmutableList<Exercise>,
    onSave: (FullRoutine) -> Unit,
    onDelete: (UUID) -> Unit,
    onBackClick: () -> Unit,
) {
    val clearFocus = rememberClearFocus()
    val id = rememberSaveable(routine) { routine?.id ?: UUID.randomUUID() }
    var name by rememberSaveable(routine) {
        mutableStateOf(routine?.name ?: "")
    }
    var isFavorite by rememberSaveable(routine) {
        mutableStateOf(
            routine?.isFavorite ?: false
        )
    }
    var steps by remember(routine) {
        mutableStateOf(routine?.steps ?: persistentListOf())
    }

    var isNewStepDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    val lazyListState = rememberLazyListState()
    val hapticFeedback = rememberHapticFeedback()
    val reorderableLazyColumnState: ReorderableLazyListState =
        rememberReorderableLazyColumnState(lazyListState) { from, to ->
            try {
                steps = steps.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                }.toImmutableList()
                hapticFeedback.segmentFrequentTick()
            } catch (e: IndexOutOfBoundsException) {
                Log.e("RoutineEditorScreen", "Unexpected error while reordering.", e)
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { CloseIconButton(onClick = onBackClick) },
                title = {},
                actions = {
                    Button(
                        onClick = {
                            val edited = FullRoutine(
                                id = id,
                                name = name.trim(),
                                steps = steps.updateSortIndices(),
                                isFavorite = isFavorite,
                                lastRunDuration = null,
                                lastRunEnded = null,
                            )
                            onSave(edited)
                        },
                        enabled = !isLoading && name.isNotBlank() && steps.any { it is RoutineStep.ExerciseStep },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth,
                            minHeight = 36.dp
                        )
                    ) {
                        ButtonLoading(isVisible = isLoading)
                        Text(text = "Speichern")
                    }
                    MoreMenu(enabled = routine != null) {
                        DropdownMenuItem(
                            text = { Text(text = "Routine löschen") },
                            onClick = { if (routine != null) onDelete(routine.id) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusAsOutsideClick()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = "Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                IsFavoriteCheckbox(
                    text = "Lieblingsroutine",
                    value = isFavorite,
                    onValueChange = { isFavorite = it },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    clearFocus()
                    isNewStepDialogOpen = true
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp),
            ) {
                Text(text = "Neuer Schritt")
            }

            LazyColumn(
                state = lazyListState,
            ) {
                stepsItems(
                    reorderableLazyColumnState = reorderableLazyColumnState,
                    value = steps,
                    onValueChange = { steps = it.toImmutableList() },
                    hapticFeedback = hapticFeedback,
                    allExercises = allExercises,
                )
            }
        }

        if (isNewStepDialogOpen) {
            RoutineStepEditorDialog(
                step = null,
                allExercises = allExercises,
                onSave = {
                    steps = (steps.toPersistentList() + it).updateSortIndices()
                    isNewStepDialogOpen = false
                },
                onDismissRequest = { isNewStepDialogOpen = false }
            )
        }
    }
}

@Composable
fun RoutineStepEditorDialog(
    step: RoutineStep?,
    allExercises: ImmutableList<Exercise>,
    onSave: (RoutineStep) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var isExercise by rememberSaveable(step) { mutableStateOf(step == null || step is RoutineStep.ExerciseStep) }

    var pauseStep by remember {
        mutableStateOf<Duration?>(
            (step as? RoutineStep.PauseStep)?.duration ?: 1.minutes
        )
    }

    var selectedExercise by remember { mutableStateOf((step as? RoutineStep.ExerciseStep)?.exercise) }
    var customExecution by remember { mutableStateOf((step as? RoutineStep.ExerciseStep)?.customExecution?.asCustomExecution()) }

    var exerciseFilterQuery by rememberSaveable { mutableStateOf("") }

    val filteredExercises by remember {
        derivedStateOf {
            if (exerciseFilterQuery.isBlank()) listOfNotNull(selectedExercise).toImmutableList()
            else allExercises
                .filter {
                    it.name.contains(exerciseFilterQuery, ignoreCase = true) ||
                            it.description.contains(exerciseFilterQuery, ignoreCase = true) ||
                            it.tags.any { tag ->
                                tag.label.contains(
                                    exerciseFilterQuery,
                                    ignoreCase = true
                                )
                            }
                }
                .toImmutableList()
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {

            Column(modifier = Modifier.padding(24.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Crossfade(
                        targetState = isExercise,
                        modifier = Modifier.weight(1F),
                        label = "CrossfadeButtonSelection:Exercise"
                    ) {
                        if (it) {
                            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Übung")
                            }
                        } else {

                            TextButton(
                                onClick = { isExercise = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Übung")
                            }
                        }
                    }
                    Crossfade(
                        targetState = isExercise,
                        modifier = Modifier.weight(1F),

                        label = "CrossfadeButtonSelection:Pause"
                    ) {
                        if (it) {
                            TextButton(
                                onClick = { isExercise = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Pause")
                            }
                        } else {

                            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Pause")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isExercise) {

                    OutlinedTextField(
                        value = exerciseFilterQuery,
                        onValueChange = { exerciseFilterQuery = it },
                        singleLine = true,
                        label = { Text("Nach Übung suchen…") },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    LazyColumn {
                        items(filteredExercises, key = { it.id }) { exercise ->
                            ExerciseListItem(
                                exercise = exercise,
                                onClick = {
                                    selectedExercise = exercise
                                    customExecution = null
                                },
                                leadingContent = null,
                                trailingContent = {
                                    if (selectedExercise == exercise) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            tint = Success,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                },
                                supportingContent = {
                                    Text(text = exerciseExecution(execution = exercise.execution))
                                }
                            )
                        }
                    }

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sie können die Ausführung individuell für diese Routine überschreiben.",
                        style = MaterialTheme.typography.bodySmall,
                    )

                    CustomExerciseExecutionEditor(
                        value = customExecution,
                        onValueChange = { customExecution = it },
                        basedOn = selectedExercise?.execution,
                        enabled = selectedExercise != null,
                    )
                } else {
                    DurationTextField(
                        value = pauseStep,
                        onValueChange = { pauseStep = it },
                        required = false,
                        label = { Text(text = "Pause", softWrap = false, maxLines = 1) },
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Done,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val stepToSave = if (isExercise) {
                            selectedExercise?.let {
                                RoutineStep.ExerciseStep(
                                    sortIndex = -1,
                                    exercise = it,
                                    customExecution = customExecution?.asExerciseExecutionOrNull(),
                                )
                            }
                        } else {
                            pauseStep?.let {
                                RoutineStep.PauseStep(
                                    sortIndex = -1,
                                    duration = it,
                                )
                            }
                        }
                        if (stepToSave != null) onSave(stepToSave)
                    },
                    enabled =
                    if (isExercise) selectedExercise != null
                    else pauseStep?.isPositive() ?: false,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(text = "Speichern")
                }

            }
        }
    }
}


fun LazyListScope.stepsItems(
    reorderableLazyColumnState: ReorderableLazyListState,
    allExercises: ImmutableList<Exercise>,
    hapticFeedback: HapticFeedback,
    value: ImmutableList<RoutineStep>,
    onValueChange: (List<RoutineStep>) -> Unit,
) {
    items(value, key = { it.sortIndex }, contentType = {
        when (it) {
            is RoutineStep.ExerciseStep -> "ExerciseStep"
            is RoutineStep.PauseStep -> "PauseStep"
        }
    }) { step ->
        var isStepDialogOpen by remember {
            mutableStateOf(false)
        }
        ReorderableItem(reorderableLazyColumnState, key = step.sortIndex) { isDragging ->
            val elevation by animateDpAsState(
                if (isDragging) 4.dp else 0.dp,
                label = "DragElevation"
            )

            val dismissState = rememberDismissState(
                confirmValueChange = {
                    if (!isDragging && (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd)) {
                        onValueChange(value.toPersistentList() - step)
                        true
                    } else false
                },
                positionalThreshold = with(LocalDensity.current) {
                    {
                        112.dp.toPx()
                    }
                }
            )

            SwipeToDismiss(
                state = dismissState,
                background = { DismissBackground(dismissState) },
                dismissContent = {
                    when (step) {
                        is RoutineStep.ExerciseStep -> {
                            ExerciseListItem(
                                exercise = step.exercise,
                                execution = step.execution,
                                trailingContent = {
                                    IconButton(
                                        modifier = Modifier
                                            .draggableHandle(
                                                onDragStarted = {
                                                    hapticFeedback.dragStart()
                                                },
                                                onDragStopped = {
                                                    hapticFeedback.gestureEnd()
                                                },
                                            ),
                                        onClick = {},
                                    ) {
                                        Icon(Icons.Rounded.DragHandle, contentDescription = null)
                                    }
                                },
                                overlineContent = null,
                                tonalElevation = elevation,
                                shadowElevation = elevation,
                                onClick = { isStepDialogOpen = true },
                            )
                        }

                        is RoutineStep.PauseStep -> {
                            ListItem(
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(ExerciseListItemDefaults.ImageSizeTwoLines)
                                            .background(
                                                color = FloatingActionButtonDefaults.containerColor,
                                                shape = RoundedCornerShape(ImageCornerSize),
                                            )
                                    ) {
                                        Icon(
                                            Icons.Rounded.Pause,
                                            contentDescription = null,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                },
                                headlineContent = { Text(text = "${step.duration.inWholeSeconds}s Pause") },
                                trailingContent = {
                                    IconButton(
                                        modifier = Modifier
                                            .draggableHandle(
                                                onDragStarted = {
                                                    hapticFeedback.dragStart()
                                                },
                                                onDragStopped = {
                                                    hapticFeedback.gestureEnd()
                                                },
                                            ),
                                        onClick = {},
                                    ) {
                                        Icon(Icons.Rounded.DragHandle, contentDescription = null)
                                    }
                                },
                                tonalElevation = elevation,
                                shadowElevation = elevation,
                                modifier = Modifier.clickableWithClearFocus {
                                    isStepDialogOpen = true
                                },
                            )
                        }
                    }
                }
            )
        }


        if (isStepDialogOpen) {
            RoutineStepEditorDialog(
                step = step,
                allExercises = allExercises,
                onSave = { updatedStep ->
                    val steps = value.toMutableList().also {
                        it[it.indexOf(step)] = updatedStep
                    }
                    onValueChange(steps)
                    isStepDialogOpen = false
                },
                onDismissRequest = { isStepDialogOpen = false }
            )
        }
    }
}


@Composable
private fun DismissBackground(dismissState: DismissState) {
    val direction = dismissState.dismissDirection ?: return
    val backgroundColor by animateColorAsState(
        targetValue = when (dismissState.targetValue) {
            DismissValue.Default -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            else -> Color(0xFFFF1744)
        },
        label = "DismissColorAnimation",
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    val iconColor by animateColorAsState(
        targetValue = when (dismissState.targetValue) {
            DismissValue.Default -> LocalContentColor.current.copy(alpha = 0.2f)
            else -> LocalContentColor.current
        },
        label = "DismissIconColorAnimation",
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (direction == DismissDirection.StartToEnd) Icon(
            Icons.Rounded.Delete,
            contentDescription = null,
            tint = iconColor,
        )
        Spacer(modifier = Modifier)
        if (direction == DismissDirection.EndToStart) Icon(
            Icons.Rounded.Delete,
            contentDescription = null,
            tint = iconColor,
        )
    }
}


@Composable
fun CustomExerciseExecutionEditor(
    value: CustomExerciseExecution?,
    basedOn: ExerciseExecution?,
    onValueChange: (CustomExerciseExecution) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var sets: Int? by rememberSaveable(value) { mutableStateOf(value?.sets) }
    var reps: Int? by rememberSaveable(value) { mutableStateOf(value?.reps) }
    var hold: Duration? by rememberSaveable(value, stateSaver = DurationSaver) {
        mutableStateOf(value?.hold)
    }
    var pause: Duration? by rememberSaveable(value, stateSaver = DurationSaver) {
        mutableStateOf(value?.pause)
    }

    fun emitChange() {
        onValueChange(
            CustomExerciseExecution(
                sets ?: basedOn?.sets, reps, hold, pause
            )
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NumberTextField(
            value = sets,
            onValueChange = {
                sets = it
                emitChange()
            },
            required = value != null,
            label = { Text(text = "Sätze", softWrap = false, maxLines = 1) },
            modifier = Modifier.weight(1F),
            enabled = enabled,
        )

        if (basedOn?.reps != null) {
            NumberTextField(
                value = reps,
                onValueChange = {
                    reps = it
                    emitChange()
                },
                required = false,
                label = { Text(text = "Wdh.", softWrap = false, maxLines = 1) },
                modifier = Modifier.weight(1F),
                enabled = enabled,
            )
        }

        if (basedOn?.hold != null) {
            DurationTextField(
                value = hold,
                onValueChange = {
                    hold = it
                    emitChange()
                },
                required = false,
                label = { Text(text = "Halten", softWrap = false, maxLines = 1) },
                modifier = Modifier.weight(1F),
                enabled = enabled,
            )
        }

        DurationTextField(
            value = pause,
            onValueChange = {
                pause = it
                emitChange()
            },
            required = false,
            label = { Text(text = "Pause", softWrap = false, maxLines = 1) },
            modifier = Modifier.weight(1F),
            imeAction = ImeAction.Done,
            enabled = enabled,
        )
    }
}
