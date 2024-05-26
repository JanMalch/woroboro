package io.github.janmalch.woroboro.business

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.woroboro.models.Media
import io.github.janmalch.woroboro.utils.AppDispatchers.IO
import io.github.janmalch.woroboro.utils.Dispatcher
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface MediaFileManager {
    suspend fun add(uris: Collection<Uri>): List<Media>

    suspend fun delete(mediaIds: Collection<UUID>)
}

class MediaFileManagerImpl
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val mediaOptimizer: MediaOptimizer,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : MediaFileManager {

    private val thumbnailsDir =
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "thumbnails")
    private val imagesDir =
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "images")
    private val videosDir =
        File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "videos")

    override suspend fun add(uris: Collection<Uri>): List<Media> {
        val created = mutableListOf<Media>()

        try {
            uris.forEach { uri ->
                val mediaId = UUID.randomUUID()

                // TODO: support video

                val thumbnail = thumbnailOf(mediaId)
                mediaOptimizer.toThumb(uri, thumbnail)

                val image = imageOf(mediaId)
                mediaOptimizer.toImage(uri, image)

                created +=
                    Media.Image(
                        id = mediaId,
                        source = image.toUri().toString(),
                        thumbnail = thumbnail.toUri().toString(),
                    )
            }
        } catch (e: Exception) {
            delete(created.map(Media::id))
            throw e
        }
        return created
    }

    override suspend fun delete(mediaIds: Collection<UUID>) {
        withContext(ioDispatcher) {
            mediaIds.forEach {
                thumbnailOf(it).deleteSafely()
                imageOf(it).deleteSafely()
                videoOf(it).deleteSafely()
            }
        }
    }

    private fun thumbnailOf(id: UUID): File =
        File(thumbnailsDir, id.toString() + mediaOptimizer.imageExtension)

    private fun imageOf(id: UUID): File =
        File(imagesDir, id.toString() + mediaOptimizer.imageExtension)

    private fun videoOf(id: UUID): File =
        File(videosDir, id.toString() + mediaOptimizer.videoExtension)
}

private fun File.deleteSafely() {
    try {
        if (exists()) delete()
    } catch (e: Exception) {
        Log.e("MediaFileManagerImpl", "Failed to delete '${this.absolutePath}' safely.", e)
    }
}
