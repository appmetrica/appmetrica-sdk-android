package io.appmetrica.analytics.impl.crash.jvm.converter;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.crash.jvm.client.RegularError;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;

public class RegularErrorConverter implements ProtobufConverter<RegularError, CrashAndroid.Error> {

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

    public RegularErrorConverter() {
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
    RegularErrorConverter(@NonNull ThrowableConverter throwableConverter,
                          @NonNull AllThreadsConverter allThreadsConverter,
                          @NonNull CrashOptionalBoolConverter optionalBoolConverter,
                          @NonNull StackTraceConverter stackTraceConverter,
                          @NonNull PlatformConverter platformConverter,
                          @NonNull PluginEnvironmentConverter pluginEnvironmentConverter) {
        this.throwableConverter = throwableConverter;
        this.allThreadsConverter = allThreadsConverter;
        this.optionalBoolConverter = optionalBoolConverter;
        this.stackTraceConverter = stackTraceConverter;
        this.platformConverter = platformConverter;
        this.pluginEnvironmentConverter = pluginEnvironmentConverter;
    }

    @NonNull
    @Override
    public CrashAndroid.Error fromModel(@NonNull RegularError value) {
        CrashAndroid.Error error = new CrashAndroid.Error();
        error.message = StringUtils.correctIllFormedString(WrapUtils.getOrDefault(value.message, error.message));
        UnhandledException unhandledException = value.exception;
        if (unhandledException != null) {
            if (unhandledException.exception != null) {
                error.throwable = throwableConverter.fromModel(unhandledException.exception);
            }
            if (unhandledException.allThreads != null) {
                error.threads = allThreadsConverter.fromModel(unhandledException.allThreads);
            }
            if (unhandledException.methodCallStacktrace != null) {
                error.methodCallStacktrace = stackTraceConverter.fromModel(unhandledException.methodCallStacktrace);
            }
            error.buildId = WrapUtils.getOrDefault(unhandledException.buildId, error.buildId);
            error.isOffline = optionalBoolConverter.toProto(unhandledException.isOffline);
            if (!TextUtils.isEmpty(unhandledException.platform)) {
                error.virtualMachine = platformConverter.fromModel(unhandledException.platform);
            }
            if (!TextUtils.isEmpty(unhandledException.virtualMachineVersion)) {
                error.virtualMachineVersion = unhandledException.virtualMachineVersion.getBytes();
            }
            if (!Utils.isNullOrEmpty(unhandledException.pluginEnvironment)) {
                error.pluginEnvironment = pluginEnvironmentConverter.fromModel(unhandledException.pluginEnvironment);
            }
        }
        return error;
    }

    @NonNull
    @Override
    public RegularError toModel(@NonNull CrashAndroid.Error nano) {
        throw new UnsupportedOperationException();
    }
}
