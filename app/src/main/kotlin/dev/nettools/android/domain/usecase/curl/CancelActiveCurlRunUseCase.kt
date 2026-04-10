package dev.nettools.android.domain.usecase.curl

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nettools.android.service.CurlRunHolder
import dev.nettools.android.service.CurlForegroundService
import javax.inject.Inject

/**
 * Requests cancellation of the active curl run.
 */
class CancelActiveCurlRunUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val holder: CurlRunHolder,
) {
    /** Requests cancellation for [runId]. */
    operator fun invoke(runId: String) {
        holder.requestCancel(runId)
        ContextCompat.startForegroundService(
            context,
            Intent(context, CurlForegroundService::class.java).apply {
                action = CurlForegroundService.ACTION_CANCEL
                putExtra(CurlForegroundService.EXTRA_RUN_ID, runId)
            },
        )
    }
}
