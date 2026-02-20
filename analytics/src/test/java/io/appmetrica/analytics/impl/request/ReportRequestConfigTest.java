package io.appmetrica.analytics.impl.request;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope;
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider;
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils;
import io.appmetrica.analytics.impl.AutoCollectedDataSubscribersHolder;
import io.appmetrica.analytics.impl.CertificatesFingerprintsProvider;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.startup.ClidsStateChecker;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReportRequestConfigTest extends CommonTest {

    @Rule
    public MockedStaticRule<PackageManagerUtils> packageManagerUtilsMockedRule =
        new MockedStaticRule<>(PackageManagerUtils.class);
    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Mock
    private ReportRequestConfig.DataSendingStrategy dataSendingStrategy;
    private ReportRequestConfig reportRequestConfig;

    @Mock
    private Context context;
    @Mock
    private StartupState startupState;
    @Mock
    private RetryPolicyConfig retryPolicyConfig;
    private final Set<String> autoCollectedDataObservers = Set.of("first", "second");
    @Mock
    private AutoCollectedDataSubscribersHolder autoCollectedDataSubscribersHolder;
    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private ClidsStateChecker clidsStateChecker;
    @Mock
    private ReportRequestConfig.Arguments arguments;
    @Mock
    private ComponentId componentId;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private CertificatesFingerprintsProvider certificatesFingerprintsProvider;
    @Mock
    private CollectingFlags collectingFlags;
    @Mock
    private SdkEnvironmentProvider sdkEnvironmentProvider;
    @Mock
    private AppSetIdProvider appSetIdProvider;
    @Mock
    private AppSetId appSetId;
    @Mock
    private AdvertisingIdGetter advertisingIdGetter;
    @Mock
    private PlatformIdentifiers platformIdentifiers;

    private final String appSetIdValue = "AppSetIdValue";
    private final AppSetIdScope appSetIdScope = AppSetIdScope.APP;
    private final String packageName = "test.package.name";
    private final String appVersionName = "2.4.5";
    private final String appVersionCode = "245";
    private final int attributionId = 100500;
    private final String certificateFingerprint = "Certificate fingerprint";

    private CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments> coreDataSource;
    private ReportRequestConfig.Loader loader;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(PackageManagerUtils.getAppVersionName(context)).thenReturn(appVersionName);
        when(PackageManagerUtils.getAppVersionCodeString(context)).thenReturn(appVersionCode);

        collectingFlags = new CollectingFlags.CollectingFlagsBuilder().build();
        when(startupState.getCollectingFlags()).thenReturn(collectingFlags);
        when(startupState.getRetryPolicyConfig()).thenReturn(retryPolicyConfig);

        when(componentUnit.getContext()).thenReturn(context);
        when(componentUnit.getVitalComponentDataProvider()).thenReturn(vitalComponentDataProvider);
        when(vitalComponentDataProvider.getAttributionId()).thenReturn(attributionId);
        when(componentUnit.getCertificatesFingerprintsProvider()).thenReturn(certificatesFingerprintsProvider);
        when(certificatesFingerprintsProvider.getSha1()).thenReturn(Collections.singletonList(certificateFingerprint));
        when(componentUnit.getComponentId()).thenReturn(componentId);
        when(componentId.getPackage()).thenReturn(packageName);
        when(platformIdentifiers.getAdvIdentifiersProvider()).thenReturn(advertisingIdGetter);
        when(platformIdentifiers.getAppSetIdProvider()).thenReturn(appSetIdProvider);
        when(appSetIdProvider.getAppSetId()).thenReturn(appSetId);
        when(appSetId.getId()).thenReturn(appSetIdValue);
        when(appSetId.getScope()).thenReturn(appSetIdScope);

        when(autoCollectedDataSubscribersHolder.getSubscribers()).thenReturn(autoCollectedDataObservers);
        when(componentUnit.getAutoCollectedDataSubscribersHolder()).thenReturn(autoCollectedDataSubscribersHolder);

        coreDataSource = new CoreRequestConfig.CoreDataSource<>(
            startupState,
            sdkEnvironmentProvider,
            platformIdentifiers,
            arguments
        );
        loader = new ReportRequestConfig.Loader(componentUnit, dataSendingStrategy, clidsStateChecker);

        reportRequestConfig = loader.load(coreDataSource);
    }

    @Test
    public void testStrategyCalled() {
        reportRequestConfig.setDataSendingProperties(null, dataSendingStrategy);

        reportRequestConfig.getCurrentDataSendingState();

        verify(dataSendingStrategy).shouldSend(null);
    }

    @Test
    public void testShouldSendPreloadInfo() {
        when(componentUnit.shouldSend()).thenReturn(true);
        assertThat(reportRequestConfig.needToSendPreloadInfo()).isTrue();
    }

    @Test
    public void testShouldNotSendPreloadInfo() {
        when(componentUnit.shouldSend()).thenReturn(false);
        assertThat(reportRequestConfig.needToSendPreloadInfo()).isFalse();
    }

    @Test
    public void testRetryPolicyConfig() {
        assertThat(reportRequestConfig.getRetryPolicyConfig()).isEqualTo(retryPolicyConfig);
    }

    @Test
    public void appSetIdIsNotSet() {
        when(platformIdentifiers.getAppSetIdProvider().getAppSetId()).thenReturn(null);
        reportRequestConfig = loader.load(coreDataSource);
        assertThat(reportRequestConfig.getAppSetId()).isNotNull().isEmpty();
        assertThat(reportRequestConfig.getAppSetIdScope()).isNotNull().isEmpty();
    }

    @Test
    public void appSetIdIsSet() {
        assertThat(reportRequestConfig.getAppSetId()).isEqualTo(appSetIdValue);
        assertThat(reportRequestConfig.getAppSetIdScope()).isEqualTo(appSetIdScope.getValue());
    }

    @Test
    public void getAutoCollectedDataSubscribers() {
        assertThat(reportRequestConfig.getAutoCollectedDataSubscribers()).isEqualTo(autoCollectedDataObservers);
    }
}
