package dev.nettools.android.data.curl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Option catalog generated from the bundled curl source used to build the embedded runtime.
 */
@Singleton
class BundledCurlOptionCatalog @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CurlOptionCatalog {

    private val options: Set<String> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        context.assets.open("curl/supported-options.txt").bufferedReader().useLines { lines ->
            lines.map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        }
    }

    override fun supportedOptions(): Set<String> = options
}
