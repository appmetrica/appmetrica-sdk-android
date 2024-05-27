package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Handler;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ICrashTransformer;
import io.appmetrica.analytics.coreapi.internal.clientcomponents.ClientComponentsInitializer;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.clientcomponents.ClientComponentsInitializerProvider;
import io.appmetrica.analytics.impl.crash.client.ICrashProcessor;
import io.appmetrica.analytics.logger.common.internal.BaseReleaseLogger;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaServiceCoreTest extends CommonTest {

    @Mock
    private Handler mHandler;
    @Mock
    private ClientTimeTracker mClientTimeTracker;
    @Mock
    private IHandlerExecutor mDefaultExecutor;
    @Mock
    private IReporterFactoryProvider mReporterFactoryProvider;
    @Mock
    private AppOpenWatcher appOpenWatcher;
    @Mock
    private ClientComponentsInitializer clientComponentsInitializer;
    private Context mContext;
    private AppMetricaCore mCore;

    @Rule
    public final ClientServiceLocatorRule mClientServiceLocatorRule = new ClientServiceLocatorRule();

    @Rule
    public final MockedStaticRule<BaseReleaseLogger> releaseLoggerRule = new MockedStaticRule<>(BaseReleaseLogger.class);
    @Rule
    public final MockedStaticRule<SdkUtils> sSdkUtils = new MockedStaticRule<>(SdkUtils.class);
    @Rule
    public final MockedConstructionRule<ClientComponentsInitializerProvider> clientComponentsInitializerProviderMockedConstructionRule =
        new MockedConstructionRule<>(
            ClientComponentsInitializerProvider.class,
            new MockedConstruction.MockInitializer<ClientComponentsInitializerProvider>() {
                @Override
                public void prepare(ClientComponentsInitializerProvider mock,
                                    MockedConstruction.Context context) throws Throwable {
                    when(mock.getClientComponentsInitializer()).thenReturn(clientComponentsInitializer);
                }
            }
        );

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(mDefaultExecutor.getHandler()).thenReturn(mHandler);
        AppMetricaFacade.killInstance();
        mCore = new AppMetricaCore(
                mContext,
                mDefaultExecutor,
                mClientTimeTracker,
                appOpenWatcher
        );
    }

    @After
    public void tearDown()  {
        mClientServiceLocatorRule.after();
    }

    @Test
    public void constructor() {
        verify(mClientTimeTracker).trackCoreCreation();
        releaseLoggerRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() {
                BaseReleaseLogger.init(mContext);
            }
        });
        sSdkUtils.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    SdkUtils.logSdkInfo();
                }
            },
            never()
        );
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mDefaultExecutor).execute(runnableArgumentCaptor.capture());
        runnableArgumentCaptor.getValue().run();
        sSdkUtils.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() {
                SdkUtils.logSdkInfo();
            }
        });
        assertThat(clientComponentsInitializerProviderMockedConstructionRule.getConstructionMock().constructed().size())
            .isEqualTo(1);
        assertThat(clientComponentsInitializerProviderMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .isEmpty();
        verify(clientComponentsInitializer).onCreate();
    }

    @Test
    public void getMetricaHandler() {
        assertThat(mCore.getMetricaHandler()).isSameAs(mHandler);
    }

    @Test
    public void getClientTimeTracker() {
        assertThat(mCore.getClientTimeTracker()).isSameAs(mClientTimeTracker);
    }

    @Test
    public void getExecutor() {
        assertThat(mCore.getExecutor()).isSameAs(mDefaultExecutor);
    }

    @Test
    public void shouldStartWatchingAppOpensIfFlagIsTrue() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
                .withAppOpenTrackingEnabled(true)
                .build();
        mCore.activate(config, mReporterFactoryProvider);
        verify(appOpenWatcher).startWatching();
    }

    @Test
    public void shouldStartWatchingAppOpensIfFlagIsNull() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
                .build();
        mCore.activate(config, mReporterFactoryProvider);
        verify(appOpenWatcher).startWatching();
    }

    @Test
    public void shouldNotStartWatchingAppOpensIfFlagIsFalse() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
                .withAppOpenTrackingEnabled(false)
                .build();
        mCore.activate(config, mReporterFactoryProvider);
        verify(appOpenWatcher, never()).startWatching();
    }

    @Test
    public void activateTwice() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
                .withAppOpenTrackingEnabled(true)
                .build();
        mCore.activate(config, mReporterFactoryProvider);
        verify(appOpenWatcher).startWatching();
        mCore.activate(config, mReporterFactoryProvider);
        verifyNoMoreInteractions(appOpenWatcher);
    }

    @Test
    public void activateNoCrashReporting() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY).build();
        mCore.activate(config, mReporterFactoryProvider);
        AppMetricaUncaughtExceptionHandler uncaughtExceptionHandler = mCore.getUncaughtExceptionHandler();
        assertThat(uncaughtExceptionHandler).isNotNull();
        assertThat(Thread.getDefaultUncaughtExceptionHandler()).isSameAs(uncaughtExceptionHandler);
    }

    @Test
    public void activateCrashReportingDisabled() {
        mCore.activate(createConfigWithCrashReporting(false), mReporterFactoryProvider);
        assertThat(mCore.getUncaughtExceptionHandler()).isNull();
    }

    @Test
    public void activateCrashReportingEnabled() {
        mCore.activate(createConfigWithCrashReporting(true), mReporterFactoryProvider);
        AppMetricaUncaughtExceptionHandler uncaughtExceptionHandler = mCore.getUncaughtExceptionHandler();
        assertThat(uncaughtExceptionHandler).isNotNull();
        assertThat(Thread.getDefaultUncaughtExceptionHandler()).isSameAs(uncaughtExceptionHandler);
    }

    @Test
    public void activateTwiceCrashReportingEnabled() {
        mCore.activate(createConfigWithCrashReporting(true), mReporterFactoryProvider);
        AppMetricaUncaughtExceptionHandler uncaughtExceptionHandler = mCore.getUncaughtExceptionHandler();
        assertThat(uncaughtExceptionHandler).isNotNull();
        assertThat(Thread.getDefaultUncaughtExceptionHandler()).isSameAs(uncaughtExceptionHandler);
        mCore.activate(createConfigWithCrashReporting(true), mReporterFactoryProvider);
        assertThat(uncaughtExceptionHandler).isSameAs(uncaughtExceptionHandler);
    }

    @Test
    public void getAppOpenWatcher() {
        assertThat(mCore.getAppOpenWatcher()).isSameAs(appOpenWatcher);
    }

    @Test
    public void setUpCrashHandling() {
        final String apiKey = TestsData.UUID_API_KEY;
        ICrashTransformer crashTransformer = mock(ICrashTransformer.class);
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
                .withCrashTransformer(crashTransformer)
                .build();

        final ICrashProcessor crashProcessor = mock(ICrashProcessor.class);
        when(mClientServiceLocatorRule.crashProcessorFactory.createCrashProcessors(mContext, config, mReporterFactoryProvider))
                .thenReturn(Collections.singletonList(crashProcessor));

        mCore.activate(config, mReporterFactoryProvider);
        assertThat(mCore.getUncaughtExceptionHandler().getCrashProcessors()).containsExactly(crashProcessor);
    }

    private AppMetricaConfig createConfigWithCrashReporting(final boolean crashReporting) {
        return AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY).withCrashReporting(crashReporting).build();
    }
}
