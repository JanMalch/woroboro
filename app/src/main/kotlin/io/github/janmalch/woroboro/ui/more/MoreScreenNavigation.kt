package io.github.janmalch.woroboro.ui.more

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.ui.CollectAsEvents
import io.github.janmalch.woroboro.ui.DataOutcome
import io.github.janmalch.woroboro.ui.Outcome


const val MORE_SCREEN_ROUTE = "$MORE_GRAPH_ROUTE/more"


fun NavGraphBuilder.moreScreen(
    onShowSnackbar: (String) -> Unit,
) {
    composable(
        route = MORE_SCREEN_ROUTE,
    ) {
        val context = LocalContext.current
        val viewModel = hiltViewModel<MoreScreenViewModel>()
        val share = rememberShareFunction(onResult = { viewModel.cleanExports() })

        CollectAsEvents(viewModel.onClearLastRunsFinished) {
            val message = when (it) {
                Outcome.Success -> context.getString(R.string.last_runs_cleared_success)
                Outcome.Failure -> context.getString(R.string.unknown_error_message)
            }
            onShowSnackbar(message)
        }

        CollectAsEvents(viewModel.onExportFinished) {
            when (it) {
                is DataOutcome.Success -> share(it.data)
                is DataOutcome.Failure -> onShowSnackbar(context.getString(R.string.unknown_error_message))
            }
        }

        MoreScreen(
            onViewLicenses = {
                context.startActivity(
                    Intent(
                        context,
                        OssLicensesMenuActivity::class.java
                    )
                )
            },
            onClearLastRuns = { viewModel.clearLastRuns() },
            onExport = { viewModel.export() },
        )
    }
}
