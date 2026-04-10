package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.domain.repository.WorkspaceRepository
import java.io.OutputStream
import javax.inject.Inject

/**
 * Exports a workspace file into an external output stream.
 */
class ExportWorkspaceFileUseCase @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
) {

    /**
     * Copies the workspace file at [path] into [outputStream].
     */
    suspend operator fun invoke(path: String, outputStream: OutputStream) {
        workspaceRepository.exportFile(path = path, outputStream = outputStream)
    }
}
