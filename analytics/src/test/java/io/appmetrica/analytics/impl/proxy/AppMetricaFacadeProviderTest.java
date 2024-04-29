package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP)
public class AppMetricaFacadeProviderTest extends CommonTest {

    @Mock
    private AppMetricaFacade mAppMetricaFacade;

    private Context mContext;

    private AppMetricaFacadeProvider mProvider;

    @Rule
    public final MockedStaticRule<AppMetricaFacade> sAppMetricaFacade = new MockedStaticRule<>(AppMetricaFacade.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mProvider = new AppMetricaFacadeProvider();
    }

    @Test
    public void peekInitializedImpl() {
        when(AppMetricaFacade.peekInstance()).thenReturn(mAppMetricaFacade);
        assertThat(mProvider.peekInitializedImpl()).isEqualTo(mAppMetricaFacade);
    }

    @Test
    public void peekInitializedImplNull() {
        when(AppMetricaFacade.peekInstance()).thenReturn(null);
        assertThat(mProvider.peekInitializedImpl()).isNull();
    }

    @Test
    public void getInitializedImpl() {
        when(AppMetricaFacade.getInstance(mContext, false)).thenReturn(mAppMetricaFacade);
        assertThat(mProvider.getInitializedImpl(mContext)).isEqualTo(mAppMetricaFacade);
    }

    @Test
    public void isInitializedForAppForTrue() {
        when(AppMetricaFacade.isInitializedForApp()).thenReturn(true);
        assertThat(mProvider.isInitializedForApp()).isTrue();
    }

    @Test
    public void isInitializedForAppForFalse() {
        when(AppMetricaFacade.isInitializedForApp()).thenReturn(false);
        assertThat(mProvider.isInitializedForApp()).isFalse();
    }

    @Test
    public void isActivatedTrue() {
        when(AppMetricaFacade.isActivated()).thenReturn(true);
        assertThat(mProvider.isActivated()).isTrue();
    }

    @Test
    public void isActivatedFalse() {
        when(AppMetricaFacade.isActivated()).thenReturn(false);
        assertThat(mProvider.isActivated()).isFalse();
    }

    @Test
    public void markActivated() {
        mProvider.markActivated();
        sAppMetricaFacade.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() {
                AppMetricaFacade.markActivated();
            }
        });
    }

    @Test
    public void setLocation() {
        final Location location = mock(Location.class);
        mProvider.setLocation(location);
        sAppMetricaFacade.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                AppMetricaFacade.setLocation(location);
            }
        });
    }

    @Test
    public void setLocationTracking() {
        final boolean locationTracking = new Random().nextBoolean();
        mProvider.setLocationTracking(locationTracking);
        sAppMetricaFacade.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                AppMetricaFacade.setLocationTracking(locationTracking);
            }
        });
    }

    @Test
    public void setDataSendingEnabled() {
        final boolean dataSendingEnabled = new Random().nextBoolean();
        mProvider.setDataSendingEnabled(dataSendingEnabled);
        sAppMetricaFacade.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                AppMetricaFacade.setDataSendingEnabled(dataSendingEnabled);
            }
        });
    }

    @Test
    public void putErrorEnvironmentValue() {
        final String key = "key";
        final String value = "value";
        mProvider.putErrorEnvironmentValue(key, value);
        sAppMetricaFacade.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                AppMetricaFacade.putErrorEnvironmentValue(key, value);
            }
        });
    }

    @Test
    public void putAppEnvironmentValue() {
        final String key = "key";
        final String value = "value";
        mProvider.putAppEnvironmentValue(key, value);
        sAppMetricaFacade.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                AppMetricaFacade.putAppEnvironmentValue(key, value);
            }
        });
    }

    @Test
    public void clearAppEnvironment() {
        mProvider.clearAppEnvironment();
        sAppMetricaFacade.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                AppMetricaFacade.clearAppEnvironment();
            }
        });
    }

    @Test
    public void setUserProfileID() {
        final String userProfileID = "user_profile_id";
        mProvider.setUserProfileID(userProfileID);
        sAppMetricaFacade.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                AppMetricaFacade.setUserProfileID(userProfileID);
            }
        });
    }
}
