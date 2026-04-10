package dev.nettools.android.data.curl

import dev.nettools.android.domain.model.CurlOutputChunk
import dev.nettools.android.domain.model.CurlOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-based curl executor.
 *
 * The actual executable path is supplied by [CurlBinaryProvider], which allows
 * the app to swap from a PATH-based development command to a bundled per-ABI
 * curl binary later without changing the service layer.
 */
@Singleton
class ProcessCurlExecutor @Inject constructor(
    private val binaryProvider: CurlBinaryProvider,
) : CurlExecutor {

    override suspend fun execute(
        request: CurlExecutionRequest,
        onOutput: suspend (CurlOutputChunk) -> Unit,
    ): CurlExecutionResult = coroutineScope {
        val runtime = binaryProvider.getRuntime()
        val command = buildProcessCommandLine(
            runtime = runtime,
            tokens = request.parsedCommand.tokens,
        )
        val process = ProcessBuilder(command)
            .directory(java.io.File(request.workspaceDirectory))
            .redirectErrorStream(false)
            .start()
        val startedAt = System.currentTimeMillis()

        val stdoutJob = launch(Dispatchers.IO) {
            process.inputStream.streamChunks(CurlOutputStream.STDOUT, onOutput)
        }
        val stderrJob = launch(Dispatchers.IO) {
            process.errorStream.streamChunks(CurlOutputStream.STDERR, onOutput)
        }

        try {
            val exitCode = withContext(Dispatchers.IO) { process.waitFor() }
            joinAll(stdoutJob, stderrJob)
            CurlExecutionResult(
                exitCode = exitCode,
                durationMillis = System.currentTimeMillis() - startedAt,
            )
        } catch (e: CancellationException) {
            process.destroy()
            withContext(NonCancellable + Dispatchers.IO) {
                if (!process.waitFor(1, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                    process.waitFor(1, TimeUnit.SECONDS)
                }
                process.inputStream.close()
                process.errorStream.close()
                process.outputStream.close()
            }
            withContext(NonCancellable) { joinAll(stdoutJob, stderrJob) }
            throw e
        }
    }
}

private suspend fun InputStream.streamChunks(
    stream: CurlOutputStream,
    emit: suspend (CurlOutputChunk) -> Unit,
) {
    InputStreamReader(this, StandardCharsets.UTF_8).use { reader ->
        val buffer = CharArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = reader.read(buffer)
            if (read <= 0) break
            emit(CurlOutputChunk(stream = stream, text = String(buffer, 0, read)))
        }
    }
}
