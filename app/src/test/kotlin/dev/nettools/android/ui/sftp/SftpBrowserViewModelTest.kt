package dev.nettools.android.ui.sftp

import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.ssh.SftpClient
import dev.nettools.android.data.ssh.SshConnectionManager
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.RemoteFileEntry
import dev.nettools.android.service.TransferProgressHolder
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.schmizz.sshj.SSHClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [SftpBrowserViewModel], covering sort order, navigate success/failure,
 * rename/delete/new-directory dialog state machines, and entry selection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SftpBrowserViewModelTest {

    private val sshConnectionManager: SshConnectionManager = mockk()
    private val sftpClient: SftpClient = mockk()
    private val knownHostsManager: KnownHostsManager = mockk()
    private val progressHolder: TransferProgressHolder = mockk()
    private val mockSshClient: SSHClient = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SftpBrowserViewModel

    private fun makeEntry(
        name: String,
        path: String = "/some/$name",
        isDirectory: Boolean = false,
        sizeBytes: Long = 100L,
        modifiedAt: Long = 1000L,
    ) = RemoteFileEntry(
        name = name,
        path = path,
        sizeBytes = sizeBytes,
        permissions = "rwxr-xr-x",
        isDirectory = isDirectory,
        modifiedAt = modifiedAt,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Prevent auto-connect in init{}
        every { progressHolder.pendingSftpConnectionParams } returns null
        justRun { progressHolder.pendingSftpConnectionParams = any() }
        viewModel = SftpBrowserViewModel(
            sshConnectionManager = sshConnectionManager,
            sftpClient = sftpClient,
            knownHostsManager = knownHostsManager,
            progressHolder = progressHolder,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Injects a mock SSHClient into the ViewModel via reflection. */
    private fun injectSshClient() {
        val field = SftpBrowserViewModel::class.java.getDeclaredField("sshClient")
        field.isAccessible = true
        field.set(viewModel, mockSshClient)
    }

    /** Populates the ViewModel state with a list of entries for sort tests. */
    private fun injectEntries(entries: List<RemoteFileEntry>) {
        val field = SftpBrowserViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<SftpBrowserUiState>
        flow.value = flow.value.copy(entries = entries)
    }

    // ── Task 3.1 — setSortOrder ───────────────────────────────────────────────

    @Test
    fun `setSortOrder re-sorts entries without new SFTP calls`() = runTest(testDispatcher) {
        val entries = listOf(
            makeEntry("large.bin", sizeBytes = 9000L, isDirectory = false),
            makeEntry("small.txt", sizeBytes = 100L, isDirectory = false),
            makeEntry("medium.dat", sizeBytes = 500L, isDirectory = false),
        )
        injectEntries(entries)

        viewModel.setSortOrder(SortOrder.SIZE)

        // Verify no SFTP calls were made
        coVerify(exactly = 0) { sftpClient.listDirectory(any(), any()) }
        coVerify(exactly = 0) { sftpClient.resolvePath(any(), any()) }
    }

    @Test
    fun `setSortOrder updates uiState_sortOrder`() = runTest(testDispatcher) {
        viewModel.setSortOrder(SortOrder.DATE)
        assertEquals(SortOrder.DATE, viewModel.uiState.value.sortOrder)

        viewModel.setSortOrder(SortOrder.SIZE)
        assertEquals(SortOrder.SIZE, viewModel.uiState.value.sortOrder)

        viewModel.setSortOrder(SortOrder.NAME)
        assertEquals(SortOrder.NAME, viewModel.uiState.value.sortOrder)
    }

    @Test
    fun `setSortOrder keeps directories before files regardless of sort order`() = runTest(testDispatcher) {
        val entries = listOf(
            makeEntry("z_file.txt", sizeBytes = 100L, isDirectory = false, modifiedAt = 9000L),
            makeEntry("a_dir", sizeBytes = 0L, isDirectory = true, modifiedAt = 1L),
            makeEntry("b_file.bin", sizeBytes = 500L, isDirectory = false, modifiedAt = 5000L),
        )
        injectEntries(entries)

        for (order in SortOrder.entries) {
            viewModel.setSortOrder(order)
            val sorted = viewModel.uiState.value.entries
            assertTrue(sorted.first().isDirectory, "Directories should come first for sort order $order")
        }
    }

    // ── Task 3.2 — navigate: success ─────────────────────────────────────────

    @Test
    fun `navigate calls sftpClient_listDirectory with the path`() = runTest(testDispatcher) {
        injectSshClient()
        val entries = listOf(makeEntry("file.txt"))
        coEvery { sftpClient.listDirectory(mockSshClient, "/some/path") } returns entries

        viewModel.navigate("/some/path")
        waitUntil { !viewModel.uiState.value.isLoading }

        coVerify { sftpClient.listDirectory(mockSshClient, "/some/path") }
    }

    @Test
    fun `navigate success updates currentPath`() = runTest(testDispatcher) {
        injectSshClient()
        coEvery { sftpClient.listDirectory(mockSshClient, "/some/path") } returns emptyList()

        viewModel.navigate("/some/path")
        waitUntil { !viewModel.uiState.value.isLoading }

        assertEquals("/some/path", viewModel.uiState.value.currentPath)
    }

    @Test
    fun `navigate success sets entries sorted by current order`() = runTest(testDispatcher) {
        injectSshClient()
        val entries = listOf(
            makeEntry("b.txt", sizeBytes = 200L, isDirectory = false),
            makeEntry("dir_a", sizeBytes = 0L, isDirectory = true),
            makeEntry("a.txt", sizeBytes = 100L, isDirectory = false),
        )
        coEvery { sftpClient.listDirectory(mockSshClient, "/some/path") } returns entries

        viewModel.navigate("/some/path")
        waitUntil { !viewModel.uiState.value.isLoading }

        val state = viewModel.uiState.value
        assertTrue(state.entries.isNotEmpty())
        assertTrue(state.entries.first().isDirectory)
    }

    @Test
    fun `navigate success updates breadcrumbs`() = runTest(testDispatcher) {
        injectSshClient()
        coEvery { sftpClient.listDirectory(mockSshClient, "/some/path") } returns emptyList()

        viewModel.navigate("/some/path")
        waitUntil { !viewModel.uiState.value.isLoading }

        assertEquals(
            SftpBrowserViewModel.buildBreadcrumbs("/some/path"),
            viewModel.uiState.value.breadcrumbs,
        )
    }

    @Test
    fun `navigate sets isLoading=false after completion`() = runTest(testDispatcher) {
        injectSshClient()
        coEvery { sftpClient.listDirectory(mockSshClient, "/some/path") } returns emptyList()

        viewModel.navigate("/some/path")
        waitUntil { !viewModel.uiState.value.isLoading }

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── Task 3.3 — navigate: failure ─────────────────────────────────────────

    @Test
    fun `navigate failure sets non-null errorMessage`() = runTest(testDispatcher) {
        injectSshClient()
        coEvery { sftpClient.listDirectory(mockSshClient, "/bad/path") } throws RuntimeException("Connection reset")

        viewModel.navigate("/bad/path")
        waitUntil { viewModel.uiState.value.errorMessage != null }

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage!!.isNotBlank())
    }

    @Test
    fun `navigate failure sets isLoading=false`() = runTest(testDispatcher) {
        injectSshClient()
        coEvery { sftpClient.listDirectory(mockSshClient, "/bad/path") } throws RuntimeException("timeout")

        viewModel.navigate("/bad/path")
        waitUntil { !viewModel.uiState.value.isLoading }

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onErrorDismissed clears errorMessage to null`() = runTest(testDispatcher) {
        injectSshClient()
        coEvery { sftpClient.listDirectory(any(), any()) } throws RuntimeException("error")
        viewModel.navigate("/bad/path")
        waitUntil { viewModel.uiState.value.errorMessage != null }

        viewModel.onErrorDismissed()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ── Task 3.4 — rename dialog state machine ────────────────────────────────

    @Test
    fun `requestRename sets renameTarget and renameNewName`() = runTest(testDispatcher) {
        val entry = makeEntry("oldname.txt", path = "/some/oldname.txt")

        viewModel.requestRename(entry)

        assertEquals(entry, viewModel.uiState.value.renameTarget)
        assertEquals("oldname.txt", viewModel.uiState.value.renameNewName)
    }

    @Test
    fun `onRenameNameChange updates renameNewName`() = runTest(testDispatcher) {
        viewModel.requestRename(makeEntry("file.txt"))
        viewModel.onRenameNameChange("new_name.txt")

        assertEquals("new_name.txt", viewModel.uiState.value.renameNewName)
    }

    @Test
    fun `dismissRename clears renameTarget and renameNewName`() = runTest(testDispatcher) {
        viewModel.requestRename(makeEntry("file.txt"))
        viewModel.dismissRename()

        assertNull(viewModel.uiState.value.renameTarget)
        assertEquals("", viewModel.uiState.value.renameNewName)
    }

    @Test
    fun `confirmRename calls sftpClient_rename with correct paths`() = runTest(testDispatcher) {
        injectSshClient()
        val entry = makeEntry("old.txt", path = "/parent/old.txt")
        coJustRun { sftpClient.rename(mockSshClient, "/parent/old.txt", "/parent/new.txt") }
        coEvery { sftpClient.listDirectory(mockSshClient, any()) } returns emptyList()

        viewModel.requestRename(entry)
        viewModel.onRenameNameChange("new.txt")
        viewModel.confirmRename()
        waitUntil { viewModel.uiState.value.renameTarget == null }

        coVerify { sftpClient.rename(mockSshClient, "/parent/old.txt", "/parent/new.txt") }
    }

    @Test
    fun `confirmRename with blank name does NOT call sftpClient_rename`() = runTest(testDispatcher) {
        injectSshClient()
        viewModel.requestRename(makeEntry("file.txt"))
        viewModel.onRenameNameChange("   ")

        viewModel.confirmRename()

        coVerify(exactly = 0) { sftpClient.rename(any(), any(), any()) }
    }

    @Test
    fun `confirmRename success clears renameTarget`() = runTest(testDispatcher) {
        injectSshClient()
        val entry = makeEntry("old.txt", path = "/parent/old.txt")
        coJustRun { sftpClient.rename(mockSshClient, any(), any()) }
        coEvery { sftpClient.listDirectory(mockSshClient, any()) } returns emptyList()

        viewModel.requestRename(entry)
        viewModel.onRenameNameChange("new.txt")
        viewModel.confirmRename()
        waitUntil { viewModel.uiState.value.renameTarget == null }

        assertNull(viewModel.uiState.value.renameTarget)
    }

    // ── Task 3.5 — delete dialog state machine ────────────────────────────────

    @Test
    fun `requestDelete sets deleteTarget`() = runTest(testDispatcher) {
        val entry = makeEntry("file.txt")

        viewModel.requestDelete(entry)

        assertEquals(entry, viewModel.uiState.value.deleteTarget)
    }

    @Test
    fun `dismissDelete clears deleteTarget`() = runTest(testDispatcher) {
        viewModel.requestDelete(makeEntry("file.txt"))
        viewModel.dismissDelete()

        assertNull(viewModel.uiState.value.deleteTarget)
    }

    @Test
    fun `confirmDelete calls sftpClient_delete with target path`() = runTest(testDispatcher) {
        injectSshClient()
        val entry = makeEntry("file.txt", path = "/root/file.txt")
        coJustRun { sftpClient.delete(mockSshClient, "/root/file.txt") }
        coEvery { sftpClient.listDirectory(mockSshClient, any()) } returns emptyList()

        viewModel.requestDelete(entry)
        viewModel.confirmDelete()
        waitUntil { viewModel.uiState.value.deleteTarget == null }

        coVerify { sftpClient.delete(mockSshClient, "/root/file.txt") }
    }

    @Test
    fun `confirmDelete success clears deleteTarget`() = runTest(testDispatcher) {
        injectSshClient()
        val entry = makeEntry("file.txt", path = "/root/file.txt")
        coJustRun { sftpClient.delete(mockSshClient, any()) }
        coEvery { sftpClient.listDirectory(mockSshClient, any()) } returns emptyList()

        viewModel.requestDelete(entry)
        viewModel.confirmDelete()
        waitUntil { viewModel.uiState.value.deleteTarget == null }

        assertNull(viewModel.uiState.value.deleteTarget)
    }

    @Test
    fun `confirmDelete when deleteTarget is null does NOT call sftpClient_delete`() = runTest(testDispatcher) {
        injectSshClient()

        viewModel.confirmDelete()

        coVerify(exactly = 0) { sftpClient.delete(any(), any()) }
    }

    // ── Task 3.6 — new directory dialog state machine ─────────────────────────

    @Test
    fun `requestNewDir sets showNewDirDialog=true and newDirName=empty`() = runTest(testDispatcher) {
        viewModel.requestNewDir()

        assertTrue(viewModel.uiState.value.showNewDirDialog)
        assertEquals("", viewModel.uiState.value.newDirName)
    }

    @Test
    fun `onNewDirNameChange updates newDirName`() = runTest(testDispatcher) {
        viewModel.requestNewDir()
        viewModel.onNewDirNameChange("uploads")

        assertEquals("uploads", viewModel.uiState.value.newDirName)
    }

    @Test
    fun `dismissNewDir sets showNewDirDialog=false and newDirName=empty`() = runTest(testDispatcher) {
        viewModel.requestNewDir()
        viewModel.onNewDirNameChange("temp")
        viewModel.dismissNewDir()

        assertFalse(viewModel.uiState.value.showNewDirDialog)
        assertEquals("", viewModel.uiState.value.newDirName)
    }

    @Test
    fun `confirmNewDir calls sftpClient_mkdir with currentPath plus dirName`() = runTest(testDispatcher) {
        injectSshClient()
        // Set a known currentPath via navigate
        val uiStateField = SftpBrowserViewModel::class.java.getDeclaredField("_uiState")
        uiStateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = uiStateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<SftpBrowserUiState>
        flow.value = flow.value.copy(currentPath = "/home/user")

        coJustRun { sftpClient.mkdir(mockSshClient, "/home/user/uploads") }
        coEvery { sftpClient.listDirectory(mockSshClient, any()) } returns emptyList()

        viewModel.requestNewDir()
        viewModel.onNewDirNameChange("uploads")
        viewModel.confirmNewDir()
        waitUntil { !viewModel.uiState.value.showNewDirDialog }

        coVerify { sftpClient.mkdir(mockSshClient, "/home/user/uploads") }
    }

    @Test
    fun `confirmNewDir with blank name does NOT call sftpClient_mkdir`() = runTest(testDispatcher) {
        injectSshClient()
        viewModel.requestNewDir()
        viewModel.onNewDirNameChange("   ")

        viewModel.confirmNewDir()

        coVerify(exactly = 0) { sftpClient.mkdir(any(), any()) }
    }

    @Test
    fun `confirmNewDir success sets showNewDirDialog=false and clears newDirName`() = runTest(testDispatcher) {
        injectSshClient()
        coJustRun { sftpClient.mkdir(mockSshClient, any()) }
        coEvery { sftpClient.listDirectory(mockSshClient, any()) } returns emptyList()

        viewModel.requestNewDir()
        viewModel.onNewDirNameChange("newdir")
        viewModel.confirmNewDir()
        waitUntil { !viewModel.uiState.value.showNewDirDialog }

        assertFalse(viewModel.uiState.value.showNewDirDialog)
        assertEquals("", viewModel.uiState.value.newDirName)
    }

    // ── Task 3.7 — selectEntry ────────────────────────────────────────────────

    @Test
    fun `selectEntry sets selectedPath to entry path`() = runTest(testDispatcher) {
        val entry = makeEntry("report.pdf", path = "/docs/report.pdf")

        viewModel.selectEntry(entry)

        assertEquals("/docs/report.pdf", viewModel.uiState.value.selectedPath)
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun waitUntil(timeoutMillis: Long = 2_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (!condition()) {
            if (System.currentTimeMillis() >= deadline) {
                throw AssertionError("Condition was not met within ${timeoutMillis}ms")
            }
            Thread.sleep(20)
        }
    }
}
