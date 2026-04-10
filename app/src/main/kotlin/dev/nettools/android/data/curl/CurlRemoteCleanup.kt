package dev.nettools.android.data.curl

import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.ParsedCurlCommand
import dev.nettools.android.util.CurlUserMessageFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plans best-effort remote cleanup commands for partial uploads.
 */
@Singleton
class CurlRemoteCleanupPlanner @Inject constructor() {

    /**
     * Builds a remote cleanup plan for [command], or null when no upload cleanup is needed.
     */
    fun plan(command: ParsedCurlCommand): CurlRemoteCleanupPlan? {
        if (!command.tokens.any { it == "-T" || it == "--upload-file" || it.startsWith("--upload-file=") }) {
            return null
        }

        val urls = extractUrls(command.tokens)
        if (urls.isEmpty()) {
            return CurlRemoteCleanupPlan(
                warnings = listOf("Remote cleanup could not be planned because no upload URL was found."),
            )
        }

        val baseTokens = sanitizeBaseTokens(command.tokens)
        val commands = mutableListOf<CurlRemoteCleanupCommand>()
        val warnings = mutableListOf<String>()

        urls.forEach { url ->
            when (url.scheme?.lowercase()) {
                "http", "https" -> commands += CurlRemoteCleanupCommand(
                    target = url.toString(),
                    tokens = baseTokens + listOf("--request", "DELETE", url.toString()),
                )

                "ftp", "ftps" -> ftpCleanupCommand(baseTokens, url)?.let(commands::add)
                    ?: warnings.add("Remote cleanup is unsupported for FTP URL: ${url}.")

                "sftp" -> sftpCleanupCommand(baseTokens, url)?.let(commands::add)
                    ?: warnings.add("Remote cleanup is unsupported for SFTP URL: ${url}.")

                else -> warnings.add(
                    "Remote cleanup is not supported for ${url.scheme ?: "this"} upload targets: ${url}.",
                )
            }
        }

        return CurlRemoteCleanupPlan(commands = commands, warnings = warnings)
    }

    private fun ftpCleanupCommand(
        baseTokens: List<String>,
        url: URI,
    ): CurlRemoteCleanupCommand? {
        val rawPath = url.rawPath ?: return null
        val fileName = rawPath.substringAfterLast('/', missingDelimiterValue = "")
        if (fileName.isBlank()) return null
        val parentPath = rawPath.removeSuffix(fileName)
        val targetUrl = url.withPath(parentPath.ifBlank { "/" }).toString()
        return CurlRemoteCleanupCommand(
            target = url.toString(),
            tokens = baseTokens + listOf("--quote", "-DELE $fileName", targetUrl),
        )
    }

    private fun sftpCleanupCommand(
        baseTokens: List<String>,
        url: URI,
    ): CurlRemoteCleanupCommand? {
        val rawPath = url.rawPath ?: return null
        if (rawPath.isBlank() || rawPath == "/") return null
        val targetUrl = url.withPath("/").toString()
        return CurlRemoteCleanupCommand(
            target = url.toString(),
            tokens = baseTokens + listOf("--quote", "rm $rawPath", targetUrl),
        )
    }

    private fun extractUrls(tokens: List<String>): List<URI> {
        val urls = mutableListOf<URI>()
        var index = 1
        while (index < tokens.size) {
            val token = tokens[index]
            val option = token.substringBefore("=")
            val inlineValue = token.substringAfter("=", missingDelimiterValue = "")
            when {
                option == "--url" -> {
                    val value = inlineValue.ifBlank { tokens.getOrNull(index + 1).orEmpty() }
                    parseUrl(value)?.let(urls::add)
                    if (inlineValue.isEmpty()) {
                        index += 1
                    }
                }

                !token.startsWith("-") -> parseUrl(token)?.let(urls::add)
            }
            index += 1
        }
        return urls.distinct()
    }

    private fun parseUrl(token: String): URI? {
        return runCatching {
            URI(token).takeIf { it.scheme != null && it.host != null }
        }.getOrNull()
    }

    private fun sanitizeBaseTokens(tokens: List<String>): List<String> {
        val sanitized = mutableListOf("curl")
        var index = 1
        while (index < tokens.size) {
            val token = tokens[index]
            val option = token.substringBefore("=")
            when {
                option in droppedOptionsWithValues -> {
                    if (!token.contains("=")) {
                        index += 1
                    }
                }

                option in droppedStandaloneOptions -> Unit
                !token.startsWith("-") -> Unit
                else -> {
                    sanitized += token
                    if (!token.contains("=") && option in preservedOptionsWithValues) {
                        tokens.getOrNull(index + 1)?.let { value ->
                            sanitized += value
                            index += 1
                        }
                    }
                }
            }
            index += 1
        }
        return sanitized
    }

