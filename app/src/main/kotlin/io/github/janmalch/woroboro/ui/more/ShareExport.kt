package io.github.janmalch.woroboro.ui.more

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import io.github.janmalch.woroboro.R
import java.io.File

class ShareExport : ActivityResultContract<File, Boolean>() {
    override fun createIntent(context: Context, input: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "io.github.janmalch.woroboro.fileprovider",
            input
        )

        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                clipData = ClipData.newRawUri("", uri)
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra("EXTRA_FILE_PATH", input)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            },
            context.resources.getString(R.string.zip_export)
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}

@Composable
fun rememberShareFunction(
    onResult: (ok: Boolean) -> Unit,
): (File) -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ShareExport(),
        onResult = onResult,
    )
    return remember {
        fun(file: File) {
            launcher.launch(file)
        }
    }
}
