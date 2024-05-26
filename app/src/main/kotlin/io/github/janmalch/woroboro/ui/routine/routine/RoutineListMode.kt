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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.models.Media
import io.github.janmalch.woroboro.models.RoutineStep
import io.github.janmalch.woroboro.ui.components.ExerciseListItem
import io.github.janmalch.woroboro.ui.components.common.LinkifyText
import io.github.janmalch.woroboro.ui.theme.Success
import io.github.janmalch.woroboro.utils.formatForTimer
import kotlin.time.Duration

@Composable
fun RoutineListMode(
    uiState: RoutineUiState.Success,
    onFinishStep: (RoutineStep) -> Unit,
    onUndoStep: (RoutineStep) -> Unit,
    onDone: (Boolean, Duration) -> Unit,
) {
    // FIXME: store in ViewModel!
    val stopwatch = remember { Stopwatch() }

    val stopwatchState by stopwatch.state.collectAsState()
    val passedTime by stopwatch.time.collectAsState(initial = Duration.ZERO)

    val isCompletelyDone = uiState.unfinishedExercises.isEmpty()
    val doneHeadlineColor by
        animateColorAsState(
            targetValue = if (isCompletelyDone) Success else LocalContentColor.current,
            label = "DoneHeadlineColorAnimation"
        )

    LaunchedEffect(isCompletelyDone) {
        stopwatch.pause()
        // FIXME: handle in ViewModel!
        onDone(isCompletelyDone, passedTime)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(uiState.unfinishedExercises, key = { it.sortIndex }) { exercise ->
                ExerciseStepListItem(
                    step = exercise,
                    isDone = false,
                    onClick = { onFinishStep(exercise) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
            item {
                Text(
                    text =
                        if (isCompletelyDone) stringResource(id = R.string.routine_done)
                        else
                            stringResource(
                                id = R.string.routine_in_progress,
                                uiState.finishedExercises.size,
                                uiState.routine.exercises.size
                            ),
                    style = MaterialTheme.typography.labelLarge,
                    modifier =
                        Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                            .animateItemPlacement(),
                    color = doneHeadlineColor,
                )
            }

            items(uiState.finishedExercises, key = { it.sortIndex }) { exercise ->
                ExerciseStepListItem(
                    step = exercise,
                    isDone = true,
                    onClick = { onUndoStep(exercise) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) {
            TimerOverlay(
                passedTime = passedTime,
                previousTime = uiState.routine.lastRunDuration,
                isRunning = stopwatchState == Stopwatch.State.Running,
                onStartPauseClick = stopwatch::toggle,
            )
        }
    }
}

@Composable
fun TimerOverlay(
    passedTime: Duration,
    previousTime: Duration?,
    isRunning: Boolean,
    onStartPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .shadow(8.dp, shape = RoundedCornerShape(8.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    RoundedCornerShape(12.dp)
                )
                .padding(8.dp),
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatForTimer(passedTime),
                style = MaterialTheme.typography.labelLarge,
                fontSize = 24.sp,
            )

            if (previousTime != null) {
                val formatted = remember { " / " + formatForTimer(previousTime) }

                Text(
                    text = formatted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.graphicsLayer(alpha = 0.8f, translationY = -3F)
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        FloatingActionButton(onClick = onStartPauseClick) {
            Icon(
                if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null
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
    var isExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Column(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        ExerciseListItem(
            exercise = step.exercise,
            execution = step.execution,
            leadingContent = {
                MediaWithDoneState(media = step.exercise.media.firstOrNull(), isDone = isDone)
            },
            trailingContent = {
                IconButton(onClick = { isExpanded = true }) {
                    Icon(Icons.Rounded.QuestionMark, contentDescription = null)
                }
            },
            overlineContent = null,
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

            LinkifyText(
                text = step.exercise.description,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

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
                            modifier =
                                Modifier
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
fun MediaWithDoneState(
    media: Media?,
    isDone: Boolean,
) {
    Box(
        modifier =
            Modifier.size(64.dp)
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
                modifier =
                    Modifier.fillMaxSize()
                        .background(Success.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    tint = Success,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).background(Color.White, CircleShape)
                )
            }
        }
    }
}
