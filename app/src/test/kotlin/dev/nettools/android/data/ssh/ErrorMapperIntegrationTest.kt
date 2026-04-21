package dev.nettools.android.data.ssh

import android.content.Context
import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.TransferError
import io.mockk.mockk
import net.schmizz.sshj.sftp.Response
import net.schmizz.sshj.sftp.SFTPException
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.scp.server.ScpCommandFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.sftp.server.SftpSubsystemFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.net.ServerSocket
import java.nio.file.Path

/**
 * Integration tests for [ErrorMapper] and [SshConnectionManager.connect] error mapping,
 * verifying that real SSHJ exceptions thrown against a live MINA server are correctly
 * categorised into [TransferError] sealed subtypes.
 */
class ErrorMapperIntegrationTest {

    @TempDir
    lateinit var serverRoot: Path

    private lateinit var server: SshServer
    private lateinit var knownHostsManager: KnownHostsManager

    private val context: Context = mockk(relaxed = true)
    private val manager = SshConnectionManager(context)

    @BeforeEach
    fun startServer() {
        server = SshServer.setUpDefaultServer().apply {
            port = 0
            keyPairProvider = SimpleGeneratorHostKeyProvider()
            passwordAuthenticator = PasswordAuthenticator { user, pass, _ ->
                user == "testuser" && pass == "correct"
            }
            commandFactory = ScpCommandFactory.Builder().build()
            subsystemFactories = listOf(SftpSubsystemFactory.Builder().build())
            fileSystemFactory = VirtualFileSystemFactory(serverRoot)
            start()
        }
        setupKnownHosts()
    }

    @AfterEach
    fun stopServer() {
        server.stop(true)
    }

    private fun setupKnownHosts() {
        val repo = FakeKnownHostRepository()
        knownHostsManager = KnownHostsManager(repo)
        val fp = manager.peekHostKey("localhost", server.port)!!
        knownHostsManager.acceptHost("localhost", server.port, fp)
    }

    // ── 4.1 Authentication failures ──────────────────────────────────────────

    @Test
    fun `connect - wrong password throws AuthFailure`() {
        assertThrows<TransferError.AuthFailure> {
            manager.connect(
                host = "localhost",
                port = server.port,
                username = "testuser",
                authType = AuthType.PASSWORD,
                password = "wrong",
                knownHostsManager = knownHostsManager,
            )
        }
    }

    @Test
    fun `connect - private key auth without key file throws TransferError`() {
        assertThrows<TransferError> {
            manager.connect(
                host = "localhost",
                port = server.port,
                username = "testuser",
                authType = AuthType.PRIVATE_KEY,
                keyPath = "/nonexistent/key.pem",
                knownHostsManager = knownHostsManager,
            )
        }
    }

    // ── 4.2 Network failures ──────────────────────────────────────────────────

    @Test
    fun `connect - refused port throws HostUnreachable`() {
        val closedPort = ServerSocket(0).use { it.localPort }
        val emptyRepo = FakeKnownHostRepository()
        val km = KnownHostsManager(emptyRepo)
        assertThrows<TransferError.HostUnreachable> {
            manager.connect(
                host = "localhost",
                port = closedPort,
                username = "testuser",
                authType = AuthType.PASSWORD,
                password = "correct",
                knownHostsManager = km,
            )
        }
    }

    @Test
    fun `connect - server stopped before connect throws HostUnreachable`() {
        val port = server.port
        server.stop(true)
        val emptyRepo = FakeKnownHostRepository()
        val km = KnownHostsManager(emptyRepo)
        assertThrows<TransferError.HostUnreachable> {
            manager.connect(
                host = "localhost",
                port = port,
                username = "testuser",
                authType = AuthType.PASSWORD,
                password = "correct",
                knownHostsManager = km,
            )
        }
    }

    // ── 4.3 Permission denied ─────────────────────────────────────────────────

    @Test
    fun `ErrorMapper - SFTP PermissionDenied exception maps to TransferError PermissionDenied`() {
        val ex = SFTPException(Response.StatusCode.PERMISSION_DENIED, "Permission denied")
        val result = ErrorMapper.mapException(ex)
        assertTrue(result is TransferError.PermissionDenied, "Expected PermissionDenied, got: ${result::class.simpleName}")
    }
}
