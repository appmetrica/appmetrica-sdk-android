package io.appmetrica.analytics.impl.request;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope;
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class CoreRequestConfigLoaderTest extends CommonTest {

    @Mock
    protected SdkEnvironmentProvider sdkEnvironmentProvider;
    protected AppSetId appSetId = new AppSetId(UUID.randomUUID().toString(), AppSetIdScope.APP);
    @Mock
    protected AppSetIdProvider appSetIdProvider;
    @Mock
    protected AdvertisingIdGetter advertisingIdGetter;
    @Mock
    protected PlatformIdentifiers platformIdentifiers;

    @Rule
    public ContextRule contextRule = new ContextRule();
    protected Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = contextRule.getContext();
        when(appSetIdProvider.getAppSetId()).thenReturn(appSetId);
        when(platformIdentifiers.getAppSetIdProvider()).thenReturn(appSetIdProvider);
        when(platformIdentifiers.getAdvIdentifiersProvider()).thenReturn(advertisingIdGetter);
    }

    @Test
    public void loadIdentifiers() {
        String uuid = UUID.randomUUID().toString();
        String deviceId = UUID.randomUUID().toString();
        String deviceIdHash = UUID.randomUUID().toString();
        StartupState startupState = mock(StartupState.class);
        when(startupState.getUuid()).thenReturn(uuid);
        when(startupState.getDeviceId()).thenReturn(deviceId);
        when(startupState.getDeviceIdHash()).thenReturn(deviceIdHash);
        CoreRequestConfig config = getLoader().load(
            new CoreRequestConfig.CoreDataSource(
                startupState,
                sdkEnvironmentProvider,
                platformIdentifiers,
                getArguments()
            )
        );
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(config.getDeviceId()).isEqualTo(deviceId);
        assertions.assertThat(config.getUuid()).isEqualTo(uuid);
        assertions.assertThat(config.getDeviceIDHash()).isEqualTo(deviceIdHash);
        assertions.assertAll();
    }

    @Test
    public void testLoadFlagsAreTrue() {
        ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
        when(getSafePackageManagerMock().getApplicationInfo(any(Context.class), anyString(), eq(0))).thenReturn(applicationInfo);
        applicationInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE | ApplicationInfo.FLAG_SYSTEM;
        CoreRequestConfig config = getLoader().load(
            new CoreRequestConfig.CoreDataSource(
                TestUtils.createDefaultStartupState(),
                sdkEnvironmentProvider,
                platformIdentifiers,
                getArguments()
            )
        );

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(config.isAppDebuggable()).isEqualTo("1");
        softly.assertThat(config.isAppSystem()).isEqualTo("1");
        softly.assertAll();
    }

    @Test
    public void testLoadBothFlagsAreFalse() {
        ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
        when(getSafePackageManagerMock().getApplicationInfo(any(Context.class), anyString(), eq(0))).thenReturn(applicationInfo);
        applicationInfo.flags &= ~ApplicationInfo.FLAG_DEBUGGABLE & ~ApplicationInfo.FLAG_SYSTEM;
        CoreRequestConfig config = getLoader().load(
            new CoreRequestConfig.CoreDataSource(
                TestUtils.createDefaultStartupState(),
                sdkEnvironmentProvider,
                platformIdentifiers,
                getArguments()
            )
        );

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(config.isAppDebuggable()).isEqualTo("0");
        softly.assertThat(config.isAppSystem()).isEqualTo("0");
        softly.assertAll();
    }

    @Test
    public void testLoadNullApplicationInfoSamePackage() {
        Context context = getContextMock();
        when(context.getPackageName()).thenReturn(getPackageName());
        when(getSafePackageManagerMock().getApplicationInfo(any(Context.class), anyString(), eq(0))).thenReturn(null);
        ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
        applicationInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE | ApplicationInfo.FLAG_SYSTEM;
        when(context.getApplicationInfo()).thenReturn(applicationInfo);
        CoreRequestConfig config = getLoader().load(
            new CoreRequestConfig.CoreDataSource(
                TestUtils.createDefaultStartupState(),
                sdkEnvironmentProvider,
                platformIdentifiers,
                getArguments()
            )
        );

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(config.isAppDebuggable()).isEqualTo("1");
        softly.assertThat(config.isAppSystem()).isEqualTo("1");
        softly.assertAll();
    }

    @Test
    public void testLoadNullApplicationInfoDifferentPackages() {
        Context context = getContextMock();
        when(getSafePackageManagerMock().getApplicationInfo(any(Context.class), anyString(), eq(0))).thenReturn(null);
        ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
        applicationInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE | ApplicationInfo.FLAG_SYSTEM;
        when(context.getApplicationInfo()).thenReturn(applicationInfo);
        CoreRequestConfig config = getLoader().load(
            new CoreRequestConfig.CoreDataSource(
                TestUtils.createDefaultStartupState(),
                sdkEnvironmentProvider,
                platformIdentifiers,
                getArguments()
            )
        );

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(config.isAppDebuggable()).isEqualTo("0");
        softly.assertThat(config.isAppSystem()).isEqualTo("0");
        softly.assertAll();
    }

    abstract CoreRequestConfig.CoreLoader getLoader();

    abstract SafePackageManager getSafePackageManagerMock();

    abstract Context getContextMock();

    abstract String getPackageName();

    abstract Object getArguments();
}
