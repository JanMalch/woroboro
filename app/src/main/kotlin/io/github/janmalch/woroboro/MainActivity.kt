package io.github.janmalch.woroboro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.janmalch.woroboro.business.LaunchDataService
import io.github.janmalch.woroboro.business.reminders.AndroidReminderNotifications.Companion.getRoutineFilter
import io.github.janmalch.woroboro.ui.AppContainer
import io.github.janmalch.woroboro.ui.routine.ROUTINE_GRAPH_ROUTE
import io.github.janmalch.woroboro.ui.theme.WoroboroTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var launchDataService: LaunchDataService

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        launchDataService.setRoutineFilter(intent.extras?.getRoutineFilter())

        setContent {
            WoroboroTheme {
                AppContainer(startDestination = ROUTINE_GRAPH_ROUTE)
            }
        }
    }
}

