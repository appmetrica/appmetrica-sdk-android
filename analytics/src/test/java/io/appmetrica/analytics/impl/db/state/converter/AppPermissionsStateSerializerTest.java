package io.appmetrica.analytics.impl.db.state.converter;

import io.appmetrica.analytics.impl.db.protobuf.AppPermissionsStateSerializer;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AppPermissionsStateSerializerTest extends CommonTest {

    private final AppPermissionsStateSerializer mSerializer = new AppPermissionsStateSerializer();

    @Test
    public void testToByteArrayDefaultObject() throws IOException {
        AppPermissionsStateProtobuf.AppPermissionsState protoState = new AppPermissionsStateProtobuf.AppPermissionsState();
        byte[] rawData = mSerializer.toByteArray(protoState);
        AppPermissionsStateProtobuf.AppPermissionsState restored = mSerializer.toState(rawData);
        assertThat(restored).isEqualToComparingFieldByField(protoState);
    }

    @Test
    public void testToByteArrayFilledObject() throws IOException {
        AppPermissionsStateProtobuf.AppPermissionsState protoState = new AppPermissionsStateProtobuf.AppPermissionsState();
        protoState.permissions = new AppPermissionsStateProtobuf.AppPermissionsState.PermissionState[2];
        protoState.permissions[0] = createProtoPermissionState("name1", true);
        protoState.permissions[1] = createProtoPermissionState("name2", false);
        protoState.availableProviders = new String[2];
        protoState.availableProviders[0] = "provider1";
        protoState.availableProviders[1] = "provider2";
        protoState.backgroundRestrictionsState = new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        protoState.backgroundRestrictionsState.appStandbyBucket =
                AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.ACTIVE;
        protoState.backgroundRestrictionsState.backgroundRestricted =
                AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_TRUE;

        byte[] rawData = mSerializer.toByteArray(protoState);
        assertThat(rawData).isNotEmpty();
        AppPermissionsStateProtobuf.AppPermissionsState restored = mSerializer.toState(rawData);
        assertThat(restored).isEqualToComparingFieldByFieldRecursively(protoState);
    }

    @Test(expected = InvalidProtocolBufferNanoException.class)
    public void testDeserializationInvalidByteArray() throws IOException {
        mSerializer.toState(new byte[]{1, 2, 3});
    }

    @Test
    public void testDefaultValue() {
        assertThat(mSerializer.defaultValue()).isEqualToComparingFieldByFieldRecursively(
                new AppPermissionsStateProtobuf.AppPermissionsState()
        );
    }

    private AppPermissionsStateProtobuf.AppPermissionsState.PermissionState createProtoPermissionState(String name, boolean enabled) {
        AppPermissionsStateProtobuf.AppPermissionsState.PermissionState proto =
                new AppPermissionsStateProtobuf.AppPermissionsState.PermissionState();
        proto.name = name;
        proto.enabled = enabled;
        return proto;
    }

}
