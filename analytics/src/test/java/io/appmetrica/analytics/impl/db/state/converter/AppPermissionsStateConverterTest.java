package io.appmetrica.analytics.impl.db.state.converter;

import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.impl.BackgroundRestrictionsState;
import io.appmetrica.analytics.impl.permissions.AppPermissionsState;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppPermissionsStateConverterTest extends CommonTest {

    @Mock
    private BackgroundRestrictionsConverter mBackgroundRestrictionsConverter;
    @Mock
    private BackgroundRestrictionsState mBackgroundRestrictionsStateModel;
    private AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState mBackgroundRestrictionsStateProto;
    private AppPermissionsState mEmptyModel;
    private AppPermissionsStateProtobuf.AppPermissionsState mEmptyProto;
    private AppPermissionsState mFilledModel;
    private AppPermissionsStateProtobuf.AppPermissionsState mFilledProto;
    private AppPermissionsStateConverter mConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mBackgroundRestrictionsStateProto = new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        when(mBackgroundRestrictionsConverter.toModel(mBackgroundRestrictionsStateProto)).thenReturn(mBackgroundRestrictionsStateModel);
        when(mBackgroundRestrictionsConverter.fromModel(mBackgroundRestrictionsStateModel)).thenReturn(mBackgroundRestrictionsStateProto);

        String permissionName1 = "permission1";
        String permissionName2 = "permission2";
        boolean permissionEnabled1 = true;
        boolean permissionEnabled2 = false;
        PermissionState[] permissions = new PermissionState[]{
                new PermissionState(permissionName1, permissionEnabled1),
                new PermissionState(permissionName2, permissionEnabled2)
        };
        String[] providers = new String[]{"provider1", "provider2"};
        mEmptyModel = new AppPermissionsState(new ArrayList<PermissionState>(), null, new ArrayList<String>());
        mEmptyProto = new AppPermissionsStateProtobuf.AppPermissionsState();
        mFilledModel = new AppPermissionsState(Arrays.asList(permissions), mBackgroundRestrictionsStateModel, Arrays.asList(providers));
        mFilledProto = new AppPermissionsStateProtobuf.AppPermissionsState();
        AppPermissionsStateProtobuf.AppPermissionsState.PermissionState permission1 = new AppPermissionsStateProtobuf.AppPermissionsState.PermissionState();
        permission1.name = permissionName1;
        permission1.enabled = permissionEnabled1;
        AppPermissionsStateProtobuf.AppPermissionsState.PermissionState permission2 = new AppPermissionsStateProtobuf.AppPermissionsState.PermissionState();
        permission2.name = permissionName2;
        permission2.enabled = permissionEnabled2;
        mFilledProto.permissions = new AppPermissionsStateProtobuf.AppPermissionsState.PermissionState[]{permission1, permission2};
        mFilledProto.backgroundRestrictionsState = mBackgroundRestrictionsStateProto;
        mFilledProto.availableProviders = providers;
        mConverter = new AppPermissionsStateConverter(mBackgroundRestrictionsConverter);
    }

    @Test
    public void testDefaultToModel() {
        assertThat(mConverter.toModel(mEmptyProto)).usingRecursiveComparison().isEqualTo(mEmptyModel);
    }

    @Test
    public void testDefaultToProto() {
        assertThat(mConverter.fromModel(mEmptyModel)).usingRecursiveComparison().isEqualTo(mEmptyProto);
    }

    @Test
    public void testFilledToModel() {
        assertThat(mConverter.toModel(mFilledProto)).usingRecursiveComparison().isEqualTo(mFilledModel);
    }

    @Test
    public void testFilledToProto() {
        assertThat(mConverter.fromModel(mFilledModel)).usingRecursiveComparison().isEqualTo(mFilledProto);
    }
}
