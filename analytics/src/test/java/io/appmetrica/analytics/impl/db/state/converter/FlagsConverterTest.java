package io.appmetrica.analytics.impl.db.state.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.OptionalBoolConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FlagsConverterTest extends CommonTest {

    private final StartupStateProtobuf.StartupState.Flags mDefault = new StartupStateProtobuf.StartupState.Flags();
    private final Random mRandom = new Random();
    private final boolean mPermissionsCollectingEnabled = mRandom.nextBoolean();
    private final boolean mFeaturesCollectingEnabled = mRandom.nextBoolean();
    private final boolean mGoogleAid = mRandom.nextBoolean();
    private final boolean mSimInfo = mRandom.nextBoolean();
    private final boolean huaweiOaid = mRandom.nextBoolean();
    private final boolean sslPinningEnabled = mRandom.nextBoolean();
    private final int sslPinningEnabledProto = mRandom.nextInt(3);
    @Mock
    private OptionalBoolConverter optionalBoolConverter;

    private CollectingFlags mModelCollectingFlags;
    private StartupStateProtobuf.StartupState.Flags mProtoFlags;

    private FlagsConverter mFlagsConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mFlagsConverter = new FlagsConverter(optionalBoolConverter);
        when(optionalBoolConverter.toProto(sslPinningEnabled)).thenReturn(sslPinningEnabledProto);
        when(optionalBoolConverter.toModel(sslPinningEnabledProto)).thenReturn(sslPinningEnabled);
        mModelCollectingFlags = new CollectingFlags.CollectingFlagsBuilder()
                .withSslPinning(sslPinningEnabled)
                .withPermissionsCollectingEnabled(mPermissionsCollectingEnabled)
                .withFeaturesCollectingEnabled(mFeaturesCollectingEnabled)
                .withGoogleAid(mGoogleAid)
                .withSimInfo(mSimInfo)
                .withHuaweiOaid(huaweiOaid)
                .build();
        mProtoFlags = new StartupStateProtobuf.StartupState.Flags();
        mProtoFlags.permissionsCollectingEnabled = mPermissionsCollectingEnabled;
        mProtoFlags.featuresCollectingEnabled = mFeaturesCollectingEnabled;
        mProtoFlags.googleAid = mGoogleAid;
        mProtoFlags.simInfo = mSimInfo;
        mProtoFlags.huaweiOaid = huaweiOaid;
        mProtoFlags.sslPinning = sslPinningEnabledProto;
    }

    @Test
    public void testToProto() throws Exception {
        StartupStateProtobuf.StartupState.Flags proto = mFlagsConverter.fromModel(mModelCollectingFlags);
        ObjectPropertyAssertions<StartupStateProtobuf.StartupState.Flags> assertions =
                ObjectPropertyAssertions(proto)
                        .withFinalFieldOnly(false);

        assertions.checkField("permissionsCollectingEnabled", mPermissionsCollectingEnabled);
        assertions.checkField("featuresCollectingEnabled", mFeaturesCollectingEnabled);
        assertions.checkField("googleAid", mGoogleAid);
        assertions.checkField("simInfo", mSimInfo);
        assertions.checkField("huaweiOaid", huaweiOaid);
        assertions.checkField("sslPinning", sslPinningEnabledProto);

        assertions.checkAll();
    }

    @Test
    public void testToModel() throws Exception {
        CollectingFlags model = mFlagsConverter.toModel(mProtoFlags);
        ObjectPropertyAssertions<CollectingFlags> assertions =
                ObjectPropertyAssertions(model)
                        .withFinalFieldOnly(false);

        assertions.checkField("permissionsCollectingEnabled", mPermissionsCollectingEnabled);
        assertions.checkField("featuresCollectingEnabled", mFeaturesCollectingEnabled);
        assertions.checkField("googleAid", mGoogleAid);
        assertions.checkField("simInfo", mSimInfo);
        assertions.checkField("huaweiOaid", huaweiOaid);
        assertions.checkField("sslPinning", sslPinningEnabled);

        assertions.checkAll();
    }

    @Test
    public void testToProtoDefault() throws Exception {
        when(optionalBoolConverter.toProto(null)).thenReturn(StartupStateProtobuf.StartupState.OPTIONAL_BOOL_UNDEFINED);
        StartupStateProtobuf.StartupState.Flags proto = mFlagsConverter.fromModel(new CollectingFlags.CollectingFlagsBuilder().build());
        ObjectPropertyAssertions<StartupStateProtobuf.StartupState.Flags> assertions =
                ObjectPropertyAssertions(proto)
                        .withFinalFieldOnly(false);

        assertions.checkField("permissionsCollectingEnabled", false);
        assertions.checkField("featuresCollectingEnabled", false);
        assertions.checkField("googleAid", false);
        assertions.checkField("simInfo", false);
        assertions.checkField("huaweiOaid", false);
        assertions.checkField("sslPinning", StartupStateProtobuf.StartupState.OPTIONAL_BOOL_UNDEFINED);

        assertions.checkAll();
    }

    @Test
    public void testToModelDefault() throws Exception {
        when(optionalBoolConverter.toModel(StartupStateProtobuf.StartupState.OPTIONAL_BOOL_UNDEFINED)).thenReturn(null);
        CollectingFlags model = mFlagsConverter.toModel(new StartupStateProtobuf.StartupState.Flags());
        ObjectPropertyAssertions<CollectingFlags> assertions =
                ObjectPropertyAssertions(model)
                        .withFinalFieldOnly(false);
        assertions.checkField("permissionsCollectingEnabled", false);
        assertions.checkField("featuresCollectingEnabled", false);
        assertions.checkField("googleAid", false);
        assertions.checkField("simInfo", false);
        assertions.checkField("huaweiOaid", false);
        assertions.checkFieldIsNull("sslPinning");

        assertions.checkAll();
    }
}
