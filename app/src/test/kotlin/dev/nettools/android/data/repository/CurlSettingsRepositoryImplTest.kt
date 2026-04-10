package dev.nettools.android.data.repository

import dev.nettools.android.data.curl.CurlDefaults
import dev.nettools.android.data.db.AppSettingDao
import dev.nettools.android.data.db.AppSettingEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [CurlSettingsRepositoryImpl].
 */
class CurlSettingsRepositoryImplTest {

    private val dao: AppSettingDao = mockk(relaxed = true)
    private val repository = CurlSettingsRepositoryImpl(dao)

    @Test
    fun `observeSettings returns defaults when no rows exist`() = runTest {
        every { dao.observeAll() } returns flowOf(emptyList())

        val settings = repository.observeSettings().first()

        assertFalse(settings.loggingEnabled)
        assertFalse(settings.saveHistoryEnabled)
        assertEquals(CurlDefaults.stdoutByteCap, settings.stdoutBytesCap)
        assertEquals(CurlDefaults.stderrByteCap, settings.stderrBytesCap)
    }

    @Test
    fun `getSettings maps persisted values`() = runTest {
        coEvery { dao.getAll() } returns listOf(
            AppSettingEntity(CurlDefaults.settingLoggingEnabled, "true"),
            AppSettingEntity(CurlDefaults.settingSaveHistoryEnabled, "true"),
            AppSettingEntity(CurlDefaults.settingWorkspaceRootPath, "/custom/workspace"),
        )

        val settings = repository.getSettings()

        assertTrue(settings.loggingEnabled)
        assertTrue(settings.saveHistoryEnabled)
        assertEquals("/custom/workspace", settings.workspaceRootPath)
    }

    @Test
    fun `setLoggingEnabled writes setting row`() = runTest {
        repository.setLoggingEnabled(enabled = true)

        coVerify {
            dao.upsert(AppSettingEntity(CurlDefaults.settingLoggingEnabled, "true"))
        }
    }

    @Test
    fun `setWorkspaceRootPath deletes row when null`() = runTest {
        repository.setWorkspaceRootPath(path = null)

        coVerify {
            dao.deleteByKey(CurlDefaults.settingWorkspaceRootPath)
        }
    }
}
