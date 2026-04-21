package dev.nettools.android.data.workspace

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [WorkspacePathResolver.normalize], covering blank input, root paths,
 * simple paths, dot segments, double-dot segments, whitespace, and relative paths.
 */
class WorkspacePathResolverTest {

    // ── Task 2.1 — empty / blank input ────────────────────────────────────────

    @Test
    fun `normalize empty string returns root`() {
        assertEquals("/", WorkspacePathResolver.normalize(""))
    }

    @Test
    fun `normalize whitespace-only string returns root`() {
        assertEquals("/", WorkspacePathResolver.normalize("   "))
    }

    // ── Task 2.2 — root ───────────────────────────────────────────────────────

    @Test
    fun `normalize root slash returns root`() {
        assertEquals("/", WorkspacePathResolver.normalize("/"))
    }

    @Test
    fun `normalize double slash returns root`() {
        assertEquals("/", WorkspacePathResolver.normalize("//"))
    }

    @Test
    fun `normalize triple slash with segment collapses consecutive slashes`() {
        assertEquals("/a", WorkspacePathResolver.normalize("///a"))
    }

    // ── Task 2.3 — simple paths ───────────────────────────────────────────────

    @Test
    fun `normalize single segment path`() {
        assertEquals("/foo", WorkspacePathResolver.normalize("/foo"))
    }

    @Test
    fun `normalize two segment path`() {
        assertEquals("/foo/bar", WorkspacePathResolver.normalize("/foo/bar"))
    }

    @Test
    fun `normalize three segment path`() {
        assertEquals("/foo/bar/baz", WorkspacePathResolver.normalize("/foo/bar/baz"))
    }

    // ── Task 2.4 — dot segments ───────────────────────────────────────────────

    @Test
    fun `normalize removes leading dot segment`() {
        assertEquals("/foo", WorkspacePathResolver.normalize("/./foo"))
    }

    @Test
    fun `normalize removes middle dot segment`() {
        assertEquals("/foo/bar", WorkspacePathResolver.normalize("/foo/./bar"))
    }

    @Test
    fun `normalize removes trailing dot segment`() {
        assertEquals("/foo", WorkspacePathResolver.normalize("/foo/."))
    }

    // ── Task 2.5 — double-dot segments ───────────────────────────────────────

    @Test
    fun `normalize double-dot pops last segment`() {
        assertEquals("/a/c", WorkspacePathResolver.normalize("/a/b/../c"))
    }

    @Test
    fun `normalize multiple double-dots`() {
        assertEquals("/c", WorkspacePathResolver.normalize("/a/../b/../c"))
    }

    @Test
    fun `normalize double-dot beyond root stays at root`() {
        assertEquals("/b", WorkspacePathResolver.normalize("/a/../../b"))
    }

    @Test
    fun `normalize only double-dots returns root`() {
        assertEquals("/", WorkspacePathResolver.normalize("/../.."))
    }

    @Test
    fun `normalize mixed double-dots`() {
        assertEquals("/a/d", WorkspacePathResolver.normalize("/a/b/c/../../d"))
    }

    // ── Task 2.6 — leading/trailing whitespace ────────────────────────────────

    @Test
    fun `normalize trims surrounding whitespace`() {
        assertEquals("/foo/bar", WorkspacePathResolver.normalize("  /foo/bar  "))
    }

    // ── Task 2.7 — paths without leading slash ────────────────────────────────

    @Test
    fun `normalize relative path without leading slash gets slash prepended`() {
        assertEquals("/foo/bar", WorkspacePathResolver.normalize("foo/bar"))
    }
}
