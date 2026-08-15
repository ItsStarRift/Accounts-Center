package com.omerplt.accountmanager.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object CameraFileHelper {

    /** Kamera ile çekilecek fotoğraf için geçici bir dosya URI'si oluşturur. */
    fun createTempImageUri(context: Context): Uri {
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(cameraDir, "${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /** Seçilen/çekilen görseli kalıcı dahili depolamaya kopyalar ve yolunu döner. */
    fun persistImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val iconsDir = File(context.filesDir, "icons").apply { mkdirs() }
            val destFile = File(iconsDir, "${UUID.randomUUID()}.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
