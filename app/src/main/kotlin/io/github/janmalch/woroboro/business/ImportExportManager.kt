package io.github.janmalch.woroboro.business

import android.content.Context
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.woroboro.models.Exercise
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

interface ImportExportManager {

    suspend fun export(): File

    suspend fun clean()
}

class ImportExportManagerImpl
@Inject
constructor(
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    @ApplicationContext private val context: Context,
) : ImportExportManager {

    private val exportsDir = File(context.cacheDir, "exports").also(File::mkdirs)

    override suspend fun export(): File {
        val exercises = exerciseRepository.findAll().first()
        val routines = routineRepository.findAll().first()

        return withContext(Dispatchers.IO) {
            val exportFile =
                File(
                    exportsDir,
                    "woroboro-${
                    LocalDateTime.now().toString().replace('T', '_').replace('.', '-')
                        .replace(':', '-')
                }.zip"
                )
            exportFile.createNewFile()
            writeZip(
                exportFile,
                exercises.map<Exercise, ZipContent> {
                    ZipContent.TextFile(
                        name = it.name + ".woroboro.txt",
                        content = it.asText(includeMedia = true)
                    )
                } +
                    exercises.flatMap {
                        it.media.map { media ->
                            ZipContent.MediaFile(file = media.source.toUri().toFile())
                        }
                    }
            )
            exportFile
        }
    }

    override suspend fun clean() {
        withContext(Dispatchers.IO) {
            exportsDir.listFiles()?.forEach { file ->
                runCatching { file.delete() }
                    .onFailure { tr ->
                        Log.e("ImportExportManagerImpl", "Error while deleting export file.", tr)
                    }
                    .onSuccess { deleted ->
                        if (deleted) {
                            Log.d(
                                "ImportExportManagerImpl",
                                "Export file '${file.name}' has been deleted successfully."
                            )
                        } else {
                            Log.w(
                                "ImportExportManagerImpl",
                                "Export file '${file.name}' has not been deleted."
                            )
                        }
                    }
            }
        }
    }
}
