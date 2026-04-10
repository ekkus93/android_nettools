package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.data.curl.CurlCommandParser
import dev.nettools.android.domain.model.CurlCommandParseResult
import javax.inject.Inject

/**
 * Validates and parses raw curl input.
 */
class ValidateCurlCommandUseCase @Inject constructor(
    private val parser: CurlCommandParser,
) {
    /** Parses [input] into a validated curl command result. */
    operator fun invoke(input: String): CurlCommandParseResult = parser.parse(input)
}
