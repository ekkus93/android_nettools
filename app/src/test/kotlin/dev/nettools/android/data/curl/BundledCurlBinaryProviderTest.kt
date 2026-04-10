package dev.nettools.android.data.curl

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.file.Path

/**
 * Unit tests for [BundledCurlBinaryProvider].
 */
class BundledCurlBinaryProviderTest {

    @TempDir
    lateinit var tempDir: Path

    private val context: Context = mockk()
    private val assetManager: AssetManager = mockk()

    @AfterEach
    fun tearDown() {
        unmockkStatic(::currentSupportedAbis)
    }

    @Test
    fun `getRuntime returns executable path and extracted cacert when runtime is available`() = runTest {
        mockSupportedAbis("x86_64")
        val nativeExecutable = createExecutable()
        val provider = createProvider()

        val runtime = provider.getRuntime()
        val caCertificatePath = requireNotNull(runtime.caCertificatePath)

        assertEquals(nativeExecutable.absolutePath, runtime.executablePath)
        assertTrue(File(caCertificatePath).exists())
        assertArrayEquals("cacert".toByteArray(), File(caCertificatePath).readBytes())
    }

    @Test
    fun `getRuntime fails when no supported abi is reported`() = runTest {
        mockSupportedAbis()
        val provider = createProvider()

        val error = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { provider.getRuntime() }
        }

        assertEquals("No supported device ABI was reported by Android.", error.message)
    }

    @Test
    fun `getRuntime fails when bundled executable is missing`() = runTest {
        mockSupportedAbis("x86_64")
        val provider = createProvider()

        val error = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { provider.getRuntime() }
        }

        assertEquals("No bundled curl runtime is available for this device ABI.", error.message)
    }

    @Test
    fun `getRuntime fails when bundled executable is not executable`() = runTest {
        mockSupportedAbis("x86_64")
        createExecutable(executable = false)
        val provider = createProvider()

        val error = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { provider.getRuntime() }
        }

        assertEquals("No bundled curl runtime is available for this device ABI.", error.message)
    }

    @Test
    fun `getRuntime removes temp cacert file when asset extraction fails`() = runTest {
        mockSupportedAbis("x86_64")
        createExecutable()
        val provider = createProvider(assetFailure = IOException("missing cacert"))

        val error = assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { provider.getRuntime() }
        }

        assertEquals("missing cacert", error.message)
        assertFalse(tempDir.resolve("files/curl-runtime/x86_64/cacert.pem").toFile().exists())
        assertFalse(tempDir.resolve("files/curl-runtime/x86_64/cacert.pem.tmp").toFile().exists())
    }

    private fun createProvider(assetFailure: Throwable? = null): BundledCurlBinaryProvider {
        val filesDir = tempDir.resolve("files").toFile().apply { mkdirs() }
        val nativeLibraryDirectory = tempDir.resolve("native").toFile().apply { mkdirs() }
        val applicationInfo = ApplicationInfo().apply {
            nativeLibraryDir = nativeLibraryDirectory.absolutePath
        }

        every { context.applicationInfo } returns applicationInfo
        every { context.filesDir } returns filesDir
        every { context.assets } returns assetManager
        if (assetFailure == null) {
            every { assetManager.open("curl/cacert.pem") } returns ByteArrayInputStream("cacert".toByteArray())
        } else {
            every { assetManager.open("curl/cacert.pem") } throws assetFailure
        }

        return BundledCurlBinaryProvider(context)
    }

    private fun createExecutable(executable: Boolean = true): File {
        return tempDir.resolve("native/libcurl_exec.so").toFile().apply {
            parentFile?.mkdirs()
            writeText("curl")
            setExecutable(executable)
        }
    }

    private fun mockSupportedAbis(vararg abis: String) {
        mockkStatic(::currentSupportedAbis)
        every { currentSupportedAbis() } returns abis.toList().toTypedArray()
    }
}
