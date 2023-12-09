package io.github.janmalch.woroboro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.janmalch.woroboro.business.LaunchDataService
import io.github.janmalch.woroboro.business.reminders.AndroidReminderNotifications.Companion.getRoutineFilter
import io.github.janmalch.woroboro.ui.AppContainer
import io.github.janmalch.woroboro.ui.routine.ROUTINE_GRAPH_ROUTE
import io.github.janmalch.woroboro.ui.theme.WoroboroTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var launchDataService: LaunchDataService

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        launchDataService.setRoutineFilter(intent.extras?.getRoutineFilter())

        var uiState: MainUiState by mutableStateOf(MainUiState.Loading)

        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state
                    .onEach {
                        uiState = it
                    }
                    .collect()
            }
        }

        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainUiState.Loading -> true
                is MainUiState.Success -> false
            }
        }

        setContent {
            WoroboroTheme {
                AppContainer(startDestination = ROUTINE_GRAPH_ROUTE)
            }
        }
    }
}

