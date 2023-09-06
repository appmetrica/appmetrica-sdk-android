package io.appmetrica.analytics.impl.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.CertificatesFingerprintsProvider;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.startup.ClidsStateChecker;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import io.appmetrica.analytics.testutils.rules.networktasks.NetworkServiceLocatorRule;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportRequestConfigLoaderClidsTest extends CommonTest {

    @Mock
    private ReportRequestConfig.DataSendingStrategy dataSendingSendingStrategy;
    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private ComponentId componentId;
    @Mock
    private ClidsStateChecker clidsStateChecker;

    private ReportRequestConfig.Loader loader;
    private CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments> dataSource;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Rule
    public NetworkServiceLocatorRule networkServiceLocatorRule = new NetworkServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        doReturn(componentId).when(componentUnit).getComponentId();
        doReturn(RuntimeEnvironment.getApplication()).when(componentUnit).getContext();
        doReturn(mock(CertificatesFingerprintsProvider.class)).when(componentUnit).getCertificatesFingerprintsProvider();
        doReturn(mock(VitalComponentDataProvider.class)).when(componentUnit).getVitalComponentDataProvider();
        loader = new ReportRequestConfig.Loader(componentUnit, dataSendingSendingStrategy, clidsStateChecker);
        dataSource = new CoreRequestConfig.CoreDataSource<>(
                TestUtils.createDefaultStartupState(), new ReportRequestConfig.Arguments(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        )
        );
    }

    @Test
    public void passDataSendingStrategy() {
        assertThat(loader.load(dataSource).getDataSendingStrategy()).isSameAs(dataSendingSendingStrategy);
    }

    @Test
    public void testPassPreloadInfoSendingStrategy() {
        assertThat(loader.load(dataSource).getPreloadInfoSendingStrategy()).isSameAs(componentUnit);
    }

    @Test
    public void chosenClidsForRequestMatchLastRequestClids() {
        Map<String, String> clidsFromClient = new HashMap<String, String>();
        clidsFromClient.put("clid0", "0");
        StartupState startupState = TestUtils.createDefaultStartupState();
        dataSource = createDataSource(startupState, clidsFromClient);
        when(clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                clidsFromClient,
                startupState,
                GlobalServiceLocator.getInstance().getClidsStorage()
        )).thenReturn(true);
        ReportRequestConfig config = loader.load(dataSource);
        assertThat(config.getClidsFromClientMatchClidsFromStartupRequest()).isTrue();
    }

    @Test
    public void chosenClidsForRequestDoNotMatchLastRequestClids() {
        Map<String, String> clidsFromClient = new HashMap<String, String>();
        clidsFromClient.put("clid0", "0");
        StartupState startupState = TestUtils.createDefaultStartupState();
        dataSource = createDataSource(startupState, clidsFromClient);
        when(clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                clidsFromClient,
                startupState,
                GlobalServiceLocator.getInstance().getClidsStorage()
        )).thenReturn(false);
        ReportRequestConfig config = loader.load(dataSource);
        assertThat(config.getClidsFromClientMatchClidsFromStartupRequest()).isFalse();
    }

    private CoreRequestConfig.CoreDataSource createDataSource(@NonNull StartupState startupState,
                                                              @Nullable Map<String, String> clientClids) {
        return new CoreRequestConfig.CoreDataSource(
                startupState,
                new ReportRequestConfig.Arguments(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        clientClids,
                        null
                )
        );
    }
}
