package dev.nettools.android.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Unit tests for [CurlUserMessageFormatter].
 */
class CurlUserMessageFormatterTest {

    @Test
    fun `execution failure maps missing files`() {
        val message = CurlUserMessageFormatter.executionFailure(
            FileNotFoundException("No such file or directory"),
        )

        assertEquals("A referenced workspace file or directory could not be found.", message)
    }

    @Test
    fun `execution failure maps missing curl runtime`() {
        val message = CurlUserMessageFormatter.executionFailure(
            IOException("Cannot run program \"curl\": error=2, No such file or directory"),
        )

        assertEquals("The embedded curl runtime is unavailable on this build.", message)
    }

    @Test
    fun `workspace failure maps revoked permission`() {
        val message = CurlUserMessageFormatter.workspaceFailure(
            action = "import the file",
            error = IllegalStateException("Unable to open the selected file. Permission may have been revoked."),
        )

        assertEquals("Android no longer allows access to the selected file or destination.", message)
    }

    @Test
    fun `workspace failure maps name conflicts`() {
        val message = CurlUserMessageFormatter.workspaceFailure(
            action = "create the directory",
            error = IOException("Workspace entry already exists: /demo"),
        )

        assertEquals("A workspace item with that name already exists.", message)
    }
}
