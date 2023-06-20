package io.appmetrica.analytics.impl.crash.client.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashHandlerDescription;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashModel;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashSource;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import java.util.EnumMap;

public class NativeCrashConverter implements ProtobufConverter<NativeCrashModel, CrashAndroid.Crash> {

    private static final EnumMap<NativeCrashSource, Integer> handlerMapping;

    static {
        handlerMapping = new EnumMap<>(NativeCrashSource.class);
        handlerMapping.put(NativeCrashSource.UNKNOWN, CrashAndroid.UNKNOWN);
        handlerMapping.put(NativeCrashSource.CRASHPAD, CrashAndroid.CRASHPAD);
    }

    @NonNull
    @Override
    public CrashAndroid.Crash fromModel(@NonNull NativeCrashModel value) {
        CrashAndroid.Crash crash = new CrashAndroid.Crash();
        crash.type = CrashAndroid.Crash.NATIVE;
        crash.native_ = new CrashAndroid.Crash.NativeCrash();
        crash.native_.nativeCrashPayload = value.getData();
        NativeCrashHandlerDescription description = value.getHandlerDescription();
        crash.native_.handler = new CrashAndroid.NativeCrashHandler();
        Integer library = handlerMapping.get(description.getSource());
        if (library != null) {
            crash.native_.handler.source = library;
        }
        crash.native_.handler.version = WrapUtils.getOrDefault(description.getHandlerVersion(), "");
        return crash;
    }

    @NonNull
    @Override
    public NativeCrashModel toModel(@NonNull CrashAndroid.Crash nano) {
        throw new UnsupportedOperationException();
    }
}
