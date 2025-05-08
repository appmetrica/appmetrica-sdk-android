package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class CounterReportMetaDataTest extends CommonTest {

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
    public static Collection<Object[]> getData() {
        return Arrays.asList(
            new Object[]{new Function<CounterReport, CounterReport>() {

                @Override
                public CounterReport apply(CounterReport input) {
                    return CounterReport.formAliveReportData(input);
                }
            }, InternalEvents.EVENT_TYPE_ALIVE, ""},
            new Object[]{new Function<CounterReport, CounterReport>() {

                @Override
                public CounterReport apply(CounterReport input) {
                    return CounterReport.formFeaturesReportData(input, "some value");
                }
            }, InternalEvents.EVENT_TYPE_APP_FEATURES, ""},
            new Object[]{new Function<CounterReport, CounterReport>() {

                @Override
                public CounterReport apply(CounterReport input) {
                    return CounterReport.formFirstEventReportData(input);
                }
            }, InternalEvents.EVENT_TYPE_FIRST_ACTIVATION, ""},
            new Object[]{new Function<CounterReport, CounterReport>() {

                @Override
                public CounterReport apply(CounterReport input) {
                    return CounterReport.formInitReportData(input);
                }
            }, InternalEvents.EVENT_TYPE_INIT, ""},
            new Object[]{new Function<CounterReport, CounterReport>() {

                @Override
                public CounterReport apply(CounterReport input) {
                    return CounterReport.formPermissionsReportData(
                        input,
                        new ArrayList<PermissionState>(),
                        null,
                        mock(AppStandbyBucketConverter.class),
                        new ArrayList<String>()
                    );
                }
            }, InternalEvents.EVENT_TYPE_PERMISSIONS, ""},
            new Object[]{new Function<CounterReport, CounterReport>() {

                @Override
                public CounterReport apply(CounterReport input) {
                    return CounterReport.formSessionStartReportData(input, mock(ExtraMetaInfoRetriever.class));
                }
            }, InternalEvents.EVENT_TYPE_START, ""},
            new Object[]{new Function<CounterReport, CounterReport>() {

                @Override
                public CounterReport apply(CounterReport input) {
                    return CounterReport.formUpdateReportData(input);
                }
            }, InternalEvents.EVENT_TYPE_APP_UPDATE, ""}
        );
    }

    @NonNull
    private final Function<CounterReport, CounterReport> reportProvider;
    @NonNull
    private final String expectedName;
    private final int expectedType;

    public CounterReportMetaDataTest(@NonNull Function<CounterReport, CounterReport> reportProvider,
                                     @NonNull InternalEvents expectedType,
                                     @NonNull String expectedName) {
        this.reportProvider = reportProvider;
        this.expectedName = expectedName;
        this.expectedType = expectedType.getTypeId();
    }

    @Test
    public void reportMetadata() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String originalValue = "original value";
        String originalProfileId = "original profile ID";
        String originalEventEnvironment = "original event environment";
        long originalElapsedRealtime = 7090;
        long originalCreationTimestamp = 666777;
        boolean attributionIdChanged = new Random().nextBoolean();
        final int openId = 8000;
        Bundle originalPayload = new Bundle();
        originalPayload.putInt("some key", 100);
        Map<String, byte[]> extras = Collections.singletonMap("key", new byte[]{1, 3, 5, 7});
        CounterReport originalReport = new CounterReport();
        originalReport.setType(InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        originalReport.setCustomType(InternalEvents.EVENT_TYPE_APP_OPEN.getTypeId());
        originalReport.setName("original event");
        originalReport.setValue(originalValue);
        originalReport.setEventEnvironment(originalEventEnvironment);
        originalReport.setAppEnvironment("original key", "original value");
        originalReport.setProfileID(originalProfileId);
        originalReport.setBytesTruncated(4);
        originalReport.setFirstOccurrenceStatus(FirstOccurrenceStatus.FIRST_OCCURRENCE);
        originalReport.setCreationEllapsedRealtime(originalElapsedRealtime);
        originalReport.setCreationTimestamp(originalCreationTimestamp);
        originalReport.setSource(EventSource.JS);
        originalReport.setPayload(originalPayload);
        originalReport.setAttributionIdChanged(attributionIdChanged);
        originalReport.setOpenId(openId);
        originalReport.setExtras(extras);
        ObjectPropertyAssertions(reportProvider.apply(originalReport))
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("name", expectedName)
            .checkFieldMatchPredicate("value", new Predicate<String>() {
                @Override
                public boolean test(String s) {
                    return !originalValue.equals(s);
                }
            })
            .checkField("eventEnvironment", originalEventEnvironment)
            .checkField("type", expectedType)
            .checkField("customType", 0)
            .checkField("appEnvironmentDiff", new Pair<String, String>("original key", "original value"))
            .checkField("bytesTruncated", 0)
            .checkField("profileID", originalProfileId)
            .checkField("creationElapsedRealtime", originalElapsedRealtime)
            .checkField("creationTimestamp", originalCreationTimestamp)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("source")
            .checkField("payload", originalPayload)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("extras", extras)
            .checkAll();
    }

    private static ComponentUnit getMockedComponentUnit() {
        final ComponentUnit componentUnit = mock(ComponentUnit.class);
        Context context = TestUtils.createMockedContext();
        when(componentUnit.getContext()).thenReturn(context);
        return componentUnit;
    }
}
