package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Handler;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.referrer.client.ReferrerHelper;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ConstructionArgumentCaptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaImplFieldsProviderTest extends CommonTest {

    @Mock
    private Handler mHandler;
    @Mock
    private AppMetricaImpl mAppMetrica;
    @Mock
    private DataResultReceiver mReceiver;
    @Mock
    private ProcessConfiguration mProcessConfiguration;
    @Mock
    private ReportsHandler mReportsHandler;
    @Mock
    private ICommonExecutor mCommonExecutor;
    @Mock
    private StartupHelper mStartupHelper;
    @Mock
    private PreferencesClientDbStorage mPreferences;
    private Context mContext;
    private AppMetricaImplFieldsProvider mFieldsProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mFieldsProvider = new AppMetricaImplFieldsProvider();
    }

    @Test
    public void createDataResultReceiver() {
        ConstructionArgumentCaptor<DataResultReceiver> interceptor = new ConstructionArgumentCaptor<>();
        try (MockedConstruction<DataResultReceiver> constr = Mockito.mockConstruction(
                DataResultReceiver.class, interceptor
        )) {
            mFieldsProvider.createDataResultReceiver(mHandler, mAppMetrica);
            assertThat(constr.constructed()).hasSize(1);

            assertThat(interceptor.flatArguments()).containsExactly(mHandler, mAppMetrica);
        }
    }

    @Test
    public void createProcessConfiguration() {
        ConstructionArgumentCaptor<ProcessConfiguration> interceptor = new ConstructionArgumentCaptor<>();
        try (MockedConstruction<ProcessConfiguration> constr = Mockito.mockConstruction(
                ProcessConfiguration.class, interceptor
        )) {
            mFieldsProvider.createProcessConfiguration(mContext, mReceiver);
            assertThat(constr.constructed()).hasSize(1);
            assertThat(interceptor.flatArguments()).containsExactly(mContext, mReceiver);
        }
    }

    @Test
    public void createReportsHandler() {
        ConstructionArgumentCaptor<ReportsHandler> interceptor = new ConstructionArgumentCaptor<>();
        try (MockedConstruction<ReportsHandler> constr = Mockito.mockConstruction(
                ReportsHandler.class, interceptor
        )) {
            mFieldsProvider.createReportsHandler(mProcessConfiguration, mContext, mCommonExecutor);
            assertThat(constr.constructed()).hasSize(1);
            assertThat(interceptor.flatArguments()).containsExactly(mProcessConfiguration, mContext, mCommonExecutor);
        }
    }

    @Test
    public void createStartupHelper() {
        ConstructionArgumentCaptor<StartupHelper> interceptor = new ConstructionArgumentCaptor<>();
        try (MockedConstruction<StartupHelper> constr = Mockito.mockConstruction(
                StartupHelper.class, interceptor
        )) {
            mFieldsProvider.createStartupHelper(mContext, mReportsHandler, mPreferences, mHandler);
            assertThat(constr.constructed()).hasSize(1);
            assertThat(interceptor.flatArguments()).containsExactly(mContext, mReportsHandler, mPreferences, mHandler);
        }
    }

    @Test
    public void createReferrerHelper() {
        ConstructionArgumentCaptor<ReferrerHelper> interceptor = new ConstructionArgumentCaptor<>();
        try (MockedConstruction<ReferrerHelper> constr = Mockito.mockConstruction(
                ReferrerHelper.class, interceptor
        )) {
            mFieldsProvider.createReferrerHelper(mReportsHandler, mPreferences, mHandler);
            assertThat(constr.constructed()).hasSize(1);
            assertThat(interceptor.flatArguments()).containsExactly(mReportsHandler, mPreferences, mHandler);
        }
    }

    @Test
    public void createReporterFactory() {
        ConstructionArgumentCaptor<ReporterFactory> interceptor = new ConstructionArgumentCaptor<>();
        try (MockedConstruction<ReporterFactory> constr = Mockito.mockConstruction(
                ReporterFactory.class, interceptor
        )) {
            mFieldsProvider.createReporterFactory(mContext, mProcessConfiguration, mReportsHandler, mHandler, mStartupHelper);
            assertThat(constr.constructed()).hasSize(1);
            assertThat(interceptor.flatArguments()).containsExactly(mContext, mProcessConfiguration, mReportsHandler, mHandler, mStartupHelper);
        }
    }
}
