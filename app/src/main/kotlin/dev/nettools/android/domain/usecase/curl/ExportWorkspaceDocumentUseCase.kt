package dev.nettools.android.domain.usecase.curl

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Exports one workspace file into a picker-selected destination document.
 */
class ExportWorkspaceDocumentUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val exportWorkspaceFile: ExportWorkspaceFileUseCase,
) {

    /** Exports the workspace file at [path] into [destinationUri]. */
    suspend operator fun invoke(path: String, destinationUri: String) {
        val output = context.contentResolver.openOutputStream(Uri.parse(destinationUri))
            ?: error("Unable to write the selected destination. Permission may have been revoked.")
        output.use { stream ->
            exportWorkspaceFile(path = path, outputStream = stream)
        }
    }
}
