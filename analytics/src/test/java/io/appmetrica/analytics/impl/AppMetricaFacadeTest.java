package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaFacadeTest extends CommonTest {

    private Context mContext;

    @Mock
    private AppMetricaFacade mInstance;
    @Mock
    private IAppMetricaCore metricaCore;
    @Mock
    private IAppMetricaImpl metricaImpl;
    @Mock
    private IReporterExtended reporterExtended;
    @Rule
    public final ClientServiceLocatorRule mClientServiceLocatorRule = new ClientServiceLocatorRule();
    @Rule
    public final MockedConstructionRule<AppMetricaCoreComponentsProvider> coreMockedRule =
            new MockedConstructionRule<>(
                    AppMetricaCoreComponentsProvider.class,
                    new MockedConstruction.MockInitializer<AppMetricaCoreComponentsProvider>() {
                        @Override
                        public void prepare(AppMetricaCoreComponentsProvider mock,
                                            MockedConstruction.Context context) throws Throwable {
                            when(mock.getCore(any(Context.class), any(ClientExecutorProvider.class)))
                                    .thenReturn(metricaCore);
                            when(mock.getImpl(any(Context.class), any(IAppMetricaCore.class)))
                                    .thenReturn(metricaImpl);
                        }
                    }
            );
    @Rule
    public final MockedStaticRule<AppMetricaSelfReportFacade> selfReporterFacadeRule =
            new MockedStaticRule<>(AppMetricaSelfReportFacade.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(metricaImpl.getReporter(any(ReporterConfig.class)))
                .thenReturn(reporterExtended);
        mContext = RuntimeEnvironment.getApplication();
    }

    @Test
    public void peekInstanceNull() {
        AppMetricaFacade.killInstance();
        assertThat(AppMetricaFacade.peekInstance()).isNull();
    }

    @Test
    public void peekInstanceNotNull() {
        AppMetricaFacade.setInstance(mInstance);
        assertThat(AppMetricaFacade.peekInstance()).isSameAs(mInstance);
    }

    @Test
    public void getInstanceCreate() {
        AppMetricaFacade.killInstance();
        assertThat(AppMetricaFacade.getInstance(mContext, true)).isNotNull();
        selfReporterFacadeRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                AppMetricaSelfReportFacade.onInitializationFinished(mContext);
            }
        });
    }

    @Test
    public void getInstanceCreatedOnlyOnce() {
        AppMetricaFacade.killInstance();
        AppMetricaFacade instance = AppMetricaFacade.getInstance(mContext, true);
        assertThat(instance).isNotNull();
        assertThat(AppMetricaFacade.getInstance(mContext, true)).isSameAs(instance);
        assertThat(AppMetricaFacade.peekInstance()).isSameAs(instance);
    }

    @Test
    public void getInstanceAlreadyCreated() {
        AppMetricaFacade.setInstance(mInstance);
        assertThat(AppMetricaFacade.getInstance(mContext, true)).isSameAs(mInstance);
    }

    @Test
    public void isActivated() {
        AppMetricaFacade.killInstance();
        assertThat(AppMetricaFacade.isActivated()).isFalse();
        AppMetricaFacade.markActivated();
        assertThat(AppMetricaFacade.isActivated()).isTrue();
        AppMetricaFacade.markActivated();
        assertThat(AppMetricaFacade.isActivated()).isTrue();
    }

    @Test
    public void isInitializedForAppNullInstance() {
        AppMetricaFacade.killInstance();
        assertThat(AppMetricaFacade.isInitializedForApp()).isFalse();
    }

    @Test
    public void isInitializedForAppFutureIsNotDone() {
        AppMetricaFacade.setInstance(mInstance);
        when(mInstance.isFullyInitialized()).thenReturn(false);
        assertThat(AppMetricaFacade.isInitializedForApp()).isFalse();
    }

    @Test
    public void isInitializedForAppNoMainReporterApiConsumerProvider() {
        AppMetricaFacade.setInstance(mInstance);
        when(mInstance.isFullyInitialized()).thenReturn(true);
        when(mInstance.peekMainReporterApiConsumerProvider()).thenReturn(null);
        assertThat(AppMetricaFacade.isInitializedForApp()).isFalse();
    }

    @Test
    public void isInitializedForAppInitialized() {
        AppMetricaFacade.setInstance(mInstance);
        when(mInstance.isFullyInitialized()).thenReturn(true);
        when(mInstance.peekMainReporterApiConsumerProvider()).thenReturn(mock(MainReporterApiConsumerProvider.class));
        assertThat(AppMetricaFacade.isInitializedForApp()).isTrue();
    }
}
