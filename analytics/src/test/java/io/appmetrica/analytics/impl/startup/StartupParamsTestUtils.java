package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

public class StartupParamsTestUtils {

    static final String IDENTIFIER_KEY_CLIDS = "appmetrica_clids";
    static final String IDENTIFIER_KEY_UUID = "appmetrica_uuid";
    static final String IDENTIFIER_KEY_DEVICE_ID = "appmetrica_device_id";
    static final String IDENTIFIER_KEY_DEVICE_ID_HASH = "appmetrica_device_id_hash";
    static final String IDENTIFIER_GET_AD_URL = "appmetrica_get_ad_url";
    static final String IDENTIFIER_REPORT_AD_URL = "appmetrica_report_ad_url";
    static final String IDENTIFIER_GOOGLE_ADV_ID = "appmetrica_google_adv_id";
    static final String IDENTIFIER_HUAWEI_ADV_ID = "appmetrica_huawei_oaid";
    static final String IDENTIFIER_YANDEX_ADV_ID = "appmetrica_yandex_adv_id";
    static final String CUSTOM_IDENTIFIER1 = "custom1";
    static final String CUSTOM_IDENTIFIER2 = "custom2";
    static final Map<String, List<String>> CUSTOM_HOSTS_MAP = new HashMap<>();
    static final IdentifiersResult SSL_ENABLED_FEATURE = new IdentifiersResult("true", IdentifierStatus.OK, null);

    static {
        CUSTOM_HOSTS_MAP.put(CUSTOM_IDENTIFIER1, Arrays.asList("host1"));
        CUSTOM_HOSTS_MAP.put(CUSTOM_IDENTIFIER2, Arrays.asList("host2"));
    }

    static final List<String> IDENTIFIERS_WITH_SSL_FEATURE = Arrays.asList(
            Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED
    );
    static final List<String> CUSTOM_IDENTIFIERS = Arrays.asList(
            CUSTOM_IDENTIFIER1,
            CUSTOM_IDENTIFIER2
    );
    static final List<String> ALL_IDENTIFIERS_WITH_CUSTOM = Arrays.asList(
            IDENTIFIER_KEY_CLIDS,
            IDENTIFIER_KEY_UUID,
            IDENTIFIER_KEY_DEVICE_ID,
            IDENTIFIER_KEY_DEVICE_ID_HASH,
            IDENTIFIER_GET_AD_URL,
            IDENTIFIER_REPORT_AD_URL,
            IDENTIFIER_GOOGLE_ADV_ID,
            IDENTIFIER_HUAWEI_ADV_ID,
            IDENTIFIER_YANDEX_ADV_ID,
            CUSTOM_IDENTIFIER1,
            CUSTOM_IDENTIFIER2
    );
    static final List<String> ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE = Arrays.asList(
            IDENTIFIER_KEY_CLIDS,
            IDENTIFIER_KEY_UUID,
            IDENTIFIER_KEY_DEVICE_ID,
            IDENTIFIER_KEY_DEVICE_ID_HASH,
            IDENTIFIER_GET_AD_URL,
            IDENTIFIER_REPORT_AD_URL,
            IDENTIFIER_GOOGLE_ADV_ID,
            IDENTIFIER_HUAWEI_ADV_ID,
            IDENTIFIER_YANDEX_ADV_ID,
            CUSTOM_IDENTIFIER1,
            CUSTOM_IDENTIFIER2,
            Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED
    );
    static final List<String> ALL_IDENTIFIERS = Arrays.asList(
            IDENTIFIER_KEY_CLIDS,
            IDENTIFIER_KEY_UUID,
            IDENTIFIER_KEY_DEVICE_ID,
            IDENTIFIER_KEY_DEVICE_ID_HASH,
            IDENTIFIER_GET_AD_URL,
            IDENTIFIER_REPORT_AD_URL,
            IDENTIFIER_GOOGLE_ADV_ID,
            IDENTIFIER_HUAWEI_ADV_ID,
            IDENTIFIER_YANDEX_ADV_ID
    );
    static final List<String> ALL_IDENTIFIERS_EXCEPT_ADV = Arrays.asList(
            IDENTIFIER_KEY_CLIDS,
            IDENTIFIER_KEY_UUID,
            IDENTIFIER_KEY_DEVICE_ID,
            IDENTIFIER_KEY_DEVICE_ID_HASH,
            IDENTIFIER_GET_AD_URL,
            IDENTIFIER_REPORT_AD_URL
    );
    static final List<String> ALL_IDENTIFIERS_WITH_CUSTOM_EXCEPT_ADS = Arrays.asList(
            IDENTIFIER_KEY_CLIDS,
            IDENTIFIER_KEY_UUID,
            IDENTIFIER_KEY_DEVICE_ID,
            IDENTIFIER_KEY_DEVICE_ID_HASH,
            IDENTIFIER_GET_AD_URL,
            IDENTIFIER_REPORT_AD_URL,
            CUSTOM_IDENTIFIER1,
            CUSTOM_IDENTIFIER2
    );
    static final List<String> ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV = Arrays.asList(
            IDENTIFIER_KEY_CLIDS,
            IDENTIFIER_KEY_UUID,
            IDENTIFIER_KEY_DEVICE_ID,
            IDENTIFIER_KEY_DEVICE_ID_HASH,
            IDENTIFIER_GET_AD_URL,
            IDENTIFIER_REPORT_AD_URL,
            CUSTOM_IDENTIFIER1,
            CUSTOM_IDENTIFIER2,
            Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED
    );

    static final Map<String, String> CLIDS_MAP_1 = Collections.singletonMap("clid0", "0");
    static final Map<String, String> CLIDS_MAP_2 = Collections.singletonMap("clid0", "1");
    static final Map<String, String> CLIDS_MAP_3 = Collections.singletonMap("clid0", "2");

    static final String CLIDS_1 = StartupUtils.encodeClids(CLIDS_MAP_1);
    static final String CLIDS_2 = StartupUtils.encodeClids(CLIDS_MAP_2);
    static final String CLIDS_3 = StartupUtils.encodeClids(CLIDS_MAP_3);
    static final String CLIDS_EMPTY = "";
    static final String CLIDS_NULL = null;

    static final List<String> CUSTOM_HOST_SINGLE = Collections.singletonList("https://single.custom.host");
    static final List<String> CUSTOM_HOST_EMPTY = Collections.emptyList();
    static final List<String> CUSTOM_HOSTS = Arrays.asList("https://first.custom.host", "https://second.custom.host");

    public static void mockPreferencesClientDbStoragePutResponses(PreferencesClientDbStorage mock) {
        when(mock.putAdUrlGetResult(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putAdUrlReportResult(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putResponseClidsResult(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putClientClids(nullable(String.class))).thenReturn(mock);
        when(mock.putCustomHosts(nullable(List.class))).thenReturn(mock);
        when(mock.putDeviceIdResult(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putDeviceIdHashResult(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putUuidResult(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putGaid(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putHoaid(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putYandexAdvId(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putServerTimeOffset(nullable(Long.class))).thenReturn(mock);
        when(mock.putClientClidsChangedAfterLastIdentifiersUpdate(anyBoolean())).thenReturn(mock);
        when(mock.putCustomSdkHosts(nullable(IdentifiersResult.class))).thenReturn(mock);
        when(mock.putNextStartupTime(anyLong())).thenReturn(mock);
        when(mock.putFeatures(any(FeaturesInternal.class))).thenReturn(mock);
    }

}
