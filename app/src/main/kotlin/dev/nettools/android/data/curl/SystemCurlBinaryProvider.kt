package dev.nettools.android.data.curl

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Temporary curl binary provider that resolves `curl` from the process PATH.
 *
 * This is the seam that will later switch to the bundled per-ABI curl binary.
 */
@Singleton
class SystemCurlBinaryProvider @Inject constructor() : CurlBinaryProvider {
    override suspend fun getExecutablePath(): String = "curl"
}
