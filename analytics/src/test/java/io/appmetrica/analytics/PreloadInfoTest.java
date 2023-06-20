package io.appmetrica.analytics;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PreloadInfoTest extends CommonTest {

    private static final String TRACKING_ID = "test_tracking_id";
    private static final Map<String, String> ADDITIONAL_INFO = new HashMap<String, String>() {
        {
            put("test_key_1", "test_value_1");
            put("test_key_2", "test_value_2");
            put("test_key_3", "test_value_3");
        }
    };

    private PreloadInfo.Builder mPreloadInfoBuilder;
    private PreloadInfo mPreloadInfo;

    @Before
    public void setUp() {
        mPreloadInfoBuilder = getDefaultPreloadInfoBuilder();
        mPreloadInfo = getDefaultPreloadInfo();
    }

    @Test
    public void testGetTrackingIdReturnValueFromInitialization() {
        assertThat(mPreloadInfo.getTrackingId()).isEqualTo(TRACKING_ID);
    }

    @Test
    public void testGetAdditionalInfoReturnValueFromInitialization() {
        for (Map.Entry<String, String> entry : ADDITIONAL_INFO.entrySet()) {
            mPreloadInfoBuilder.setAdditionalParams(entry.getKey(), entry.getValue());
        }
        PreloadInfo preloadInfo = mPreloadInfoBuilder.build();
        for (Map.Entry<String, String> entry : preloadInfo.getAdditionalParams().entrySet()) {
            assertThat(ADDITIONAL_INFO.containsKey(entry.getKey())).isTrue();
            assertThat(ADDITIONAL_INFO.get(entry.getKey())).isEqualTo(entry.getValue());
        }
    }

    @Test
    public void testShouldNotAddAdditionalInfoIfKeyIsNull() {
        PreloadInfo preloadInfo = mPreloadInfoBuilder.setAdditionalParams(null, "test string").build();
        assertThat(preloadInfo.getAdditionalParams()).isEmpty();
    }

    @Test
    public void testShouldNotAddAdditionalInfoIfValueIsNull() {
        PreloadInfo preloadInfo = mPreloadInfoBuilder.setAdditionalParams("test string", null).build();
        assertThat(preloadInfo.getAdditionalParams()).isEmpty();
    }

    @Test
    public void testGetAdditionalInfoReturnEmptyMapIfNoDef() {
        assertThat(mPreloadInfo.getAdditionalParams()).isEmpty();
    }

    private PreloadInfo getDefaultPreloadInfo() {
        return getDefaultPreloadInfoBuilder().build();
    }

    private PreloadInfo.Builder getDefaultPreloadInfoBuilder() {
        PreloadInfo.Builder builder = PreloadInfo.newBuilder(TRACKING_ID);
        return builder;
    }
}
