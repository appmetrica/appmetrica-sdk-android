package io.appmetrica.analytics.impl.crash.jvm.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class UnhandledExceptionConverter implements ProtobufConverter<UnhandledException, CrashAndroid.Crash> {

    private static final String TAG = "[UnhandledExceptionConverter]";

    @NonNull
    private final ThrowableConverter throwableConverter;
    @NonNull
    private final AllThreadsConverter allThreadsConverter;
    @NonNull
    private final CrashOptionalBoolConverter optionalBoolConverter;
    @NonNull
    private final StackTraceConverter stackTraceConverter;
    @NonNull
    private final PlatformConverter platformConverter;
    @NonNull
    private final PluginEnvironmentConverter pluginEnvironmentConverter;

    public UnhandledExceptionConverter() {
        this(
                new ThrowableConverter(),
                new AllThreadsConverter(new ThreadStateConverter()),
                new CrashOptionalBoolConverter(),
                new StackTraceConverter(),
                new PlatformConverter(),
                new PluginEnvironmentConverter()
        );
    }

    @VisibleForTesting
    UnhandledExceptionConverter(@NonNull ThrowableConverter throwableConverter,
                                @NonNull AllThreadsConverter allThreadsConverter,
                                @NonNull CrashOptionalBoolConverter optionalBoolConverter,
                                @NonNull StackTraceConverter stackTraceConverter,
                                @NonNull PlatformConverter platformConverter,
                                @NonNull PluginEnvironmentConverter pluginEnvironmentConverter) {
        this.allThreadsConverter = allThreadsConverter;
        this.throwableConverter = throwableConverter;
        this.optionalBoolConverter = optionalBoolConverter;
        this.stackTraceConverter = stackTraceConverter;
        this.platformConverter = platformConverter;
        this.pluginEnvironmentConverter = pluginEnvironmentConverter;
    }

    @NonNull
    @Override
    public CrashAndroid.Crash fromModel(@NonNull UnhandledException value) {
        CrashAndroid.Crash outCrash = new CrashAndroid.Crash();
        if (value.exception != null) {
            outCrash.throwable = throwableConverter.fromModel(value.exception);
        }
        if (value.allThreads != null) {
            outCrash.threads = allThreadsConverter.fromModel(value.allThreads);
        }
        if (value.methodCallStacktrace != null) {
            outCrash.methodCallStacktrace = stackTraceConverter.fromModel(value.methodCallStacktrace);
        }
        DebugLogger.INSTANCE.info(TAG, "Convert build_id: %s", value.buildId);
        if (value.buildId != null) {
            outCrash.buildId = value.buildId;
        }
        outCrash.isOffline = optionalBoolConverter.toProto(value.isOffline);
        if (!StringUtils.isNullOrEmpty(value.platform)) {
            outCrash.virtualMachine = platformConverter.fromModel(value.platform);
        }
        if (!StringUtils.isNullOrEmpty(value.virtualMachineVersion)) {
            outCrash.virtualMachineVersion = value.virtualMachineVersion.getBytes();
        }
        if (!Utils.isNullOrEmpty(value.pluginEnvironment)) {
            outCrash.pluginEnvironment = pluginEnvironmentConverter.fromModel(value.pluginEnvironment);
        }
        return outCrash;
    }

    @NonNull
    @Override
    public UnhandledException toModel(@NonNull CrashAndroid.Crash nano) {
        throw new UnsupportedOperationException();
    }
}
