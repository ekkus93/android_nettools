package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.service.CurlRunHolder
import javax.inject.Inject

/**
 * Exposes the active curl run state held by [CurlRunHolder].
 */
class ObserveActiveCurlRunUseCase @Inject constructor(
    private val holder: CurlRunHolder,
) {
    /** Returns the live state flow for the current curl run. */
    operator fun invoke() = holder.liveState
}
