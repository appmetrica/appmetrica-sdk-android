package io.appmetrica.analytics.impl.crash.ndk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.BiFunction;
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.crash.client.converter.NativeCrashConverter;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.io.File;

public final class NativeDumpHandler implements BiFunction<File, NativeCrashHandlerDescription, String> {

    @NonNull
    private final NativeCrashConverter nativeCrashConverter;

    public NativeDumpHandler() {
        this(new NativeCrashConverter());
    }

    @VisibleForTesting
    public NativeDumpHandler(@NonNull NativeCrashConverter nativeCrashConverter) {
        this.nativeCrashConverter = nativeCrashConverter;
    }

    @Override
    @Nullable
    public String apply(@NonNull File file, @NonNull NativeCrashHandlerDescription data) {
        try {
            final byte[] dumpDescription = IOUtils.readAll(file.getAbsolutePath());

            if (Utils.isNullOrEmpty(dumpDescription) == false) {
                String nativeCrash = Base64Utils.compressBase64(
                        MessageNano.toByteArray(
                                //todo(avitenko) dump required fields with native crash
                                nativeCrashConverter.fromModel(
                                        new NativeCrashModel(dumpDescription, data)
                                )
                        )
                );
                return nativeCrash;
            }
        } catch (Throwable ignored) { }
        return null;
    }
}
