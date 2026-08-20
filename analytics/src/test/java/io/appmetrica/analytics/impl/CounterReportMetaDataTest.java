package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

@SuppressLint("RobolectricUsage") // Parcelable
@RunWith(ParameterizedRobolectricTestRunner.class)
public class CounterReportMetaDataTest extends CommonTest {

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
    public static Collection<Object[]> getData() {
        return Arrays.asList(
            new Object[]{new Function<ServiceEvent, ServiceEvent>() {

                @Override
                public ServiceEvent apply(ServiceEvent serviceEvent) {
                    return ServiceEvent.formAliveReportData(serviceEvent);
                }
            }, InternalEvents.EVENT_TYPE_ALIVE, ""},
            new Object[]{new Function<ServiceEvent, ServiceEvent>() {

                @Override
                public ServiceEvent apply(ServiceEvent serviceEvent) {
                    return ServiceEvent.formFeaturesReportData(serviceEvent, "some value");
                }
            }, InternalEvents.EVENT_TYPE_APP_FEATURES, ""},
            new Object[]{new Function<ServiceEvent, ServiceEvent>() {

                @Override
                public ServiceEvent apply(ServiceEvent serviceEvent) {
                    return ServiceEvent.formFirstEventReportData(serviceEvent);
                }
            }, InternalEvents.EVENT_TYPE_FIRST_ACTIVATION, ""},
            new Object[]{new Function<ServiceEvent, ServiceEvent>() {

                @Override
                public ServiceEvent apply(ServiceEvent serviceEvent) {
                    return ServiceEvent.formInitReportData(serviceEvent);
                }
            }, InternalEvents.EVENT_TYPE_INIT, ""},
            new Object[]{new Function<ServiceEvent, ServiceEvent>() {

                @Override
                public ServiceEvent apply(ServiceEvent serviceEvent) {
                    return ServiceEvent.formPermissionsReportData(
                        serviceEvent,
                        new ArrayList<PermissionState>(),
                        null,
                        null,
                        new ArrayList<String>()
                    );
                }
            }, InternalEvents.EVENT_TYPE_PERMISSIONS, ""},
            new Object[]{new Function<ServiceEvent, ServiceEvent>() {

                @Override
                public ServiceEvent apply(ServiceEvent serviceEvent) {
                    return ServiceEvent.formSessionStartReportData(serviceEvent, null);
                }
            }, InternalEvents.EVENT_TYPE_START, ""},
            new Object[]{new Function<ServiceEvent, ServiceEvent>() {

                @Override
                public ServiceEvent apply(ServiceEvent serviceEvent) {
                    return ServiceEvent.formUpdateReportData(serviceEvent);
                }
            }, InternalEvents.EVENT_TYPE_APP_UPDATE, ""}
        );
    }

    @NonNull
    private final Function<ServiceEvent, ServiceEvent> reportProvider;
    @NonNull
    private final String expectedName;
    private final int expectedType;

    public CounterReportMetaDataTest(@NonNull Function<ServiceEvent, ServiceEvent> reportProvider,
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
        Bundle originalPayload = new Bundle();
        originalPayload.putInt("some key", 100);
        Map<String, byte[]> extras = Collections.singletonMap("key", new byte[]{1, 3, 5, 7});
        int valueProtocolVersion = 2;
        ServiceEvent originalServiceEvent = new ServiceEvent();
        originalServiceEvent.setType(InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        originalServiceEvent.setCustomType(InternalEvents.EVENT_TYPE_APP_OPEN.getTypeId());
        originalServiceEvent.setName("original event");
        originalServiceEvent.setValue(originalValue);
        originalServiceEvent.setEventEnvironment(originalEventEnvironment);
        originalServiceEvent.setProfileID(originalProfileId);
        originalServiceEvent.setBytesTruncated(4);
        originalServiceEvent.setCreationElapsedRealtime(originalElapsedRealtime);
        originalServiceEvent.setCreationTimestamp(originalCreationTimestamp);
        originalServiceEvent.setSource(EventSource.JS);
        originalServiceEvent.setPayload(originalPayload);
        originalServiceEvent.setExtras(extras);
        originalServiceEvent.setValueProtocolVersion(valueProtocolVersion);
        ServiceEvent resultServiceEvent = reportProvider.apply(originalServiceEvent);
        Assertions.INSTANCE.ObjectPropertyAssertions(resultServiceEvent)
            .withIgnoredFields("systemTimeProvider", "value", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("name", expectedName)
            .checkField("eventEnvironment", originalEventEnvironment)
            .checkField("type", expectedType)
            .checkField("customType", 0)
            .checkField("bytesTruncated", 0)
            .checkField("profileID", originalProfileId)
            .checkField("creationElapsedRealtime", originalElapsedRealtime)
            .checkField("creationTimestamp", originalCreationTimestamp)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("source")
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("payload", originalPayload)
            .checkField("extras", extras)
            .checkField("valueProtocolVersion", valueProtocolVersion)
            .checkAll();
        if (resultServiceEvent.getValue() != null) {
            org.assertj.core.api.Assertions.assertThat(resultServiceEvent.getValue()).isNotEqualTo(originalValue);
        }
    }
}
