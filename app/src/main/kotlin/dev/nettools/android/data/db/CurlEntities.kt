package dev.nettools.android.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.nettools.android.domain.model.CurlRunOutput
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.model.CurlRunSummary
import dev.nettools.android.domain.model.CurlSettings

/**
 * Room entity for persisted curl run summary metadata.
 */
@Entity(tableName = "curl_runs")
data class CurlRunEntity(
    @PrimaryKey val id: String,
    val commandText: String,
    val normalizedCommandText: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val status: String,
    val exitCode: Int?,
    val durationMillis: Long?,
    val loggingEnabled: Boolean,
    val cleanupWarning: String?,
) {
    /** Converts this entity to its domain-model counterpart. */
    fun toDomain(): CurlRunSummary = CurlRunSummary(
        id = id,
        commandText = commandText,
        normalizedCommandText = normalizedCommandText,
        startedAt = startedAt,
        finishedAt = finishedAt,
        status = CurlRunStatus.valueOf(status),
        exitCode = exitCode,
        durationMillis = durationMillis,
        loggingEnabled = loggingEnabled,
        cleanupWarning = cleanupWarning,
    )
}

/** Converts a [CurlRunSummary] domain model to a Room entity. */
fun CurlRunSummary.toEntity(): CurlRunEntity = CurlRunEntity(
    id = id,
    commandText = commandText,
    normalizedCommandText = normalizedCommandText,
    startedAt = startedAt,
    finishedAt = finishedAt,
    status = status.name,
    exitCode = exitCode,
    durationMillis = durationMillis,
    loggingEnabled = loggingEnabled,
    cleanupWarning = cleanupWarning,
)

/**
 * Room entity for retained stdout/stderr content for a curl run.
 */
@Entity(tableName = "curl_run_outputs")
data class CurlRunOutputEntity(
    @PrimaryKey val runId: String,
    val stdoutText: String,
    val stderrText: String,
    val stdoutBytes: Int,
    val stderrBytes: Int,
    val stdoutTruncated: Boolean,
    val stderrTruncated: Boolean,
) {
    /** Converts this entity to its domain-model counterpart. */
    fun toDomain(): CurlRunOutput = CurlRunOutput(
        stdoutText = stdoutText,
        stderrText = stderrText,
        stdoutBytes = stdoutBytes,
        stderrBytes = stderrBytes,
        stdoutTruncated = stdoutTruncated,
        stderrTruncated = stderrTruncated,
    )
}

/** Converts a [CurlRunOutput] domain model to a Room entity. */
fun CurlRunOutput.toEntity(runId: String): CurlRunOutputEntity = CurlRunOutputEntity(
    runId = runId,
    stdoutText = stdoutText,
    stderrText = stderrText,
    stdoutBytes = stdoutBytes,
    stderrBytes = stderrBytes,
    stdoutTruncated = stdoutTruncated,
    stderrTruncated = stderrTruncated,
)

/**
 * Room key-value entity for app-level settings.
 */
@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
)

/**
 * Converts Room entities into a complete [CurlRunRecord].
 */
fun CurlRunEntity.toRecord(output: CurlRunOutputEntity?): CurlRunRecord =
    CurlRunRecord(
        summary = toDomain(),
        output = output?.toDomain() ?: CurlRunOutput(),
    )

/**
 * Default curl settings used when no explicit rows have been persisted.
 */
fun defaultCurlSettings(): CurlSettings = CurlSettings()
