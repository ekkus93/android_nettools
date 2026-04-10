package dev.nettools.android.data.workspace

/**
 * Utility for normalizing user-entered Unix-style workspace paths.
 */
object WorkspacePathResolver {

    /**
     * Normalizes [rawPath] into a canonical workspace path beginning with `/`.
     *
     * This strips redundant separators, removes `.` segments, and prevents
     * escaping above the workspace root via `..`.
     */
    fun normalize(rawPath: String): String {
        val trimmed = rawPath.trim()
        if (trimmed.isEmpty()) return "/"

        val stack = ArrayDeque<String>()
        trimmed.split('/').forEach { segment ->
            when {
                segment.isBlank() || segment == "." -> Unit
                segment == ".." -> if (stack.isNotEmpty()) stack.removeLast()
                else -> stack.addLast(segment)
            }
        }

        return if (stack.isEmpty()) "/" else stack.joinToString(separator = "/", prefix = "/")
    }
}
