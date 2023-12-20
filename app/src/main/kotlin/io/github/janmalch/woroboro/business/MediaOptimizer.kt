package io.github.janmalch.woroboro.business

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.Px
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.woroboro.utils.AppDispatchers
import io.github.janmalch.woroboro.utils.Dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface MediaOptimizer {
    val imageExtension: String
    val videoExtension: String

    suspend fun toThumb(src: Any, dest: File)
    suspend fun toImage(src: Any, dest: File)
    suspend fun toVideo(src: Any, dest: File)
}

class OnDeviceMediaOptimizer @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : MediaOptimizer {
    private val imageLoader: ImageLoader = context.imageLoader
    private val thumbSize by lazy {
        val size = context.toPx(100)
        Size(size, size)
    }

    override val imageExtension = if (isWebpUsable()) ".webp" else ".png"
    override val videoExtension = "" // TODO


    override suspend fun toThumb(src: Any, dest: File) {
        withContext(ioDispatcher) {
            optimize(
                quality = 80,
                dest = dest,
            ) {
                data(src)
                    .size(thumbSize)
                    .scale(Scale.FILL)
            }
        }
    }

    override suspend fun toImage(
        src: Any,
        dest: File,
    ) {
        withContext(ioDispatcher) {
            optimize(
                quality = 70,
                dest = dest,
            ) {
                data(src)
            }
        }
    }

    override suspend fun toVideo(src: Any, dest: File) {
        TODO("Not yet implemented")
    }

    private suspend fun optimize(
        quality: Int,
        dest: File,
        block: ImageRequest.Builder.() -> Unit,
    ) {
        try {
            ensureParentOf(dest)
            loadAndCompress(dest, quality = quality, block)
        } catch (e: Exception) {
            Log.e("OnDeviceMediaOptimizer", "Unknown error while optimizing file.", e)
            dest.takeIf { it.exists() }?.delete()
            throw e
        }
    }

    private suspend fun loadAndCompress(
        dest: File,
        quality: Int,
        block: ImageRequest.Builder.() -> Unit,
    ) = suspendCancellableCoroutine { cont ->
        val request = ImageRequest.Builder(context)
            .apply(block)
            .target(
                onSuccess = { drawable ->
                    if (!cont.isActive) {
                        cont.cancel()
                        return@target
                    }
                    val bitmap = (drawable as? BitmapDrawable)?.bitmap
                    if (bitmap == null) {
                        cont.resumeWithException(IllegalStateException("Drawable is not a bitmap."))
                        return@target
                    }
                    dest.outputStream().use { outStream ->
                        val success = if (isWebpUsable()) {
                            bitmap.compress(
                                Bitmap.CompressFormat.WEBP_LOSSY,
                                quality,
                                outStream,
                            )
                        } else {
                            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outStream)
                        }
                        if (success) {
                            cont.resume(Unit)
                        } else {
                            dest.takeIf { it.exists() }?.delete()
                            cont.resumeWithException(IllegalStateException("Unknown error while compressing image."))
                        }
                    }
                },
                onError = {
                    cont.resumeWithException(IllegalStateException("Unknown error while loading image."))
                },
            )
            .build()
        imageLoader.enqueue(request)
    }

    private fun ensureParentOf(file: File) {
        val parent = checkNotNull(file.parentFile)
        parent.mkdirs()
        check(parent.exists()) { "Failed to create parent of $file." }
    }

    companion object {
        @ChecksSdkIntAtLeast(Build.VERSION_CODES.R)
        fun isWebpUsable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }
}

/**
 * @author https://stackoverflow.com/a/6327095
 */
@Px
private fun Context.toPx(dp: Int): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    dp.toFloat(),
    resources.displayMetrics
).toInt()
