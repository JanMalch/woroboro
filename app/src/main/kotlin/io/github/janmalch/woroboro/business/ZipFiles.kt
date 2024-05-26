package io.github.janmalch.woroboro.business

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

sealed interface ZipContent {
    data class TextFile(val name: String, val content: String) : ZipContent

    data class MediaFile(val file: File) : ZipContent
}

fun writeZip(
    dest: File,
    contents: List<ZipContent>,
) {
    ZipOutputStream(BufferedOutputStream(FileOutputStream(dest))).use { out ->
        for (content in contents) {
            when (content) {
                is ZipContent.MediaFile -> {
                    FileInputStream(content.file).use { fi ->
                        BufferedInputStream(fi).use { buffi ->
                            val entry = ZipEntry(content.file.name)
                            out.putNextEntry(entry)
                            buffi.copyTo(out, 1024)
                            out.closeEntry()
                        }
                    }
                }
                is ZipContent.TextFile -> {
                    val entry = ZipEntry(content.name)
                    out.putNextEntry(entry)
                    out.write(content.content.toByteArray())
                    out.closeEntry()
                }
            }
        }
    }
}
