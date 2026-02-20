package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.impl.component.MainReporterComponentId;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.features.FeatureDescription;
import io.appmetrica.analytics.impl.features.FeatureDescriptionTest;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReportFeaturesHandlerTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Mock
    ComponentUnit mComponentUnit;
    @Mock
    VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    PreferencesComponentDbStorage mPreferencesComponentDbStorage;
    @Mock
    private EventSaver mEventSaver;
    ComponentId mComponentId;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mComponentId = new MainReporterComponentId(
            contextRule.getContext().getPackageName(),
            TestsData.generateApiKey()
        );
        doReturn(mComponentId).when(mComponentUnit).getComponentId();
        doReturn(true).when(vitalComponentDataProvider).isFirstEventDone();
        doReturn(vitalComponentDataProvider).when(mComponentUnit).getVitalComponentDataProvider();
        doReturn(mPreferencesComponentDbStorage).when(mComponentUnit).getComponentPreferences();
        doReturn(true).when(mComponentUnit).needToCollectFeatures();
        doReturn(mEventSaver).when(mComponentUnit).getEventSaver();
    }

    @Test
    public void testNewFeatures() {
        ReportFeaturesHandler handler = spy(new ReportFeaturesHandler(mComponentUnit));
        CounterReport report = mock(CounterReport.class);
        doReturn(new HashSet<FeatureDescription>(Collections.singletonList(new FeatureDescription(
            "feature_name",
            false
        )))).when(handler).parseFeaturesFromStorage();
        doReturn(new ArrayList<FeatureDescription>(Arrays.asList(
            new FeatureDescription("feature_name", false),
            new FeatureDescription("feature_name_2", 2, true)
        ))).when(handler).getFeaturesFromSystem();
        handler.process(report);
        verify(mEventSaver, times(1)).saveFeaturesReport(any(CounterReport.class));
        verify(mPreferencesComponentDbStorage, times(1)).putApplicationFeatures(anyString());
    }

    @Test
    public void testNoNewFeatures() {
        ReportFeaturesHandler handler = spy(new ReportFeaturesHandler(mComponentUnit));
        CounterReport report = mock(CounterReport.class);
        doReturn(new HashSet<FeatureDescription>(Collections.singletonList(new FeatureDescription(
            "feature_name",
            false
        )))).when(handler).parseFeaturesFromStorage();
        doReturn(new ArrayList<FeatureDescription>(Collections.singletonList(
            new FeatureDescription("feature_name", false)
        ))).when(handler).getFeaturesFromSystem();
        handler.process(report);
        verify(mComponentUnit, times(1)).markFeaturesChecked();
    }

    @Test
    public void testNoFeatures() {
        ReportFeaturesHandler handler = spy(new ReportFeaturesHandler(mComponentUnit));
        CounterReport report = mock(CounterReport.class);
        doReturn(null).when(handler).parseFeaturesFromStorage();
        doReturn(new ArrayList<FeatureDescription>()).when(handler).getFeaturesFromSystem();
        handler.process(report);
        verify(mEventSaver, times(1)).saveFeaturesReport(any(CounterReport.class));
        verify(mPreferencesComponentDbStorage, times(1)).putApplicationFeatures(eq("[]"));
    }

    @Test
    public void testEmptyJSONInStorage() {
        ReportFeaturesHandler handler = new ReportFeaturesHandler(mComponentUnit);
        doReturn("").when(mPreferencesComponentDbStorage).getApplicationFeatures();
        assertThat(handler.parseFeaturesFromStorage()).isNull();
    }

    @Test
    public void testNotEmptyJSON() throws JSONException {
        Random random = new Random();
        ReportFeaturesHandler handler = new ReportFeaturesHandler(mComponentUnit);
        String name = UUID.randomUUID().toString();
        int version = random.nextInt(100) + 10;
        boolean required = random.nextBoolean();
        doReturn(new JSONArray().put(new JSONObject()
            .put(FeatureDescriptionTest.NAME, name)
            .put(FeatureDescriptionTest.VERSION, version)
            .put(FeatureDescriptionTest.REQUIRED, required)
        ).toString()).when(mPreferencesComponentDbStorage).getApplicationFeatures();
        assertThat(handler.parseFeaturesFromStorage()).extracting(
            FeatureDescriptionTest.NAME,
            FeatureDescriptionTest.VERSION,
            FeatureDescriptionTest.REQUIRED
        ).containsOnly(Tuple.tuple(name, version, required));
    }

    @Test
    public void testIncorrectJSON() throws JSONException {
        ReportFeaturesHandler handler = new ReportFeaturesHandler(mComponentUnit);
        doReturn(new JSONArray().put(new JSONObject()
            .put(FeatureDescriptionTest.NAME, "name")
            .put(FeatureDescriptionTest.VERSION, 1)
            .put(FeatureDescriptionTest.REQUIRED, 134)
        ).toString()).when(mPreferencesComponentDbStorage).getApplicationFeatures();
        assertThat(handler.parseFeaturesFromStorage()).isNull();
    }
}
