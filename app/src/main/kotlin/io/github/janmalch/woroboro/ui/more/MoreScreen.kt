package io.github.janmalch.woroboro.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.github.janmalch.woroboro.R

@Composable
fun MoreScreen(
    onViewLicenses: () -> Unit,
    onClearLastRuns: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(id = R.string.more)) })
        },
        modifier = modifier,
    ) { padding ->
        var isClearLastRunsConfirmationVisible by remember {
            mutableStateOf(false)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Rounded.CleaningServices,
                            contentDescription = null,
                        )
                    },
                    headlineContent = { Text(text = stringResource(R.string.clear_last_runs)) },
                    supportingContent = {
                        Text(text = stringResource(R.string.clear_last_runs_explanation))
                    },
                    modifier = Modifier.clickable(onClick = {
                        isClearLastRunsConfirmationVisible = true
                    })
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Rounded.Upload,
                            contentDescription = null,
                        )
                    },
                    headlineContent = { Text(text = stringResource(R.string.zip_export)) },
                    supportingContent = {
                        Text(text = stringResource(R.string.zip_export_explanation))
                    },
                    modifier = Modifier.clickable(onClick = onExport)
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Rounded.Download,
                            contentDescription = null,
                        )
                    },
                    headlineContent = { Text(text = stringResource(R.string.zip_import)) },
                    supportingContent = {
                        Text(text = stringResource(R.string.zip_import_explanation))
                    },
                    modifier = Modifier.clickable(onClick = { /* TODO */ })
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Rounded.Book,
                            contentDescription = null,
                        )
                    },
                    headlineContent = { Text(text = stringResource(id = R.string.licenses)) },
                    supportingContent = {
                        Text(text = stringResource(R.string.licenses_explanation))
                    },
                    modifier = Modifier.clickable(onClick = onViewLicenses)
                )
                HorizontalDivider()
            }
        }

        if (isClearLastRunsConfirmationVisible) {
            AlertDialog(
                icon = {
                    Icon(
                        Icons.Rounded.CleaningServices,
                        contentDescription = null,
                    )
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.clear_last_runs),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(text = stringResource(id = R.string.clear_last_runs_confirmation))
                },
                onDismissRequest = { isClearLastRunsConfirmationVisible = false },
                confirmButton = {
                    TextButton(onClick = {
                        onClearLastRuns()
                        isClearLastRunsConfirmationVisible = false
                    }) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            )
        }
    }
}