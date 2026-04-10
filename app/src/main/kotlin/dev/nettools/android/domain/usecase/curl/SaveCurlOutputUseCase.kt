package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.domain.model.CurlRunOutput
import dev.nettools.android.domain.repository.WorkspaceRepository
import javax.inject.Inject

/**
 * Saves retained curl output into the workspace as a text file.
 */
class SaveCurlOutputUseCase @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
) {

    /**
     * Writes [output] to a workspace text file and returns the saved workspace path.
     */
    suspend operator fun invoke(runId: String, output: CurlRunOutput): String {
        val workspacePath = "/curl-output-$runId.txt"
        val content = buildString {
            appendLine("=== stdout ===")
            appendLine(output.stdoutText)
            appendLine()
            appendLine("=== stderr ===")
            appendLine(output.stderrText)
        }
        workspaceRepository.writeTextFile(path = workspacePath, text = content)
        return workspacePath
    }
}
