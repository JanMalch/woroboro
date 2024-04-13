package io.github.janmalch.woroboro.ui.more

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation


const val MORE_GRAPH_ROUTE = "more"

fun NavController.navigateToMoreGraph(navOptions: NavOptions? = null) {
    this.navigate(MORE_GRAPH_ROUTE, navOptions)
}

fun NavController.navigateToMoreGraph(builder: NavOptionsBuilder.() -> Unit) {
    this.navigate(MORE_GRAPH_ROUTE, builder)
}

fun NavGraphBuilder.moreGraph(
    onShowSnackbar: (String) -> Unit
) {
    navigation(
        route = MORE_GRAPH_ROUTE,
        startDestination = MORE_SCREEN_ROUTE,
    ) {
        moreScreen(
            onShowSnackbar = onShowSnackbar,
        )
    }

}