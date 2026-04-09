package dev.nettools.android.data.security

import dev.nettools.android.domain.repository.KnownHostRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [KnownHostsManager], covering TOFU flow, changed-key detection,
 * and SHA-256 fingerprint computation.
 */
class KnownHostsManagerTest {

    private val repository: KnownHostRepository = mockk(relaxed = true)
    private lateinit var manager: KnownHostsManager

    private val testHost = "192.168.1.1"
    private val testPort = 22
    private val keyBytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

    @BeforeEach
    fun setUp() {
        manager = KnownHostsManager(repository)
    }

    @Test
    fun `first connect returns FirstConnect with fingerprint`() {
        coEvery { repository.getByHost(testHost, testPort) } returns null

        val result = manager.checkAndVerify(testHost, testPort, "RSA", keyBytes)

        assertInstanceOf(KnownHostsManager.VerificationResult.FirstConnect::class.java, result)
        val fc = result as KnownHostsManager.VerificationResult.FirstConnect
        assertTrue(fc.fingerprint.startsWith("SHA256:"))
    }

    @Test
    fun `trusted key returns Trusted`() {
        val fingerprint = manager.computeFingerprint(keyBytes)
        coEvery { repository.getByHost(testHost, testPort) } returns fingerprint

        val result = manager.checkAndVerify(testHost, testPort, "RSA", keyBytes)

        assertInstanceOf(KnownHostsManager.VerificationResult.Trusted::class.java, result)
    }

    @Test
    fun `changed key returns KeyChanged with old and new fingerprints`() {
        val oldFingerprint = "SHA256:AAABBBCCC"
        coEvery { repository.getByHost(testHost, testPort) } returns oldFingerprint

        val result = manager.checkAndVerify(testHost, testPort, "RSA", keyBytes)

        assertInstanceOf(KnownHostsManager.VerificationResult.KeyChanged::class.java, result)
        val kc = result as KnownHostsManager.VerificationResult.KeyChanged
        assertEquals(oldFingerprint, kc.old)
        assertTrue(kc.new.startsWith("SHA256:"))
    }

    @Test
    fun `acceptHost calls repository save with correct params`() {
        val fingerprint = "SHA256:TESTFP"
        manager.acceptHost(testHost, testPort, fingerprint)

        coVerify { repository.save(testHost, testPort, fingerprint) }
    }

    @Test
    fun `getStoredFingerprint returns null when not stored`() {
        coEvery { repository.getByHost(testHost, testPort) } returns null

        assertNull(manager.getStoredFingerprint(testHost, testPort))
    }

    @Test
    fun `getStoredFingerprint returns stored fingerprint`() {
        val expected = "SHA256:TESTFP"
        coEvery { repository.getByHost(testHost, testPort) } returns expected

        assertEquals(expected, manager.getStoredFingerprint(testHost, testPort))
    }

    @Test
    fun `computeFingerprint returns SHA256 prefixed string`() {
        val fingerprint = manager.computeFingerprint(keyBytes)
        assertTrue(fingerprint.startsWith("SHA256:"))
    }

    @Test
    fun `computeFingerprint is deterministic for same input`() {
        val fp1 = manager.computeFingerprint(keyBytes)
        val fp2 = manager.computeFingerprint(keyBytes)
        assertEquals(fp1, fp2)
    }

    @Test
    fun `computeFingerprint differs for different inputs`() {
        val fp1 = manager.computeFingerprint(byteArrayOf(1, 2, 3))
        val fp2 = manager.computeFingerprint(byteArrayOf(4, 5, 6))
        assertTrue(fp1 != fp2)
    }

    @Test
    fun `rejectHost does not persist anything`() {
        manager.rejectHost()
        coVerify(exactly = 0) { repository.save(any(), any(), any()) }
    }

    @Test
    fun `fingerprint is not null for real key bytes`() {
        val realKeyBytes = ByteArray(512) { it.toByte() }
        val fp = manager.computeFingerprint(realKeyBytes)
        assertNotNull(fp)
        assertTrue(fp.length > 10)
    }
}
