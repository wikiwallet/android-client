package io.golos.data.file_system

import android.content.Context
import android.net.Uri
import io.golos.domain.BitmapsUtils
import io.golos.domain.DispatchersProvider
import io.golos.domain.FileSystemHelper
import io.golos.utils.id.IdUtil
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FileSystemHelperImpl
@Inject
constructor(
    private val appContext: Context,
    private val dispatchersProvider: DispatchersProvider,
    private val bitmapUtils: BitmapsUtils
) : FileSystemHelper {

    private val junkFolderName = "junk"

    override suspend fun copyImageToJunkFolder(sourceUri: Uri?): Uri? =
        withContext(dispatchersProvider.ioDispatcher) {
            try {
                if(sourceUri == null) {
                    return@withContext null
                }

                val isGif = bitmapUtils.isGif(sourceUri)
                val fileExt = if(isGif) ".gif" else ".jpg"

                val target = File(getCacheFolder(junkFolderName), "${IdUtil.generateStringId()}$fileExt")

                appContext.contentResolver.openInputStream(sourceUri).use { input ->
                    target.outputStream().use { fileOut ->
                        input?.copyTo(fileOut)
                    }
                }

                if(!isGif) {
                    bitmapUtils.correctOrientation(target)
                }

                Uri.fromFile(target)
            } catch (ex: Exception) {
                Timber.e(ex)
                null
            }
        }

    override fun getTempImageFile(): File =
        File.createTempFile("JPG_", IdUtil.generateLongId().toString(), appContext.externalCacheDir)

    @Suppress("SameParameterValue")
    private fun getCacheFolder(subFolderName: String): File {
        val path = File(appContext.externalCacheDir, subFolderName)
        if (!path.exists()) {
            path.mkdirs()
        }
        return path
    }
}