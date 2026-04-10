package dev.nettools.android.domain.usecase.curl

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nettools.android.service.CurlForegroundService
import dev.nettools.android.service.CurlRunHolder
import dev.nettools.android.service.PendingCurlRunParams
import javax.inject.Inject

/**
 * Stores a prepared curl run and starts the foreground service that executes it.
 */
class DispatchPendingCurlRunUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val curlRunHolder: CurlRunHolder,
) {

    /** Queues [pendingRun] for service execution. */
    operator fun invoke(pendingRun: PendingCurlRunParams) {
        curlRunHolder.setPendingRun(pendingRun)
        ContextCompat.startForegroundService(
            context,
            Intent(context, CurlForegroundService::class.java),
        )
    }
}
