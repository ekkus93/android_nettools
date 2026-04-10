package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.domain.repository.WorkspaceRepository
import java.io.InputStream
import javax.inject.Inject

/**
 * Imports an external file stream into the curl workspace.
 */
class ImportWorkspaceFileUseCase @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
) {

    /**
     * Copies [inputStream] into [targetDirectoryPath] using [fileName].
     */
    suspend operator fun invoke(
        targetDirectoryPath: String,
        fileName: String,
        inputStream: InputStream,
    ) = workspaceRepository.importFile(
        targetDirectoryPath = targetDirectoryPath,
        fileName = fileName,
        inputStream = inputStream,
    )
}
