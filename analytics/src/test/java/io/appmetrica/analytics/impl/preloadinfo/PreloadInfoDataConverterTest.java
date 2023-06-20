package io.appmetrica.analytics.impl.preloadinfo;

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PreloadInfoDataConverterTest extends CommonTest {

    @Mock
    private PreloadInfoStateConverter stateConverter;
    @Mock
    private PreloadInfoCandidateConverter candidateConverter;
    @Mock
    private PreloadInfoState modelState;
    @Mock
    private PreloadInfoProto.PreloadInfoData.PreloadInfo protoState;
    @Mock
    private PreloadInfoData.Candidate firstModelCandidate;
    @Mock
    private PreloadInfoData.Candidate secondModelCandidate;
    @Mock
    private PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate firstProtoCandidate;
    @Mock
    private PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate secondProtoCandidate;
    private PreloadInfoDataConverter converter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new PreloadInfoDataConverter(stateConverter, candidateConverter);
    }

    @Test
    public void toModelFilled() throws Exception {
        when(stateConverter.toModel(protoState)).thenReturn(modelState);
        when(candidateConverter.toModel(firstProtoCandidate)).thenReturn(firstModelCandidate);
        when(candidateConverter.toModel(secondProtoCandidate)).thenReturn(secondModelCandidate);

        PreloadInfoProto.PreloadInfoData proto = new PreloadInfoProto.PreloadInfoData();
        proto.chosenPreloadInfo = protoState;
        proto.candidates = new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate[2];
        proto.candidates[0] = firstProtoCandidate;
        proto.candidates[1] = secondProtoCandidate;

        ObjectPropertyAssertions(converter.toModel(proto))
                .checkField("chosenPreloadInfo", modelState)
                .checkField("candidates", Arrays.asList(firstModelCandidate, secondModelCandidate), true)
                .checkAll();
    }

    @Test
    public void toModelEmpty() throws Exception {
        when(stateConverter.toModel(any(PreloadInfoProto.PreloadInfoData.PreloadInfo.class))).thenReturn(modelState);
        PreloadInfoProto.PreloadInfoData proto = new PreloadInfoProto.PreloadInfoData();
        ObjectPropertyAssertions(converter.toModel(proto))
                .checkField("chosenPreloadInfo", modelState)
                .checkField("candidates", new ArrayList<PreloadInfoData.Candidate>())
                .checkAll();
        ArgumentCaptor<PreloadInfoProto.PreloadInfoData.PreloadInfo> captor = ArgumentCaptor
                .forClass(PreloadInfoProto.PreloadInfoData.PreloadInfo.class);
        verify(stateConverter).toModel(captor.capture());
        assertThat(captor.getValue()).isEqualToComparingFieldByFieldRecursively(new PreloadInfoProto.PreloadInfoData.PreloadInfo());
    }

    @Test
    public void toProto() throws Exception {
        when(stateConverter.fromModel(modelState)).thenReturn(protoState);
        when(candidateConverter.fromModel(firstModelCandidate)).thenReturn(firstProtoCandidate);
        when(candidateConverter.fromModel(secondModelCandidate)).thenReturn(secondProtoCandidate);
        PreloadInfoData model = new PreloadInfoData(modelState, Arrays.asList(firstModelCandidate, secondModelCandidate));
        PreloadInfoProto.PreloadInfoData proto = converter.fromModel(model);

        new ProtoObjectPropertyAssertions<PreloadInfoProto.PreloadInfoData>(proto)
                .checkField("chosenPreloadInfo", protoState)
                .checkField("candidates", new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate[] { firstProtoCandidate, secondProtoCandidate })
                .checkAll();
    }
}
