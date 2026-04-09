package dev.nettools.android;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

/**
 * Application class for Android NetTools.
 * Annotated with [HiltAndroidApp] to trigger Hilt's code generation
 * and set up the application-level dependency injection component.
 */
@dagger.hilt.android.HiltAndroidApp()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Ldev/nettools/android/NetToolsApp;", "Landroid/app/Application;", "<init>", "()V", "app_debug"})
public final class NetToolsApp extends android.app.Application {
    
    public NetToolsApp() {
        super();
    }
}