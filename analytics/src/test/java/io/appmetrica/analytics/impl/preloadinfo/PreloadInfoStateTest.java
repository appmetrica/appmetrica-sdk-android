package io.appmetrica.analytics.impl.preloadinfo;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

public class PreloadInfoStateTest extends CommonTest {

    private final static String PARAM_KEY_FIRST = "first key";
    private final static String PARAM_KEY_SECOND = "second key";
    private final static String PARAM_VALUE_FIRST = "first value";
    private final static String PARAM_VALUE_SECOND = "second value";

    private static final String KEY_TRACKING_ID = "trackingId";
    private static final String KEY_ADDITIONAL_PARAMS = "additionalParams";
    private static final String KEY_WAS_SET = "wasSet";
    private static final String KEY_AUTO_TRACKING = "autoTracking";
    private static final String KEY_SOURCE = "source";

    private final String mTrackingId = "999888777";
    private final JSONObject mParams = new JSONObject();
    private final boolean mWasSet = true;
    private final boolean mAutoTracking = true;
    private final DistributionSource mSource = DistributionSource.APP;
    private final JSONObject mEmptyJson = new JSONObject();
    private PreloadInfoState mPreloadInfoState;
    private PreloadInfoState mDefaultPreloadInfoState;

    @Before
    public void setUp() throws JSONException {
        mParams.put(PARAM_KEY_FIRST, PARAM_VALUE_FIRST).put(PARAM_KEY_SECOND, PARAM_VALUE_SECOND);
        mPreloadInfoState = new PreloadInfoState(mTrackingId, mParams, mWasSet, mAutoTracking, mSource);
        mDefaultPreloadInfoState = new PreloadInfoState(null, mEmptyJson, false, false, DistributionSource.UNDEFINED);
    }

    @Test
    public void constructor() throws IllegalAccessException {
        ObjectPropertyAssertions<PreloadInfoState> assertions = ObjectPropertyAssertions(mPreloadInfoState);
        assertions.checkField("trackingId", mTrackingId);
        assertions.checkField("additionalParameters", mParams);
        assertions.checkField("wasSet", mWasSet);
        assertions.checkField("autoTrackingEnabled", mAutoTracking);
        assertions.checkField("source", mSource);
        assertions.checkAll();
    }

    @Test
    public void constructorDefault() throws IllegalAccessException {
        ObjectPropertyAssertions<PreloadInfoState> assertions = ObjectPropertyAssertions(mDefaultPreloadInfoState);
        assertions.checkField("trackingId", (String) null);
        assertions.checkField("additionalParameters", mEmptyJson);
        assertions.checkField("wasSet", false);
        assertions.checkField("autoTrackingEnabled", false);
        assertions.checkField("source", DistributionSource.UNDEFINED);
        assertions.checkAll();
    }

    @Test
    public void toInternalJson() throws JSONException {
        JSONAssert.assertEquals(
            new JSONObject()
                .put(KEY_TRACKING_ID, mTrackingId)
                .put(KEY_ADDITIONAL_PARAMS, mParams)
                .put(KEY_WAS_SET, mWasSet)
                .put(KEY_AUTO_TRACKING, mAutoTracking)
                .put(KEY_SOURCE, mSource.getDescription()),
            mPreloadInfoState.toInternalJson(),
            true
        );
    }

    @Test
    public void toInternalJsonDefault() throws JSONException {
        JSONAssert.assertEquals(
            new JSONObject()
                .put(KEY_ADDITIONAL_PARAMS, new JSONObject())
                .put(KEY_WAS_SET, false)
                .put(KEY_AUTO_TRACKING, false)
                .put(KEY_SOURCE, DistributionSource.UNDEFINED.getDescription()),
            mDefaultPreloadInfoState.toInternalJson(),
            true
        );
    }

    @Test
    public void fromJson() throws Exception {
        PreloadInfoState preloadInfoState = PreloadInfoState.fromJson(new JSONObject()
            .put(KEY_TRACKING_ID, mTrackingId)
            .put(KEY_ADDITIONAL_PARAMS, mParams)
            .put(KEY_WAS_SET, mWasSet)
            .put(KEY_AUTO_TRACKING, mAutoTracking)
            .put(KEY_SOURCE, mSource.getDescription())
        );
        ObjectPropertyAssertions<PreloadInfoState> assertions = ObjectPropertyAssertions(preloadInfoState);
        assertions.checkField("trackingId", mTrackingId);
        assertions.checkField("additionalParameters", mParams);
        assertions.checkField("wasSet", mWasSet);
        assertions.checkField("autoTrackingEnabled", mAutoTracking);
        assertions.checkField("source", mSource);
        assertions.checkAll();
    }

    @Test
    public void fromEmptyJson() throws Exception {
        PreloadInfoState preloadInfoState = PreloadInfoState.fromJson(new JSONObject());
        ObjectPropertyAssertions<PreloadInfoState> assertions = ObjectPropertyAssertions(preloadInfoState)
            .withIgnoredFields("additionalParameters");
        assertions.checkField("trackingId", (String) null);
        assertions.checkField("wasSet", false);
        assertions.checkField("autoTrackingEnabled", false);
        assertions.checkField("source", DistributionSource.UNDEFINED);
        assertions.checkAll();
        JSONAssert.assertEquals(new JSONObject(), preloadInfoState.additionalParameters, true);
    }

    @Test
    public void toEventJsonFilled() throws JSONException {
        JSONAssert.assertEquals(
            new JSONObject()
                .put(KEY_TRACKING_ID, mTrackingId)
                .put(KEY_ADDITIONAL_PARAMS, mParams),
            mPreloadInfoState.toEventJson(),
            true
        );
    }

    @Test
    public void toEventJsonNoParameters() throws JSONException {
        PreloadInfoState preloadInfoState = new PreloadInfoState(mTrackingId, new JSONObject(), true, mAutoTracking, mSource);
        JSONAssert.assertEquals(
            new JSONObject().put(KEY_TRACKING_ID, mTrackingId),
            preloadInfoState.toEventJson(),
            true
        );
    }

    @Test
    public void toEventJsonWasNotSet() {
        PreloadInfoState preloadInfoState = new PreloadInfoState(mTrackingId, mParams, false, mAutoTracking, mSource);
        assertThat(preloadInfoState.toEventJson()).isNull();
    }

    @Test
    public void toEventJsonNoTrackingIdButSet() throws JSONException {
        PreloadInfoState preloadInfoState = new PreloadInfoState(null, mParams, true, mAutoTracking, mSource);
        JSONAssert.assertEquals(
            new JSONObject().put(KEY_ADDITIONAL_PARAMS, mParams),
            preloadInfoState.toEventJson(),
            true
        );
    }

    @Test
    public void toEventJsonEmptyTrackingIdButSet() throws JSONException {
        PreloadInfoState preloadInfoState = new PreloadInfoState("", mParams, true, mAutoTracking, mSource);
        JSONAssert.assertEquals(
            new JSONObject()
                .put(KEY_TRACKING_ID, "")
                .put(KEY_ADDITIONAL_PARAMS, mParams),
            preloadInfoState.toEventJson(),
            true
        );
    }

    @Test
    public void getSource() {
        assertThat(mPreloadInfoState.getSource()).isEqualTo(mSource);
    }

}
