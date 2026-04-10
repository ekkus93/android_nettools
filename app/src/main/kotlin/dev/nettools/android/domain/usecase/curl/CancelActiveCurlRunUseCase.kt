package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.service.CurlRunHolder
import javax.inject.Inject

/**
 * Requests cancellation of the active curl run.
 */
class CancelActiveCurlRunUseCase @Inject constructor(
    private val holder: CurlRunHolder,
) {
    /** Requests cancellation for [runId]. */
    operator fun invoke(runId: String) {
        holder.requestCancel(runId)
    }
}
