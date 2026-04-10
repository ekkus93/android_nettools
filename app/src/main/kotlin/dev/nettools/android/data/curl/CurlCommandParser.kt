package dev.nettools.android.data.curl

import dev.nettools.android.data.workspace.WorkspacePathResolver
import dev.nettools.android.domain.model.CurlCommandParseResult
import dev.nettools.android.domain.model.CurlPathReferenceRole
import dev.nettools.android.domain.model.CurlValidationError
import dev.nettools.android.domain.model.ParsedCurlCommand
import dev.nettools.android.domain.model.ParsedCurlPathReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser and lightweight validator for raw curl command input.
 */
@Singleton
class CurlCommandParser @Inject constructor(
    private val optionCatalog: CurlOptionCatalog,
) {

    /**
     * Parses and validates raw [input] into a normalized curl command.
     */
    fun parse(input: String): CurlCommandParseResult {
        val trimmedInput = input.trim()
        if (trimmedInput.isEmpty()) {
            return CurlCommandParseResult(
                errors = listOf(CurlValidationError(message = "Enter a curl command or arguments.")),
            )
        }

        val normalizedText = normalizeContinuations(trimmedInput)
        val tokenization = tokenize(normalizedText)
        if (tokenization.error != null) {
            return CurlCommandParseResult(
                errors = listOf(CurlValidationError(message = tokenization.error)),
            )
        }

        val tokens = tokenization.tokens.toMutableList()
        if (tokens.isEmpty()) {
            return CurlCommandParseResult(
                errors = listOf(CurlValidationError(message = "Enter a curl command or arguments.")),
            )
        }
        if (tokens.first() != "curl") {
            tokens.add(index = 0, element = "curl")
        }
        if (tokens.size == 1) {
            return CurlCommandParseResult(
                errors = listOf(CurlValidationError(message = "Add at least one curl argument or URL.")),
            )
        }

        val errors = validateOptions(tokens)
        if (errors.isNotEmpty()) {
            return CurlCommandParseResult(errors = errors)
        }

        return CurlCommandParseResult(
            command = ParsedCurlCommand(
                originalText = input,
                normalizedText = tokens.joinToString(separator = " "),
                tokens = tokens,
                pathReferences = extractPathReferences(tokens),
            ),
        )
    }

    private fun normalizeContinuations(input: String): String =
        input.replace(Regex("""\\\r?\n[ \t]*"""), " ")

    private fun tokenize(input: String): TokenizationResult {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var quoteMode = QuoteMode.NONE
        var index = 0

        while (index < input.length) {
            val ch = input[index]
            when (quoteMode) {
                QuoteMode.NONE -> when {
                    ch.isWhitespace() -> {
                        if (current.isNotEmpty()) {
                            tokens += current.toString()
                            current.clear()
                        }
                    }
                    ch == '\'' -> quoteMode = QuoteMode.SINGLE
                    ch == '"' -> quoteMode = QuoteMode.DOUBLE
                    ch == '\\' && index + 1 < input.length -> {
                        index += 1
                        current.append(input[index])
                    }
                    else -> current.append(ch)
                }

                QuoteMode.SINGLE -> {
                    if (ch == '\'') {
                        quoteMode = QuoteMode.NONE
                    } else {
                        current.append(ch)
                    }
                }

                QuoteMode.DOUBLE -> when {
                    ch == '"' -> quoteMode = QuoteMode.NONE
                    ch == '\\' && index + 1 < input.length -> {
                        index += 1
                        current.append(input[index])
                    }
                    else -> current.append(ch)
                }
            }
            index += 1
        }

        if (quoteMode != QuoteMode.NONE) {
            return TokenizationResult(tokens = emptyList(), error = "Close all quoted strings before running the command.")
        }
        if (current.isNotEmpty()) {
            tokens += current.toString()
        }
        return TokenizationResult(tokens = tokens)
    }

    private fun validateOptions(tokens: List<String>): List<CurlValidationError> {
        val errors = mutableListOf<CurlValidationError>()
        val supportedOptions = optionCatalog.supportedOptions()
        var index = 1
        var endOfOptions = false

        while (index < tokens.size) {
            val token = tokens[index]
            if (endOfOptions) {
                index += 1
                continue
            }
            if (token == "--") {
                endOfOptions = true
                index += 1
                continue
            }
            if (!token.startsWith("-") || token == "-") {
                index += 1
                continue
            }

            val optionToken = token.substringBefore("=")
            if (supportedOptions.contains(optionToken)) {
                index += 1
                continue
            }

            if (token.startsWith("--")) {
                errors += CurlValidationError(
                    message = "Unknown curl option: $optionToken",
                    token = optionToken,
                )
                index += 1
                continue
            }

            val groupedShortOptions = optionToken.drop(1)
            val firstUnknown = groupedShortOptions.firstOrNull { candidate ->
                !supportedOptions.contains("-$candidate")
            }
            if (firstUnknown != null) {
                errors += CurlValidationError(
                    message = "Unknown curl option: -$firstUnknown",
                    token = "-$firstUnknown",
                )
            }
            index += 1
        }

        return errors
    }

    private fun extractPathReferences(tokens: List<String>): List<ParsedCurlPathReference> {
        val references = mutableListOf<ParsedCurlPathReference>()
        var index = 1
        while (index < tokens.size) {
            val token = tokens[index]
            val option = token.substringBefore("=")
            val inlineValue = token.substringAfter("=", missingDelimiterValue = "")

            val role = OPTION_PATH_ROLES[option]
            if (role != null) {
                val value = when {
                    inlineValue.isNotEmpty() -> inlineValue
                    index + 1 < tokens.size -> tokens[index + 1]
                    else -> null
                }
                if (!value.isNullOrBlank()) {
                    references += ParsedCurlPathReference(
                        originalPath = value,
                        normalizedPath = WorkspacePathResolver.normalize(value),
                        role = role,
                    )
                    if (inlineValue.isEmpty()) {
                        index += 1
                    }
                }
            } else if (option in PAYLOAD_OPTIONS) {
                val value = when {
                    inlineValue.isNotEmpty() -> inlineValue
                    index + 1 < tokens.size -> tokens[index + 1]
                    else -> null
                }
                if (!value.isNullOrBlank()) {
                    extractPayloadReference(value)?.let(references::add)
                    if (inlineValue.isEmpty()) {
                        index += 1
                    }
                }
            }
            index += 1
        }
        return references
    }

    private fun extractPayloadReference(value: String): ParsedCurlPathReference? {
        if (!value.startsWith("@")) return null
        val rawPath = value.removePrefix("@").substringBefore(";")
        if (rawPath.isBlank()) return null
        return ParsedCurlPathReference(
            originalPath = rawPath,
            normalizedPath = WorkspacePathResolver.normalize(rawPath),
            role = CurlPathReferenceRole.PAYLOAD_FILE,
        )
    }

    private data class TokenizationResult(
        val tokens: List<String>,
        val error: String? = null,
    )

    private enum class QuoteMode {
        NONE,
        SINGLE,
        DOUBLE,
    }

    private companion object {
        private val OPTION_PATH_ROLES = mapOf(
            "-K" to CurlPathReferenceRole.CONFIG_FILE,
            "--config" to CurlPathReferenceRole.CONFIG_FILE,
            "-o" to CurlPathReferenceRole.OUTPUT_FILE,
            "--output" to CurlPathReferenceRole.OUTPUT_FILE,
            "-D" to CurlPathReferenceRole.OUTPUT_FILE,
            "--dump-header" to CurlPathReferenceRole.OUTPUT_FILE,
            "-T" to CurlPathReferenceRole.INPUT_FILE,
            "--upload-file" to CurlPathReferenceRole.INPUT_FILE,
            "-c" to CurlPathReferenceRole.COOKIE_JAR,
            "--cookie-jar" to CurlPathReferenceRole.COOKIE_JAR,
            "--stderr" to CurlPathReferenceRole.STDERR_FILE,
            "--etag-save" to CurlPathReferenceRole.OUTPUT_FILE,
            "--etag-compare" to CurlPathReferenceRole.INPUT_FILE,
            "--cacert" to CurlPathReferenceRole.INPUT_FILE,
            "--cert" to CurlPathReferenceRole.INPUT_FILE,
            "--key" to CurlPathReferenceRole.INPUT_FILE,
            "--proxy-cert" to CurlPathReferenceRole.INPUT_FILE,
            "--proxy-key" to CurlPathReferenceRole.INPUT_FILE,
        )

        private val PAYLOAD_OPTIONS = setOf(
            "-d",
            "--data",
            "--data-ascii",
            "--data-binary",
            "--data-raw",
            "--data-urlencode",
            "-F",
            "--form",
            "--form-string",
            "-H",
            "--header",
        )
    }
}
