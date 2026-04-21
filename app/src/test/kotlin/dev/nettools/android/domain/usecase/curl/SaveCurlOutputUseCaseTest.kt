package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.domain.model.CurlRunOutput
import dev.nettools.android.domain.model.WorkspaceEntry
import dev.nettools.android.domain.repository.WorkspaceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [SaveCurlOutputUseCase], verifying path generation, content format,
 * and correct delegation to [WorkspaceRepository].
 */
class SaveCurlOutputUseCaseTest {

    private val workspaceRepository: WorkspaceRepository = mockk()
    private val mockWorkspaceEntry: WorkspaceEntry = mockk()
    private lateinit var useCase: SaveCurlOutputUseCase

    private fun makeOutput(
        stdoutText: String = "Hello, stdout!",
        stderrText: String = "Warning: stderr content",
    ) = CurlRunOutput(
        stdoutText = stdoutText,
        stderrText = stderrText,
    )

    @BeforeEach
    fun setUp() {
        coEvery { workspaceRepository.writeTextFile(any(), any()) } returns mockWorkspaceEntry
        useCase = SaveCurlOutputUseCase(workspaceRepository)
    }

    // ── Task 5.1 — path generation ────────────────────────────────────────────

    @Test
    fun `invoke returns path matching curl-output-runId pattern`() = runTest {
        val path = useCase("run-abc123", makeOutput())

        assertEquals("/curl-output-run-abc123.txt", path)
    }

    @Test
    fun `different runId values produce different paths`() = runTest {
        val path1 = useCase("run-001", makeOutput())
        val path2 = useCase("run-002", makeOutput())

        assertTrue(path1 != path2)
        assertEquals("/curl-output-run-001.txt", path1)
        assertEquals("/curl-output-run-002.txt", path2)
    }

    // ── Task 5.2 — content format ─────────────────────────────────────────────

    @Test
    fun `written text contains stdout header`() = runTest {
        val pathSlot = slot<String>()
        val textSlot = slot<String>()
        coEvery { workspaceRepository.writeTextFile(capture(pathSlot), capture(textSlot)) } returns mockWorkspaceEntry

        useCase("run-1", makeOutput())

        assertTrue(textSlot.captured.contains("=== stdout ==="), "Content should contain stdout header")
    }

    @Test
    fun `written text contains stderr header`() = runTest {
        val pathSlot = slot<String>()
        val textSlot = slot<String>()
        coEvery { workspaceRepository.writeTextFile(capture(pathSlot), capture(textSlot)) } returns mockWorkspaceEntry

        useCase("run-1", makeOutput())

        assertTrue(textSlot.captured.contains("=== stderr ==="), "Content should contain stderr header")
    }

    @Test
    fun `written text contains stdoutText verbatim`() = runTest {
        val textSlot = slot<String>()
        coEvery { workspaceRepository.writeTextFile(any(), capture(textSlot)) } returns mockWorkspaceEntry
        val output = makeOutput(stdoutText = "my stdout content line 1\nline 2")

        useCase("run-1", output)

        assertTrue(textSlot.captured.contains("my stdout content line 1\nline 2"))
    }

    @Test
    fun `written text contains stderrText verbatim`() = runTest {
        val textSlot = slot<String>()
        coEvery { workspaceRepository.writeTextFile(any(), capture(textSlot)) } returns mockWorkspaceEntry
        val output = makeOutput(stderrText = "error detail here")

        useCase("run-1", output)

        assertTrue(textSlot.captured.contains("error detail here"))
    }

    @Test
    fun `stdout section appears before stderr section`() = runTest {
        val textSlot = slot<String>()
        coEvery { workspaceRepository.writeTextFile(any(), capture(textSlot)) } returns mockWorkspaceEntry

        useCase("run-1", makeOutput())

        val text = textSlot.captured
        val stdoutIndex = text.indexOf("=== stdout ===")
        val stderrIndex = text.indexOf("=== stderr ===")
        assertTrue(stdoutIndex < stderrIndex, "stdout section must appear before stderr section")
    }

    // ── Task 5.3 — delegation ─────────────────────────────────────────────────

    @Test
    fun `invoke calls writeTextFile exactly once with correct path and content`() = runTest {
        val pathSlot = slot<String>()
        val textSlot = slot<String>()
        coEvery { workspaceRepository.writeTextFile(capture(pathSlot), capture(textSlot)) } returns mockWorkspaceEntry
        val output = makeOutput(stdoutText = "out", stderrText = "err")

        useCase("run-42", output)

        coVerify(exactly = 1) { workspaceRepository.writeTextFile(any(), any()) }
        assertEquals("/curl-output-run-42.txt", pathSlot.captured)
        assertTrue(textSlot.captured.contains("out"))
        assertTrue(textSlot.captured.contains("err"))
    }
}
