package dev.nettools.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.nettools.android.data.curl.NativeCurlBridge

/**
 * Application class for Android NetTools.
 * Annotated with [HiltAndroidApp] to trigger Hilt's code generation
 * and set up the application-level dependency injection component.
 */
@HiltAndroidApp
class NetToolsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NativeCurlBridge.initializeGlobal()
    }
}
