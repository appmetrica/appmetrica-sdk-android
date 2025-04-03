package io.appmetrica.analytics.impl;

import android.util.Base64;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.PreloadInfo;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoWrapper;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class EventsManagerTest extends CommonTest {

    public static final String PAYLOAD_CRASH_ID = "payload_crash_id";

    @Mock
    private PublicLogger mPublicLogger;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCustomEventReportEntryShouldContainsCustomReportType() {
        ModuleEvent moduleEvent = ModuleEvent.newBuilder(new Random().nextInt()).build();
        assertThat(EventsManager.customEventReportEntry(moduleEvent, mPublicLogger).getType())
            .isEqualTo(InternalEvents.EVENT_TYPE_CUSTOM_EVENT.getTypeId());
    }

    @Test
    public void testCustomEventReportEntryShouldContainsExpectedSubtype() {
        int type = new Random().nextInt();
        CounterReport report = EventsManager.customEventReportEntry(
            ModuleEvent.newBuilder(type).build(),
            mPublicLogger
        );
        assertThat(report.getCustomType()).isEqualTo(type);
    }

    @Test
    public void testCustomEventReportEntryShouldContainsExpectedName() {
        String name = new RandomStringGenerator(20).nextString();
        CounterReport report = EventsManager.customEventReportEntry(
            ModuleEvent.newBuilder(new Random().nextInt()).withName(name).build(),
            mPublicLogger
        );
        assertThat(report.getName()).isEqualTo(name);
    }

    @Test
    public void testCustomEventReportEntryShouldContainsExpectedValue() {
        String value = new RandomStringGenerator(1000).nextString();
        CounterReport report = EventsManager.customEventReportEntry(
            ModuleEvent.newBuilder(new Random().nextInt()).withValue(value).build(),
            mPublicLogger
        );
        assertThat(report.getValue()).isEqualTo(value);
    }

    @Test
    public void testCustomEventReportEntryShouldContainsExpectedEnvironmentValues() throws Exception {
        String envKey = new RandomStringGenerator(20).nextString();
        String envValue = new RandomStringGenerator(100).nextString();
        Map<String, Object> environment = new HashMap<String, Object>();
        environment.put(envKey, envValue);
        CounterReport report = EventsManager.customEventReportEntry(
            ModuleEvent.newBuilder(new Random().nextInt()).withEnvironment(environment).build(),
            mPublicLogger
        );
        JSONObject jsonObject = new JSONObject(report.getEventEnvironment());
        assertThat(jsonObject.keys().next()).isEqualTo(envKey);
        assertThat(jsonObject.get(envKey)).isEqualTo(envValue);
    }

    @Test
    public void customEventReportEntryExtras() throws Exception {
        Map<String, byte[]> extras = Collections.singletonMap("key", new byte[]{2, 6, 8});
        CounterReport report = EventsManager.customEventReportEntry(
            ModuleEvent.newBuilder(new Random().nextInt()).withExtras(extras).build(),
            mPublicLogger
        );
        assertThat(report.getExtras()).isEqualTo(extras);
    }

    @Test
    public void testOpenAppReportEntryShouldContainsExpectedValue() throws JSONException {
        String link = "some://link/deep";
        boolean auto = new Random().nextBoolean();
        CounterReport report = EventsManager.openAppReportEntry(link, auto, mPublicLogger);
        JSONAssert.assertEquals(
            new JSONObject()
                .put("link", link)
                .put("type", "open")
                .put("auto", auto)
                .toString(),
            report.getValue(),
            true
        );
    }

    @Test
    public void referralUrlEntry() throws JSONException {
        String link = "some://link/deep";
        CounterReport report = EventsManager.referralUrlReportEntry(link, mPublicLogger);
        JSONAssert.assertEquals(
            new JSONObject()
                .put("link", link)
                .put("type", "referral")
                .put("auto", false)
                .toString(),
            report.getValue(),
            true
        );
    }

    @Test
    public void customErrorEntry() {
        String message = "mes";
        byte[] value = "somevalue".getBytes();
        CounterReport report = EventsManager.customErrorReportEntry(message, value, mPublicLogger);
        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(report.getName()).as("message").isEqualTo(message);
        soft.assertThat(report.getValueBytes()).as("value").isEqualTo(value);
        soft.assertThat(report.getType()).as("type").isEqualTo(
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF.getTypeId()
        );

        soft.assertAll();
    }

    @Test
    public void testUnhandledExceptionReportEntry() {
        final String name = "name";
        final String value = "value";
        final int bytesTruncated = 200;
        final String errorEnvironment = "error env";
        final HashMap<ClientCounterReport.TrimmedField, Integer> trimmedFields = new HashMap<ClientCounterReport.TrimmedField, Integer>();
        trimmedFields.put(ClientCounterReport.TrimmedField.VALUE, 200);
        ClientCounterReport clientCounterReport = (ClientCounterReport) EventsManager
            .unhandledExceptionFromFileReportEntry(name, value.getBytes(), bytesTruncated, trimmedFields, errorEnvironment, mPublicLogger);
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(clientCounterReport.getName()).isEqualTo(name);
        assertions.assertThat(clientCounterReport.getValueBytes()).isEqualTo(value.getBytes());
        assertions.assertThat(clientCounterReport.getType()).isEqualTo(InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE.getTypeId());
        assertions.assertThat(clientCounterReport.getBytesTruncated()).isEqualTo(bytesTruncated);
        assertions.assertThat(clientCounterReport.getTrimmedFields()).isEqualTo(trimmedFields);
        assertions.assertThat(clientCounterReport.getEventEnvironment()).isEqualTo(errorEnvironment);
        assertions.assertAll();
    }

    @Test
    public void testCleanupEventReportEntry() {
        final String value = "value";
        final CounterReport report = EventsManager.cleanupEventReportEntry(value, mPublicLogger);
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(report.getValue()).isEqualTo(value);
        assertions.assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_CLEANUP.getTypeId());
        assertions.assertAll();
    }

    @Test
    public void testCurrentSessionCrashpadCrashEntry() {
        String value = new String(Base64.encode("native crash".getBytes(), Base64.DEFAULT));
        CounterReport report = EventsManager.currentSessionNativeCrashEntry(value, "uuid", mPublicLogger);
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF.getTypeId());
        assertions.assertThat(report.getValue()).isEqualTo(value);
        assertions.assertThat(report.getName()).isEmpty();
        assertions.assertThat(report.getPayload().getString(PAYLOAD_CRASH_ID)).isEqualTo("uuid");
        assertions.assertAll();
    }

    @Test
    public void testPrevSessionCrashpadCrashEntry() {
        String value = new String(Base64.encode("native crash".getBytes(), Base64.DEFAULT));
        CounterReport report = EventsManager.prevSessionNativeCrashEntry(value, "uuid", mPublicLogger);
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF.getTypeId());
        assertions.assertThat(report.getValue()).isEqualTo(value);
        assertions.assertThat(report.getName()).isEmpty();
        assertions.assertThat(report.getPayload().getString(PAYLOAD_CRASH_ID)).isEqualTo("uuid");
        assertions.assertAll();
    }

    @Test
    public void testAnrEntry() {
        byte[] value = "value".getBytes();
        CounterReport clientCounterReport = EventsManager.anrEntry(value, mPublicLogger);
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(clientCounterReport.getName()).isEmpty();
        assertions.assertThat(clientCounterReport.getValueBytes()).isEqualTo(value);
        assertions.assertThat(clientCounterReport.getType()).isEqualTo(InternalEvents.EVENT_TYPE_ANR.getTypeId());
        assertions.assertAll();
    }

    @Test
    public void testActivationEventReportEntry() throws JSONException {
        final String trackingId = "487568768";
        final String key1 = "key 1";
        final String key2 = "key 2";
        final String value1 = "value 1";
        final String value2 = "value 2";
        String userProfileId = "user_profile_id";
        final boolean autoTracking = new Random().nextBoolean();
        PreloadInfoWrapper preloadInfoWrapper = new PreloadInfoWrapper(PreloadInfo.newBuilder(trackingId)
            .setAdditionalParams(key1, value1)
            .setAdditionalParams(key2, value2)
            .build(),
            mPublicLogger,
            autoTracking);
        CounterReport report = EventsManager.activationEventReportEntry(
            preloadInfoWrapper,
            userProfileId,
            mPublicLogger
        );
        assertThat(report.getName()).isEmpty();
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_ACTIVATION.getTypeId());
        assertThat(report.getProfileID()).isEqualTo(userProfileId);
        JSONAssert.assertEquals(new JSONObject().put("preloadInfo",
            new JSONObject()
                .put("trackingId", trackingId)
                .put("additionalParams", new JSONObject().put(key1, value1).put(key2, value2))
                .put("wasSet", true)
                .put("autoTracking", autoTracking)
                .put("source", DistributionSource.APP.getDescription())
        ).toString(), report.getValue(), true);
    }

    @Test
    public void testActivationEventReportEntryNullPreloadInfo() throws JSONException {
        CounterReport report = EventsManager.activationEventReportEntry(
            null,
            "user_profile_id",
            mPublicLogger
        );
        assertThat(report.getName()).isEmpty();
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_ACTIVATION.getTypeId());
        JSONAssert.assertEquals(new JSONObject().toString(), report.getValue(), true);
    }

    @Test
    public void requestReferrerEntry() {
        CounterReport report = EventsManager.requestReferrerEntry(mPublicLogger);
        assertThat(report.getName()).isEmpty();
        assertThat(report.getValue()).isEmpty();
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_REQUEST_REFERRER.getTypeId());
    }

    @Test
    public void testActivationEventReportEntryNullProfileID() {
        CounterReport report = EventsManager.activationEventReportEntry(null, null, mPublicLogger);
        assertThat(report.getProfileID()).isNull();
    }

    @Test
    public void setSessionExtraReportEntry() {
        String key = "Key";
        byte[] value = new byte[]{1, 4, 7};
        CounterReport report = EventsManager.setSessionExtraReportEntry(key, value, mPublicLogger);
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA.getTypeId());
        assertThat(report.getName()).isNullOrEmpty();
        assertThat(report.getValue()).isNullOrEmpty();
        assertThat(report.getExtras()).containsExactlyEntriesOf(Collections.singletonMap(key, value));
    }

    @Test
    public void clientExternalAttributionEntry() {
        byte[] value = new byte[]{1, 4, 7};
        CounterReport report = EventsManager.clientExternalAttributionEntry(value, mPublicLogger);
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION.getTypeId());
        assertThat(report.getName()).isEmpty();
        assertThat(report.getValueBytes()).isEqualTo(value);
    }

    @Test
    public void eventAndGlobalNumber() {
        assertThat(InternalEvents.values()).filteredOn(new Predicate<InternalEvents>() {
            @Override
            public boolean test(InternalEvents internalEvents) {
                return EventsManager.shouldGenerateGlobalNumber(internalEvents.getTypeId());
            }
        }).containsOnly(
            InternalEvents.EVENT_TYPE_UNDEFINED,
            InternalEvents.EVENT_TYPE_INIT,
            InternalEvents.EVENT_TYPE_REGULAR,
            InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME,
            InternalEvents.EVENT_TYPE_ALIVE,
            InternalEvents.EVENT_TYPE_SEND_USER_PROFILE,
            InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID,
            InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT,
            InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT,
            InternalEvents.EVENT_TYPE_PURGE_BUFFER,
            InternalEvents.EVENT_TYPE_STARTUP,
            InternalEvents.EVENT_TYPE_SEND_REFERRER,
            InternalEvents.EVENT_TYPE_REQUEST_REFERRER,
            InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED,
            InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF,
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF,
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF,
            InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
            InternalEvents.EVENT_TYPE_ANR,
            InternalEvents.EVENT_TYPE_ACTIVATION,
            InternalEvents.EVENT_TYPE_FIRST_ACTIVATION,
            InternalEvents.EVENT_TYPE_START,
            InternalEvents.EVENT_TYPE_CUSTOM_EVENT,
            InternalEvents.EVENT_TYPE_APP_OPEN,
            InternalEvents.EVENT_TYPE_APP_UPDATE,
            InternalEvents.EVENT_TYPE_PERMISSIONS,
            InternalEvents.EVENT_TYPE_APP_FEATURES,
            InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG,
            InternalEvents.EVENT_TYPE_CLEANUP,
            InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT,
            InternalEvents.EVENT_TYPE_WEBVIEW_SYNC,
            InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA,
            InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION,
            InternalEvents.EVENT_TYPE_ANR
        );
        assertThat(InternalEvents.values()).filteredOn(new Predicate<InternalEvents>() {
            @Override
            public boolean test(InternalEvents internalEvents) {
                return !EventsManager.shouldGenerateGlobalNumber(internalEvents.getTypeId());
            }
        }).containsOnly(
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF
        );
    }

    @Test
    public void eventPublicForLogs() {
        assertThat(InternalEvents.values()).filteredOn(new Predicate<InternalEvents>() {
            @Override
            public boolean test(InternalEvents internalEvents) {
                return EventsManager.isPublicForLogs(internalEvents.getTypeId());
            }
        }).containsOnly(
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF,
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF,
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF,
            InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
            InternalEvents.EVENT_TYPE_REGULAR,
            InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION,
            InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT,
            InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT,
            InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT,
            InternalEvents.EVENT_TYPE_PURGE_BUFFER,
            InternalEvents.EVENT_TYPE_INIT,
            InternalEvents.EVENT_TYPE_SEND_USER_PROFILE,
            InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID,
            InternalEvents.EVENT_TYPE_SEND_REFERRER,
            InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED,
            InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED,
            InternalEvents.EVENT_TYPE_FIRST_ACTIVATION,
            InternalEvents.EVENT_TYPE_START,
            InternalEvents.EVENT_TYPE_APP_OPEN,
            InternalEvents.EVENT_TYPE_APP_UPDATE,
            InternalEvents.EVENT_TYPE_ANR
        );

        assertThat(InternalEvents.values()).filteredOn(internalEvents -> !EventsManager.isPublicForLogs(internalEvents.getTypeId())).containsOnly(
            InternalEvents.EVENT_TYPE_UNDEFINED,
            InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME,
            InternalEvents.EVENT_TYPE_STARTUP,
            InternalEvents.EVENT_TYPE_REQUEST_REFERRER,
            InternalEvents.EVENT_TYPE_CUSTOM_EVENT,
            InternalEvents.EVENT_TYPE_PERMISSIONS,
            InternalEvents.EVENT_TYPE_APP_FEATURES,
            InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG,
            InternalEvents.EVENT_TYPE_CLEANUP,
            InternalEvents.EVENT_TYPE_WEBVIEW_SYNC,
            InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA,
            InternalEvents.EVENT_TYPE_ALIVE,
            InternalEvents.EVENT_TYPE_ACTIVATION
        );
    }

    @Test
    public void eventAndSessionState() {
        assertThat(InternalEvents.values()).filteredOn(new Predicate<InternalEvents>() {
            @Override
            public boolean test(InternalEvents internalEvents) {
                return EventsManager.affectSessionState(internalEvents);
            }
        }).containsOnly(
            InternalEvents.EVENT_TYPE_REGULAR,
            InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME,
            InternalEvents.EVENT_TYPE_SEND_USER_PROFILE,
            InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID,
            InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT,
            InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT,
            InternalEvents.EVENT_TYPE_REQUEST_REFERRER,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT,
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF,
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF,
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF,
            InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
            InternalEvents.EVENT_TYPE_ANR,
            InternalEvents.EVENT_TYPE_FIRST_ACTIVATION,
            InternalEvents.EVENT_TYPE_START,
            InternalEvents.EVENT_TYPE_CUSTOM_EVENT,
            InternalEvents.EVENT_TYPE_APP_OPEN,
            InternalEvents.EVENT_TYPE_PERMISSIONS,
            InternalEvents.EVENT_TYPE_APP_FEATURES,
            InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG,
            InternalEvents.EVENT_TYPE_CLEANUP,
            InternalEvents.EVENT_TYPE_INIT,
            InternalEvents.EVENT_TYPE_ALIVE,
            InternalEvents.EVENT_TYPE_STARTUP,
            InternalEvents.EVENT_TYPE_APP_UPDATE,
            InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT,
            InternalEvents.EVENT_TYPE_WEBVIEW_SYNC,
            InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION
        );

        assertThat(InternalEvents.values()).filteredOn(new Predicate<InternalEvents>() {
            @Override
            public boolean test(InternalEvents internalEvents) {
                return !EventsManager.affectSessionState(internalEvents);
            }
        }).containsOnly(
            InternalEvents.EVENT_TYPE_UNDEFINED,
            InternalEvents.EVENT_TYPE_PURGE_BUFFER,
            InternalEvents.EVENT_TYPE_SEND_REFERRER,
            InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED,
            InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED,
            InternalEvents.EVENT_TYPE_ACTIVATION,
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
            InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA
        );
    }

    @Test
    public void eventIdIsUnique() {
        InternalEvents[] eventTypes = InternalEvents.values();
        Set<Integer> identifiersSet = new HashSet<Integer>();
        for (InternalEvents internalEvents : eventTypes) {
            identifiersSet.add(internalEvents.getTypeId());
        }

        assertThat(eventTypes.length).isEqualTo(identifiersSet.size());
    }

    private String generateString(int size) {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(size);
        return randomStringGenerator.nextString();
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class ShouldGenerateGlobalNumberTest {

        private static final List<Integer> EVENT_TYPES_WITHOUT_GLOBAL_NUMBER = Arrays.asList(
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF.getTypeId()
        );

        @ParameterizedRobolectricTestRunner.Parameters(name = "For arguments {0} expected value is {1}")
        public static Collection<Object[]> data() {
            List<Object[]> data = new ArrayList<Object[]>();
            for (InternalEvents internalEvent : InternalEvents.values()) {
                data.add(new Object[]{internalEvent.getTypeId(), EVENT_TYPES_WITHOUT_GLOBAL_NUMBER.contains(internalEvent.getTypeId()) == false});
            }
            return data;
        }

        private final int mEventType;
        private final boolean mExpected;

        public ShouldGenerateGlobalNumberTest(final int eventType, final boolean expected) {
            mEventType = eventType;
            mExpected = expected;
        }

        @Test
        public void testShouldGenerateGlobalNumber() {
            assertThat(EventsManager.shouldGenerateGlobalNumber(mEventType)).isEqualTo(mExpected);
        }
    }

}
