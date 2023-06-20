package io.appmetrica.analytics.impl.permissions;

import io.appmetrica.analytics.impl.BackgroundRestrictionsState;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AppPermissionsStateTest extends CommonTest {

    @Test
    public void testConstructor() {
        List permissions = mock(List.class);
        List providers = mock(List.class);
        BackgroundRestrictionsState backgroundRestrictionsState = mock(BackgroundRestrictionsState.class);
        AppPermissionsState state = new AppPermissionsState(permissions, backgroundRestrictionsState, providers);
        assertThat(state.mPermissionStateList).isEqualTo(permissions);
        assertThat(state.mBackgroundRestrictionsState).isEqualTo(backgroundRestrictionsState);
        assertThat(state.mAvailableProviders).isEqualTo(providers);
    }
}
