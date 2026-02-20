package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class UserProfileTest extends CommonTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testListIsUnmodifiable() {
        UserProfile.newBuilder().build().getUserProfileUpdates().add(mock(UserProfileUpdate.class));
    }

    @Test
    public void testUserProfileBuilder() {
        UserProfileUpdate update1 = mock(UserProfileUpdate.class);
        UserProfileUpdate update2 = mock(UserProfileUpdate.class);
        UserProfileUpdate update3 = mock(UserProfileUpdate.class);
        UserProfileUpdate update4 = mock(UserProfileUpdate.class);
        UserProfileUpdate update5 = mock(UserProfileUpdate.class);
        UserProfile profile = UserProfile.newBuilder().apply(update1).apply(update2).apply(update3).apply(update4).apply(update5).build();
        assertThat(profile.getUserProfileUpdates()).containsExactly(update1, update2, update3, update4, update5);
    }

    @Test
    public void testSetSameUpdate() {
        UserProfileUpdate update1 = mock(UserProfileUpdate.class);
        UserProfile profile = UserProfile.newBuilder().apply(update1).apply(update1).apply(update1).build();
        assertThat(profile.getUserProfileUpdates()).containsExactly(update1, update1, update1);

    }

}
