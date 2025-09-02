package io.appmetrica.analytics.impl.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ReportRequestConfigIsReadyForSendingTest {

    private ReportRequestConfig mReportRequestConfig;
    private final boolean expected;
    private final boolean clidsMatch;
    private final List<String> hosts;
    private final String uuid;
    private final String deviceId;
    private final String deviceIdHash;

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
            new Object[]{false, new ArrayList<String>(), "", "", "", false},
            new Object[]{false, null, null, null, null, false},
            new Object[]{false, Collections.singletonList("host"), "", "", "", false},
            new Object[]{false, null, "uuid", "deviceId", "deviceIdHash", false},
            new Object[]{true, new ArrayList<String>(), "uuid", "deviceId", "deviceIdHash", false},
            new Object[]{false, Collections.singletonList("host"), "uuid", "", "", false},
            new Object[]{false, Collections.singletonList("host"), "uuid", "deviceId", "", false},
            new Object[]{false, Collections.singletonList("host"), "uuid", "deviId", "deviceIdHash", false},
            new Object[]{true, Collections.singletonList("host"), "uuid", "deviId", "", false},
            new Object[]{true, Collections.singletonList("host"), "uuid", "deviceId", "deviceIdHash", true}
        );
    }

    public ReportRequestConfigIsReadyForSendingTest(boolean clidsMatch,
                                                    @Nullable List<String> hosts,
                                                    String uuid,
                                                    String deviceId,
                                                    String deviceIdHash,
                                                    boolean expected) {
        this.clidsMatch = clidsMatch;
        this.expected = expected;
        this.hosts = hosts;
        this.uuid = uuid;
        this.deviceId = deviceId;
        this.deviceIdHash = deviceIdHash;
    }

    @Rule
    public MockedStaticRule<PackageManagerUtils> packageManagerUtilsMockedRule =
        new MockedStaticRule<>(PackageManagerUtils.class);
    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Mock
    private Context context;
    @Mock
    private StartupState startupState;
    @Mock
    private RetryPolicyConfig retryPolicyConfig;
    @Mock
    private ComponentUnit componentUnit;
    private Set<String> autoCollectedDataObservers = Set.of("first", "seconds");
    @Mock
    private AutoCollectedDataSubscribersHolder autoCollectedDataSubscribersHolder;
    @Mock
    private ComponentId componentId;
    @Mock
    private ReportRequestConfig.DataSendingStrategy dataSendingStrategy;
    @Mock
    private ClidsStateChecker clidsStateChecker;
    @Mock
    private ReportRequestConfig.Arguments arguments;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private CertificatesFingerprintsProvider certificatesFingerprintsProvider;
    @Mock
    private CollectingFlags collectingFlags;
    @Mock
    private SdkEnvironmentProvider sdkEnvironmentProvider;
    @Mock
    private AdvertisingIdGetter advertisingIdGetter;
    private final AppSetId appSetId = new AppSetId(UUID.randomUUID().toString(), AppSetIdScope.DEVELOPER);
    @Mock
    private AppSetIdProvider appSetIdProvider;
    @Mock
    private PlatformIdentifiers platformIdentifiers;

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

        when(startupState.getUuid()).thenReturn(uuid);
        when(startupState.getDeviceId()).thenReturn(deviceId);
        when(startupState.getDeviceIdHash()).thenReturn(deviceIdHash);
        when(startupState.getRetryPolicyConfig()).thenReturn(retryPolicyConfig);

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

        when(appSetIdProvider.getAppSetId()).thenReturn(appSetId);
        when(platformIdentifiers.getAdvIdentifiersProvider()).thenReturn(advertisingIdGetter);
        when(platformIdentifiers.getAppSetIdProvider()).thenReturn(appSetIdProvider);

        when(autoCollectedDataSubscribersHolder.getSubscribers()).thenReturn(autoCollectedDataObservers);
        when(componentUnit.getAutoCollectedDataSubscribersHolder()).thenReturn(autoCollectedDataSubscribersHolder);

        coreDataSource = new CoreRequestConfig.CoreDataSource<>(
            startupState,
            sdkEnvironmentProvider,
            platformIdentifiers,
            arguments
        );
        loader = new ReportRequestConfig.Loader(componentUnit, dataSendingStrategy, clidsStateChecker);

        mReportRequestConfig = loader.load(coreDataSource);
        mReportRequestConfig.setClidsFromClientMatchClidsFromStartupRequest(clidsMatch);
        mReportRequestConfig.setReportHosts(hosts);
    }

    @Test
    public void test() {
        assertThat(mReportRequestConfig.isReadyForSending()).isEqualTo(expected);
    }
}
