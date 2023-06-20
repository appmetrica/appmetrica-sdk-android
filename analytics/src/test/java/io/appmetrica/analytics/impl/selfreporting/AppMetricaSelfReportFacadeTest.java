package io.appmetrica.analytics.impl.selfreporting;

import android.content.Context;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.impl.SdkData;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaSelfReportFacadeTest extends CommonTest {

    @Mock
    private SelfReporterWrapper mSelfReporterWrapper;
    private Context mContext;

    @Rule
    public MockedStaticRule<SelfReportFacadeProvider> selfReportFacadeProvider =
            new MockedStaticRule<>(SelfReportFacadeProvider.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(SelfReportFacadeProvider.getReporterWrapper()).thenReturn(mSelfReporterWrapper);
    }

    @Test
    public void testGetReporter() {
        assertThat(AppMetricaSelfReportFacade.getReporter()).isEqualTo(mSelfReporterWrapper);
    }

    @Test
    public void testOnInitializationFinished() {
        AppMetricaSelfReportFacade.onInitializationFinished(mContext);
        verify(mSelfReporterWrapper).onInitializationFinished(mContext);
    }

    @Test
    public void testWarmupForMetricaProcess() {
        try (MockedStatic<AppMetrica> sAppMetrica = Mockito.mockStatic(AppMetrica.class)) {
            AppMetricaSelfReportFacade.warmupForMetricaProcess(mContext);
            sAppMetrica.verify(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    AppMetrica.getReporter(mContext, SdkData.SDK_API_KEY_UUID);
                }
            });
        }
    }
}
