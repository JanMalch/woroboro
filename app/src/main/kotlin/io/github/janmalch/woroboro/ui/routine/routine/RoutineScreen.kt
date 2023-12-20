package io.github.janmalch.woroboro.ui.routine.routine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.models.RoutineStep
import io.github.janmalch.woroboro.ui.components.common.DoneCelebration
import java.util.UUID
import kotlin.time.Duration


@Composable
fun RoutineScreen(
    uiState: RoutineUiState,
    onGoToEditor: (UUID) -> Unit,
    onFinishStep: (RoutineStep) -> Unit,
    onUndoStep: (RoutineStep) -> Unit,
    onRoutineDone: (FullRoutine, Duration) -> Unit,
    onBackClick: () -> Unit,
) {
    when (uiState) {
        RoutineUiState.Loading -> Box(modifier = Modifier.fillMaxSize())

        RoutineUiState.Failure -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    text = "Ein unbekannter Fehler ist aufgetreten.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        is RoutineUiState.Success -> RoutineSuccessScreen(
            uiState = uiState,
            onGoToEditor = onGoToEditor,
            onFinishStep = onFinishStep,
            onUndoStep = onUndoStep,
            onRoutineDone = onRoutineDone,
            onBackClick = onBackClick,
        )
    }
}


@Composable
fun RoutineSuccessScreen(
    uiState: RoutineUiState.Success,
    onGoToEditor: (UUID) -> Unit,
    onRoutineDone: (FullRoutine, Duration) -> Unit,
    onFinishStep: (RoutineStep) -> Unit,
    onUndoStep: (RoutineStep) -> Unit,
    onBackClick: () -> Unit,
) {
    var isCelebrationVisible by remember { mutableStateOf(false) }
    Box {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = uiState.routine.name)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { onGoToEditor(uiState.routine.id) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                RoutineListMode(
                    uiState = uiState,
                    onFinishStep = onFinishStep,
                    onUndoStep = onUndoStep,
                    onDone = { done, totalTime ->
                        isCelebrationVisible = done
                        onRoutineDone(uiState.routine, totalTime)
                    })
            }
        }

        if (isCelebrationVisible) {
            DoneCelebration(
                modifier = Modifier.fillMaxSize(),
                // optional, just to remove view again
                onFinished = { isCelebrationVisible = false },
            )
        }
    }
}