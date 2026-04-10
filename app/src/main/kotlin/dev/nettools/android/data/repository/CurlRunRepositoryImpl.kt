package dev.nettools.android.data.repository

import dev.nettools.android.data.db.CurlRunDao
import dev.nettools.android.data.db.CurlRunOutputDao
import dev.nettools.android.data.db.CurlRunOutputEntity
import dev.nettools.android.data.db.toEntity
import dev.nettools.android.data.db.toRecord
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlOutputStream
import dev.nettools.android.domain.model.CurlRunOutput
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.model.CurlRunSummary
import dev.nettools.android.domain.repository.CurlRunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Room-backed implementation of [CurlRunRepository].
 */
class CurlRunRepositoryImpl @Inject constructor(
    private val runDao: CurlRunDao,
    private val outputDao: CurlRunOutputDao,
) : CurlRunRepository {

    override fun observeAll(): Flow<List<CurlRunRecord>> =
        combine(runDao.observeAll(), outputDao.observeAll()) { runs, outputs ->
            val outputMap = outputs.associateBy { it.runId }
            runs.map { entity -> entity.toRecord(outputMap[entity.id]) }
        }

    override fun observeById(runId: String): Flow<CurlRunRecord?> =
        combine(runDao.observeById(runId), outputDao.observeById(runId)) { run, output ->
            run?.toRecord(output)
        }

    override suspend fun getById(runId: String): CurlRunRecord? {
        val run = runDao.getById(runId) ?: return null
        return run.toRecord(outputDao.getById(runId))
    }

    override suspend fun upsert(record: CurlRunRecord) {
        runDao.upsert(record.summary.toEntity())
        outputDao.upsert(record.output.toEntity(record.summary.id))
    }

    override suspend fun upsertSummary(summary: CurlRunSummary) {
        runDao.upsert(summary.toEntity())
        if (outputDao.getById(summary.id) == null) {
            outputDao.upsert(CurlRunOutput().toEntity(summary.id))
        }
    }

    override suspend fun appendOutput(runId: String, stream: CurlOutputStream, text: String, byteCap: Int) {
        val current = outputDao.getById(runId) ?: CurlRunOutput().toEntity(runId)
        val updated = when (stream) {
            CurlOutputStream.STDOUT -> {
                val appendResult = appendWithCap(current.stdoutText, current.stdoutBytes, current.stdoutTruncated, text, byteCap)
                current.copy(
                    stdoutText = appendResult.text,
                    stdoutBytes = appendResult.bytes,
                    stdoutTruncated = appendResult.truncated,
                )
            }

            CurlOutputStream.STDERR -> {
                val appendResult = appendWithCap(current.stderrText, current.stderrBytes, current.stderrTruncated, text, byteCap)
                current.copy(
                    stderrText = appendResult.text,
                    stderrBytes = appendResult.bytes,
                    stderrTruncated = appendResult.truncated,
                )
            }
        }
        outputDao.upsert(updated)
    }

    override suspend fun updateStatus(
        runId: String,
        status: CurlRunStatus,
        finishedAt: Long?,
        exitCode: Int?,
        durationMillis: Long?,
        cleanupWarning: String?,
        effectiveCommandText: String?,
        cleanupStatus: CurlCleanupStatus?,
    ) {
        val existing = runDao.getById(runId) ?: return
        runDao.upsert(
            existing.copy(
                status = status.name,
                finishedAt = finishedAt ?: existing.finishedAt,
                exitCode = exitCode ?: existing.exitCode,
                durationMillis = durationMillis ?: existing.durationMillis,
                cleanupWarning = cleanupWarning ?: existing.cleanupWarning,
                effectiveCommandText = effectiveCommandText ?: existing.effectiveCommandText,
                cleanupStatus = cleanupStatus?.name ?: existing.cleanupStatus,
            )
        )
    }

    override suspend fun clearAll() {
        outputDao.clearAll()
        runDao.clearAll()
    }
}

private data class AppendResult(
    val text: String,
    val bytes: Int,
    val truncated: Boolean,
)

private fun appendWithCap(
    existingText: String,
    existingBytes: Int,
    alreadyTruncated: Boolean,
    chunk: String,
    byteCap: Int,
): AppendResult {
    if (alreadyTruncated || chunk.isEmpty()) {
        return AppendResult(existingText, existingBytes, alreadyTruncated)
    }

    val chunkBytes = chunk.toByteArray(Charsets.UTF_8)
    val remainingBytes = byteCap - existingBytes
    if (remainingBytes <= 0) {
        return AppendResult(existingText, existingBytes, truncated = true)
    }

    return if (chunkBytes.size <= remainingBytes) {
        AppendResult(
            text = existingText + chunk,
            bytes = existingBytes + chunkBytes.size,
            truncated = false,
        )
    } else {
        val keptText = String(chunkBytes.copyOfRange(0, remainingBytes), Charsets.UTF_8)
        AppendResult(
            text = existingText + keptText,
            bytes = existingBytes + remainingBytes,
            truncated = true,
        )
    }
}
