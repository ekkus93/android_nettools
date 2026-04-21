package dev.nettools.android.ui.sftp

import dev.nettools.android.domain.model.RemoteFileEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [SftpBrowserViewModel.sortComparator], verifying that entries are
 * sorted correctly for each [SortOrder] and that directories always appear before files.
 */
class SftpBrowserViewModelSortTest {

    private fun entry(
        name: String,
        isDir: Boolean,
        sizeBytes: Long = 0L,
        modifiedAt: Long = 0L,
    ) = RemoteFileEntry(
        name = name,
        path = "/path/$name",
        sizeBytes = sizeBytes,
        permissions = "rwxr-xr-x",
        isDirectory = isDir,
        modifiedAt = modifiedAt,
    )

    // ── SortOrder.NAME ────────────────────────────────────────────────────────

    @Test
    fun `NAME sort - directories come before files`() {
        val entries = listOf(
            entry("b_file.txt", isDir = false),
            entry("a_dir", isDir = true),
            entry("a_file.txt", isDir = false),
        )

        val sorted = entries.sortedWith(SftpBrowserViewModel.sortComparator(SortOrder.NAME))

        assertTrue(sorted.first().isDirectory, "First entry should be a directory")
        assertFalse(sorted.last().isDirectory, "Last entry should be a file")
    }

    @Test
    fun `NAME sort - files sorted alphabetically case-insensitive`() {
        val entries = listOf(
            entry("Zebra.txt", isDir = false),
            entry("apple.txt", isDir = false),
            entry("Mango.txt", isDir = false),
        )

        val sorted = entries.sortedWith(SftpBrowserViewModel.sortComparator(SortOrder.NAME))

        assertEquals("apple.txt", sorted[0].name)
        assertEquals("Mango.txt", sorted[1].name)
        assertEquals("Zebra.txt", sorted[2].name)
    }

    @Test
    fun `NAME sort - directories sorted alphabetically among themselves`() {
        val entries = listOf(
            entry("zeta_dir", isDir = true),
            entry("alpha_dir", isDir = true),
        )

        val sorted = entries.sortedWith(SftpBrowserViewModel.sortComparator(SortOrder.NAME))

        assertEquals("alpha_dir", sorted[0].name)
        assertEquals("zeta_dir", sorted[1].name)
    }

    // ── SortOrder.SIZE ────────────────────────────────────────────────────────

    @Test
    fun `SIZE sort - directories come before files regardless of size`() {
        val entries = listOf(
            entry("huge.bin", isDir = false, sizeBytes = 999_999L),
            entry("dir", isDir = true, sizeBytes = 0L),
        )

        val sorted = entries.sortedWith(SftpBrowserViewModel.sortComparator(SortOrder.SIZE))

        assertTrue(sorted.first().isDirectory)
    }

    @Test
    fun `SIZE sort - files sorted ascending by size`() {
        val entries = listOf(
            entry("large.bin", isDir = false, sizeBytes = 10_000L),
            entry("small.txt", isDir = false, sizeBytes = 100L),
            entry("medium.dat", isDir = false, sizeBytes = 5_000L),
        )

        val sorted = entries.sortedWith(SftpBrowserViewModel.sortComparator(SortOrder.SIZE))

        assertEquals("small.txt", sorted[0].name)
        assertEquals("medium.dat", sorted[1].name)
        assertEquals("large.bin", sorted[2].name)
    }

    // ── SortOrder.DATE ────────────────────────────────────────────────────────

    @Test
    fun `DATE sort - directories come before files`() {
        val entries = listOf(
            entry("file.txt", isDir = false, modifiedAt = 9_999L),
            entry("dir", isDir = true, modifiedAt = 1L),
        )

        val sorted = entries.sortedWith(SftpBrowserViewModel.sortComparator(SortOrder.DATE))

        assertTrue(sorted.first().isDirectory)
    }

    @Test
    fun `DATE sort - files sorted newest first`() {
        val entries = listOf(
            entry("old.txt", isDir = false, modifiedAt = 1_000L),
            entry("new.txt", isDir = false, modifiedAt = 9_000L),
            entry("mid.txt", isDir = false, modifiedAt = 5_000L),
        )

        val sorted = entries.sortedWith(SftpBrowserViewModel.sortComparator(SortOrder.DATE))

        assertEquals("new.txt", sorted[0].name)
        assertEquals("mid.txt", sorted[1].name)
        assertEquals("old.txt", sorted[2].name)
    }

    // ── Mixed directory + file with all sort orders ───────────────────────────

    @Test
    fun `all sort orders - mixed list always starts with directories`() {
        val entries = listOf(
            entry("file_a.txt", isDir = false, sizeBytes = 100L, modifiedAt = 500L),
            entry("dir_b", isDir = true, sizeBytes = 0L, modifiedAt = 1_000L),
            entry("file_b.txt", isDir = false, sizeBytes = 50L, modifiedAt = 200L),
            entry("dir_a", isDir = true, sizeBytes = 0L, modifiedAt = 600L),
        )

        SortOrder.entries.forEach { order ->
            val sorted = entries.sortedWith(SftpBrowserViewModel.sortComparator(order))
            val firstNonDir = sorted.indexOfFirst { !it.isDirectory }
            val lastDir = sorted.indexOfLast { it.isDirectory }
            assertTrue(
                lastDir < firstNonDir || firstNonDir == -1,
                "All directories must precede files for sort order $order",
            )
        }
    }

    @Test
    fun `buildBreadcrumbs - returns single element for root path`() {
        val breadcrumbs = SftpBrowserViewModel.buildBreadcrumbs("/")
        assertEquals(listOf("/"), breadcrumbs)
    }

    @Test
    fun `buildBreadcrumbs - keeps root breadcrumb for absolute paths`() {
        val breadcrumbs = SftpBrowserViewModel.buildBreadcrumbs("/var/www/html")

        assertEquals(listOf("/", "/var", "/var/www", "/var/www/html"), breadcrumbs)
    }

    @Test
    fun `buildBreadcrumbs - keeps home breadcrumb for home relative paths`() {
        val breadcrumbs = SftpBrowserViewModel.buildBreadcrumbs("~/projects/nettools")

        assertEquals(listOf("~", "~/projects", "~/projects/nettools"), breadcrumbs)
    }

    @Test
    fun `parentPath - handles root and absolute children correctly`() {
        assertEquals("/", SftpBrowserViewModel.parentPath("/"))
        assertEquals("/", SftpBrowserViewModel.parentPath("/var"))
        assertEquals("/var", SftpBrowserViewModel.parentPath("/var/www"))
    }

    @Test
    fun `parentPath - handles home relative children correctly`() {
        assertEquals("~", SftpBrowserViewModel.parentPath("~"))
        assertEquals("~", SftpBrowserViewModel.parentPath("~/projects"))
        assertEquals("~/projects", SftpBrowserViewModel.parentPath("~/projects/nettools"))
    }

    // Standalone assertion helper to avoid importing org.junit.jupiter.api.Assertions.assertFalse
    private fun assertFalse(condition: Boolean, message: String = "") {
        assertTrue(!condition, message)
    }
}
