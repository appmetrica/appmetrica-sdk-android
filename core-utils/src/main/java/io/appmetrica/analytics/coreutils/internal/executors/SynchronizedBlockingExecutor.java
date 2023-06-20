package io.appmetrica.analytics.coreutils.internal.executors;

public class SynchronizedBlockingExecutor extends BlockingExecutor {

    @Override
    public synchronized void execute(final Runnable command) {
        super.execute(command);
    }

}
