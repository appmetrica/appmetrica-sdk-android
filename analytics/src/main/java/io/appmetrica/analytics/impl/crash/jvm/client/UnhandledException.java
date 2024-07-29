package io.appmetrica.analytics.impl.crash.jvm.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import java.util.List;
import java.util.Map;

public class UnhandledException {

    @Nullable
    public final ThrowableModel exception;
    @Nullable
    public final AllThreads allThreads;
    @Nullable
    public final List<StackTraceItemInternal> methodCallStacktrace;
    @Nullable
    public final String platform;
    @Nullable
    public final String virtualMachineVersion;
    @Nullable
    public final Map<String, String> pluginEnvironment;
    @Nullable
    public final String buildId;
    @Nullable
    public final Boolean isOffline;

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public UnhandledException(@Nullable ThrowableModel exception,
                              @Nullable AllThreads allThreads,
                              @Nullable List<StackTraceItemInternal> methodCallStacktrace,
                              @Nullable String platform,
                              @Nullable String virtualMachineVersion,
                              @Nullable Map<String, String> pluginEnvironment,
                              @Nullable String buildId,
                              @Nullable Boolean isOffline) {
        this.exception = exception;
        this.allThreads = allThreads;
        this.methodCallStacktrace = methodCallStacktrace;
        this.platform = platform;
        this.virtualMachineVersion = virtualMachineVersion;
        this.pluginEnvironment = pluginEnvironment;
        this.buildId = buildId;
        this.isOffline = isOffline;
    }

    @NonNull
    public static String getErrorName(@NonNull UnhandledException unhandledException) {
        ThrowableModel exception = unhandledException.exception;
        if (exception == null) {
            return StringUtils.EMPTY;
        }
        return WrapUtils.getOrDefault(exception.getExceptionClass(), StringUtils.EMPTY);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (exception != null) {
            for (StackTraceItemInternal element : exception.getStacktrace()) {
                builder.append("at " + element.getClassName() + "." + element.getMethodName() +
                        "(" + element.getFileName() + ":" + element.getLine() + ":" + element.getColumn() + ")\n");
            }
        }
        return "UnhandledException{" +
                "exception=" + exception + "\n" + builder.toString() +
                '}';
    }
}
