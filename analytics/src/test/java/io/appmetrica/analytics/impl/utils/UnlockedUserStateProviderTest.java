package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import android.os.Build;
import android.os.UserManager;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UnlockedUserStateProviderTest extends CommonTest {

    private Context context;

    @Mock
    private UserManager userManager;

    private UnlockedUserStateProvider unlockedUserStateProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();

        when(context.getSystemService(UserManager.class)).thenReturn(userManager);

        unlockedUserStateProvider = new UnlockedUserStateProvider();
    }

    @Test
    public void isUserUnlockedForTrue() {
        when(userManager.isUserUnlocked()).thenReturn(true);
        assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isTrue();
    }

    @Test
    public void isUserUnlockedForFalse() {
        when(userManager.isUserUnlocked()).thenReturn(false);
        assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isFalse();
    }

    @Test
    public void isUserUnlockedForMissingService() {
        when(context.getSystemService(UserManager.class)).thenReturn(null);
        assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isTrue();
    }

    @Test
    public void isUserUnlockedIfThrowException() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenThrow(new RuntimeException("Some throwable"));
        assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isTrue();
    }

    @Config(sdk = Build.VERSION_CODES.M)
    @Test
    public void isUserUnlockedPreN() {
        assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isTrue();
        verifyNoMoreInteractions(context, userManager);
    }
}
