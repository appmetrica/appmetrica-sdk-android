package io.appmetrica.analytics.impl;

import android.util.Pair;
import io.appmetrica.analytics.impl.utils.MeasuredJsonMap;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppEnvironmentTest extends CommonTest {

    @Mock
    private PublicLogger mPublicLogger;
    @Mock
    private SimpleMapLimitation mSimpleMapLimitation;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRevisionChangedAfterAddingSomeItems() {
        AppEnvironment environment = new AppEnvironment("{\"a\":\"2\"}", 1000, mSimpleMapLimitation);
        when(mSimpleMapLimitation.tryToAddValue(any(MeasuredJsonMap.class), anyString(), anyString())).thenReturn(true);
        environment.add("a", "1");
        environment.add("key1", "value");
        environment.getLastRevision();
        environment.add("key2", "value");
        environment.getLastRevision();
        environment.add("key3", "value");
        assertThat(environment.getLastRevision().revisionNumber).isEqualTo(1003);

    }

    @Test
    public void testRevisionChangedIfAdded() {
        AppEnvironment environment = new AppEnvironment("", 0, mSimpleMapLimitation);
        when(mSimpleMapLimitation.tryToAddValue(any(MeasuredJsonMap.class), eq("key"), eq("value"))).thenReturn(true);
        environment.add("key", "value");
        assertThat(environment.getLastRevision().revisionNumber).isEqualTo(1);
    }

    @Test
    public void testGetLastRevision() throws Exception {
        AppEnvironment environment = new AppEnvironment("", 0, mSimpleMapLimitation);
        when(mSimpleMapLimitation.tryToAddValue(any(MeasuredJsonMap.class), anyString(), anyString()))
                .then(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {
                        MeasuredJsonMap map = invocation.getArgument(0);
                        String key = invocation.getArgument(1);
                        String value = invocation.getArgument(2);
                        map.put(key, value);
                        return true;
                    }
                });
        String firstKey = "first key";
        String firstValue = "first value";
        String secondKey = "key1@#$%^&*<>~:";
        String secondValue = "value1@#$%^&*<>~:";
        environment.add(firstKey, firstValue);
        environment.add(secondKey, secondValue);
        AppEnvironment.EnvironmentRevision environmentRevision = environment.getLastRevision();
        assertThat(environmentRevision.revisionNumber).isEqualTo(1L);
        JSONObject expectedJson = new JSONObject().put(firstKey, firstValue).put(secondKey, secondValue);
        JSONAssert.assertEquals(
                expectedJson,
                new JSONObject(environmentRevision.value),
                true
        );
    }

    @Test
    public void testRevisionChangedOnce() {
        AppEnvironment environment = new AppEnvironment("", 0, mSimpleMapLimitation);
        when(mSimpleMapLimitation.tryToAddValue(any(MeasuredJsonMap.class), eq("key"), eq("value"))).thenReturn(true);
        environment.add("key", "value");
        environment.getLastRevision();
        assertThat(environment.getLastRevision().revisionNumber).isEqualTo(1);
    }

    @Test
    public void testRevisionNotChangedIfNotAdded() {
        AppEnvironment environment = new AppEnvironment("", 0, mSimpleMapLimitation);
        when(mSimpleMapLimitation.tryToAddValue(any(MeasuredJsonMap.class), eq("key"), eq("value"))).thenReturn(false);
        environment.add("key", "value");
        assertThat(environment.getLastRevision().revisionNumber).isEqualTo(0);
    }

    @Test
    public void testEnvironmentWasReset() {
        AppEnvironment environment = new AppEnvironment("{}", 0, mPublicLogger);
        environment.add("a", "1");
        environment.getLastRevision();
        assertThat(environment.getLastRevision().revisionNumber).isEqualTo(1);
        environment.reset();
        assertThat(environment.getLastRevision().revisionNumber).isEqualTo(1);
        environment.add("a", "2");
        environment.add("a", "1");
        environment.add("a", "1");
        assertThat(environment.getLastRevision().revisionNumber).isEqualTo(2);
    }

    @Test
    public void testAdd() {
        AppEnvironment environment = new AppEnvironment("{\"b\":\"2\"}", 0, mSimpleMapLimitation);
        environment.add("c", "3");
        verify(mSimpleMapLimitation).tryToAddValue(environment.getValues(), "c", "3");
    }

    @Test
    public void testAddPair() {
        AppEnvironment environment = new AppEnvironment("{\"b\":\"2\"}", 0, mSimpleMapLimitation);
        environment.add(new Pair<String, String>("c", "3"));
        verify(mSimpleMapLimitation).tryToAddValue(environment.getValues(), "c", "3");
    }

    @Test
    public void toStringMatchConditions() {
        AppEnvironment environment = new AppEnvironment("{\"b\":\"2\"}", 10, mSimpleMapLimitation);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(environment.toString())
                .contains("Map size 1.")
                .contains("Is changed false.")
                .contains("Current revision 10");
        assertions.assertAll();
    }
}
