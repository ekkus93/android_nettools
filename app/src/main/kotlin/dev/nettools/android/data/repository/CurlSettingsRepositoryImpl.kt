package dev.nettools.android.data.repository

import dev.nettools.android.data.curl.CurlDefaults
import dev.nettools.android.data.db.AppSettingDao
import dev.nettools.android.data.db.AppSettingEntity
import dev.nettools.android.domain.model.CurlSettings
import dev.nettools.android.domain.repository.CurlSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed implementation of [CurlSettingsRepository].
 */
class CurlSettingsRepositoryImpl @Inject constructor(
    private val dao: AppSettingDao,
) : CurlSettingsRepository {

    override fun observeSettings(): Flow<CurlSettings> =
        dao.observeAll().map { entities -> entities.toSettings() }

    override suspend fun getSettings(): CurlSettings = dao.getAll().toSettings()

    override suspend fun setLoggingEnabled(enabled: Boolean) {
        dao.upsert(AppSettingEntity(CurlDefaults.settingLoggingEnabled, enabled.toString()))
    }

    override suspend fun setSaveHistoryEnabled(enabled: Boolean) {
        dao.upsert(AppSettingEntity(CurlDefaults.settingSaveHistoryEnabled, enabled.toString()))
    }

    override suspend fun setWorkspaceRootPath(path: String?) {
        if (path.isNullOrBlank()) {
            dao.deleteByKey(CurlDefaults.settingWorkspaceRootPath)
        } else {
            dao.upsert(AppSettingEntity(CurlDefaults.settingWorkspaceRootPath, path))
        }
    }
}

private fun List<AppSettingEntity>.toSettings(): CurlSettings {
    val map = associate { it.key to it.value }
    return CurlSettings(
        loggingEnabled = map[CurlDefaults.settingLoggingEnabled]?.toBooleanStrictOrNull() ?: false,
        saveHistoryEnabled = map[CurlDefaults.settingSaveHistoryEnabled]?.toBooleanStrictOrNull() ?: false,
        workspaceRootPath = map[CurlDefaults.settingWorkspaceRootPath],
        stdoutBytesCap = CurlDefaults.stdoutByteCap,
        stderrBytesCap = CurlDefaults.stderrByteCap,
    )
}
