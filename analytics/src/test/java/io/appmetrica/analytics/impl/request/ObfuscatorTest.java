package io.appmetrica.analytics.impl.request;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ObfuscatorTest extends CommonTest {

    private final String mFullValue;
    private final String mObfuscatedValue;

    private Obfuscator mObfuscator = new Obfuscator();

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0} is obfuscated into {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{"google_aid", "g"},
                new Object[]{"huawei_oaid", "h"},
                new Object[]{"sim_info", "si"},
                new Object[]{"features_collecting", "fc"},
                new Object[]{"permissions_collecting", "pc"},
                new Object[]{"retry_policy", "rp"},
                new Object[]{"not_to_be_obfuscated", "not_to_be_obfuscated"},
                new Object[] {"cache_control", "cc"},
                new Object[] {"auto_inapp_collecting", "aic"},
                new Object[] {"attribution", "at"},
                new Object[] {"startup_update", "su"},
                new Object[] {"ssl_pinning", "sp"}
        );
    }

    public ObfuscatorTest(String full, String obfuscated) {
        mFullValue = full;
        mObfuscatedValue = obfuscated;
    }

    @Test
    public void test() {
        assertThat(mObfuscator.obfuscate(mFullValue)).isEqualTo(mObfuscatedValue);
    }
}
