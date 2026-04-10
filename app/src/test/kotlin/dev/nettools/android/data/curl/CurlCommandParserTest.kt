package dev.nettools.android.data.curl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [CurlCommandParser].
 */
class CurlCommandParserTest {

    private val parser = CurlCommandParser(EmbeddedCurlOptionCatalog())

    @Test
    fun `parse prepends curl for bare arguments`() {
        val result = parser.parse("--head https://example.com")

        assertTrue(result.isValid)
        assertEquals(listOf("curl", "--head", "https://example.com"), result.command?.tokens)
    }

    @Test
    fun `parse collapses shell style continuations`() {
        val result = parser.parse(
            """
            curl --request POST \
              --header "Content-Type: application/json" \
              https://example.com
            """.trimIndent()
        )

        assertTrue(result.isValid)
        assertEquals(
            listOf(
                "curl",
                "--request",
                "POST",
                "--header",
                "Content-Type: application/json",
                "https://example.com",
            ),
            result.command?.tokens,
        )
    }

    @Test
    fun `parse fails for unclosed quotes`() {
        val result = parser.parse("""curl --header "Authorization: Bearer token""")

        assertFalse(result.isValid)
        assertEquals("Close all quoted strings before running the command.", result.errors.single().message)
    }

    @Test
    fun `parse fails for unknown long option`() {
        val result = parser.parse("curl --verbse https://example.com")

        assertFalse(result.isValid)
        assertEquals("Unknown curl option: --verbse", result.errors.single().message)
    }

    @Test
    fun `parse extracts path references`() {
        val result = parser.parse("""curl -o /tmp/out.txt --config ./curl.conf --data-binary @payload.json https://example.com""")

        assertTrue(result.isValid)
        val refs = result.command?.pathReferences.orEmpty()
        assertEquals(3, refs.size)
        assertEquals("/tmp/out.txt", refs[0].normalizedPath)
        assertEquals("/curl.conf", refs[1].normalizedPath)
        assertEquals("/payload.json", refs[2].normalizedPath)
    }
}
