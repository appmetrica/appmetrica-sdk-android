package io.appmetrica.analytics.impl.crash.jvm.converter;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class CrashOptionalBoolConverterTest extends CommonTest {

    @Parameters(name = "state: {0}, model: {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {true, CrashAndroid.OPTIONAL_BOOL_TRUE},
            {false, CrashAndroid.OPTIONAL_BOOL_FALSE},
            {null, CrashAndroid.OPTIONAL_BOOL_UNDEFINED}
        });
    }

    @Nullable
    private final Boolean mState;
    private final int mProto;
    private final CrashOptionalBoolConverter mOptionalBoolConverter;

    public CrashOptionalBoolConverterTest(@Nullable Boolean state, int proto) {
        mState = state;
        mProto = proto;
        mOptionalBoolConverter = new CrashOptionalBoolConverter();
    }

    @Test
    public void test() {
        assertThat(mOptionalBoolConverter.toModel(mProto)).isEqualTo(mState);
        assertThat(mOptionalBoolConverter.toProto(mState)).isEqualTo(mProto);
    }
}
