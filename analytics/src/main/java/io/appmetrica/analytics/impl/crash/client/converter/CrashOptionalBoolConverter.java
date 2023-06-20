package io.appmetrica.analytics.impl.crash.client.converter;

import io.appmetrica.analytics.impl.OptionalBoolConverter;

import static io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid.OPTIONAL_BOOL_FALSE;
import static io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid.OPTIONAL_BOOL_TRUE;
import static io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid.OPTIONAL_BOOL_UNDEFINED;

public class CrashOptionalBoolConverter extends OptionalBoolConverter {

    public CrashOptionalBoolConverter() {
        super(OPTIONAL_BOOL_UNDEFINED, OPTIONAL_BOOL_FALSE, OPTIONAL_BOOL_TRUE);
    }
}
