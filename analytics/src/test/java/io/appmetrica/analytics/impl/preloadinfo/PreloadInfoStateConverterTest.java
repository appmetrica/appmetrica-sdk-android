package io.appmetrica.analytics.impl.preloadinfo;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class PreloadInfoStateConverterTest extends CommonTest {

    private final static String PARAM_KEY_FIRST = "first key";
    private final static String PARAM_KEY_SECOND = "second key";
    private final static String PARAM_VALUE_FIRST = "first value";
    private final static String PARAM_VALUE_SECOND = "second value";

    private final String mTrackingId = "777333555";
    private final JSONObject mParams = new JSONObject();

    private PreloadInfoStateConverter mConverter;

    @Before
    public void setUp() throws JSONException {
        mParams.put(PARAM_KEY_FIRST, PARAM_VALUE_FIRST).put(PARAM_KEY_SECOND, PARAM_VALUE_SECOND);
        mConverter = new PreloadInfoStateConverter();
    }

    @Test
    public void toProtoFilled() throws IllegalAccessException {
        PreloadInfoState preloadInfoState = new PreloadInfoState(mTrackingId, mParams, true, true, DistributionSource.RETAIL);
        PreloadInfoProto.PreloadInfoData.PreloadInfo nano = mConverter.fromModel(preloadInfoState);
        ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfo> assertions =
            new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfo>(nano);
        assertions.checkField("trackingId", mTrackingId);
        assertions.checkField("additionalParameters", mParams.toString());
        assertions.checkField("wasSet", true);
        assertions.checkField("preloadInfoAutoTracking", true);
        assertions.checkField("source", PreloadInfoProto.PreloadInfoData.RETAIL);
        assertions.checkAll();
    }

    @Test
    public void toProtoNullTrackingId() throws IllegalAccessException {
        PreloadInfoState preloadInfoState = new PreloadInfoState(null, mParams, true, true, DistributionSource.RETAIL);
        PreloadInfoProto.PreloadInfoData.PreloadInfo nano = mConverter.fromModel(preloadInfoState);
        ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfo> assertions =
            new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfo>(nano);
        assertions.checkField("trackingId", "");
        assertions.checkField("additionalParameters", mParams.toString());
        assertions.checkField("wasSet", true);
        assertions.checkField("preloadInfoAutoTracking", true);
        assertions.checkField("source", PreloadInfoProto.PreloadInfoData.RETAIL);
        assertions.checkAll();
    }

    @Test
    public void toProtoEmptyTrackingId() throws IllegalAccessException {
        PreloadInfoState preloadInfoState = new PreloadInfoState("", mParams, true, true, DistributionSource.RETAIL);
        PreloadInfoProto.PreloadInfoData.PreloadInfo nano = mConverter.fromModel(preloadInfoState);
        ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfo> assertions =
            new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfo>(nano);
        assertions.checkField("trackingId", "");
        assertions.checkField("additionalParameters", mParams.toString());
        assertions.checkField("wasSet", true);
        assertions.checkField("preloadInfoAutoTracking", true);
        assertions.checkField("source", PreloadInfoProto.PreloadInfoData.RETAIL);
        assertions.checkAll();
    }

    @Test
    public void toModelFilled() throws Exception {
        PreloadInfoProto.PreloadInfoData.PreloadInfo nano = new PreloadInfoProto.PreloadInfoData.PreloadInfo();
        nano.trackingId = mTrackingId;
        nano.additionalParameters = mParams.toString();
        nano.wasSet = true;
        nano.preloadInfoAutoTracking = true;
        nano.source = PreloadInfoProto.PreloadInfoData.RETAIL;
        PreloadInfoState state = mConverter.toModel(nano);
        ObjectPropertyAssertions<PreloadInfoState> assertions = ObjectPropertyAssertions(state)
            .withIgnoredFields("additionalParameters");
        assertions.checkField("trackingId", mTrackingId);
        assertions.checkField("wasSet", true);
        assertions.checkField("autoTrackingEnabled", true);
        assertions.checkField("source", DistributionSource.RETAIL);
        assertions.checkAll();
        JSONAssert.assertEquals(mParams, state.additionalParameters, true);
    }

    @Test
    public void toModelEmpty() throws Exception {
        PreloadInfoProto.PreloadInfoData.PreloadInfo nano = new PreloadInfoProto.PreloadInfoData.PreloadInfo();
        PreloadInfoState state = mConverter.toModel(nano);
        ObjectPropertyAssertions<PreloadInfoState> assertions = ObjectPropertyAssertions(state)
            .withIgnoredFields("additionalParameters");
        assertions.checkField("trackingId", "");
        assertions.checkField("wasSet", false);
        assertions.checkField("autoTrackingEnabled", false);
        assertions.checkField("source", DistributionSource.UNDEFINED);
        assertions.checkAll();
        JSONAssert.assertEquals(new JSONObject(), state.additionalParameters, true);
    }

    @Test
    public void toModelBadParametersJson() throws Exception {
        PreloadInfoProto.PreloadInfoData.PreloadInfo nano = new PreloadInfoProto.PreloadInfoData.PreloadInfo();
        nano.trackingId = mTrackingId;
        nano.additionalParameters = "not json";
        nano.wasSet = true;
        nano.preloadInfoAutoTracking = true;
        nano.source = PreloadInfoProto.PreloadInfoData.RETAIL;
        PreloadInfoState state = mConverter.toModel(nano);
        ObjectPropertyAssertions<PreloadInfoState> assertions = ObjectPropertyAssertions(state)
            .withIgnoredFields("additionalParameters");
        assertions.checkField("trackingId", mTrackingId);
        assertions.checkField("wasSet", true);
        assertions.checkField("autoTrackingEnabled", true);
        assertions.checkField("source", DistributionSource.RETAIL);
        assertions.checkAll();
        JSONAssert.assertEquals(new JSONObject(), state.additionalParameters, true);
    }
}
