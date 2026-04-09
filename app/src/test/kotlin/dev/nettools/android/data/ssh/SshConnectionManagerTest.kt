package dev.nettools.android.data.ssh

import io.mockk.every
import io.mockk.mockk
import net.schmizz.sshj.common.Factory
import net.schmizz.sshj.transport.kex.KeyExchange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [SshConnectionManager] SSHJ configuration behavior.
 */
class SshConnectionManagerTest {

    @Test
    fun `filterUnsupportedKeyExchangeFactories removes curve25519 factories`() {
        val curve25519: Factory.Named<KeyExchange> = mockk()
        every { curve25519.getName() } returns "curve25519-sha256"

        val curve25519LibSsh: Factory.Named<KeyExchange> = mockk()
        every { curve25519LibSsh.getName() } returns "curve25519-sha256@libssh.org"

        val group14: Factory.Named<KeyExchange> = mockk()
        every { group14.getName() } returns "diffie-hellman-group14-sha256"

        val manager = SshConnectionManager()

        val filtered = manager.filterUnsupportedKeyExchangeFactories(
            listOf(curve25519, curve25519LibSsh, group14),
        )

        assertEquals(listOf(group14), filtered)
    }
}
