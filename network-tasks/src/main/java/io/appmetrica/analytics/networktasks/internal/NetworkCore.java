package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.networktasks.impl.NetworkTaskRunnable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkCore extends InterruptionSafeThread {

    private static final String TAG = "[NetworkCore]";

    private final BlockingQueue<QueueTaskEntry> mTasksQueue = new LinkedBlockingQueue<QueueTaskEntry>();
    private final Object mAddTaskLock = new Object();
    private final Object mStopTasksLock = new Object();

    private volatile QueueTaskEntry mCurrentTask;
    @NonNull
    private final NetworkTaskRunnable.Provider runnableProvider;
    @NonNull
    private final IExecutionPolicy executionPolicy;

    public NetworkCore(@NonNull IExecutionPolicy executionPolicy) {
        this(executionPolicy, new NetworkTaskRunnable.Provider());
    }

    @VisibleForTesting
    NetworkCore(
        @NonNull IExecutionPolicy executionPolicy,
        @NonNull NetworkTaskRunnable.Provider runnableProvider
    ) {
        this.executionPolicy = executionPolicy;
        this.runnableProvider = runnableProvider;
    }

    public void startTask(final NetworkTask networkTask) {
        DebugLogger.INSTANCE.info(TAG, "Try to start task %s", networkTask.description());
        if (!executionPolicy.canBeExecuted()) {
            DebugLogger.INSTANCE.info(
                TAG,
                "Task %s cannot be executed due to execution policy.",
                networkTask.description()
            );
            return;
        }
        synchronized (mAddTaskLock) {
            final QueueTaskEntry taskEntry = new QueueTaskEntry(networkTask);

            // This is the only place where tasks are added, so if containsTaskEntry is false,
            // it will be false at the point of mTasksQueue.offer(taskEntry) call.
            if (isRunning() && !containsTaskEntry(taskEntry)) {
                if (taskEntry.networkTask.onTaskAdded()) {
                    final boolean isOfferOK = mTasksQueue.offer(taskEntry);
                    DebugLogger.INSTANCE.info(
                        TAG,
                        "Task was added: %s, desc: %s",
                        isOfferOK,
                        networkTask.description()
                    );
                } else {
                    DebugLogger.INSTANCE.info(
                        TAG,
                        "Task %s was not added because its state is inconsistent",
                        networkTask.description());
                }
            } else {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "Task %s was not added because there is another entry. Current task: %s, queue size: %d",
                    networkTask.description(),
                    mCurrentTask,
                    mTasksQueue.size()
                );
            }
        }
    }

    public void stopTasks() {
        synchronized (mStopTasksLock) {
            DebugLogger.INSTANCE.info(TAG, "NetworkCore shall stop");
            List<QueueTaskEntry> queueTaskEntries = new ArrayList<QueueTaskEntry>(mTasksQueue.size());
            mTasksQueue.drainTo(queueTaskEntries);
            DebugLogger.INSTANCE.info(TAG, "Remove %d tasks from queue", queueTaskEntries.size());
            for (QueueTaskEntry queueTaskEntry : queueTaskEntries) {
                queueTaskEntry.networkTask.onTaskRemoved();
            }
        }
    }

    @Override
    public void run() {
        DebugLogger.INSTANCE.info(TAG, "Starting tasks processing ...");

        NetworkTask networkTask = null;
        while (isRunning()) {
            try {
                DebugLogger.INSTANCE.info(TAG, "Getting task ...");
                synchronized (mStopTasksLock) {}
                mCurrentTask = mTasksQueue.take();

                networkTask = mCurrentTask.networkTask;
                //Warning! OkHTTP can swallow InterruptedException and throw InterruptedIOException instead.
                networkTask.getExecutor().execute(runnableProvider.create(networkTask, this));

            } catch (InterruptedException ex) {
                DebugLogger.INSTANCE.error(TAG, ex);
            } finally {
                if (networkTask != null) {
                    networkTask.onTaskFinished();
                    synchronized (mStopTasksLock) {
                        mCurrentTask = null;
                    }
                    networkTask.onTaskRemoved();
                } else {
                    DebugLogger.INSTANCE.warning(TAG, "Network task is null");
                }
            }
        }

        DebugLogger.INSTANCE.info(TAG, "Stopping tasks processing ...");
    }

    private boolean containsTaskEntry(QueueTaskEntry entry) {
        return mTasksQueue.contains(entry) || entry.equals(mCurrentTask);
    }

    private static class QueueTaskEntry {

        @NonNull final NetworkTask networkTask;

        @NonNull
        private final String taskDescription;

        private QueueTaskEntry(@NonNull NetworkTask networkTask) {
            this.networkTask = networkTask;
            this.taskDescription = networkTask.description();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final QueueTaskEntry that = (QueueTaskEntry) o;

            return taskDescription.equals(that.taskDescription);
        }

        @Override
        public int hashCode() {
            return taskDescription.hashCode();
        }

    }
}
