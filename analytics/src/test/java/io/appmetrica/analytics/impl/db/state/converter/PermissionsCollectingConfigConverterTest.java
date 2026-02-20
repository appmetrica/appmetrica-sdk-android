package io.appmetrica.analytics.impl.db.state.converter;

import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.PermissionsCollectingConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;

public class PermissionsCollectingConfigConverterTest extends CommonTest {

    private final long mCheckIntervalSeconds = 444333;
    private final long mForstSendIntervalSeconds = 777888;
    private PermissionsCollectingConfigConverter mConverter;

    @Before
    public void setUp() {
        mConverter = new PermissionsCollectingConfigConverter();
    }

    @Test
    public void testToProto() {
        PermissionsCollectingConfig model = new PermissionsCollectingConfig(mCheckIntervalSeconds, mForstSendIntervalSeconds);
        StartupStateProtobuf.StartupState.PermissionsCollectingConfig proto = mConverter.fromModel(model);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(proto.checkIntervalSeconds).isEqualTo(mCheckIntervalSeconds);
        softly.assertThat(proto.forceSendIntervalSeconds).isEqualTo(mForstSendIntervalSeconds);
        softly.assertAll();
    }

    @Test
    public void testToModel() {
        StartupStateProtobuf.StartupState.PermissionsCollectingConfig proto = new StartupStateProtobuf.StartupState.PermissionsCollectingConfig();
        proto.checkIntervalSeconds = mCheckIntervalSeconds;
        proto.forceSendIntervalSeconds = mForstSendIntervalSeconds;
        PermissionsCollectingConfig model = mConverter.toModel(proto);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(model.mCheckIntervalSeconds).isEqualTo(mCheckIntervalSeconds);
        softly.assertThat(model.mForceSendIntervalSeconds).isEqualTo(mForstSendIntervalSeconds);
        softly.assertAll();
    }
}
