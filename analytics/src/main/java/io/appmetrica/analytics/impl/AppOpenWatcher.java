package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.ArrayList;
import java.util.List;

public class AppOpenWatcher implements ActivityLifecycleListener {

    private static final String TAG = "[AppOpenWatcher]";

    @NonNull
    private final List<NonNullConsumer<IMainReporter>> savedCommands =
        new ArrayList<NonNullConsumer<IMainReporter>>();
    @Nullable
    private volatile IMainReporter reporter = null;

    public void startWatching() {
        DebugLogger.INSTANCE.info(TAG, "Start watching app opens");
        ClientServiceLocator.getInstance().getActivityLifecycleManager().registerListener(
            this,
            ActivityEvent.CREATED
        );
    }

    public void stopWatching() {
        DebugLogger.INSTANCE.info(TAG, "Stop watching app opens");
        ClientServiceLocator.getInstance().getActivityLifecycleManager().unregisterListener(
            this,
            ActivityEvent.CREATED
        );
    }

    @ApiProxyThread
    public void setReporter(@NonNull IMainReporter reporter) {
        DebugLogger.INSTANCE.info(TAG, "setReporter to %s", reporter);
        List<NonNullConsumer<IMainReporter>> commandsToExecute;
        synchronized (this) {
            this.reporter = reporter;
            commandsToExecute = getCommandsToExecute();
        }
        for (NonNullConsumer<IMainReporter> command : commandsToExecute) {
            command.consume(reporter);
        }
    }

    @NonNull
    private synchronized List<NonNullConsumer<IMainReporter>> getCommandsToExecute() {
        List<NonNullConsumer<IMainReporter>> commands =
            new ArrayList<NonNullConsumer<IMainReporter>>(savedCommands);
        savedCommands.clear();
        return commands;
    }

    @MainThread
    private synchronized void addOrExecuteCommand(@NonNull final NonNullConsumer<IMainReporter> command) {
        final IMainReporter reporterCopy = reporter;
        DebugLogger.INSTANCE.info(TAG, "addOrExecuteCommand. Reporter: %s", reporterCopy);
        if (reporterCopy == null) {
            savedCommands.add(command);
        } else {
            ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor().post(new Runnable() {
                @Override
                public void run() {
                    command.consume(reporterCopy);
                }
            });
        }
    }

    @Override
    @MainThread
    public void onEvent(@NonNull Activity activity, @NonNull ActivityEvent event) {
        DebugLogger.INSTANCE.info(TAG, "onEvent %s for activity %s", event, activity);
        final Intent intent = SystemServiceUtils.accessSystemServiceSafely(
            activity,
            "getting intent",
            "activity",
            new FunctionWithThrowable<Activity, Intent>() {
                @Override
                public Intent apply(@NonNull Activity input) throws Throwable {
                    return input.getIntent();
                }
            }
        );
        final String deeplink = intent == null ? null : intent.getDataString();
        if (!StringUtils.isNullOrEmpty(deeplink)) {
            addOrExecuteCommand(new NonNullConsumer<IMainReporter>() {
                @Override
                @ApiProxyThread
                public void consume(@NonNull IMainReporter reporter) {
                    DebugLogger.INSTANCE.info(TAG, "Reporting app open: %s", deeplink);
                    reporter.reportAutoAppOpen(deeplink);
                }
            });
        }
    }
}
