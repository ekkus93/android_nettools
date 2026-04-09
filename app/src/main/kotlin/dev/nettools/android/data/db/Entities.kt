package dev.nettools.android.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.ConnectionProfile
import dev.nettools.android.domain.model.HistoryStatus
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferHistoryEntry

// ── ConnectionProfile ─────────────────────────────────────────────────────────

/**
 * Room entity for [ConnectionProfile] domain objects.
 */
@Entity(tableName = "connection_profiles")
data class ConnectionProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val authType: String,
    val keyPath: String?,
    val savePassword: Boolean
) {
    /** Converts this entity to its domain model counterpart. */
    fun toDomain(): ConnectionProfile = ConnectionProfile(
        id = id,
        name = name,
        host = host,
        port = port,
        username = username,
        authType = AuthType.valueOf(authType),
        keyPath = keyPath,
        savePassword = savePassword
    )
}

/** Converts a [ConnectionProfile] domain model to its Room entity. */
fun ConnectionProfile.toEntity(): ConnectionProfileEntity = ConnectionProfileEntity(
    id = id,
    name = name,
    host = host,
    port = port,
    username = username,
    authType = authType.name,
    keyPath = keyPath,
    savePassword = savePassword
)

// ── TransferHistory ───────────────────────────────────────────────────────────

/**
 * Room entity for [TransferHistoryEntry] domain objects.
 */
@Entity(tableName = "transfer_history")
data class TransferHistoryEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val direction: String,
    val host: String,
    val username: String,
    val fileName: String,
    val remoteDir: String,
    val fileSizeBytes: Long,
    val status: String,
    val errorMessage: String? = null,
) {
    /** Converts this entity to its domain model counterpart. */
    fun toDomain(): TransferHistoryEntry = TransferHistoryEntry(
        id = id,
        timestamp = timestamp,
        direction = TransferDirection.valueOf(direction),
        host = host,
        username = username,
        fileName = fileName,
        remoteDir = remoteDir,
        fileSizeBytes = fileSizeBytes,
        status = HistoryStatus.valueOf(status),
        errorMessage = errorMessage,
    )
}

/** Converts a [TransferHistoryEntry] domain model to its Room entity. */
fun TransferHistoryEntry.toEntity(): TransferHistoryEntity = TransferHistoryEntity(
    id = id,
    timestamp = timestamp,
    direction = direction.name,
    host = host,
    username = username,
    fileName = fileName,
    remoteDir = remoteDir,
    fileSizeBytes = fileSizeBytes,
    status = status.name,
    errorMessage = errorMessage,
)

// ── KnownHost ─────────────────────────────────────────────────────────────────

/**
 * Room entity storing trusted SSH host key fingerprints (TOFU).
 */
@Entity(tableName = "known_hosts")
data class KnownHostEntity(
    @PrimaryKey val id: String, // "$host:$port"
    val host: String,
    val port: Int,
    val fingerprint: String
)

// ── QueuedJob ─────────────────────────────────────────────────────────────────

/**
 * Room entity that persists a queued transfer job so it survives process death.
 * Sensitive credentials (password) are stored separately in [CredentialStore]
 * and retrieved at restore time using [jobId] as the key.
 */
@Entity(tableName = "queued_jobs")
data class QueuedJobEntity(
    @PrimaryKey val jobId: String,
    val host: String,
    val port: Int,
    val username: String,
    val authType: String,
    val keyPath: String?,
    val profileId: String?,
    val direction: String,
    val localPath: String,
    val remotePath: String,
    val enqueuedAt: Long,
)
