package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener;
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.ArrayList;
import java.util.List;

public class AppOpenWatcher implements ActivityLifecycleListener {

    private static final String TAG = "[AppOpenWatcher]";

    @NonNull
    private final List<NonNullConsumer<DeeplinkConsumer>> savedCommands =
        new ArrayList<NonNullConsumer<DeeplinkConsumer>>();
    @Nullable
    private volatile DeeplinkConsumer deeplinkConsumer = null;

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
    public void setDeeplinkConsumer(@NonNull DeeplinkConsumer deeplinkConsumer) {
        DebugLogger.INSTANCE.info(TAG, "setReporter to %s", deeplinkConsumer);
        List<NonNullConsumer<DeeplinkConsumer>> commandsToExecute;
        synchronized (this) {
            this.deeplinkConsumer = deeplinkConsumer;
            commandsToExecute = getCommandsToExecute();
        }
        for (NonNullConsumer<DeeplinkConsumer> command : commandsToExecute) {
            command.consume(deeplinkConsumer);
        }
    }

    @NonNull
    private synchronized List<NonNullConsumer<DeeplinkConsumer>> getCommandsToExecute() {
        List<NonNullConsumer<DeeplinkConsumer>> commands =
            new ArrayList<NonNullConsumer<DeeplinkConsumer>>(savedCommands);
        savedCommands.clear();
        return commands;
    }

    @MainThread
    private synchronized void addOrExecuteCommand(@NonNull final NonNullConsumer<DeeplinkConsumer> command) {
        final DeeplinkConsumer deeplinkConsumerCopy = deeplinkConsumer;
        DebugLogger.INSTANCE.info(TAG, "addOrExecuteCommand. Deeplink consumer: %s", deeplinkConsumerCopy);
        if (deeplinkConsumerCopy == null) {
            savedCommands.add(command);
        } else {
            ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    command.consume(deeplinkConsumerCopy);
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
        if (TextUtils.isEmpty(deeplink) == false) {
            addOrExecuteCommand(new NonNullConsumer<DeeplinkConsumer>() {
                @Override
                @ApiProxyThread
                public void consume(@NonNull DeeplinkConsumer deeplinkConsumer) {
                    DebugLogger.INSTANCE.info(TAG, "Reporting app open: %s", deeplink);
                    deeplinkConsumer.reportAutoAppOpen(deeplink);
                }
            });
        }
    }
}
