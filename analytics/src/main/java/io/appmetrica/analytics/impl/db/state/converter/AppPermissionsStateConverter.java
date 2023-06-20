package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.impl.BackgroundRestrictionsState;
import io.appmetrica.analytics.impl.permissions.AppPermissionsState;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;
import java.util.ArrayList;
import java.util.List;

public class AppPermissionsStateConverter implements
        ProtobufConverter<AppPermissionsState, AppPermissionsStateProtobuf.AppPermissionsState> {

    @NonNull
    private final BackgroundRestrictionsConverter mBackgroundRestrictionsConverter;

    public AppPermissionsStateConverter() {
        this(new BackgroundRestrictionsConverter());
    }

    @VisibleForTesting
    AppPermissionsStateConverter(@NonNull BackgroundRestrictionsConverter backgroundRestrictionsConverter) {
        mBackgroundRestrictionsConverter = backgroundRestrictionsConverter;
    }

    @NonNull
    @Override
    public AppPermissionsStateProtobuf.AppPermissionsState fromModel(@NonNull AppPermissionsState value) {
        AppPermissionsStateProtobuf.AppPermissionsState proto = new AppPermissionsStateProtobuf.AppPermissionsState();
        proto.permissions = new AppPermissionsStateProtobuf.AppPermissionsState
                .PermissionState[value.mPermissionStateList.size()];
        int i = 0;
        for (PermissionState permissionState : value.mPermissionStateList) {
            proto.permissions[i] = permissionStateToProto(permissionState);
            i++;
        }
        if (value.mBackgroundRestrictionsState != null) {
            proto.backgroundRestrictionsState = mBackgroundRestrictionsConverter
                    .fromModel(value.mBackgroundRestrictionsState);
        }
        proto.availableProviders = new String[value.mAvailableProviders.size()];
        i = 0;
        for (String provider : value.mAvailableProviders) {
            proto.availableProviders[i] = provider;
            i++;
        }
        return proto;
    }

    @NonNull
    @Override
    public AppPermissionsState toModel(@NonNull AppPermissionsStateProtobuf.AppPermissionsState nano) {
        List<PermissionState> permissions = new ArrayList<PermissionState>();
        for (int i = 0; i < nano.permissions.length; i++) {
            permissions.add(permissionStateToModel(nano.permissions[i]));
        }
        BackgroundRestrictionsState backgroundRestrictionsState = null;
        if (nano.backgroundRestrictionsState != null) {
            backgroundRestrictionsState = mBackgroundRestrictionsConverter.toModel(nano.backgroundRestrictionsState);
        }
        List<String> availableProviders = new ArrayList<String>();
        for (int i = 0; i < nano.availableProviders.length; i++) {
            availableProviders.add(nano.availableProviders[i]);
        }
        return new AppPermissionsState(permissions, backgroundRestrictionsState, availableProviders);
    }

    private AppPermissionsStateProtobuf.AppPermissionsState.PermissionState permissionStateToProto(
            @NonNull PermissionState value) {
        AppPermissionsStateProtobuf.AppPermissionsState.PermissionState permission =
                new AppPermissionsStateProtobuf.AppPermissionsState.PermissionState();
        permission.name = value.name;
        permission.enabled = value.granted;
        return permission;
    }

    private PermissionState permissionStateToModel(
            @NonNull AppPermissionsStateProtobuf.AppPermissionsState.PermissionState nano) {
        return new PermissionState(nano.name, nano.enabled);
    }
}
