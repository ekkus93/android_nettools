package dev.nettools.android.data.curl

import dev.nettools.android.domain.model.CurlPathReferenceRole
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlValidationError
import dev.nettools.android.domain.model.ParsedCurlCommand
import dev.nettools.android.domain.model.ParsedCurlPathReference
import dev.nettools.android.domain.repository.WorkspaceRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves workspace-managed curl file paths into executable local filesystem paths.
 */
@Singleton
class CurlCommandWorkspaceAdapter @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
) {

    /**
     * Produces a command copy with local path arguments rewritten to concrete local paths.
     */
    suspend fun prepareForExecution(command: ParsedCurlCommand): PreparedCurlCommand {
        val replacements = command.pathReferences.associateWith { reference ->
            workspaceRepository.resolveLocalPath(reference.normalizedPath)
        }
        val rewrittenTokens = command.tokens.map { token ->
            rewriteToken(token = token, replacements = replacements)
        }
        return PreparedCurlCommand(
            command = command.copy(tokens = rewrittenTokens),
            cleanupTargets = replacements
                .filterKeys { reference -> reference.role in cleanupTargetRoles }
                .values
                .distinct(),
            localPathMap = replacements.mapKeys { entry -> entry.key.originalPath },
        )
    }

    /**
     * Validates local workspace-backed paths before execution starts.
     */
    suspend fun validate(command: ParsedCurlCommand): List<CurlValidationError> {
        val errors = mutableListOf<CurlValidationError>()
        command.pathReferences.forEach { reference ->
            val localFile = File(workspaceRepository.resolveLocalPath(reference.normalizedPath))
            when (reference.role) {
                CurlPathReferenceRole.INPUT_FILE,
                CurlPathReferenceRole.CONFIG_FILE,
                CurlPathReferenceRole.PAYLOAD_FILE,
                    -> if (!localFile.exists() || !localFile.isFile) {
                    errors += CurlValidationError(
                        message = "Workspace file not found: ${reference.originalPath}",
                        token = reference.originalPath,
                    )
                }

                CurlPathReferenceRole.OUTPUT_FILE,
                CurlPathReferenceRole.STDERR_FILE,
                CurlPathReferenceRole.COOKIE_JAR,
                    -> {
                    val parent = localFile.parentFile
                    if (parent != null && !parent.exists()) {
                        errors += CurlValidationError(
                            message = "Workspace directory does not exist for ${reference.originalPath}",
                            token = reference.originalPath,
                        )
                    }
                }

                CurlPathReferenceRole.HEADER_FILE -> Unit
            }
        }
        return errors
    }

    /**
     * Attempts to delete partial local output files after failure or cancellation.
     *
     * @return cleanup details describing whether local outputs were removed.
     */
    fun cleanupPartialOutputs(preparedCommand: PreparedCurlCommand): CurlCleanupResult {
        if (preparedCommand.cleanupTargets.isEmpty()) {
            return CurlCleanupResult(status = CurlCleanupStatus.SKIPPED)
        }
        val failures = preparedCommand.cleanupTargets.filter { path ->
            val file = File(path)
            file.exists() && !file.delete()
        }
        return if (failures.isEmpty()) {
            CurlCleanupResult(status = CurlCleanupStatus.SUCCEEDED)
        } else {
            CurlCleanupResult(
                status = CurlCleanupStatus.FAILED,
                warning = "Failed to clean up local partial output: ${failures.joinToString()}",
            )
        }
    }

    private fun rewriteToken(
        token: String,
        replacements: Map<ParsedCurlPathReference, String>,
    ): String {
        var rewritten = token
        replacements.forEach { (reference, localPath) ->
            rewritten = when {
                rewritten == reference.originalPath -> localPath
                rewritten.endsWith("=${reference.originalPath}") ->
                    rewritten.substringBeforeLast("=") + "=" + localPath
                rewritten == "@${reference.originalPath}" -> "@$localPath"
                rewritten.startsWith("@${reference.originalPath};") ->
                    "@$localPath" + rewritten.substringAfter("@${reference.originalPath}")
                else -> rewritten
            }
        }
        return rewritten
    }

    private companion object {
        val cleanupTargetRoles = setOf(
            CurlPathReferenceRole.OUTPUT_FILE,
            CurlPathReferenceRole.STDERR_FILE,
            CurlPathReferenceRole.COOKIE_JAR,
        )
    }
}

/**
 * Prepared command plus local bookkeeping used by execution and cleanup paths.
 */
data class PreparedCurlCommand(
    val command: ParsedCurlCommand,
    val cleanupTargets: List<String>,
    val localPathMap: Map<String, String>,
) {
    /** Human-readable command text representing the rewritten executable invocation. */
    val effectiveCommandText: String
        get() = command.tokens.joinToString(separator = " ") { token -> token.toDisplayToken() }
}

private fun String.toDisplayToken(): String {
    if (isEmpty()) return "''"
    val safe = all { char ->
        char.isLetterOrDigit() || char in setOf('-', '_', '.', '/', ':', '@', '=', '+', ',')
    }
    return if (safe) {
        this
    } else {
        "'" + replace("'", "'\\''") + "'"
    }
}

/**
 * Cleanup outcome returned after attempting to delete local partial outputs.
 */
data class CurlCleanupResult(
    val status: CurlCleanupStatus,
    val warning: String? = null,
)
