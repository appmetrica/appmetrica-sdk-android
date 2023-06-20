package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.BackgroundRestrictionsState;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;

public class BackgroundRestrictionsConverter implements ProtobufConverter<BackgroundRestrictionsState,
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState> {

    @NonNull
    @Override
    public AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState fromModel(
            @NonNull BackgroundRestrictionsState state) {
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState backgroundRestrictions =
                new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        if (state.mAppStandByBucket != null) {
            switch (state.mAppStandByBucket) {
                case ACTIVE:
                    backgroundRestrictions.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState
                            .BackgroundRestrictionsState.ACTIVE;
                    break;
                case WORKING_SET:
                    backgroundRestrictions.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState
                            .BackgroundRestrictionsState.WORKING_SET;
                    break;
                case FREQUENT:
                    backgroundRestrictions.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState
                            .BackgroundRestrictionsState.FREQUENT;
                    break;
                case RARE:
                    backgroundRestrictions.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState
                            .BackgroundRestrictionsState.RARE;
                    break;
                case RESTRICTED:
                    backgroundRestrictions.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState
                            .BackgroundRestrictionsState.RESTRICTED;
                    break;
            }
        }
        if (state.mBackgroundRestricted != null) {
            if (state.mBackgroundRestricted) {
                backgroundRestrictions.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState
                        .BackgroundRestrictionsState.OPTIONAL_BOOL_TRUE;
            } else {
                backgroundRestrictions.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState
                        .BackgroundRestrictionsState.OPTIONAL_BOOL_FALSE;
            }
        }
        return backgroundRestrictions;
    }

    @NonNull
    @Override
    public BackgroundRestrictionsState toModel(
            @NonNull AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState nano) {
        BackgroundRestrictionsState.AppStandByBucket appStandByBucket = null;
        Boolean backgroundRestricted = null;
        switch (nano.appStandbyBucket) {
            case AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.ACTIVE:
                appStandByBucket = BackgroundRestrictionsState.AppStandByBucket.ACTIVE;
                break;
            case AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.WORKING_SET:
                appStandByBucket = BackgroundRestrictionsState.AppStandByBucket.WORKING_SET;
                break;
            case AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.FREQUENT:
                appStandByBucket = BackgroundRestrictionsState.AppStandByBucket.FREQUENT;
                break;
            case AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.RARE:
                appStandByBucket = BackgroundRestrictionsState.AppStandByBucket.RARE;
                break;
            case AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.RESTRICTED:
                appStandByBucket = BackgroundRestrictionsState.AppStandByBucket.RESTRICTED;
                break;
        }
        switch (nano.backgroundRestricted) {
            case AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_TRUE:
                backgroundRestricted = true;
                break;
            case AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_FALSE:
                backgroundRestricted = false;
                break;
        }
        return new BackgroundRestrictionsState(appStandByBucket, backgroundRestricted);
    }
}
