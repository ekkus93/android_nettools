package dev.nettools.android.ui.navigation;

/**
 * Centralised navigation route constants for the NavHost.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u000b\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u0005R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Ldev/nettools/android/ui/navigation/Routes;", "", "<init>", "()V", "HOME", "", "TRANSFER", "SFTP_BROWSER", "SAVED_CONNECTIONS", "HISTORY", "PROGRESS", "progress", "jobId", "app_release"})
public final class Routes {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String HOME = "home";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TRANSFER = "transfer";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SFTP_BROWSER = "sftp_browser";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SAVED_CONNECTIONS = "saved_connections";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String HISTORY = "history";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PROGRESS = "progress/{jobId}";
    @org.jetbrains.annotations.NotNull()
    public static final dev.nettools.android.ui.navigation.Routes INSTANCE = null;
    
    private Routes() {
        super();
    }
    
    /**
     * Builds the progress route for a specific [jobId].
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String progress(@org.jetbrains.annotations.NotNull()
    java.lang.String jobId) {
        return null;
    }
}