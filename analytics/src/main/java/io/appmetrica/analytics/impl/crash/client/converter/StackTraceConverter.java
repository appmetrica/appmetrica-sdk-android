package io.appmetrica.analytics.impl.crash.client.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ListConverter;
import io.appmetrica.analytics.impl.crash.client.StackTraceItemInternal;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import java.util.List;

public class StackTraceConverter implements ListConverter<StackTraceItemInternal, CrashAndroid.StackTraceElement> {

    @NonNull
    private StackTraceElementConverter elementConverter = new StackTraceElementConverter();

    @NonNull
    @Override
    public CrashAndroid.StackTraceElement[] fromModel(@NonNull List<StackTraceItemInternal> value) {
        CrashAndroid.StackTraceElement[] outState = new CrashAndroid.StackTraceElement[value.size()];
        int i = 0;
        for (StackTraceItemInternal element : value) {
            outState[i] = elementConverter.fromModel(element);
            i++;
        }
        return outState;
    }

    @NonNull
    @Override
    public List<StackTraceItemInternal> toModel(CrashAndroid.StackTraceElement[] nano) {
        throw new UnsupportedOperationException();
    }
}
