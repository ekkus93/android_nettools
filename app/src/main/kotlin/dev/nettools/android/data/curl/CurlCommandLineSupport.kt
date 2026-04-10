package dev.nettools.android.data.curl

/**
 * Builds the final process command line for an embedded curl invocation.
 */
internal fun buildProcessCommandLine(
    runtime: CurlRuntime,
    tokens: List<String>,
): List<String> {
    return buildList {
        add(runtime.executablePath)
        if (runtime.caCertificatePath != null && tokens.none { token ->
                token == "--cacert" ||
                    token.startsWith("--cacert=") ||
                    token == "--capath" ||
                    token.startsWith("--capath=") ||
                    token == "--insecure" ||
                    token == "-k"
            }
        ) {
            add("--cacert")
            add(runtime.caCertificatePath)
        }
        addAll(tokens.drop(1))
    }
}
