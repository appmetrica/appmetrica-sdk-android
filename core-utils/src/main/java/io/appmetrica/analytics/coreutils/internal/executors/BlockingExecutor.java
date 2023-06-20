package io.appmetrica.analytics.coreutils.internal.executors;

import java.util.concurrent.Executor;

public class BlockingExecutor implements Executor {

    @Override
    public void execute(final Runnable command) {
        if (command != null) {
            command.run();
        }
    }

}
