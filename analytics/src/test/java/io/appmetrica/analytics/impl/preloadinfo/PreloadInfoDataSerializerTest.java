package io.appmetrica.analytics.impl.preloadinfo;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class PreloadInfoDataSerializerTest extends CommonTest {

    private final PreloadInfoDataSerializer serializer = new PreloadInfoDataSerializer();

    @Test
    public void toByteArrayDefaultObject() throws IOException {
        PreloadInfoProto.PreloadInfoData protoData = new PreloadInfoProto.PreloadInfoData();
        byte[] rawData = serializer.toByteArray(protoData);
        PreloadInfoProto.PreloadInfoData restored = serializer.toState(rawData);
        assertThat(restored).isEqualToComparingFieldByField(protoData);
    }

    @Test
    public void toByteArrayFilledObject() throws Exception {
        final String chosenTrackingId = "6677";
        final JSONObject chosenParams = new JSONObject().put("key", "value");
        final boolean wasSet = new Random().nextBoolean();
        final boolean autoTracking = new Random().nextBoolean();

        final String firstCandidateTrackingId = "9879879";
        final JSONObject firstCandidateParams = new JSONObject().put("qwe", "rty");
        final int firstCandidateSource = PreloadInfoProto.PreloadInfoData.APP;

        final String secondCandidateTrackingId = "1254351243";
        final JSONObject secondCandidateParams = new JSONObject().put("ytr", "ewq");
        final int secondCandidateSource = PreloadInfoProto.PreloadInfoData.SATELLITE;

        PreloadInfoProto.PreloadInfoData.PreloadInfo chosenState = new PreloadInfoProto.PreloadInfoData.PreloadInfo();
        chosenState.trackingId = chosenTrackingId;
        chosenState.additionalParameters = chosenParams.toString();
        chosenState.wasSet = wasSet;
        chosenState.preloadInfoAutoTracking = autoTracking;
        chosenState.source = PreloadInfoProto.PreloadInfoData.RETAIL;

        PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate firstCandidate = new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate();
        firstCandidate.trackingId = firstCandidateTrackingId;
        firstCandidate.additionalParameters = firstCandidateParams.toString();
        firstCandidate.source = firstCandidateSource;

        PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate secondCandidate = new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate();
        secondCandidate.trackingId = secondCandidateTrackingId;
        secondCandidate.additionalParameters = secondCandidateParams.toString();
        secondCandidate.source = secondCandidateSource;

        PreloadInfoProto.PreloadInfoData protoData = new PreloadInfoProto.PreloadInfoData();
        protoData.chosenPreloadInfo = chosenState;
        protoData.candidates = new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate[] { firstCandidate, secondCandidate };

        byte[] rawData = serializer.toByteArray(protoData);
        assertThat(rawData).isNotEmpty();
        PreloadInfoProto.PreloadInfoData restored = serializer.toState(rawData);
        final ObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData> assertions =
                new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData>(restored)
                .withIgnoredFields("candidates");
        assertions.checkFieldRecursively(
                "chosenPreloadInfo",
                new Consumer<ObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfo>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfo> innerAssertions) {
                        try {
                            innerAssertions.withIgnoredFields("additionalParameters");
                            innerAssertions.checkField("trackingId", chosenTrackingId);
                            innerAssertions.checkField("wasSet", wasSet);
                            innerAssertions.checkField("preloadInfoAutoTracking", autoTracking);
                            innerAssertions.checkField("source", PreloadInfoProto.PreloadInfoData.RETAIL);
                            JSONAssert.assertEquals(chosenParams.toString(), assertions.getActual().chosenPreloadInfo.additionalParameters, true);
                        } catch (Throwable ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
        assertions.checkAll();

        assertThat(restored.candidates).hasSize(2);

        ObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate> firstCandidateAssertions =
                new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate>(restored.candidates[0])
                .withIgnoredFields("additionalParameters");
        firstCandidateAssertions.checkField("trackingId", firstCandidateTrackingId);
        firstCandidateAssertions.checkField("source", firstCandidateSource);
        firstCandidateAssertions.checkAll();
        JSONAssert.assertEquals(firstCandidateParams.toString(), firstCandidateAssertions.getActual().additionalParameters, true);

        ObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate> secondCandidateAssertions =
                new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate>(restored.candidates[1])
                        .withIgnoredFields("additionalParameters");
        secondCandidateAssertions.checkField("trackingId", secondCandidateTrackingId);
        secondCandidateAssertions.checkField("source", secondCandidateSource);
        secondCandidateAssertions.checkAll();
        JSONAssert.assertEquals(secondCandidateParams.toString(), secondCandidateAssertions.getActual().additionalParameters, true);
    }

    @Test(expected = InvalidProtocolBufferNanoException.class)
    public void testDeserializationInvalidByteArray() throws IOException {
        serializer.toState(new byte[]{1, 2, 3});
    }

    @Test
    public void testDefaultValue() {
        assertThat(serializer.defaultValue()).usingRecursiveComparison().isEqualTo(
                new PreloadInfoProto.PreloadInfoData()
        );
    }
}
