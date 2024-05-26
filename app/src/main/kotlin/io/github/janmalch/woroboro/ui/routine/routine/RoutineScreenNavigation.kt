package io.github.janmalch.woroboro.ui.routine.routine

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import io.github.janmalch.woroboro.ui.routine.ROUTINE_GRAPH_ROUTE
import io.github.janmalch.woroboro.ui.routine.isRoutineList
import java.util.UUID

const val ROUTINE_SCREEN_ARG_ID = "id"
private const val ROUTINE_SCREEN_PATH = "$ROUTINE_GRAPH_ROUTE/routine/"
const val ROUTINE_SCREEN_DEEPLINK = "woroboro://$ROUTINE_SCREEN_PATH"
private const val ROUTINE_SCREEN_ROUTE = "$ROUTINE_SCREEN_PATH{$ROUTINE_SCREEN_ARG_ID}"

fun NavController.navigateToRoutineScreen(id: UUID, navOptions: NavOptions? = null) {
    this.navigate(ROUTINE_SCREEN_PATH + id, navOptions)
}

fun NavGraphBuilder.routineScreen(
    onBackClick: () -> Unit,
    onGoToEditor: (UUID) -> Unit,
) {
    composable(
        route = ROUTINE_SCREEN_ROUTE,
        arguments = listOf(navArgument(ROUTINE_SCREEN_ARG_ID) { type = NavType.StringType }),
        deepLinks = listOf(navDeepLink { uriPattern = "$ROUTINE_SCREEN_DEEPLINK{id}" }),
        enterTransition = {
            if (initialState.destination.isRoutineList) {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    spring(
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            } else {
                null
            }
        },
        exitTransition = {
            if (targetState.destination.isRoutineList) {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    spring(
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            } else {
                null
            }
        },
    ) {
        val viewModel = hiltViewModel<RoutineViewModel>()
        val uiState by viewModel.uiState.collectAsState()

        RoutineScreen(
            uiState = uiState,
            onGoToEditor = onGoToEditor,
            onFinishStep = viewModel::finishStep,
            onUndoStep = viewModel::undoStep,
            onRoutineDone = viewModel::saveAsLastRun,
            onBackClick = onBackClick,
        )
    }
}
