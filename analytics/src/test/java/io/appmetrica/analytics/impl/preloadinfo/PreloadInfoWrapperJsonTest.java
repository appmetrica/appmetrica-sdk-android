package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.PreloadInfo;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.mockito.Mockito.mock;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class PreloadInfoWrapperJsonTest extends CommonTest {

    private static final String KEY_PRELOAD_INFO = "preloadInfo";
    private static final String KEY_TRACKING_ID = "trackingId";
    private static final String KEY_ADDITIONAL_PARAMS = "additionalParams";
    private static final String KEY_WAS_SET = "wasSet";
    private static final String KEY_AUTO_TRACKING = "autoTracking";
    private static final String KEY_SOURCE = "source";

    private static final String TRACKING_ID = "test_tracking_id";
    private static final String ADDITIONAL_INFO_KEY = "additional_info_key";
    private static final String ADDITIONAL_INFO_VALUE = "addition_info_value";
    private static final String ADDITIONAL_INFO_KEY_SECOND = "additional_info_key_second";
    private static final String ADDITIONAL_INFO_VALUE_SECOND = "addition_info_value_second";

    @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
    public static Collection<Object[]> data() throws JSONException {
        final JSONObject baseJson = new JSONObject().put("key", "value");
        return Arrays.asList(new Object[][]{
                {null, false, baseJson},
                {null, true, baseJson},
                {PreloadInfo.newBuilder(null).setAdditionalParams(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE).build(), false, baseJson},
                {PreloadInfo.newBuilder(null).setAdditionalParams(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE).build(), true, baseJson},
                {PreloadInfo.newBuilder(null).build(), false, baseJson},
                {PreloadInfo.newBuilder(null).build(), true, baseJson},
                {PreloadInfo.newBuilder("").setAdditionalParams(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE).build(), false, baseJson},
                {PreloadInfo.newBuilder("").setAdditionalParams(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE).build(), true, baseJson},
                {PreloadInfo.newBuilder("").build(), false, baseJson},
                {PreloadInfo.newBuilder("").build(), true, baseJson},
                {
                    PreloadInfo.newBuilder(TRACKING_ID).build(),
                        false,
                        new JSONObject(baseJson.toString()).put(KEY_PRELOAD_INFO, new JSONObject()
                                .put(KEY_TRACKING_ID, TRACKING_ID)
                                .put(KEY_ADDITIONAL_PARAMS, new JSONObject())
                                .put(KEY_WAS_SET, true)
                                .put(KEY_AUTO_TRACKING, false)
                                .put(KEY_SOURCE, DistributionSource.APP.getDescription())
                    )
                },
                {
                        PreloadInfo.newBuilder(TRACKING_ID).build(),
                        true,
                        new JSONObject(baseJson.toString()).put(KEY_PRELOAD_INFO, new JSONObject()
                                .put(KEY_TRACKING_ID, TRACKING_ID)
                                .put(KEY_ADDITIONAL_PARAMS, new JSONObject())
                                .put(KEY_WAS_SET, true)
                                .put(KEY_AUTO_TRACKING, true)
                                .put(KEY_SOURCE, DistributionSource.APP.getDescription())
                        )
                },
                {
                    PreloadInfo.newBuilder(TRACKING_ID).setAdditionalParams(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE).build(),
                        false,
                        new JSONObject(baseJson.toString()).put(
                                KEY_PRELOAD_INFO,
                                new JSONObject()
                                        .put(KEY_TRACKING_ID, TRACKING_ID)
                                        .put(KEY_WAS_SET, true)
                                        .put(KEY_AUTO_TRACKING, false)
                                        .put(KEY_ADDITIONAL_PARAMS, new JSONObject().put(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE))
                                        .put(KEY_SOURCE, DistributionSource.APP.getDescription())
                        )
                },
                {
                        PreloadInfo.newBuilder(TRACKING_ID).setAdditionalParams(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE).build(),
                        true,
                        new JSONObject(baseJson.toString()).put(
                                KEY_PRELOAD_INFO,
                                new JSONObject()
                                        .put(KEY_TRACKING_ID, TRACKING_ID)
                                        .put(KEY_WAS_SET, true)
                                        .put(KEY_AUTO_TRACKING, true)
                                        .put(KEY_ADDITIONAL_PARAMS, new JSONObject().put(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE))
                                        .put(KEY_SOURCE, DistributionSource.APP.getDescription())
                        )
                },
                {
                    PreloadInfo.newBuilder(TRACKING_ID)
                        .setAdditionalParams(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE)
                        .setAdditionalParams(ADDITIONAL_INFO_KEY_SECOND, ADDITIONAL_INFO_VALUE_SECOND)
                        .build(),
                        false,
                        new JSONObject(baseJson.toString()).put(
                                KEY_PRELOAD_INFO,
                                new JSONObject()
                                        .put(KEY_TRACKING_ID, TRACKING_ID)
                                        .put(KEY_WAS_SET, true)
                                        .put(KEY_AUTO_TRACKING, false)
                                        .put(KEY_ADDITIONAL_PARAMS, new JSONObject()
                                                .put(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE)
                                                .put(ADDITIONAL_INFO_KEY_SECOND, ADDITIONAL_INFO_VALUE_SECOND)
                                        )
                                        .put(KEY_SOURCE, DistributionSource.APP.getDescription())
                        )
                },
                {
                        PreloadInfo.newBuilder(TRACKING_ID)
                                .setAdditionalParams(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE)
                                .setAdditionalParams(ADDITIONAL_INFO_KEY_SECOND, ADDITIONAL_INFO_VALUE_SECOND)
                                .build(),
                        true,
                        new JSONObject(baseJson.toString()).put(
                                KEY_PRELOAD_INFO,
                                new JSONObject()
                                        .put(KEY_TRACKING_ID, TRACKING_ID)
                                        .put(KEY_WAS_SET, true)
                                        .put(KEY_AUTO_TRACKING, true)
                                        .put(KEY_ADDITIONAL_PARAMS, new JSONObject()
                                                .put(ADDITIONAL_INFO_KEY, ADDITIONAL_INFO_VALUE)
                                                .put(ADDITIONAL_INFO_KEY_SECOND, ADDITIONAL_INFO_VALUE_SECOND)
                                        )
                                        .put(KEY_SOURCE, DistributionSource.APP.getDescription())
                        )
                }
        });
    }

    @NonNull
    private final PreloadInfoWrapper mPreloadInfoWrapper;
    @NonNull
    private final JSONObject mExpectedJson;
    @NonNull
    private final JSONObject mBaseJson;

    public PreloadInfoWrapperJsonTest(@Nullable PreloadInfo preloadInfo,
                                      boolean autoTracking,
                                      @NonNull JSONObject expectedJson) throws JSONException {
        mPreloadInfoWrapper = new PreloadInfoWrapper(preloadInfo, mock(PublicLogger.class), autoTracking);
        mExpectedJson = expectedJson;
        mBaseJson = new JSONObject().put("key", "value");
    }

    @Test
    public void addToEventValue() throws JSONException {
        JSONAssert.assertEquals(mExpectedJson, mPreloadInfoWrapper.addToEventValue(mBaseJson), true);
    }
}
