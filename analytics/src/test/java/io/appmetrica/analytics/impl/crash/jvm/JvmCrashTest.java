package io.appmetrica.analytics.impl.crash.jvm;

import android.util.Base64;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ClientCounterReport;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class JvmCrashTest extends CommonTest {

    private static final String KEY_EVENT = "event";
    private static final String KEY_BYTES_TRUNCATED = "bytes_truncated";
    private static final String KEY_TRIMMED_FIELDS = "trimmed_fields";
    private static final String KEY_JVM_CRASH = "jvm_crash";
    private static final String KEY_EVENT_NAME = "name";
    private static final String KEY_PROCESS_CONFIGURATION = "process_configuration";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String KEY_PID = "pid";
    private static final String KEY_PSID = "psid";
    private static final String KEY_REPORTER_CONFIGURATION = "reporter_configuration";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_REPORTER_TYPE = "reporter_type";
    private static final String KEY_ENVIRONMENT = "environment";

    @Mock
    private CounterConfiguration reporterConfiguration;
    @Mock
    private ProcessConfiguration processConfiguration;
    @Mock
    private ClientConfiguration clientConfiguration;
    private final String mCrashName = "crash_name";
    private final String mCrashValue = "crash_value";
    private final String mErrorEnvironment = "error env";
    private final int mPid = 1022;
    private final String mPsid = "random_psid";
    private final String mPackageName = "pack_name";
    private final String mApiKey = TestsData.generateApiKey();
    private final CounterConfigurationReporterType mReporterType = CounterConfigurationReporterType.MAIN;
    private final int mBytesTruncated = 200;
    private final HashMap<ClientCounterReport.TrimmedField, Integer> mTrimmedFields = new HashMap<ClientCounterReport.TrimmedField, Integer>();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(processConfiguration).when(clientConfiguration).getProcessConfiguration();
        doReturn(reporterConfiguration).when(clientConfiguration).getReporterConfiguration();
        doReturn(mPid).when(processConfiguration).getProcessID();
        doReturn(mPsid).when(processConfiguration).getProcessSessionID();
        doReturn(mPackageName).when(processConfiguration).getPackageName();
        doReturn(mApiKey).when(reporterConfiguration).getApiKey();
        doReturn(mReporterType).when(reporterConfiguration).getReporterType();
        mTrimmedFields.put(ClientCounterReport.TrimmedField.VALUE, 100);
    }

    @Test
    public void testToJSON() throws JSONException {
        JvmCrash crash = new JvmCrash(EventsManager.unhandledExceptionFromFileReportEntry(
                mCrashName,
                mCrashValue.getBytes(),
                mBytesTruncated,
                mTrimmedFields,
                mErrorEnvironment,
                mock(PublicLogger.class)
        ), clientConfiguration, mTrimmedFields);

        JSONObject object = new JSONObject(crash.toJSONString());

        SoftAssertions softly = new SoftAssertions();
        JSONObject event = object.getJSONObject(KEY_EVENT);
        softly.assertThat(event.get(KEY_JVM_CRASH)).isEqualTo(Base64.encodeToString(mCrashValue.getBytes(), 0));
        softly.assertThat(event.get(KEY_EVENT_NAME)).isEqualTo(mCrashName);
        softly.assertThat(event.get(KEY_BYTES_TRUNCATED)).isEqualTo(mBytesTruncated);
        softly.assertThat(event.get(KEY_TRIMMED_FIELDS)).isEqualTo("{\"VALUE\":100}");
        softly.assertThat(event.get(KEY_ENVIRONMENT)).isEqualTo(mErrorEnvironment);

        JSONObject process = object.getJSONObject(KEY_PROCESS_CONFIGURATION);
        softly.assertThat(process.get(KEY_PID)).isEqualTo(mPid);
        softly.assertThat(process.get(KEY_PSID)).isEqualTo(mPsid);
        softly.assertThat(process.get(KEY_PACKAGE_NAME)).isEqualTo(mPackageName);

        JSONObject reporter = object.getJSONObject(KEY_REPORTER_CONFIGURATION);
        softly.assertThat(reporter.get(KEY_API_KEY)).isEqualTo(mApiKey);
        softly.assertThat(reporter.get(KEY_REPORTER_TYPE)).isEqualTo("main");

        softly.assertAll();
    }

    @Test
    public void testAllFieldsFilled() throws Exception {
        JvmCrash crash = new JvmCrash(EventsManager.unhandledExceptionFromFileReportEntry(
                mCrashName,
                mCrashValue.getBytes(),
                mBytesTruncated,
                mTrimmedFields,
                mErrorEnvironment,
                mock(PublicLogger.class)
        ), clientConfiguration, mTrimmedFields);
        ObjectPropertyAssertions<JvmCrash> assertions = ObjectPropertyAssertions(crash).withDeclaredAccessibleFields(true)
                .withIgnoredFields("trimmedFields");
        assertions.checkField("crash", "getCrashValue", mCrashValue.getBytes());
        assertions.checkField("name", "getName", mCrashName);
        assertions.checkField("bytesTruncated", "getBytesTruncated", mBytesTruncated);
        assertions.checkField("errorEnvironment", "getEnvironment", mErrorEnvironment);
        assertions.checkField("pid", "getPid", mPid);
        assertions.checkField("psid", "getPsid", mPsid);
        assertions.checkField("packageName", "getPackageName", mPackageName);
        assertions.checkField("apiKey", "getApiKey", mApiKey);
        assertions.checkField("reporterType", "getReporterType", CounterConfigurationReporterType.MAIN);
        assertions.checkAll();

        assertThat(crash.getTrimmedFields()).containsOnly(new AbstractMap.SimpleEntry<ClientCounterReport.TrimmedField, Integer>(ClientCounterReport.TrimmedField.VALUE, 100));
    }

    @Test
    public void testFromJSON() throws Exception {
        JvmCrash crash = new JvmCrash(
                new JSONObject().put(
                        KEY_PROCESS_CONFIGURATION,
                        new JSONObject().put(KEY_PID, mPid).put(KEY_PSID, mPsid).put(KEY_PACKAGE_NAME, mPackageName)
                ).put(
                        KEY_REPORTER_CONFIGURATION,
                        new JSONObject().put(KEY_API_KEY, mApiKey).put(KEY_REPORTER_TYPE, "main")
                ).put(
                        KEY_EVENT,
                        new JSONObject()
                                .put(KEY_JVM_CRASH, Base64.encodeToString(mCrashValue.getBytes(), 0))
                                .put(KEY_EVENT_NAME, mCrashName)
                                .put(KEY_BYTES_TRUNCATED, mBytesTruncated)
                                .put(KEY_ENVIRONMENT, mErrorEnvironment)
                                .put(KEY_TRIMMED_FIELDS, new JSONObject().put("VALUE", 100))
                ).toString()
        );

        ObjectPropertyAssertions<JvmCrash> assertions = ObjectPropertyAssertions(crash).withDeclaredAccessibleFields(true)
                .withIgnoredFields("trimmedFields");
        assertions.checkField("crash", "getCrashValue", mCrashValue.getBytes());
        assertions.checkField("name", "getName", mCrashName);
        assertions.checkField("bytesTruncated", "getBytesTruncated", mBytesTruncated);
        assertions.checkField("errorEnvironment", "getEnvironment", mErrorEnvironment);
        assertions.checkField("pid", "getPid", mPid);
        assertions.checkField("psid", "getPsid", mPsid);
        assertions.checkField("packageName", "getPackageName", mPackageName);
        assertions.checkField("apiKey", "getApiKey", mApiKey);
        assertions.checkField("reporterType", "getReporterType", CounterConfigurationReporterType.MAIN);
        assertions.checkAll();

        assertThat(crash.getTrimmedFields()).containsOnly(new AbstractMap.SimpleEntry<ClientCounterReport.TrimmedField, Integer>(ClientCounterReport.TrimmedField.VALUE, 100));
    }

    @Test
    public void testFromJsonNullable() throws Exception {
        JvmCrash crash = new JvmCrash(
                new JSONObject().put(
                        KEY_PROCESS_CONFIGURATION,
                        new JSONObject().put(KEY_PID, mPid).put(KEY_PSID, mPsid).put(KEY_PACKAGE_NAME, mPackageName)
                ).put(
                        KEY_REPORTER_CONFIGURATION,
                        new JSONObject().put(KEY_API_KEY, mApiKey).put(KEY_REPORTER_TYPE, mReporterType.getStringValue())
                ).put(
                        KEY_EVENT,
                        new JSONObject()
                                .put(KEY_JVM_CRASH, Base64.encodeToString(mCrashValue.getBytes(), 0))
                                .put(KEY_EVENT_NAME, mCrashName)
                                .put(KEY_BYTES_TRUNCATED, mBytesTruncated)
                ).toString()
        );

        ObjectPropertyAssertions<JvmCrash> assertions = ObjectPropertyAssertions(crash).withDeclaredAccessibleFields(true);
        assertions.checkField("crash", "getCrashValue", mCrashValue.getBytes());
        assertions.checkField("name", "getName", mCrashName);
        assertions.checkField("bytesTruncated", "getBytesTruncated", mBytesTruncated);
        assertions.checkField("errorEnvironment", "getEnvironment", (String) null);
        assertions.checkField("pid", "getPid", mPid);
        assertions.checkField("psid", "getPsid", mPsid);
        assertions.checkField("packageName", "getPackageName", mPackageName);
        assertions.checkField("apiKey", "getApiKey", mApiKey);
        assertions.checkField("trimmedFields", "getTrimmedFields", new HashMap<String, String>());
        assertions.checkField("reporterType", "getReporterType", CounterConfigurationReporterType.MAIN);
        assertions.checkAll();
    }

    @Test
    public void testSerializeAndDeserialize() throws JSONException {
        JvmCrash crash = new JvmCrash(EventsManager.unhandledExceptionFromFileReportEntry(
                mCrashName,
                mCrashValue.getBytes(),
                mBytesTruncated,
                mTrimmedFields,
                mErrorEnvironment,
                mock(PublicLogger.class)
        ), clientConfiguration, mTrimmedFields);
        assertThat(new JvmCrash(crash.toJSONString())).usingRecursiveComparison().isEqualTo(crash);
    }

    @Test
    public void testIsMain() throws JSONException {
        assertThat(
                new JvmCrash(
                        fillJson(
                                new JSONObject().put(KEY_API_KEY, "api").put("is_main", true).put("is_commutation", false)
                        ).toString()
                ).getReporterType()
        ).isEqualTo(CounterConfigurationReporterType.MAIN);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class ReadReporterTypeTest {

        @ParameterizedRobolectricTestRunner.Parameters(name = "Report type: {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"main", CounterConfigurationReporterType.MAIN},
                    {"crash", CounterConfigurationReporterType.CRASH},
                    {"manual", CounterConfigurationReporterType.MANUAL},
                    {"commutation", CounterConfigurationReporterType.COMMUTATION},
                    {"self_diagnostic_main", CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN},
                    {"self_diagnostic_manual", CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL}
            });
        }

        private final String mJson;
        private final CounterConfigurationReporterType mExpected;

        public ReadReporterTypeTest(@NonNull String jsonValue, @NonNull CounterConfigurationReporterType reporterType) throws JSONException {
            mExpected = reporterType;
            mJson = fillJson(new JSONObject().put(KEY_API_KEY, "api").put(KEY_REPORTER_TYPE, jsonValue)).toString();
        }

        @Test
        public void test() throws JSONException {
            assertThat(new JvmCrash(mJson).getReporterType()).isEqualTo(mExpected);
        }
    }

    private static JSONObject fillJson(@NonNull JSONObject reporterConfiguration) throws JSONException {
        return new JSONObject().put(
                KEY_PROCESS_CONFIGURATION,
                new JSONObject().put(KEY_PID, 123).put(KEY_PSID, "123").put(KEY_PACKAGE_NAME, "test")
        ).put(
                KEY_REPORTER_CONFIGURATION,
                reporterConfiguration
        ).put(
                KEY_EVENT,
                new JSONObject()
                        .put(KEY_JVM_CRASH, Base64.encodeToString("crashValue".getBytes(), 0))
                        .put(KEY_EVENT_NAME, "name")
                        .put(KEY_BYTES_TRUNCATED, 0)
                        .put(KEY_TRIMMED_FIELDS, new JSONObject().put("VALUE", 100))
        );
    }

}
