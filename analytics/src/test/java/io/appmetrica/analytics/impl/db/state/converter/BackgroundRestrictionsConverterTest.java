package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.BackgroundRestrictionsState;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class BackgroundRestrictionsConverterTest extends CommonTest {

    @Parameters
    public static Collection<Object[]> data() {
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto1 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto1.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.ACTIVE;
        proto1.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_FALSE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto2 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto2.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.ACTIVE;
        proto2.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_TRUE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto3 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto3.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.ACTIVE;
        proto3.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_UNDEFINED;

        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto4 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto4.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.WORKING_SET;
        proto4.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_FALSE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto5 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto5.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.WORKING_SET;
        proto5.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_TRUE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto6 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto6.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.WORKING_SET;
        proto6.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_UNDEFINED;

        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto7 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto7.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.FREQUENT;
        proto7.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_FALSE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto8 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto8.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.FREQUENT;
        proto8.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_TRUE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto9 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto9.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.FREQUENT;
        proto9.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_UNDEFINED;

        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto10 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto10.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.RARE;
        proto10.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_FALSE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto11 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto11.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.RARE;
        proto11.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_TRUE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto12 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto12.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.RARE;
        proto12.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_UNDEFINED;

        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto13 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto13.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.UNDEFINED;
        proto13.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_FALSE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto14 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto14.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.UNDEFINED;
        proto14.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_TRUE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto15 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto15.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.UNDEFINED;
        proto15.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_UNDEFINED;

        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto16 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto16.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.RESTRICTED;
        proto16.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_FALSE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto17 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto17.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.RESTRICTED;
        proto17.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_TRUE;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto18 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto18.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.RESTRICTED;
        proto18.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_UNDEFINED;
        AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto19 =
            new AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState();
        proto19.appStandbyBucket = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.EXEMPTED;
        proto19.backgroundRestricted = AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState.OPTIONAL_BOOL_FALSE;

        return Arrays.asList(
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.ACTIVE, false), proto1},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.ACTIVE, true), proto2},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.ACTIVE, null), proto3},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.WORKING_SET, false), proto4},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.WORKING_SET, true), proto5},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.WORKING_SET, null), proto6},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.FREQUENT, false), proto7},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.FREQUENT, true), proto8},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.FREQUENT, null), proto9},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RARE, false), proto10},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RARE, true), proto11},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RARE, null), proto12},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RESTRICTED, false), proto16},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RESTRICTED, true), proto17},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RESTRICTED, null), proto18},
            new Object[]{new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.EXEMPTED, false), proto19},
            new Object[]{new BackgroundRestrictionsState(null, false), proto13},
            new Object[]{new BackgroundRestrictionsState(null, true), proto14},
            new Object[]{new BackgroundRestrictionsState(null, null), proto15}
        );
    }

    private final BackgroundRestrictionsState mState;
    private final AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState mProto;
    private final BackgroundRestrictionsConverter mConverter;

    public BackgroundRestrictionsConverterTest(@NonNull BackgroundRestrictionsState state,
                                               @NonNull AppPermissionsStateProtobuf.AppPermissionsState.BackgroundRestrictionsState proto) {
        mState = state;
        mProto = proto;
        mConverter = new BackgroundRestrictionsConverter();
    }

    @Test
    public void testToProto() {
        assertThat(mConverter.fromModel(mState)).usingRecursiveComparison().isEqualTo(mProto);
    }

    @Test
    public void testToModel() {
        assertThat(mConverter.toModel(mProto)).usingRecursiveComparison().isEqualTo(mState);
    }
}
