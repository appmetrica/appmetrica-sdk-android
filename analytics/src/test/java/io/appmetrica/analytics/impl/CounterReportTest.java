package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.SystemClock;
import android.util.Base64;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.billing.ProductInfoWrapper;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.protobuf.backend.EventStart;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.utils.executors.ServiceExecutorProvider;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import io.appmetrica.analytics.testutils.TestUtils;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CounterReportTest extends CommonTest {

    @Mock
    private AppStandbyBucketConverter mAppStandbyBucketConverter;
    @Mock
    private ExtraMetaInfoRetriever mExtraMetaInfoRetriever;
    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private ReportRequestConfig reportRequestConfig;
    @Mock
    private ServiceExecutorProvider serviceExecutorProvider;
    @Mock
    private IHandlerExecutor commonExecutor;

    private Context context = TestUtils.createMockedContext();
    private StartupState startupState;

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    private List<String> mProviders = new ArrayList<String>();

    private final String encodedIdentity = "encoded identity";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(componentUnit.getFreshReportRequestConfig()).thenReturn(reportRequestConfig);
        when(componentUnit.getContext()).thenReturn(context);
        when(componentUnit.getStartupState()).thenReturn(startupState);
        when(GlobalServiceLocator.getInstance().getServiceExecutorProvider()).thenReturn(serviceExecutorProvider);
        when(serviceExecutorProvider.getDefaultExecutor()).thenReturn(commonExecutor);
        startupState = TestUtils.createDefaultStartupStateBuilder().build();
    }

    @Test
    public void testPermissionsAndRestrictionsAndProviders() throws Exception {
        BackgroundRestrictionsState.AppStandByBucket appStandByBucket = BackgroundRestrictionsState.AppStandByBucket.RARE;
        String appStandbyBucketString = "expected string";
        when(mAppStandbyBucketConverter.fromAppStandbyBucketToString(appStandByBucket)).thenReturn(appStandbyBucketString);
        CounterReport report = CounterReport.formPermissionsReportData(
            mock(CounterReport.class),
            Arrays.asList(new PermissionState[]{new PermissionState("1", false), new PermissionState("2", true)}),
            new BackgroundRestrictionsState(appStandByBucket, true),
            mAppStandbyBucketConverter,
            Arrays.asList("gps", "passive")
        );
        JSONAssert.assertEquals(
            new JSONObject()
                .put("permissions",
                    new JSONArray()
                        .put(new JSONObject()
                            .put("name", "1")
                            .put("granted", false)
                        )
                        .put(new JSONObject()
                            .put("name", "2")
                            .put("granted", true)
                        )
                )
                .put("background_restrictions",
                    new JSONObject()
                        .put("background_restricted", true)
                        .put("app_standby_bucket", appStandbyBucketString)
                )
                .put("available_providers",
                    new JSONArray()
                        .put("gps")
                        .put("passive")
                ),
            new JSONObject(report.getValue()),
            true
        );
    }

    @Test
    public void testNoPermissionsNullRestrictionsNoProviders() throws Exception {
        CounterReport report = CounterReport.formPermissionsReportData(
            mock(CounterReport.class),
            new ArrayList<PermissionState>(),
            null,
            mAppStandbyBucketConverter,
            mProviders
        );
        JSONAssert.assertEquals(
            new JSONObject()
                .put("permissions", new JSONArray())
                .put("background_restrictions", new JSONObject())
                .put("available_providers", new JSONArray()),
            new JSONObject(report.getValue()),
            true
        );
    }

    @Test
    public void testNoPermissionsNoProvidersAndAppStandByBucketIsNull() throws Exception {
        CounterReport report = CounterReport.formPermissionsReportData(
            mock(CounterReport.class),
            new ArrayList<PermissionState>(),
            new BackgroundRestrictionsState(null, false),
            mAppStandbyBucketConverter,
            mProviders
        );
        JSONAssert.assertEquals(
            new JSONObject()
                .put("permissions", new JSONArray())
                .put("background_restrictions",
                    new JSONObject().put("background_restricted", false))
                .put("available_providers", new JSONArray()),
            new JSONObject(report.getValue()),
            true
        );
    }

    @Test
    public void testNoPermissionsNoProvidersAndBackgroundRestrictedIsNull() throws Exception {
        when(mAppStandbyBucketConverter.fromAppStandbyBucketToString(BackgroundRestrictionsState.AppStandByBucket.RARE)).thenReturn("rare");
        CounterReport report = CounterReport.formPermissionsReportData(
            mock(CounterReport.class),
            new ArrayList<PermissionState>(),
            new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RARE, null),
            mAppStandbyBucketConverter,
            mProviders
        );
        JSONAssert.assertEquals(
            new JSONObject()
                .put("permissions", new JSONArray())
                .put("background_restrictions",
                    new JSONObject().put("app_standby_bucket", "rare")
                )
                .put("available_providers", new JSONArray()),
            new JSONObject(report.getValue()),
            true)
        ;
    }

    @Test
    public void testNoPermissionsNoPermissionsNullRestrictionsValues() throws Exception {
        CounterReport report = CounterReport.formPermissionsReportData(
            mock(CounterReport.class),
            new ArrayList<PermissionState>(),
            new BackgroundRestrictionsState(null, null),
            mAppStandbyBucketConverter,
            mProviders
        );
        JSONAssert.assertEquals(
            new JSONObject()
                .put("permissions", new JSONArray())
                .put("background_restrictions", new JSONObject())
                .put("available_providers", new JSONArray()),
            new JSONObject(report.getValue()),
            true
        );
    }

    @Test
    public void testFeaturesReport() throws JSONException {
        JSONObject features = new JSONObject()
            .put("features",
                new JSONArray()
                    .put(new JSONObject()
                        .put("name", "feature.name")
                        .put("version", 1)
                        .put("required", false))
            );
        CounterReport report = CounterReport.formFeaturesReportData(mock(CounterReport.class), features.toString());
        JSONAssert.assertEquals(features, new JSONObject(report.getValue()), true);
    }

    @Test
    public void testEmptyFeatures() throws JSONException {
        JSONObject features = new JSONObject().put("features", new JSONArray());
        CounterReport report = CounterReport.formFeaturesReportData(mock(CounterReport.class), features.toString());
        JSONAssert.assertEquals(features, new JSONObject(report.getValue()), true);
    }

    @Test
    public void testCounterReportDefaultConstructorCreationElapsedRealtime() {
        CounterReport counterReport = new CounterReport();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        assertThat(counterReport.getCreationElapsedRealtime() - elapsedRealtime).isLessThan(1000);
    }

    @Test
    public void testCounterReportCreationElapsedRealtime() {
        SystemTimeProvider systemTimeProvider = mock(SystemTimeProvider.class);
        long expected = 35345344L;
        when(systemTimeProvider.elapsedRealtime()).thenReturn(expected);
        CounterReport counterReport = new CounterReport("Test value", "Test event", 234, systemTimeProvider);
        assertThat(counterReport.getCreationElapsedRealtime()).isEqualTo(expected);
    }

    @Test
    public void testCounterReportFromBundleCreationElapsedRealtime() {
        long expected = 45345435L;
        CounterReport counterReport = new CounterReport();
        counterReport.setCreationEllapsedRealtime(expected);
        Bundle bundle = new Bundle();
        counterReport.toBundle(bundle);
        assertThat(CounterReport.fromBundle(bundle).getCreationElapsedRealtime())
            .isEqualTo(expected);
    }

    @Test
    public void testAddingCreationElapsedRealtime() {
        CounterReport counterReport = new CounterReport();
        long expected = 2453543L;
        counterReport.setCreationEllapsedRealtime(expected);
        counterReport.getCreationElapsedRealtime();
        assertThat(counterReport.getCreationElapsedRealtime()).isEqualTo(expected);
    }

    @Test
    public void testFormNewSessionReportValue() throws InvalidProtocolBufferNanoException {
        String buildId = "12345678";
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        assertThat(
            new String(
                EventStart.Value.parseFrom(
                    CounterReport.formSessionStartReportData(new CounterReport(), mExtraMetaInfoRetriever)
                        .getValueBytes()
                ).buildId
            )
        ).isEqualTo(buildId);
    }

    @Test
    public void testFormNewSessionReportNoBuilId() throws InvalidProtocolBufferNanoException {
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(null);
        assertThat(EventStart.Value.parseFrom(CounterReport.formSessionStartReportData(new CounterReport(), mExtraMetaInfoRetriever).getValueBytes()).buildId)
            .isEmpty();
    }

    @Test
    public void testCounterReportCreationTimestamp() {
        SystemTimeProvider systemTimeProvider = mock(SystemTimeProvider.class);
        long expected = 43543534L;
        when(systemTimeProvider.currentTimeMillis()).thenReturn(expected);
        CounterReport counterReport = new CounterReport("Test value", "Test event", 0, systemTimeProvider);
        assertThat(counterReport.getCreationTimestamp()).isEqualTo(expected);
    }

    @Test
    public void testCounterReportFromBundleCreationTimestamp() {
        long expected = 353454565L;
        CounterReport counterReport = new CounterReport();
        counterReport.setCreationTimestamp(expected);
        Bundle bundle = new Bundle();
        counterReport.toBundle(bundle);
        assertThat(CounterReport.fromBundle(bundle).getCreationTimestamp())
            .isEqualTo(expected);
    }

    @Test
    public void testAddintCreationTimestamp() {
        long expected = 3454534L;
        CounterReport counterReport = new CounterReport();
        counterReport.setCreationTimestamp(expected);
        assertThat(counterReport.getCreationTimestamp()).isEqualTo(expected);
    }

    @Test
    public void testSaveBytes() {
        byte[] bytes = new byte[]{1, 2, 3, 4, 10, 11, 12, 13, 14, 15, 21};
        CounterReport report = new CounterReport();
        report.setValueBytes(bytes);
        assertThat(report.getValue()).isEqualTo(new String(Base64.encode(bytes, 0)));
    }

    @Test
    public void setValueForNull() {
        CounterReport counterReport = new CounterReport();
        counterReport.setValue(null);
        assertThat(counterReport.getValue()).isNull();
        assertThat(counterReport.getValueBytes()).isNull();
    }

    @Test
    public void setValueBytesForNull() {
        CounterReport counterReport = new CounterReport();
        counterReport.setValueBytes(null);
        assertThat(counterReport.getValue()).isNull();
        assertThat(counterReport.getValueBytes()).isNull();
    }

    @Test
    public void testReadBytes() {
        byte[] bytes = new byte[]{1, 2, 3, 4, 10, 11, 12, 13, 14, 15, 21};
        CounterReport report = new CounterReport();
        report.setValue(new String(Base64.encode(bytes, 0)));
        assertThat(report.getValueBytes()).isEqualTo(bytes);

    }

    @Test
    public void testFirstOccurrenceStatus() {
        FirstOccurrenceStatus firstOccurrenceStatus = FirstOccurrenceStatus.FIRST_OCCURRENCE;
        CounterReport counterReport = new CounterReport();
        counterReport.setFirstOccurrenceStatus(firstOccurrenceStatus);
        assertThat(counterReport.getFirstOccurrenceStatus()).isEqualTo(firstOccurrenceStatus);
    }

    @Test
    public void testFormPreActivationConfigUpdate() {
        assertThat(CounterReport.formUpdatePreActivationConfig().getType()).isEqualTo(
            InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG.getTypeId()
        );
    }

    @Test
    public void testFormAutoInappEvent() {
        final ProductInfoWrapper productInfoWrapper = mock(ProductInfoWrapper.class);
        final String data = "some_data";
        when(productInfoWrapper.getDataToSend()).thenReturn(data.getBytes(StandardCharsets.UTF_8));

        final CounterReport report = CounterReport.formAutoInappEvent(productInfoWrapper);

        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.getTypeId());
        assertThat(report.getValueBytes()).isEqualTo(data.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void formJsInitEvent() {
        final String value = "event value";
        final InternalEvents internalEvents = InternalEvents.EVENT_TYPE_WEBVIEW_SYNC;
        CounterReport result = CounterReport.formJsInitEvent(value);
        assertThat(result.getSource()).isEqualTo(EventSource.JS);
        assertThat(result.getType()).isEqualTo(internalEvents.getTypeId());
        assertThat(result.getName()).isEmpty();
        assertThat(result.getValue()).isEqualTo(value);
    }

    @Test
    public void testParcelableFilled() {
        final int type = InternalEvents.EVENT_TYPE_CUSTOM_EVENT.getTypeId();
        final int customType = 8;
        final String value = "value";
        final String userInfo = "userInfo";
        final String eventEnvironment = "eventEnvironment";
        final String event = "event";
        final String appEnvKey = "key";
        final String appEnvValue = "value";
        final int bytesTruncated = 20;
        final String profileId = "profileId";
        final long creationEllapsedRealtime = 21212121L;
        final long creationTimestamp = 32323232L;
        final FirstOccurrenceStatus firstOccurrenceStatus = FirstOccurrenceStatus.FIRST_OCCURRENCE;
        final EventSource source = EventSource.JS;
        final boolean attributionIdchanged = true;
        final int openId = 888999;
        String extraKey = "extra key";
        byte[] extraValue = new byte[]{1, 2, 3, 4, 5};
        Map<String, byte[]> extras = Collections.singletonMap(extraKey, extraValue);
        final Bundle payload = new Bundle();
        payload.putString("key1", "value1");
        payload.putInt("key2", 10);

        CounterReport counterReport = new CounterReport();
        counterReport.setType(type);
        counterReport.setCustomType(customType);
        counterReport.setValue(value);
        counterReport.setEventEnvironment(eventEnvironment);
        counterReport.setName(event);
        counterReport.setAppEnvironment(appEnvKey, appEnvValue);
        counterReport.setBytesTruncated(bytesTruncated);
        counterReport.setProfileID(profileId);
        counterReport.setCreationEllapsedRealtime(creationEllapsedRealtime);
        counterReport.setCreationTimestamp(creationTimestamp);
        counterReport.setFirstOccurrenceStatus(firstOccurrenceStatus);
        counterReport.setSource(source);
        counterReport.setPayload(payload);
        counterReport.setAttributionIdChanged(attributionIdchanged);
        counterReport.setOpenId(openId);
        counterReport.setExtras(extras);

        Parcel parcel = Parcel.obtain();
        counterReport.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        CounterReport fromParcel = CounterReport.CREATOR.createFromParcel(parcel);
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(fromParcel.getType()).isEqualTo(type);
        assertions.assertThat(fromParcel.getCustomType()).isEqualTo(customType);
        assertions.assertThat(fromParcel.getValue()).isEqualTo(value);
        assertions.assertThat(fromParcel.getEventEnvironment()).isEqualTo(eventEnvironment);
        assertions.assertThat(fromParcel.getName()).isEqualTo(event);
        assertions.assertThat(fromParcel.getAppEnvironment().first).isEqualTo(appEnvKey);
        assertions.assertThat(fromParcel.getAppEnvironment().second).isEqualTo(appEnvValue);
        assertions.assertThat(fromParcel.getBytesTruncated()).isEqualTo(bytesTruncated);
        assertions.assertThat(fromParcel.getProfileID()).isEqualTo(profileId);
        assertions.assertThat(fromParcel.getCreationElapsedRealtime()).isEqualTo(creationEllapsedRealtime);
        assertions.assertThat(fromParcel.getCreationTimestamp()).isEqualTo(creationTimestamp);
        assertions.assertThat(fromParcel.getFirstOccurrenceStatus()).isEqualTo(firstOccurrenceStatus);
        assertions.assertThat(fromParcel.getSource()).isEqualTo(source);
        assertions.assertThat(fromParcel.getAttributionIdChanged()).isEqualTo(attributionIdchanged);
        assertions.assertThat(fromParcel.getOpenId()).isEqualTo(openId);
        assertions.assertThat(fromParcel.getExtras()).containsAllEntriesOf(extras);
        Bundle actualPayload = fromParcel.getPayload();
        assertThat(actualPayload.keySet()).containsOnly("key1", "key2");
        assertThat(actualPayload.getString("key1")).isEqualTo("value1");
        assertThat(actualPayload.getInt("key2")).isEqualTo(10);

        assertions.assertAll();

    }

    @Test
    public void testParcelableNull() {
        CounterReport fromParcel = CounterReport.CREATOR.createFromParcel(Parcel.obtain());
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(fromParcel.getType()).isEqualTo(InternalEvents.EVENT_TYPE_UNDEFINED.getTypeId());
        assertions.assertThat(fromParcel.getCustomType()).isEqualTo(0);
        assertions.assertThat(fromParcel.getValue()).isEmpty();
        assertions.assertThat(fromParcel.getEventEnvironment()).isNull();
        assertions.assertThat(fromParcel.getName()).isNull();
        assertions.assertThat(fromParcel.getAppEnvironment()).isNull();
        assertions.assertThat(fromParcel.getBytesTruncated()).isEqualTo(0);
        assertions.assertThat(fromParcel.getProfileID()).isNull();
        assertions.assertThat(fromParcel.getCreationElapsedRealtime()).isEqualTo(0);
        assertions.assertThat(fromParcel.getCreationTimestamp()).isEqualTo(0);
        assertions.assertThat(fromParcel.getFirstOccurrenceStatus()).isEqualTo(FirstOccurrenceStatus.UNKNOWN);
        assertions.assertThat(fromParcel.getSource()).isNull();
        assertions.assertThat(fromParcel.getPayload()).isNull();
        assertions.assertThat(fromParcel.getAttributionIdChanged()).isNull();
        assertions.assertThat(fromParcel.getOpenId()).isNull();
        assertions.assertThat(fromParcel.getExtras()).isEqualTo(Collections.emptyMap());

        assertions.assertAll();
    }

    @Test
    public void toStringForEmpty() throws Exception {
        CounterReport report = new CounterReport();

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(report.toString())
            .contains("event: ")
            .contains("type: " + InternalEvents.EVENT_TYPE_INIT.getInfo())
            .contains("value: ");
        assertions.assertAll();
    }

    @Test
    public void toStringForFilledValue() throws Exception {
        String eventName = "test name";
        String eventValue = "test value";
        int type = InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.getTypeId();
        CounterReport report = new CounterReport();
        report.setType(type);
        report.setName(eventName);
        report.setValue(eventValue);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(report.toString())
            .contains("event: " + eventName)
            .contains("type: " + InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.getInfo())
            .contains("value: " + eventValue);
        assertions.assertAll();
    }

    @Test
    public void toStringLongValue() {
        String eventName = "test name";
        String fittingValue = new RandomStringGenerator(500).nextString();
        String eventValue = fittingValue + "aaaaabbbbb";
        int type = InternalEvents.EVENT_TYPE_REGULAR.getTypeId();
        CounterReport report = new CounterReport();
        report.setType(type);
        report.setName(eventName);
        report.setValue(eventValue);
        ;

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(report.toString())
            .contains("event: " + eventName)
            .contains("type: " + InternalEvents.EVENT_TYPE_REGULAR.getInfo())
            .contains("value: " + fittingValue)
            .doesNotContain(eventValue);
        assertions.assertAll();
    }
}
