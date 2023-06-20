package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.PreloadInfoStorage;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SavePreloadInfoHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mComponentUnit;
    @Mock
    private PreloadInfoStorage mPreloadInfoStorage;
    @Mock
    private CounterReport mCounterReport;
    @Mock
    private ReportRequestConfig mConfig;
    private boolean mAutoTracking = new Random().nextBoolean();
    private SavePreloadInfoHandler mHandler;
    private final ArgumentMatcher<PreloadInfoState> mDefaultPreloadInfoStateMatcher = new ArgumentMatcher<PreloadInfoState>() {
        @Override
        public boolean matches(PreloadInfoState argument) {
            return argument.wasSet == false;
        }
    };

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mComponentUnit.getFreshReportRequestConfig()).thenReturn(mConfig);
        mHandler = new SavePreloadInfoHandler(mComponentUnit, mPreloadInfoStorage);
    }

    @Test
    public void valueIsBadJson() {
        when(mCounterReport.getValue()).thenReturn("bad json");
        mHandler.process(mCounterReport);
        verify(mPreloadInfoStorage).updateIfNeeded(argThat(mDefaultPreloadInfoStateMatcher));
    }

    @Test
    public void valueIsEmptyJson() {
        when(mCounterReport.getValue()).thenReturn(new JSONObject().toString());
        mHandler.process(mCounterReport);
        verify(mPreloadInfoStorage).updateIfNeeded(argThat(mDefaultPreloadInfoStateMatcher));
    }

    @Test
    public void valueIsNotEmptyButHasNoPreloadInfo() throws JSONException {
        when(mCounterReport.getValue()).thenReturn(new JSONObject().put("key", "value").toString());
        mHandler.process(mCounterReport);
        verify(mPreloadInfoStorage).updateIfNeeded(argThat(mDefaultPreloadInfoStateMatcher));
    }

    @Test
    public void valueHasPreloadInfo() throws JSONException {
        PreloadInfoState preloadInfoData = new PreloadInfoState("11", new JSONObject(), true, mAutoTracking, DistributionSource.APP);
        when(mCounterReport.getValue()).thenReturn(new JSONObject().put("preloadInfo", preloadInfoData.toInternalJson()).toString());
        mHandler.process(mCounterReport);
        ArgumentCaptor<PreloadInfoState> preloadInfoStateCaptor = ArgumentCaptor.forClass(PreloadInfoState.class);
        verify(mPreloadInfoStorage).updateIfNeeded(preloadInfoStateCaptor.capture());
        PreloadInfoState actual = preloadInfoStateCaptor.getValue();
        assertThat(actual).isEqualToIgnoringGivenFields(preloadInfoData, "additionalParameters");
        JSONAssert.assertEquals(new JSONObject(), actual.additionalParameters, true);
    }
}
