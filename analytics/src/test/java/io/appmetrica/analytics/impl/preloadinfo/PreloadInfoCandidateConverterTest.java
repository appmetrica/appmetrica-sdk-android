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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PreloadInfoCandidateConverterTest extends CommonTest {

    private final String trackingId = "765324765";
    private final JSONObject additionalParams = new JSONObject();
    private final DistributionSource modelSource = DistributionSource.RETAIL;
    private final int protoSource = PreloadInfoProto.PreloadInfoData.RETAIL;
    @Mock
    private PreloadInfoSourceConverter sourceConverter;
    private PreloadInfoCandidateConverter converter;

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.openMocks(this);
        additionalParams.put("cat", "мяу");
        when(sourceConverter.fromModel(any(DistributionSource.class))).thenReturn(protoSource);
        when(sourceConverter.toModel(anyInt())).thenReturn(modelSource);
        converter = new PreloadInfoCandidateConverter(sourceConverter);
    }

    @Test
    public void toProtoFilled() throws Exception {
        PreloadInfoData.Candidate model = new PreloadInfoData.Candidate(trackingId, additionalParams, modelSource);
        PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate protoCandidate = converter.fromModel(model);
        ObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate> assertions =
                new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate>(protoCandidate)
                .withIgnoredFields("additionalParameters");
        assertions.checkField("trackingId", trackingId);
        assertions.checkField("source", protoSource);
        assertions.checkAll();

        JSONAssert.assertEquals(additionalParams.toString(), protoCandidate.additionalParameters, true);
    }

    @Test
    public void toProtoEmpty() throws Exception {
        PreloadInfoData.Candidate model = new PreloadInfoData.Candidate("", new JSONObject(), modelSource);
        PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate protoCandidate = converter.fromModel(model);
        ObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate> assertions =
                new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate>(protoCandidate)
                        .withIgnoredFields("additionalParameters");
        assertions.checkField("trackingId", "");
        assertions.checkField("source", protoSource);
        assertions.checkAll();

        JSONAssert.assertEquals("{}", protoCandidate.additionalParameters, true);
    }

    @Test
    public void toProtoNullTrackingId() throws Exception {
        PreloadInfoData.Candidate model = new PreloadInfoData.Candidate(null, additionalParams, modelSource);
        PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate protoCandidate = converter.fromModel(model);
        ObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate> assertions =
                new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate>(protoCandidate)
                        .withIgnoredFields("additionalParameters");
        assertions.checkField("trackingId", "");
        assertions.checkField("source", protoSource);
        assertions.checkAll();

        JSONAssert.assertEquals(additionalParams.toString(), protoCandidate.additionalParameters, true);
    }

    @Test
    public void toModelFilled() throws Exception {
        PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate proto = new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate();
        proto.trackingId = trackingId;
        proto.additionalParameters = additionalParams.toString();
        proto.source = protoSource;

        PreloadInfoData.Candidate model = converter.toModel(proto);
        ObjectPropertyAssertions<PreloadInfoData.Candidate> assertions = ObjectPropertyAssertions(model)
                .withIgnoredFields("additionalParams");
        assertions.checkField("trackingId", trackingId);
        assertions.checkField("source", modelSource);
        assertions.checkAll();

        JSONAssert.assertEquals(additionalParams, model.additionalParams, true);
    }

    @Test
    public void toModelDefault() throws Exception {
        PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate proto = new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate();
        PreloadInfoData.Candidate model = converter.toModel(proto);
        ObjectPropertyAssertions<PreloadInfoData.Candidate> assertions = ObjectPropertyAssertions(model)
                .withIgnoredFields("additionalParams");
        assertions.checkField("trackingId", "");
        assertions.checkField("source", modelSource);
        assertions.checkAll();

        JSONAssert.assertEquals(new JSONObject(), model.additionalParams, true);
    }

    @Test
    public void toModelBadJson() throws Exception {
        PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate proto = new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate();
        proto.trackingId = trackingId;
        proto.additionalParameters = "not a json";
        proto.source = protoSource;

        PreloadInfoData.Candidate model = converter.toModel(proto);
        ObjectPropertyAssertions<PreloadInfoData.Candidate> assertions = ObjectPropertyAssertions(model)
                .withIgnoredFields("additionalParams");
        assertions.checkField("trackingId", trackingId);
        assertions.checkField("source", modelSource);
        assertions.checkAll();

        JSONAssert.assertEquals(new JSONObject(), model.additionalParams, true);
    }
}
