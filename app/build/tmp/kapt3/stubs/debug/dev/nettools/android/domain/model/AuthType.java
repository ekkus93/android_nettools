package dev.nettools.android.domain.model;

/**
 * Authentication method for an SSH connection.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Ldev/nettools/android/domain/model/AuthType;", "", "<init>", "(Ljava/lang/String;I)V", "PASSWORD", "PRIVATE_KEY", "app_debug"})
public enum AuthType {
    /*public static final*/ PASSWORD /* = new PASSWORD() */,
    /*public static final*/ PRIVATE_KEY /* = new PRIVATE_KEY() */;
    
    AuthType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<dev.nettools.android.domain.model.AuthType> getEntries() {
        return null;
    }
}