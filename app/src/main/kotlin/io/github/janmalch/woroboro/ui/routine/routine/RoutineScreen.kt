package io.github.janmalch.woroboro.ui.routine.routine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.ui.components.common.FavoriteIcon


@Composable
fun RoutineScreen(
    uiState: RoutineUiState,
    onToggleFavorite: (FullRoutine) -> Unit,
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
            routine = uiState.routine,
            onToggleFavorite = onToggleFavorite,
            onBackClick = onBackClick,
        )
    }
}


@Composable
fun RoutineSuccessScreen(
    routine: FullRoutine,
    onToggleFavorite: (FullRoutine) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = routine.name)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleFavorite(routine) }) {
                        FavoriteIcon(isFavorite = routine.isFavorite)
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
            RoutineListMode(routine = routine)
        }
    }
}