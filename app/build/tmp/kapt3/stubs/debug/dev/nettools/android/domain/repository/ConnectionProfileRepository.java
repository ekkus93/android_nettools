package dev.nettools.android.domain.repository;

import dev.nettools.android.domain.model.ConnectionProfile;
import kotlinx.coroutines.flow.Flow;

/**
 * Repository interface for [ConnectionProfile] persistence.
 * All database operations are abstracted behind this interface
 * to keep the domain layer independent of Room.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H&J\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0007\u001a\u00020\bH\u00a6@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bH\u00a6@\u00a2\u0006\u0002\u0010\t\u00a8\u0006\u000f\u00c0\u0006\u0003"}, d2 = {"Ldev/nettools/android/domain/repository/ConnectionProfileRepository;", "", "getAll", "Lkotlinx/coroutines/flow/Flow;", "", "Ldev/nettools/android/domain/model/ConnectionProfile;", "getById", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "save", "", "profile", "(Ldev/nettools/android/domain/model/ConnectionProfile;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "app_debug"})
public abstract interface ConnectionProfileRepository {
    
    /**
     * Observes all saved connection profiles as a reactive [Flow].
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<dev.nettools.android.domain.model.ConnectionProfile>> getAll();
    
    /**
     * Returns the profile with the given [id], or null if not found.
     *
     * @param id UUID of the profile.
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super dev.nettools.android.domain.model.ConnectionProfile> $completion);
    
    /**
     * Inserts or updates a [ConnectionProfile].
     *
     * @param profile The profile to persist.
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object save(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.ConnectionProfile profile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Permanently removes the profile identified by [id].
     *
     * @param id UUID of the profile to delete.
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}