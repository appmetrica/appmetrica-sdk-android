package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityUtilsTests extends CommonTest {

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class EncodingStringTests extends CommonTest {
        private Context mContext;

        private String mInputString;
        private boolean mShouldFail;

        @ParameterizedRobolectricTestRunner.Parameters(name = "Encode/decode input string = {0}; shouldFail = {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"", false},
                    {null, true},
                    {"\"json_key\":\"json_value\"", false},
                    {"Any random string", false},
                    {"1@#$%^&*()_+::<?>?{L}", false}
            });
        }

        public EncodingStringTests(String inputString, boolean shouldFail) {
            mInputString = inputString;
            mShouldFail = shouldFail;
        }

        @Before
        public void setUp() {
            mContext = RuntimeEnvironment.getApplication();
        }

        @Test
        public void testEncodeDecodeValue() {
            try {
                String encodedString = SecurityUtils.encode(mContext, mInputString);
                String decodedString = SecurityUtils.decode(mContext, encodedString);
                assertThat(decodedString).isEqualTo(mInputString);
            } catch (Exception e) {
                assertThat(mShouldFail).isTrue();
            }
        }
    }
}
