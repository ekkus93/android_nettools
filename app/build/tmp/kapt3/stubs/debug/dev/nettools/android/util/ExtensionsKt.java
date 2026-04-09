package dev.nettools.android.util;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000\u0014\n\u0000\n\u0002\u0010\u000e\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0004\u001a\n\u0010\u0000\u001a\u00020\u0001*\u00020\u0002\u001a\n\u0010\u0003\u001a\u00020\u0001*\u00020\u0004\u001a\u0012\u0010\u0005\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0006\u001a\u00020\u0004\u001a\n\u0010\u0007\u001a\u00020\u0001*\u00020\u0001\u00a8\u0006\b"}, d2 = {"toFormattedSize", "", "", "toSpeedString", "", "toEtaString", "speedBytesPerSec", "sanitizeForLog", "app_debug"})
public final class ExtensionsKt {
    
    /**
     * Formats a byte count as a human-readable size string (e.g. "1.2 MB", "345 KB").
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String toFormattedSize(long $this$toFormattedSize) {
        return null;
    }
    
    /**
     * Formats a bytes-per-second speed value as a human-readable speed string
     * (e.g. "1.2 MB/s", "345 KB/s").
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String toSpeedString(double $this$toSpeedString) {
        return null;
    }
    
    /**
     * Estimates and formats the time remaining for a transfer as a human-readable string
     * (e.g. "2m 30s remaining", "45s remaining").
     *
     * @param speedBytesPerSec Current transfer speed in bytes per second.
     * @return Formatted ETA string, or "Unknown" if speed is zero or total is unknown.
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String toEtaString(long $this$toEtaString, double speedBytesPerSec) {
        return null;
    }
    
    /**
     * Returns "[REDACTED]" for any string value.
     * Use this whenever a password or key material might otherwise be logged.
     *
     * @return The literal string "[REDACTED]".
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String sanitizeForLog(@org.jetbrains.annotations.NotNull()
    java.lang.String $this$sanitizeForLog) {
        return null;
    }
}