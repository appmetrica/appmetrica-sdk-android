package io.appmetrica.analytics.impl.crash;

import android.util.SparseArray;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.InternalEvents;

public class MetaInformation {

    private interface CrashSource {

        String JVM = "jvm";
        String JNI_NATIVE = "jni_native";

    }

    private interface DeliveryMethod {

        String FILE = "file";
        String INTENT = "intent";
        String BINDER = "binder";

    }

    public final String type;
    public final String deliveryMethod;

    private static SparseArray<MetaInformation> META_INFORMATION_MAPPING;

    static {
        META_INFORMATION_MAPPING = new SparseArray<MetaInformation>();
        META_INFORMATION_MAPPING.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF.getTypeId(),
                new MetaInformation(CrashSource.JVM, DeliveryMethod.BINDER)
        );
        META_INFORMATION_MAPPING.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT.getTypeId(),
                new MetaInformation(CrashSource.JVM, DeliveryMethod.INTENT)
        );
        META_INFORMATION_MAPPING.put(
                InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE.getTypeId(),
                new MetaInformation(CrashSource.JVM, DeliveryMethod.FILE)
        );
        META_INFORMATION_MAPPING.put(
                InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF.getTypeId(),
                new MetaInformation(CrashSource.JNI_NATIVE, DeliveryMethod.FILE)
        );
        META_INFORMATION_MAPPING.put(
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF.getTypeId(),
            new MetaInformation(CrashSource.JNI_NATIVE, DeliveryMethod.FILE)
        );
    }

    private MetaInformation(@NonNull String type, @NonNull String deliveryMethod) {
        this.type = type;
        this.deliveryMethod = deliveryMethod;
    }

    public static MetaInformation getMetaInformation(int event) {
        return META_INFORMATION_MAPPING.get(event);
    }

}
