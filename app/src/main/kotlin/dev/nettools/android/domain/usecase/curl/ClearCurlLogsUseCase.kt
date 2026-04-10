package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.domain.repository.CurlRunRepository
import javax.inject.Inject

/**
 * Deletes all persisted curl run metadata and retained output.
 */
class ClearCurlLogsUseCase @Inject constructor(
    private val repository: CurlRunRepository,
) {
    /** Clears all stored curl logs. */
    suspend operator fun invoke() {
        repository.clearAll()
    }
}
