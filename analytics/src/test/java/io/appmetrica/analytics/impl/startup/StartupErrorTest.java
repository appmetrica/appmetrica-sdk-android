package io.appmetrica.analytics.impl.startup;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class StartupErrorTest extends CommonTest {

    private static final String KEY_CODE = "startup_error_key_code";

    @RunWith(RobolectricTestRunner.class)
    public static class NullTest {

        @Test
        public void test() {
            assertThat(StartupError.fromBundle(new Bundle())).isNull();
        }
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "for {0} should be {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {0, StartupError.UNKNOWN},
            {1, StartupError.NETWORK},
            {2, StartupError.PARSE}
        });
    }

    private final int mCode;
    @NonNull
    private final StartupError mError;

    public StartupErrorTest(final int code, @NonNull StartupError error) {
        mCode = code;
        mError = error;
    }

    @Test
    public void testFromBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_CODE, mCode);
        assertThat(StartupError.fromBundle(bundle)).isEqualTo(mError);
    }

    @Test
    public void testToBundle() {
        Bundle bundle = new Bundle();
        mError.toBundle(bundle);
        assertThat(bundle.getInt(KEY_CODE)).isEqualTo(mCode);
    }
}
