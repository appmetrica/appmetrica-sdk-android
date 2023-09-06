package io.appmetrica.analytics.impl.request;

import android.location.Location;
import io.appmetrica.analytics.impl.CertificatesFingerprintsProvider;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.networktasks.internal.NetworkServiceLocator;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.rules.networktasks.NetworkServiceLocatorRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportConfigLoaderTest extends CommonTest {

    private ReportRequestConfig.Loader mLoader;
    private CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments> mDataSource;

    private final String apiKey = UUID.randomUUID().toString();
    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private CertificatesFingerprintsProvider certificatesFingerprintsProvider;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Rule
    public NetworkServiceLocatorRule networkServiceLocatorRule = new NetworkServiceLocatorRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(componentUnit.getContext()).thenReturn(RuntimeEnvironment.getApplication());
        when(componentUnit.getCertificatesFingerprintsProvider()).thenReturn(certificatesFingerprintsProvider);
        when(componentUnit.getVitalComponentDataProvider()).thenReturn(vitalComponentDataProvider);
        when(certificatesFingerprintsProvider.getSha1()).thenReturn(Arrays.asList("cert1"));
        doReturn(new ComponentId(RuntimeEnvironment.getApplication().getPackageName(), apiKey))
            .when(componentUnit).getComponentId();

        ClidsInfoStorage clidsStorage = GlobalServiceLocator.getInstance().getClidsStorage();
        when(clidsStorage.updateAndRetrieveData(any(ClidsInfo.Candidate.class)))
            .thenReturn(new ClidsInfo.Candidate(Collections.singletonMap("clid22", "33"), DistributionSource.APP));

        mLoader = new ReportRequestConfig.Loader(
            componentUnit,
            mock(ReportRequestConfig.DataSendingStrategy.class)
        );
        mDataSource = new CoreRequestConfig.CoreDataSource<>(
            new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder().build()
            ).build(),
            new ReportRequestConfig.Arguments(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                5,
                null,
                null,
                null,
                null,
                null,
                null)
        );
    }

    @Test
    public void testSessionTimeoutLimit() {
        assertThat(mLoader.load(mDataSource).getSessionTimeout()).isEqualTo(10);
    }

    @Test
    public void testLoadWhenAllArgumentsExist() {
        int attributionId = 777888;
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid0", "0");
        clids.put("clid1", "1");
        List<String> fingerprints = Arrays.asList("aaabbb", "cccddd");
        when(vitalComponentDataProvider.getAttributionId()).thenReturn(attributionId);
        doReturn(fingerprints).when(certificatesFingerprintsProvider).getSha1();

        Location location = new Location("provider");

        ReportRequestConfig reportRequestConfig = new ReportRequestConfig.Loader(componentUnit, mock(ReportRequestConfig.DataSendingStrategy.class)).load(
            new CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments>(
                new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder()
                    .withPermissionsCollectingEnabled(true)
                    .withFeaturesCollectingEnabled(true).build()
                )
                    .withReportUrls(Arrays.asList("url1", "url2"))
                    .withEncodedClidsFromResponse("clidsFromRepsonse")
                    .build(),
                new ReportRequestConfig.Arguments(
                    NetworkServiceLocator.getInstance().getNetworkAppContext().getScreenInfoProvider().getScreenInfo().getDeviceType(),
                    "customVersion",
                    "178",
                    "apiKey",
                    true,
                    location,
                    true,
                    100,
                    200,
                    300,
                    true,
                    false,
                    clids,
                    250)
            )
        );
        SoftAssertions softAssertion = new SoftAssertions();
        softAssertion.assertThat(reportRequestConfig.getApiKey()).as("apiKey")
            .isEqualTo("apiKey");
        softAssertion.assertThat(reportRequestConfig.isLocationTracking()).as("isLocationTracking")
            .isTrue();
        softAssertion.assertThat(reportRequestConfig.getManualLocation()).as("ManualLocation")
            .isSameAs(location);
        softAssertion.assertThat(reportRequestConfig.isFirstActivationAsUpdate()).as("isFirstActivationAsUpdate")
            .isTrue();
        softAssertion.assertThat(reportRequestConfig.getSessionTimeout()).as("sessionTimeout")
            .isEqualTo(100);
        softAssertion.assertThat(reportRequestConfig.getMaxReportsCount()).as("maxReportsCount")
            .isEqualTo(200);
        softAssertion.assertThat(reportRequestConfig.getDispatchPeriod()).as("dispatchPeriod")
            .isEqualTo(300);
        softAssertion.assertThat(reportRequestConfig.isLogEnabled()).as("logEnabled")
            .isTrue();

        softAssertion.assertThat(reportRequestConfig.getReportHosts()).as("reportHosts")
            .containsExactly("url1", "url2");
        softAssertion.assertThat(reportRequestConfig.getClidsFromStartupResponse()).as("getClidsFromStartupResponse")
            .isEqualTo("clidsFromRepsonse");
        softAssertion.assertThat(reportRequestConfig.getClidsFromClientMatchClidsFromStartupRequest()).as("clidsFromClientMatchClidsFromStartupRequest")
            .isFalse();

        softAssertion.assertThat(reportRequestConfig.isPermissionsCollectingEnabled()).as("isPermissionsCollectingEnabled")
            .isTrue();
        softAssertion.assertThat(reportRequestConfig.isFeaturesCollectingEnabled()).as("isFeaturesCollectingEnabled")
            .isTrue();
        softAssertion.assertThat(reportRequestConfig.getCurrentDataSendingState()).as("dataSendingEnabled").isFalse();
        softAssertion.assertThat(reportRequestConfig.getMaxEventsInDbCount()).as("maxReportsInDbCount").isEqualTo(250);
        softAssertion.assertThat(reportRequestConfig.getCertificates()).as("certificates fingerprints")
            .isEqualTo(fingerprints);
        softAssertion.assertThat(reportRequestConfig.getAppSetId())
            .isEqualTo(
                NetworkServiceLocator.getInstance().getNetworkAppContext().getAppSetIdProvider().getAppSetId().getId()
            );
        softAssertion.assertThat(reportRequestConfig.getAppSetIdScope()).isEqualTo("developer");
        softAssertion.assertThat(reportRequestConfig.getAttributionId()).isEqualTo(attributionId);

        softAssertion.assertAll();
    }

}
