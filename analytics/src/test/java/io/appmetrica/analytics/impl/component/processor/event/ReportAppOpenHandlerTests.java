package io.appmetrica.analytics.impl.component.processor.event;

import android.util.Pair;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.remarketing.EventFirstOccurrenceService;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.startup.AttributionConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ReportAppOpenHandlerTests extends CommonTest {

    @Mock
    private ComponentUnit mComponent;
    @Mock
    private EventFirstOccurrenceService mEventFirstOccurrenceService;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;

    private ReportAppOpenHandler mReportAppOpenHandler;

    private final String mInput;
    @Nullable
    private final AttributionConfig attributionConfig;
    private final boolean mShouldHandleDeeplink;
    private final boolean shouldIncrementOpenId;

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() throws JSONException {
        AttributionConfig configWithEmptyConditions = new AttributionConfig(new ArrayList<Pair<String, AttributionConfig.Filter>>());
        AttributionConfig configWithConditions = new AttributionConfig(Arrays.asList(
            new Pair<String, AttributionConfig.Filter>("yclid", new AttributionConfig.Filter("414")),
            new Pair<String, AttributionConfig.Filter>("yclid", new AttributionConfig.Filter("415")),
            new Pair<String, AttributionConfig.Filter>("yid", new AttributionConfig.Filter("")),
            new Pair<String, AttributionConfig.Filter>("yuid", null),
            new Pair<String, AttributionConfig.Filter>("ydeviceid", new AttributionConfig.Filter("666777")),
            new Pair<String, AttributionConfig.Filter>("", new AttributionConfig.Filter("12"))
        ));
        return Arrays.asList(new Object[][]{
            // #0
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yclid%3D414"
            ), null, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yclid%3D414"
            ), configWithEmptyConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yclid%3D414"
            ), configWithConditions, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yclid%3D415"
            ), configWithConditions, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yclid%3D416"
            ), configWithConditions, false, true},

            // #5
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yclid%3D"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yid%3D1"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yid%3D"
            ), configWithConditions, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yuid%3D414"
            ), configWithConditions, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26yuid%3D"
            ), configWithConditions, true, true},

            // #10
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26ydeviceid%3D666777"
            ), configWithConditions, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26%3D12"
            ), configWithConditions, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26%3D13"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26reattribution%3D1"
            ), null, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26reattribution%3D1%26yclid%3D414"
            ), configWithConditions, true, true},

            // #15
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D8842404282613596" +
                    "432%26reattribution%3D1"
            ), configWithConditions, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id=97066511625096571%26ym_tracking_id=8842404282613596432%26" +
                    "reattribution=1"
            ), null, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=" +
                    "appmetrica_tracking_id=97066511625096571%26ym_tracking_id=8842404282613596432%26" +
                    "reattribution=1"
            ), configWithConditions, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=whate" +
                    "ver%26appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D884240428261359" +
                    "6432%26whatever%26reattribution%3D1"
            ), null, true, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&referrer=whate" +
                    "ver%26appmetrica_tracking_id%3D97066511625096571%26ym_tracking_id%3D884240428261359" +
                    "6432%26whatever%26reattribution%3D1"
            ), configWithConditions, true, true},

            // #20
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&reattribution=1"
            ), null, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F&reattribution=1"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://reattribution=1?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F"
            ), null, false, true},
            {wrapDeeplink(
                "ya-search-app-open://reattribution=1?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://referrer%3Dreattribution=1?uri=https%3A%2F%2Fcollections.yandex.ru%" +
                    "2Finterery%2F"
            ), null, false, true},

            // #25
            {wrapDeeplink(
                "ya-search-app-open://referrer%3Dreattribution=1?uri=https%3A%2F%2Fcollections.yandex.ru%" +
                    "2Finterery%2F"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#reattribution=1"
            ), null, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#reattribution=1"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#yclid=414"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer=" +
                    "reattribution=1"
            ), null, false, true},

            // #30
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer=" +
                    "reattribution=1"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer%3D" +
                    "reattribution=1"
            ), null, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer%3D" +
                    "reattribution=1"
            ), configWithConditions, false, true},
            {wrapDeeplink(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer%3D" +
                    "yclid=414"
            ), configWithConditions, false, true},
            {wrapDeeplinkWithWrongType(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer=" +
                    "reattribution=1"
            ), null, false, false},

            // #35
            {wrapDeeplinkWithWrongType(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer=" +
                    "reattribution=1"
            ), configWithConditions, false, false},
            {wrapDeeplinkWithWrongType(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer=" +
                    "yclid=414"
            ), configWithConditions, false, false},
            {wrapDeeplinkWithWrongType(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer%3D" +
                    "reattribution=1"
            ), null, false, false},
            {wrapDeeplinkWithWrongType(
                "ya-search-app-open://?uri=https%3A%2F%2Fcollections.yandex.ru%2Finterery%2F#referrer%3D" +
                    "reattribution=1"
            ), configWithConditions, false, false},
            {null, null, false, false},

            // #40
            {null, configWithConditions, false, false},
            {"", null, false, false},
            {"", configWithConditions, false, false},
            {"Simple string", null, false, false},
            {"Simple string", configWithConditions, false, false},
            {new JSONObject().toString(), null, false, false},

            // #45
            {new JSONObject().toString(), configWithConditions, false, false},
            {new JSONObject().put("Another key", "Another value").toString(), null, false, false},
            {new JSONObject().put("Another key", "Another value").toString(), configWithConditions, false, false},
        });
    }

    private static String wrapDeeplink(String deeplink) throws JSONException {
        return new JSONObject()
            .put("type", "open")
            .put("link", deeplink).toString();
    }

    private static String wrapDeeplinkWithWrongType(String deeplink) throws JSONException {
        return new JSONObject()
            .put("type", "referral")
            .put("link", deeplink).toString();
    }

    public ReportAppOpenHandlerTests(final String input,
                                     @Nullable AttributionConfig attributionConfig,
                                     final boolean shouldHandleDeeplink,
                                     final boolean shouldIncrementOpenId) {
        mInput = input;
        this.attributionConfig = attributionConfig;
        mShouldHandleDeeplink = shouldHandleDeeplink;
        this.shouldIncrementOpenId = shouldIncrementOpenId;
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mComponent.getStartupState()).thenReturn(TestUtils.createDefaultStartupStateBuilder()
            .withAttributionConfig(attributionConfig)
            .build());
        when(mComponent.getEventFirstOccurrenceService()).thenReturn(mEventFirstOccurrenceService);
        when(mComponent.getVitalComponentDataProvider()).thenReturn(vitalComponentDataProvider);
        mReportAppOpenHandler = new ReportAppOpenHandler(mComponent);
    }

    @Test
    public void testProcessHandleOpenEvent() {
        CounterReport counterReport = new CounterReport();
        counterReport.setValue(mInput);
        mReportAppOpenHandler.process(counterReport);
        verify(vitalComponentDataProvider, times(shouldIncrementOpenId ? 1 : 0)).incrementOpenId();
        if (mShouldHandleDeeplink) {
            verify(vitalComponentDataProvider, times(1)).incrementAttributionId();
            verify(mComponent, times(1)).resetConfigHolder();
            verify(mEventFirstOccurrenceService, times(1)).reset();
            assertThat(counterReport.getAttributionIdChanged()).isTrue();
        } else {
            verify(vitalComponentDataProvider, never()).incrementAttributionId();
            verify(mComponent, never()).resetConfigHolder();
            verifyNoMoreInteractions(mEventFirstOccurrenceService);
            assertThat(counterReport.getAttributionIdChanged()).isNull();
        }
    }

    @Test
    public void testProcessDoesNotBreakProcessing() {
        assertThat(mReportAppOpenHandler.process(new CounterReport())).isFalse();
    }
}
