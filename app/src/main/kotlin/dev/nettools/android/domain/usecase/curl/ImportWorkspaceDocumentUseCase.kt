package dev.nettools.android.domain.usecase.curl

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Imports one picker-selected document into the curl workspace.
 */
class ImportWorkspaceDocumentUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val importWorkspaceFile: ImportWorkspaceFileUseCase,
) {

    /** Imports the document identified by [documentUri] into [targetDirectoryPath]. */
    suspend operator fun invoke(targetDirectoryPath: String, documentUri: String) {
        val uri = Uri.parse(documentUri)
        val fileName = queryDisplayName(uri)
        val input = context.contentResolver.openInputStream(uri)
            ?: error("Unable to open the selected file. Permission may have been revoked.")
        input.use { stream ->
            importWorkspaceFile(
                targetDirectoryPath = targetDirectoryPath,
                fileName = fileName,
                inputStream = stream,
            )
        }
    }

    private fun queryDisplayName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        return cursor.getString(index)
                    }
                }
            }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "imported-file"
    }
}
