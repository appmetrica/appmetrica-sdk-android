package io.appmetrica.analytics.impl.db.preferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.startup.FeaturesInternal;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PreferencesClientDbStorageTest extends CommonTest {

    private PreferencesClientDbStorage clientDbStorage;
    @Mock
    private IKeyValueTableDbHelper mDbStorage;
    private IdentifiersResult mDefaultIdentifiersResult = new IdentifiersResult(null, IdentifierStatus.UNKNOWN, "no identifier in preferences");

    static class JsonMatcher implements ArgumentMatcher<JSONObject> {

        @NonNull
        private final JSONObject mExpected;

        JsonMatcher(@NonNull JSONObject expected) {
            mExpected = expected;
        }

        @Override
        public boolean matches(@Nullable JSONObject argument) {
            if (argument == null) {
                return false;
            }
            final Iterator<String> keys = mExpected.keys();
            try {
                while (keys.hasNext()) {
                    final String key = keys.next();
                    if (mExpected.get(key).equals(argument.get(key)) == false) {
                        return false;
                    }
                    argument.remove(key);
                }
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
            if (argument.keys().hasNext()) {
                return false;
            }
            return true;
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        clientDbStorage = new PreferencesClientDbStorage(mDbStorage);
    }

    @Test
    public void testGetResponseClidsResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getResponseClidsResult()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult responseClids = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(responseClids);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.RESPONSE_CLIDS_RESULT.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getResponseClidsResult()).isSameAs(responseClids);
        }
    }

    @Test
    public void testPutResponseClidsResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult responseClids = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(responseClids)).thenReturn(json);

            clientDbStorage.putResponseClidsResult(responseClids);
            verify(mDbStorage).put(PreferencesClientDbStorage.RESPONSE_CLIDS_RESULT.fullKey(), json.toString());
        }
    }

    @Test
    public void testGetClientClids() throws Exception {
        assertThat(clientDbStorage.getClientClids(null)).isNull();

        String clids = "test clids";
        doReturn(clids).when(mDbStorage)
                .getString(eq(PreferencesClientDbStorage.CLIENT_CLIDS.fullKey()), nullable(String.class));

        assertThat(clientDbStorage.getClientClids(null)).isEqualTo(clids);
    }

    @Test
    public void testPutClientClids() throws Exception {
        String clids = "test clids";
        clientDbStorage.putClientClids(clids);
        verify(mDbStorage).put(PreferencesClientDbStorage.CLIENT_CLIDS.fullKey(), clids);
    }

    @Test
    public void testGetUuidResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getUuidResult()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult uuid = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(uuid);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.UUID_RESULT.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getUuidResult()).isSameAs(uuid);
        }
    }

    @Test
    public void testPutUuidResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult uuid = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(uuid)).thenReturn(json);

            clientDbStorage.putUuidResult(uuid);
            verify(mDbStorage).put(PreferencesClientDbStorage.UUID_RESULT.fullKey(), json.toString());
        }
    }

    @Test
    public void testGetDeviceIdResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getDeviceIdResult()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult deviceId = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(deviceId);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.DEVICE_ID_RESULT.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getDeviceIdResult()).isSameAs(deviceId);
        }
    }

    @Test
    public void testPutDeviceIdResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult deviceId = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(deviceId)).thenReturn(json);

            clientDbStorage.putDeviceIdResult(deviceId);
            verify(mDbStorage).put(PreferencesClientDbStorage.DEVICE_ID_RESULT.fullKey(), json.toString());
        }
    }

    @Test
    public void testGetDeviceIdHashResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getDeviceIdHashResult()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult deviceIdHash = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(deviceIdHash);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.DEVICE_ID_HASH_RESULT.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getDeviceIdHashResult()).isSameAs(deviceIdHash);
        }
    }

    @Test
    public void testPutDeviceIdHashResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult deviceIdHash = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(deviceIdHash)).thenReturn(json);

            clientDbStorage.putDeviceIdHashResult(deviceIdHash);
            verify(mDbStorage).put(PreferencesClientDbStorage.DEVICE_ID_HASH_RESULT.fullKey(), json.toString());
        }
    }

    @Test
    public void testGetClientApiVersion() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(1);
            }
        }).when(mDbStorage).getLong(eq(PreferencesClientDbStorage.API_LEVEL.fullKey()), anyLong());
        assertThat(clientDbStorage.getClientApiLevel(-1)).isEqualTo(-1);

        long value = 44;
        doReturn(value).when(mDbStorage)
                .getLong(eq(PreferencesClientDbStorage.API_LEVEL.fullKey()), anyLong());

        assertThat(clientDbStorage.getClientApiLevel(-1)).isEqualTo(value);
    }

    @Test
    public void testPutClientApiVersion() throws Exception {
        long value = 33;
        clientDbStorage.putClientApiLevel(value);
        verify(mDbStorage).put(PreferencesClientDbStorage.API_LEVEL.fullKey(), value);
    }

    @Test
    public void testDeprecatedNativeCrashesChecked() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(1);
            }
        }).when(mDbStorage).getLong(eq(PreferencesClientDbStorage.API_LEVEL.fullKey()), anyLong());
        assertThat(clientDbStorage.getClientApiLevel(-1)).isEqualTo(-1);
    }

    @Test
    public void testGetAdUrlGetResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getAdUrlGetResult()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult adUrlGet = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(adUrlGet);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.AD_URL_GET_RESULT.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getAdUrlGetResult()).isSameAs(adUrlGet);
        }
    }

    @Test
    public void testPutAdUrlGetResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult adUrlGet = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(adUrlGet)).thenReturn(json);

            clientDbStorage.putAdUrlGetResult(adUrlGet);
            verify(mDbStorage).put(PreferencesClientDbStorage.AD_URL_GET_RESULT.fullKey(), json.toString());
        }
    }

    @Test
    public void testGetAdUrlReportResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getAdUrlReportResult()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult adUrlReport = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(adUrlReport);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.AD_URL_REPORT_RESULT.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getAdUrlReportResult()).isSameAs(adUrlReport);
        }
    }

    @Test
    public void testPutAdUrlReportResult() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult adUrlReport = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(adUrlReport)).thenReturn(json);

            clientDbStorage.putAdUrlReportResult(adUrlReport);
            verify(mDbStorage).put(PreferencesClientDbStorage.AD_URL_REPORT_RESULT.fullKey(), json.toString());
        }
    }

    @Test
    public void testGetGaid() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getGaid()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult gaid = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(gaid);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.GAID.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getGaid()).isSameAs(gaid);
        }
    }

    @Test
    public void testPutGaid() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult gaid = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(gaid)).thenReturn(json);

            clientDbStorage.putGaid(gaid);
            verify(mDbStorage).put(PreferencesClientDbStorage.GAID.fullKey(), json.toString());
        }
    }

    @Test
    public void testGetHoaid() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getHoaid()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult hoaid = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(hoaid);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.HOAID.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getHoaid()).isSameAs(hoaid);
        }
    }

    @Test
    public void testPutHoaid() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult hoaid = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(hoaid)).thenReturn(json);

            clientDbStorage.putHoaid(hoaid);
            verify(mDbStorage).put(PreferencesClientDbStorage.HOAID.fullKey(), json.toString());
        }
    }

    @Test
    public void getYandexAdvId() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getYandexAdvId()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult yandexAdvId = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(yandexAdvId);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.YANDEX_ADV_ID.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getYandexAdvId()).isSameAs(yandexAdvId);
        }
    }

    @Test
    public void putYandexAdvId() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult yandexAdvId = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(yandexAdvId)).thenReturn(json);

            clientDbStorage.putYandexAdvId(yandexAdvId);
            verify(mDbStorage).put(PreferencesClientDbStorage.YANDEX_ADV_ID.fullKey(), json.toString());
        }
    }

    @Test
    public void getClientClidsChangedAfterLastIdentifiersUpdate() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(1);
            }
        }).when(mDbStorage).getBoolean(anyString(), anyBoolean());

        assertThat(clientDbStorage.getClientClidsChangedAfterLastIdentifiersUpdate(true)).isTrue();

        when(mDbStorage.getBoolean(eq(PreferencesClientDbStorage.CLIENT_CLIDS_CHANGED_AFTER_LAST_IDENTIFIERS_UPDATE.fullKey()), anyBoolean()))
                .thenReturn(false);
        assertThat(clientDbStorage.getClientClidsChangedAfterLastIdentifiersUpdate(true)).isFalse();
    }

    @Test
    public void putClientClidsChangedAfterLastIdentifiersUpdate() {
        clientDbStorage.putClientClidsChangedAfterLastIdentifiersUpdate(false);
        verify(mDbStorage).put(PreferencesClientDbStorage.CLIENT_CLIDS_CHANGED_AFTER_LAST_IDENTIFIERS_UPDATE.fullKey(), false);
    }

    @Test
    public void getScreenInfoIfMissing() {
        assertThat(clientDbStorage.getScreenInfo()).isNull();
    }

    @Test
    public void getScreenInfo() throws JSONException {
        ScreenInfo screenInfo = mock(ScreenInfo.class);
        String screenInfoStringValue = "Screen info string value";
        when(mDbStorage.getString(PreferencesClientDbStorage.SCREEN_INFO.fullKey(), null))
                .thenReturn(screenInfoStringValue);
        try (MockedStatic<JsonHelper> ignored = Mockito.mockStatic(JsonHelper.class)) {
            when(JsonHelper.screenInfoFromJsonString(screenInfoStringValue)).thenReturn(screenInfo);
            assertThat(clientDbStorage.getScreenInfo()).isSameAs(screenInfo);
        }
    }

    @Test
    public void saveScreenInfo() throws JSONException {
        ScreenInfo screenInfo = mock(ScreenInfo.class);
        String screenInfoStringValue = "Screen info string value";
        try (MockedStatic<JsonHelper> ignored = Mockito.mockStatic(JsonHelper.class)) {

            when(JsonHelper.screenInfoToJsonString(screenInfo)).thenReturn(screenInfoStringValue);

            clientDbStorage.saveScreenInfo(screenInfo);
            verify(mDbStorage).put(PreferencesClientDbStorage.SCREEN_INFO.fullKey(), screenInfoStringValue);
        }
    }

    @Test
    public void saveNullScreenInfo() {
        clientDbStorage.saveScreenInfo(null);
        verify(mDbStorage).put(PreferencesClientDbStorage.SCREEN_INFO.fullKey(), null);
    }

    @Test
    public void isScreenSizeCheckedByDeprecatedIfMissing() {
        assertThat(clientDbStorage.isScreenSizeCheckedByDeprecated()).isFalse();
    }

    @Test
    public void isScreenSizeCheckedByDeprecated() {
        when(mDbStorage.getBoolean(eq(PreferencesClientDbStorage.SCREEN_SIZE_CHECKED_BY_DEPRECATED.fullKey()), anyBoolean()))
                .thenReturn(true);
        assertThat(clientDbStorage.isScreenSizeCheckedByDeprecated()).isTrue();
    }

    @Test
    public void markScreenSizeCheckedByDeprecated() {
        clientDbStorage.markScreenSizeCheckedByDeprecated();
        verify(mDbStorage).put(PreferencesClientDbStorage.SCREEN_SIZE_CHECKED_BY_DEPRECATED.fullKey(), true);
    }

    @Test
    public void getCustomSdkHosts() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            assertThat(clientDbStorage.getCustomSdkHosts()).isEqualToComparingFieldByField(mDefaultIdentifiersResult);

            IdentifiersResult customSdkHosts = mock(IdentifiersResult.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultFromJson(argThat(new JsonMatcher(json)))).thenReturn(customSdkHosts);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.CUSTOM_SDK_HOSTS.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getCustomSdkHosts()).isSameAs(customSdkHosts);
        }
    }

    @Test
    public void putCustomSdkHosts() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            IdentifiersResult customSdkHosts = mock(IdentifiersResult.class);

            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.advIdentifiersResultToJson(customSdkHosts)).thenReturn(json);

            clientDbStorage.putCustomSdkHosts(customSdkHosts);
            verify(mDbStorage).put(PreferencesClientDbStorage.CUSTOM_SDK_HOSTS.fullKey(), json.toString());
        }
    }

    @Test
    public void getFeatures() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {
            FeaturesInternal defaultFeatures= new FeaturesInternal();
            when(JsonHelper.featuresFromJson(null)).thenReturn(defaultFeatures);
            assertThat(clientDbStorage.getFeatures()).isEqualTo(defaultFeatures);

            FeaturesInternal features = mock(FeaturesInternal.class);
            final JSONObject json = new JSONObject().put("key", "value");
            when(JsonHelper.featuresFromJson(eq(json.toString()))).thenReturn(features);
            doReturn(json.toString()).when(mDbStorage)
                    .getString(eq(PreferencesClientDbStorage.FEATURES.fullKey()), nullable(String.class));

            assertThat(clientDbStorage.getFeatures()).isSameAs(features);
        }
    }

    @Test
    public void putFeatures() throws Exception {
        try (MockedStatic<JsonHelper> sJsonUtils = Mockito.mockStatic(JsonHelper.class)) {

            FeaturesInternal features = mock(FeaturesInternal.class);

            final String json = new JSONObject().put("key", "value").toString();
            when(JsonHelper.featuresToJson(features)).thenReturn(json);

            clientDbStorage.putFeatures(features);
            verify(mDbStorage).put(PreferencesClientDbStorage.FEATURES.fullKey(), json);
        }
    }

    @Test
    public void getNextStartupTime() {
        final long nextStartupTime = 50;
        assertThat(clientDbStorage.getNextStartupTime()).isEqualTo(0);
        when(mDbStorage.getLong(PreferencesClientDbStorage.NEXT_STARTUP_TIME.fullKey(), 0)).thenReturn(nextStartupTime);
        assertThat(clientDbStorage.getNextStartupTime()).isEqualTo(nextStartupTime);
    }

    @Test
    public void putNextStartupTime() {
        final long nextStartupTime = 50;
        clientDbStorage.putNextStartupTime(nextStartupTime);
        verify(mDbStorage).put(PreferencesClientDbStorage.NEXT_STARTUP_TIME.fullKey(), nextStartupTime);
    }
}
