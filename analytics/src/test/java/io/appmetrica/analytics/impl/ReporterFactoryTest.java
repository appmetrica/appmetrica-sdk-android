package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReporterFactoryTest extends CommonTest {

    private ReporterFactory mReporterFactory;
    @Mock
    private StartupHelper mStartupHelper;
    @Mock
    private ReportsHandler mReportsHandler;
    private final AppMetricaConfig mAppConfig = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY).build();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mStartupHelper = mock(StartupHelper.class);
        mReportsHandler = mock(ReportsHandler.class);
        AppMetricaFacade instance = mock(AppMetricaFacade.class);
        AppMetricaFacade.setInstance(instance);
        when(instance.getClientTimeTracker()).thenReturn(mock(ClientTimeTracker.class));
        mReporterFactory = new ReporterFactory(
                RuntimeEnvironment.getApplication(),
                null,
                mReportsHandler,
                null,
                mStartupHelper
        );
    }

    @Test
    public void testMainReporterCreation() {
        assertThat(mReporterFactory.buildMainReporter(AppMetricaConfig.newConfigBuilder(TestsData.generateApiKey()).build(), false)).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMainReporterCreationForExistedApiKey() {
        String apiKey = TestsData.generateApiKey();
        mReporterFactory.getOrCreateReporter(ReporterConfig.newConfigBuilder(apiKey).build());
        mReporterFactory.buildMainReporter(AppMetricaConfig.newConfigBuilder(apiKey).build(), false);
    }

    @Test
    public void testMainReporterCreationForDifferentApiKey() {
        ReporterFactory factory = mReporterFactory;
        factory.getOrCreateReporter(ReporterConfig.newConfigBuilder(TestsData.generateApiKey()).build());
        assertThat(factory.buildMainReporter(AppMetricaConfig.newConfigBuilder(TestsData.generateApiKey()).build(), false)).isNotNull();
    }

    @Test
    public void testGettingMainReporter() {
        ReporterFactory factory = mReporterFactory;
        String apiKey = TestsData.generateApiKey();
        factory.buildMainReporter(AppMetricaConfig.newConfigBuilder(apiKey).build(), false);
        assertThat(factory.getOrCreateReporter(ReporterConfig.newConfigBuilder(apiKey).build())).isNotNull();
    }

    @Test
    public void testCallStartupHelper() {
        ReporterConfig config = ReporterConfig.newConfigBuilder(UUID.randomUUID().toString()).build();
        mReporterFactory.getOrCreateReporter(config);
        verify(mStartupHelper).sendStartupIfNeeded();
        reset(mStartupHelper);
        mReporterFactory.getOrCreateReporter(config);
        verify(mStartupHelper, never()).sendStartupIfNeeded();
    }

    @Test
    public void testNotCallStartupHelperForSelfReporter() {
        ReporterConfig config = ReporterConfig.newConfigBuilder(SdkData.SDK_API_KEY_UUID).build();
        mReporterFactory.getOrCreateReporter(config);
        verify(mStartupHelper, never()).sendStartupIfNeeded();
    }

    @Test
    public void getMainOrCrashReporterCreate() {
        IUnhandledSituationReporter reporter = mReporterFactory.getMainOrCrashReporter(mAppConfig);
        assertThat(reporter).isExactlyInstanceOf(CrashReporter.class);
    }

    @Test
    public void getMainOrCrashReporterPassesErrorEnvironment() throws Exception {
        IUnhandledSituationReporter reporter = mReporterFactory.getMainOrCrashReporter(
                AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
                        .withErrorEnvironmentValue("key1", "value1")
                        .withErrorEnvironmentValue("key2", "value2")
                        .build()
        );
        JSONAssert.assertEquals(
                new JSONObject().put("key1", "value1").put("key2", "value2").toString(),
                ((CrashReporter) reporter).getEnvironment().getErrorEnvironment(),
                true
        );
    }

    @Test
    public void getMainOrCrashReporterAlreadyHasMain() {
        MainReporter mainReporter = mReporterFactory.buildMainReporter(
                AppMetricaConfig.newConfigBuilder(mAppConfig.apiKey).build(),
                false
        );
        assertThat(mReporterFactory.getMainOrCrashReporter(mAppConfig)).isSameAs(mainReporter);
    }

    @Test
    public void getMainOrCrashReporterAlreadyHasCrash() {
        IUnhandledSituationReporter reporter = mReporterFactory.getMainOrCrashReporter(mAppConfig);
        assertThat(reporter).isExactlyInstanceOf(CrashReporter.class);
        assertThat(mReporterFactory.getMainOrCrashReporter(mAppConfig)).isNotSameAs(reporter);
    }

    @Test
    public void getReporterFactory() {
        assertThat(mReporterFactory.getReporterFactory()).isSameAs(mReporterFactory);
    }
}
