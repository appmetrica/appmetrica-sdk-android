package io.appmetrica.analytics.impl.request;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope;
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StartupRequestConfigTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Mock
    private ReferrerHolder mReferrerHolder;
    @Mock
    private DefaultStartupHostsProvider defaultStartupHostsProvider;
    @Mock
    private SdkEnvironmentProvider sdkEnvironmentProvider;
    @Mock
    private AppSetIdProvider appSetIdProvider;
    @Mock
    private AdvertisingIdGetter advertisingIdGetter;
    @Mock
    private PlatformIdentifiers platformIdentifiers;
    @NonNull
    private StartupState.Builder mStartupStateBuilder;
    private final AppSetId appSetId = new AppSetId(UUID.randomUUID().toString(), AppSetIdScope.DEVELOPER);

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(GlobalServiceLocator.getInstance().getAppSetIdGetter().getAppSetId()).thenReturn(appSetId);
        when(platformIdentifiers.getAppSetIdProvider()).thenReturn(appSetIdProvider);
        when(platformIdentifiers.getAdvIdentifiersProvider()).thenReturn(advertisingIdGetter);
        when(appSetIdProvider.getAppSetId()).thenReturn(appSetId);
        mStartupStateBuilder = TestUtils.createDefaultStartupStateBuilder();
    }

    @Test
    public void testRetryPolicyConfig() {
        RetryPolicyConfig retryPolicyConfig = mock(RetryPolicyConfig.class);
        StartupRequestConfig requestConfig = createStartupRequestConfigWithNullArgs(
            mStartupStateBuilder.withRetryPolicyConfig(retryPolicyConfig));
        assertThat(requestConfig.getRetryPolicyConfig()).isEqualTo(retryPolicyConfig);
    }

    @Test
    public void testHashSuccessfulStartups() {
        StartupRequestConfig requestConfig = createStartupRequestConfigWithNullArgs(
            mStartupStateBuilder.withHadFirstStartup(true));
        assertThat(requestConfig.hasSuccessfulStartup()).isTrue();
    }

    @Test
    public void testHashNoSuccessfulStartups() {
        StartupRequestConfig requestConfig = createStartupRequestConfigWithNullArgs(
            mStartupStateBuilder.withHadFirstStartup(false));
        assertThat(requestConfig.hasSuccessfulStartup()).isFalse();
    }

    @Test
    public void testFirstStartupTimeSetIfNeededShouldSet() {
        StartupRequestConfig requestConfig = createStartupRequestConfigWithNullArgs(mStartupStateBuilder);
        final long firstStartupTime = 1000;
        requestConfig.setFirstStartupTimeIfNeeded(firstStartupTime);
        assertThat(requestConfig.getFirstStartupTime())
            .isEqualTo(firstStartupTime);
    }

    @Test
    public void testFirstStartupTimeSetIfNeededShouldNotSet() {
        final long firstStartupTime = 1000;
        StartupRequestConfig requestConfig = createStartupRequestConfigWithNullArgs(
            mStartupStateBuilder.withFirstStartupServerTime(firstStartupTime));
        requestConfig.setFirstStartupTimeIfNeeded(firstStartupTime + 1);
        assertThat(requestConfig.getFirstStartupTime())
            .isEqualTo(firstStartupTime);
    }

    @Test
    public void testGetOrSetFirstStartupTimeShouldSetNew() {
        StartupRequestConfig requestConfig = createStartupRequestConfigWithNullArgs(mStartupStateBuilder);
        final long firstStartupTime = 1000;
        assertThat(requestConfig.getOrSetFirstStartupTime(firstStartupTime))
            .isEqualTo(firstStartupTime);

    }

    @Test
    public void testGetOrSetFirstStartupTimeShouldNotSetNew() {
        final long firstStartupTime = 1000;
        StartupRequestConfig requestConfig = createStartupRequestConfigWithNullArgs(
            mStartupStateBuilder.withFirstStartupServerTime(firstStartupTime));
        assertThat(requestConfig.getOrSetFirstStartupTime(firstStartupTime + 1))
            .isEqualTo(firstStartupTime);
    }

    @Test
    public void testReferrerHolder() {
        StartupRequestConfig requestConfig = new StartupRequestConfig(mReferrerHolder, defaultStartupHostsProvider);
        assertThat(requestConfig.getReferrerHolder()).isSameAs(mReferrerHolder);
    }

    @Test
    public void appSetIdIsNotSet() {
        when(appSetIdProvider.getAppSetId()).thenReturn(new AppSetId(null, AppSetIdScope.UNKNOWN));
        StartupRequestConfig requestConfig = createStartupRequestConfigWithNullArgs(mStartupStateBuilder);
        assertThat(requestConfig.getAppSetId()).isNotNull().isEmpty();
        assertThat(requestConfig.getAppSetIdScope()).isNotNull().isEmpty();
    }

    @Test
    public void appSetIdIsSet() {
        StartupRequestConfig requestConfig = createStartupRequestConfigWithNullArgs(mStartupStateBuilder);
        assertThat(requestConfig.getAppSetId()).isEqualTo(appSetId.getId());
        assertThat(requestConfig.getAppSetIdScope()).isEqualTo(appSetId.getScope().getValue());
    }

    @Test
    public void hasOnlyDefaultHosts() {
        String firstHost = "host.1";
        String secondHost = "host.2";
        when(defaultStartupHostsProvider.getDefaultHosts()).thenReturn(Arrays.asList(firstHost, secondHost));
        StartupRequestConfig requestConfig = new StartupRequestConfig(mReferrerHolder, defaultStartupHostsProvider);
        assertThat(requestConfig.getStartupHosts()).containsExactly(firstHost, secondHost);
    }

    @Test
    public void hasOnlyHostsFromClient() {
        String firstHost = "host.1";
        String secondHost = "host.2";
        when(defaultStartupHostsProvider.getDefaultHosts()).thenReturn(new ArrayList<String>());
        StartupRequestConfig requestConfig = new StartupRequestConfig(mReferrerHolder, defaultStartupHostsProvider);
        requestConfig.setStartupHostsFromClient(Arrays.asList(firstHost, secondHost));
        assertThat(requestConfig.getStartupHosts()).containsExactly(firstHost, secondHost);
    }

    @Test
    public void hasOnlyHostsFromStartup() {
        String firstHost = "host.1";
        String secondHost = "host.2";
        when(defaultStartupHostsProvider.getDefaultHosts()).thenReturn(new ArrayList<String>());
        StartupRequestConfig requestConfig = new StartupRequestConfig(mReferrerHolder, defaultStartupHostsProvider);
        requestConfig.setStartupHostsFromStartup(Arrays.asList(firstHost, secondHost));
        assertThat(requestConfig.getStartupHosts()).containsExactly(firstHost, secondHost);
    }

    @Test
    public void hasHostsFromAllSources() {
        String firstHost = "host.1";
        String secondHost = "host.2";
        String thirdHost = "host.3";
        String fourthHost = "host.4";
        String fifthHost = "host.5";
        String sixthHost = "host.6";
        when(defaultStartupHostsProvider.getDefaultHosts()).thenReturn(Arrays.asList(firstHost, secondHost));
        StartupRequestConfig requestConfig = new StartupRequestConfig(mReferrerHolder, defaultStartupHostsProvider);
        requestConfig.setStartupHostsFromClient(Arrays.asList(thirdHost, fourthHost));
        requestConfig.setStartupHostsFromStartup(Arrays.asList(fifthHost, sixthHost));
        assertThat(requestConfig.getStartupHosts()).containsExactly(fifthHost, sixthHost, thirdHost, fourthHost, firstHost, secondHost);
    }

    private StartupRequestConfig createStartupRequestConfigWithNullArgs(@NonNull StartupState.Builder builder) {
        return new StartupRequestConfig.Loader(
            contextRule.getContext(), contextRule.getContext().getPackageName()
        ).load(new CoreRequestConfig.CoreDataSource<StartupRequestConfig.Arguments>(
            builder.build(),
            sdkEnvironmentProvider,
            platformIdentifiers,
            new StartupRequestConfig.Arguments(
                null,
                null,
                null,
                false,
                null
            )
        ));
    }
}
