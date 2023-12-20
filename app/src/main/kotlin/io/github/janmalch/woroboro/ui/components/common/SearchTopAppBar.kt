package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun SearchTopAppBar(
    title: @Composable () -> Unit,
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    actions: (@Composable () -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
) {
    val searchFocusRequester = remember { FocusRequester() }
    var queryValue by remember(query) {
        mutableStateOf(query)
    }
    var isSearchMode by remember(query) {
        mutableStateOf(query.isNotEmpty())
    }

    CenterAlignedTopAppBar(
        navigationIcon = {
            AnimatedVisibility(
                visible = isSearchMode,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally(),
            ) {
                IconButton(onClick = {
                    queryValue = ""
                    onQueryChange("")
                    isSearchMode = false
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                }
            }
        },
        title = {
            if (isSearchMode) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                    SimpleTextField(
                        value = queryValue,
                        onValueChange = {
                            queryValue = it
                            onQueryChange(it)
                        },
                        placeholder = placeholder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
                    )

                    LaunchedEffect(Unit) {
                        searchFocusRequester.requestFocus()
                    }
                }
            } else {
                title()
            }
        },
        actions = {
            Crossfade(isSearchMode, label = "SearchTopAppBar:Actions:SearchModeChange") {
                if (it) {
                    IconButton(onClick = {
                        queryValue = ""
                        onQueryChange("")
                        searchFocusRequester.requestFocus()
                    }) {
                        Icon(Icons.Rounded.Cancel, contentDescription = null)
                    }
                } else {
                    IconButton(onClick = {
                        isSearchMode = true
                    }) {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                    }
                }
            }
            actions?.invoke()
        },
        scrollBehavior = scrollBehavior,
    )
}