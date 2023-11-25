package io.github.janmalch.woroboro.ui.routine.routine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.janmalch.woroboro.models.ExerciseExecution
import io.github.janmalch.woroboro.models.Media
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.models.RoutineStep
import io.github.janmalch.woroboro.ui.theme.Success
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Rotation
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit


/**
 * @author https://github.com/DanielMartinus/Konfetti/blob/90e6479e3f02cde424c12a05f67bf47d16349549/samples/shared/src/main/java/nl/dionsegijn/samples/shared/Presets.kt
 */
private val festiveParty = run {
    val party = Party(
        speed = 30f,
        maxSpeed = 50f,
        damping = 0.9f,
        angle = Angle.TOP,
        spread = 45,
        size = listOf(Size.SMALL, Size.LARGE),
        timeToLive = 3000L,
        rotation = Rotation(),
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(30),
        position = Position.Relative(0.5, 1.0)
    )

    listOf(
        party,
        party.copy(
            speed = 55f,
            maxSpeed = 65f,
            spread = 10,
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(10),
        ),
        party.copy(
            speed = 50f,
            maxSpeed = 60f,
            spread = 120,
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(40),
        ),
        party.copy(
            speed = 65f,
            maxSpeed = 80f,
            spread = 10,
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(10),
        )
    )
}

@Composable
fun RoutineListMode(
    routine: FullRoutine
) {
    // FIXME: store in ViewModel!
    val undoneExercises = remember {
        mutableStateListOf(
            *routine.steps.filterIsInstance<RoutineStep.ExerciseStep>().toTypedArray()
        )
    }
    val doneExercises = remember { mutableStateListOf<RoutineStep.ExerciseStep>() }
    val isCompletelyDone = undoneExercises.isEmpty()
    val doneHeadlineColor by animateColorAsState(
        targetValue = if (isCompletelyDone) Success else LocalContentColor.current,
        label = "DoneHeadlineColorAnimation"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(undoneExercises, key = { it.sortIndex }) { exercise ->
                ExerciseStepListItem(
                    step = exercise,
                    isDone = false,
                    onClick = {
                        undoneExercises.remove(exercise)
                        doneExercises.add(exercise)
                    },
                    modifier = Modifier.animateItemPlacement()
                )
            }
            item {
                Text(
                    text = if (isCompletelyDone) "Fertig! 🎉" else "Erledigt (${doneExercises.size} / ${routine.exercises.size})",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .animateItemPlacement(),
                    color = doneHeadlineColor,
                )
            }

            items(doneExercises, key = { it.sortIndex }) { exercise ->
                ExerciseStepListItem(
                    step = exercise,
                    isDone = true,
                    onClick = {
                        doneExercises.remove(exercise)
                        undoneExercises.add(0, exercise)
                    },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }

        if (isCompletelyDone) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = festiveParty,
            )
        }
    }
}

@Composable
fun ExerciseStepListItem(
    step: RoutineStep.ExerciseStep,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        ListItem(
            leadingContent = {
                MediaWithDoneState(media = step.exercise.media.firstOrNull(), isDone = isDone)
            },
            headlineContent = { Text(text = step.exercise.name) },
            supportingContent = {
                Text(text = exerciseExecution(execution = step.execution))
            },
            trailingContent = {
                IconButton(onClick = { isExpanded = true }) {
                    Icon(Icons.Rounded.QuestionMark, contentDescription = null)
                }
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        HorizontalDivider()
    }

    if (isExpanded) {
        ModalBottomSheet(onDismissRequest = { isExpanded = false }, sheetState = sheetState) {

            if (step.exercise.tags.isNotEmpty()) {
                Text(
                    text = step.exercise.tags.joinToString(separator = ", ") { it.label },
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }


            Text(
                text = step.exercise.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Text(text = step.exercise.description, modifier = Modifier.padding(horizontal = 24.dp))

            if (step.exercise.media.isNotEmpty()) {

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.Start),
                    contentPadding = PaddingValues(24.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(step.exercise.media, key = { it.id }) {
                        AsyncImage(
                            model = it.source,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                // .fillMaxHeight() better UX, .fillMaxWidth() better UI
                                .fillMaxHeight()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(16.dp)
                                )
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
@ReadOnlyComposable
fun exerciseExecution(
    execution: ExerciseExecution,
): String {
    val base = when {
        execution.reps != null -> "${execution.sets} × ${execution.reps}"
        execution.hold != null -> "${execution.sets} × ${execution.hold.inWholeSeconds}s"
        else -> "${execution.sets}"
    }
    return if (execution.pause != null) "$base · ${execution.pause.inWholeSeconds}s Pause"
    else base
}


@Composable
fun MediaWithDoneState(
    media: Media?,
    isDone: Boolean,
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = media?.thumbnail,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(
            visible = isDone,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Success.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    tint = Success,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}