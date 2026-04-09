package dev.nettools.android.service;

import dev.nettools.android.data.ssh.TransferProgress;
import dev.nettools.android.domain.model.TransferJob;
import dev.nettools.android.domain.model.TransferStatus;
import kotlinx.coroutines.flow.StateFlow;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Singleton that bridges the [TransferForegroundService] and ViewModels.
 * ViewModels enqueue jobs and observe progress without requiring direct
 * service binding.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0014J\b\u0010\u0018\u001a\u0004\u0018\u00010\u0014J\u0016\u0010\u0019\u001a\u00020\u00162\u0006\u0010\u001a\u001a\u00020\u00072\u0006\u0010\u001b\u001a\u00020\bJ\u0016\u0010\u001c\u001a\u00020\u00162\u0006\u0010\u001a\u001a\u00020\u00072\u0006\u0010\u001d\u001a\u00020\u001eJ\u000e\u0010\u001f\u001a\u00020\u00162\u0006\u0010\u001a\u001a\u00020\u0007R \u0010\u0004\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u00060\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001a\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\fR\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Ldev/nettools/android/service/TransferProgressHolder;", "", "<init>", "()V", "_progress", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "", "Ldev/nettools/android/data/ssh/TransferProgress;", "progress", "Lkotlinx/coroutines/flow/StateFlow;", "getProgress", "()Lkotlinx/coroutines/flow/StateFlow;", "_activeJobs", "", "Ldev/nettools/android/domain/model/TransferJob;", "activeJobs", "getActiveJobs", "pendingQueue", "Ljava/util/concurrent/ConcurrentLinkedQueue;", "Ldev/nettools/android/service/PendingTransferParams;", "enqueue", "", "params", "dequeue", "updateProgress", "jobId", "p", "updateJobStatus", "status", "Ldev/nettools/android/domain/model/TransferStatus;", "removeJob", "app_release"})
public final class TransferProgressHolder {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.Map<java.lang.String, dev.nettools.android.data.ssh.TransferProgress>> _progress = null;
    
    /**
     * Per-job transfer progress keyed by job ID.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.Map<java.lang.String, dev.nettools.android.data.ssh.TransferProgress>> progress = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<dev.nettools.android.domain.model.TransferJob>> _activeJobs = null;
    
    /**
     * Currently queued or in-progress jobs.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<dev.nettools.android.domain.model.TransferJob>> activeJobs = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentLinkedQueue<dev.nettools.android.service.PendingTransferParams> pendingQueue = null;
    
    @javax.inject.Inject()
    public TransferProgressHolder() {
        super();
    }
    
    /**
     * Per-job transfer progress keyed by job ID.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.Map<java.lang.String, dev.nettools.android.data.ssh.TransferProgress>> getProgress() {
        return null;
    }
    
    /**
     * Currently queued or in-progress jobs.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<dev.nettools.android.domain.model.TransferJob>> getActiveJobs() {
        return null;
    }
    
    /**
     * Adds [params] to the pending queue and registers the job as active.
     */
    public final void enqueue(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.service.PendingTransferParams params) {
    }
    
    /**
     * Removes and returns the next pending transfer, or null if the queue is empty.
     */
    @org.jetbrains.annotations.Nullable()
    public final dev.nettools.android.service.PendingTransferParams dequeue() {
        return null;
    }
    
    /**
     * Updates the progress snapshot for a running job.
     */
    public final void updateProgress(@org.jetbrains.annotations.NotNull()
    java.lang.String jobId, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.ssh.TransferProgress p) {
    }
    
    /**
     * Updates the status of a job already in [activeJobs].
     */
    public final void updateJobStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String jobId, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferStatus status) {
    }
    
    /**
     * Removes the job and its progress entry from the active set.
     */
    public final void removeJob(@org.jetbrains.annotations.NotNull()
    java.lang.String jobId) {
    }
}