    private companion object {
        val droppedOptionsWithValues = setOf(
            "-T",
            "--upload-file",
            "-o",
            "--output",
            "-D",
            "--dump-header",
            "-c",
            "--cookie-jar",
            "--stderr",
            "--etag-save",
            "--url",
            "-X",
            "--request",
            "-d",
            "--data",
            "--data-ascii",
            "--data-binary",
            "--data-raw",
            "--data-urlencode",
            "-F",
            "--form",
            "--form-string",
            "--quote",
            "-Q",
        )

        val droppedStandaloneOptions = setOf(
            "-O",
            "--remote-name",
            "-J",
            "--remote-header-name",
            "-I",
            "--head",
        )

        val preservedOptionsWithValues = setOf(
            "-u",
            "--user",
            "-U",
            "--proxy-user",
            "-x",
            "--proxy",
            "-H",
            "--header",
            "-b",
            "--cookie",
            "-e",
            "--referer",
            "-A",
            "--user-agent",
            "-E",
            "--cert",
            "--key",
            "--pass",
            "--cacert",
            "--capath",
            "--proxy-cacert",
            "--proxy-capath",
            "--oauth2-bearer",
            "--connect-timeout",
            "-m",
            "--max-time",
            "--retry",
            "--retry-delay",
            "--retry-max-time",
            "--resolve",
            "--interface",
            "--socks5",
            "--socks5-hostname",
            "--noproxy",
        )
    }
}

/**
 * Executes remote cleanup commands using the bundled curl runtime.
 */
@Singleton
class CurlRemoteCleanupExecutor @Inject constructor(
    private val binaryProvider: CurlBinaryProvider,
) {

    /**
     * Executes [plan] and returns a combined cleanup outcome.
     */
    suspend fun execute(plan: CurlRemoteCleanupPlan, workspaceDirectory: String): CurlCleanupResult {
        if (plan.commands.isEmpty()) {
            return if (plan.warnings.isEmpty()) {
                CurlCleanupResult(status = CurlCleanupStatus.SKIPPED)
            } else {
                CurlCleanupResult(
                    status = CurlCleanupStatus.FAILED,
                    warning = plan.warnings.joinToString(separator = "\n") { warning ->
                        CurlUserMessageFormatter.remoteCleanupFailure(warning)
                    },
                )
            }
        }

        val runtime = binaryProvider.getRuntime()
        val warnings = plan.warnings.toMutableList()
        plan.commands.forEach { command ->
            val stderr = executeCommand(
                command = buildProcessCommandLine(runtime = runtime, tokens = command.tokens),
                workspaceDirectory = workspaceDirectory,
            )
            if (stderr != null) {
                warnings += "Remote cleanup failed for ${command.target}: $stderr"
            }
        }

        return if (warnings.isEmpty()) {
            CurlCleanupResult(status = CurlCleanupStatus.SUCCEEDED)
        } else {
            CurlCleanupResult(
                status = CurlCleanupStatus.FAILED,
                warning = warnings.joinToString(separator = "\n") { warning ->
                    CurlUserMessageFormatter.remoteCleanupFailure(warning)
                },
            )
        }
    }

    private suspend fun executeCommand(
        command: List<String>,
        workspaceDirectory: String,
    ): String? = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(command)
            .directory(File(workspaceDirectory))
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText().trim() }
        val exitCode = process.waitFor()
        if (exitCode == 0) {
            null
        } else {
            output.ifBlank { "curl exited with code $exitCode." }
        }
    }
}

/**
 * Planned remote cleanup work for a failed or cancelled upload.
 */
data class CurlRemoteCleanupPlan(
    val commands: List<CurlRemoteCleanupCommand> = emptyList(),
    val warnings: List<String> = emptyList(),
)

/**
 * Single remote cleanup command ready to execute.
 */
data class CurlRemoteCleanupCommand(
    val target: String,
    val tokens: List<String>,
)

private fun URI.withPath(path: String): URI = URI(
    scheme,
    rawUserInfo,
    host,
    port,
    path,
    null,
    null,
)
