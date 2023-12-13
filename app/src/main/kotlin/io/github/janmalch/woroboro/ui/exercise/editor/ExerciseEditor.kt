package io.github.janmalch.woroboro.ui.exercise.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.janmalch.woroboro.models.EditedExercise
import io.github.janmalch.woroboro.models.EditedMedia
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.ExerciseExecution
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.components.DurationTextField
import io.github.janmalch.woroboro.ui.components.NumberTextField
import io.github.janmalch.woroboro.ui.components.common.ButtonLoading
import io.github.janmalch.woroboro.ui.components.common.CloseIconButton
import io.github.janmalch.woroboro.ui.components.common.IsFavoriteCheckbox
import io.github.janmalch.woroboro.ui.components.common.MoreMenu
import io.github.janmalch.woroboro.ui.components.common.MoreMenuItem
import io.github.janmalch.woroboro.ui.components.common.clearFocusAsOutsideClick
import io.github.janmalch.woroboro.ui.components.tags.TagSelectors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val DurationSaver = Saver<Duration?, String>(
    save = { it?.toIsoString() ?: "" },
    restore = { it.takeUnless(String::isEmpty)?.let(Duration.Companion::parseIsoString) }
)

@Composable
fun ExerciseEditorScreen(
    isLoading: Boolean,
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    exercise: Exercise?,
    allRoutinesFlow: Flow<ImmutableList<Routine>>,
    onSave: (EditedExercise) -> Unit,
    onDelete: (UUID) -> Unit,
    onBackClick: () -> Unit,
    onAddToRoutine: (exerciseId: UUID, routineId: UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val id: UUID = rememberSaveable(exercise) { exercise?.id ?: UUID.randomUUID() }
    var name: String by rememberSaveable(exercise) { mutableStateOf(exercise?.name ?: "") }
    var description: String by rememberSaveable(exercise) {
        mutableStateOf(
            exercise?.description ?: ""
        )
    }
    var tags: List<Tag> by rememberSaveable(exercise) {
        mutableStateOf(
            ArrayList(exercise?.tags ?: listOf())
        )
    }
    var media by remember(exercise) {
        mutableStateOf(
            EditedMedia(
                existing = exercise?.media?.toPersistentList() ?: persistentListOf(),
                added = emptySet(),
            )
        )
    }
    var sets: Int? by rememberSaveable(exercise) { mutableStateOf(exercise?.execution?.sets ?: 3) }
    var reps: Int? by rememberSaveable(exercise) { mutableStateOf(exercise?.execution?.reps) }
    var hold: Duration? by rememberSaveable(exercise, stateSaver = DurationSaver) {
        mutableStateOf(
            exercise?.execution?.hold
        )
    }
    var pause: Duration? by rememberSaveable(exercise, stateSaver = DurationSaver) {
        mutableStateOf(
            exercise?.execution?.pause ?: 30.seconds
        )
    }
    var isFavorite: Boolean by rememberSaveable(exercise) {
        mutableStateOf(
            exercise?.isFavorite ?: false
        )
    }
    var isAddToRoutineDialogOpen by rememberSaveable(exercise) {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { CloseIconButton(onClick = onBackClick) },
                title = {},
                actions = {
                    Button(
                        onClick = {
                            sets?.let {
                                val edited = EditedExercise(
                                    exercise = Exercise(
                                        id = id,
                                        name = name.trim(),
                                        description = description.trim(),
                                        tags = tags.toImmutableList(),
                                        execution = ExerciseExecution(
                                            sets = it,
                                            reps = reps,
                                            hold = hold,
                                            pause = pause,
                                        ),
                                        isFavorite = isFavorite,
                                        media = media.existing,
                                    ),
                                    addedMedia = media.added,
                                )
                                onSave(edited)
                            }
                        },
                        enabled = !isLoading && name.isNotBlank() && (reps != null || hold != null),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth,
                            minHeight = 36.dp
                        )
                    ) {
                        ButtonLoading(isVisible = isLoading)
                        Text(text = "Speichern")
                    }
                    MoreMenu(enabled = !isLoading && exercise != null) {
                        MoreMenuItem(
                            text = { Text(text = "Zu Routine hinzufügen") },
                            icon = {
                                Icon(
                                    Icons.Rounded.PostAdd,
                                    contentDescription = null,
                                )
                            },
                            onClick = { if (exercise != null) isAddToRoutineDialogOpen = true }
                        )
                        MoreMenuItem(
                            text = { Text(text = "Übung löschen") },
                            icon = {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    contentDescription = null,
                                )
                            },
                            onClick = { if (exercise != null) onDelete(exercise.id) }
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .clearFocusAsOutsideClick()
                .padding(padding),
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "Name") },
                    singleLine = true,
                    // isError = name.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Beschreibung") },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 192.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Default, // line break
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NumberTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        required = true,
                        label = { Text(text = "Sätze", softWrap = false, maxLines = 1) },
                        modifier = Modifier.weight(1F),
                    )

                    NumberTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        required = false,
                        label = { Text(text = "Wdh.", softWrap = false, maxLines = 1) },
                        modifier = Modifier.weight(1F),
                    )

                    DurationTextField(
                        value = hold,
                        onValueChange = { hold = it },
                        required = false,
                        label = { Text(text = "Halten", softWrap = false, maxLines = 1) },
                        modifier = Modifier.weight(1F),
                    )

                    DurationTextField(
                        value = pause,
                        onValueChange = { pause = it },
                        required = false,
                        label = { Text(text = "Pause", softWrap = false, maxLines = 1) },
                        modifier = Modifier.weight(1F),
                        imeAction = ImeAction.Done,
                    )
                }
            }

            HorizontalDivider()

            MediaPicker(
                value = media,
                onValueChange = { media = it },
                title = { Text(text = "Bilder und Videos") },
                contentPadding = PaddingValues(horizontal = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                headerModifier = Modifier.padding(horizontal = 24.dp),
            )

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {

                IsFavoriteCheckbox(
                    text = "Lieblingsübung",
                    value = isFavorite,
                    onValueChange = { isFavorite = it },
                )

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TagSelectors(
                        availableTags = availableTags,
                        value = tags,
                        isCounterVisible = true,
                        onValueChange = { tags = it }
                    )
                }
            }
        }

        if (isAddToRoutineDialogOpen) {
            AddToRoutineDialog(
                allRoutinesFlow = allRoutinesFlow,
                onRoutineSelected = {
                    if (exercise != null) {
                        onAddToRoutine(exercise.id, it)
                        isAddToRoutineDialogOpen = false
                    }
                },
                onDismissRequest = {
                    isAddToRoutineDialogOpen = false
                }
            )
        }
    }
}

@Composable
fun AddToRoutineDialog(
    allRoutinesFlow: Flow<ImmutableList<Routine>>,
    onRoutineSelected: (routineId: UUID) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var filter by rememberSaveable {
        mutableStateOf("")
    }
    val allRoutines by allRoutinesFlow.collectAsState(initial = persistentListOf())
    val filteredRoutines by remember {
        derivedStateOf {
            if (filter.isBlank()) persistentListOf()
            else allRoutines.filter { it.name.contains(filter, ignoreCase = true) }
                .toImmutableList()
        }
    }
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    singleLine = true,
                    label = { Text("Nach Routine suchen…") },
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(filteredRoutines, key = { it.id }) { routine ->
                        ListItem(
                            headlineContent = { Text(text = routine.name) },
                            modifier = Modifier.clickable {
                                onRoutineSelected(routine.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
