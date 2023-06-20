package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
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

    public NetworkCore() {
        this(new NetworkTaskRunnable.Provider());
    }

    @VisibleForTesting
    NetworkCore(@NonNull NetworkTaskRunnable.Provider runnableProvider) {
        this.runnableProvider = runnableProvider;
    }

    public void startTask(final NetworkTask networkTask) {
        YLogger.debug(TAG, "Try to start task %s", networkTask.description());
        synchronized (mAddTaskLock) {
            final QueueTaskEntry taskEntry = new QueueTaskEntry(networkTask);

            // This is the only place where tasks are added, so if containsTaskEntry is false,
            // it will be false at the point of mTasksQueue.offer(taskEntry) call.
            if (isRunning() && !containsTaskEntry(taskEntry)) {
                if (taskEntry.networkTask.onTaskAdded()) {
                    final boolean isOfferOK = mTasksQueue.offer(taskEntry);
                    YLogger.info(TAG, "Task was added: %s, desc: %s", isOfferOK, networkTask.description());
                } else {
                    YLogger.info(TAG, "Task %s was not added because its state is inconsistent",
                            networkTask.description());
                }
            } else {
                YLogger.info(TAG,
                        "Task %s was not added because there is another entry. " +
                                "Current task: %s, queue size: %d",
                        networkTask.description(),
                        mCurrentTask,
                        mTasksQueue.size()
                );
            }
        }
    }

    public void onDestroy() {
        synchronized (mStopTasksLock) {
            YLogger.info(TAG, "NetworkCore shall stop");
            QueueTaskEntry entry = mCurrentTask;
            if (entry != null) {
                entry.networkTask.onTaskRemoved();
            }
            List<QueueTaskEntry> queueTaskEntries = new ArrayList<QueueTaskEntry>(mTasksQueue.size());
            mTasksQueue.drainTo(queueTaskEntries);
            YLogger.info(TAG, "Remove %d tasks from queue", queueTaskEntries.size());
            for (QueueTaskEntry queueTaskEntry : queueTaskEntries) {
                queueTaskEntry.networkTask.onTaskRemoved();
            }
        }
    }

    @Override
    public void run() {
        YLogger.info(TAG, "Starting tasks processing ...");

        NetworkTask networkTask = null;
        while (isRunning()) {
            try {
                YLogger.info(TAG, "Getting task ...");
                synchronized (mStopTasksLock) {}
                mCurrentTask = mTasksQueue.take();

                networkTask = mCurrentTask.networkTask;
                //Warning! OkHTTP can swallow InterruptedException and throw InterruptedIOException instead.
                networkTask.getExecutor().execute(runnableProvider.create(networkTask, this));

            } catch (InterruptedException ex) {
                YLogger.error(TAG, ex);
            } finally {
                synchronized (mStopTasksLock) {
                    mCurrentTask = null;
                    if (networkTask != null) {
                        networkTask.onTaskFinished();
                        networkTask.onTaskRemoved();
                    } else {
                        YLogger.warning(TAG, "Network task is null");
                    }
                }
            }
        }

        YLogger.info(TAG, "Stopping tasks processing ...");
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
