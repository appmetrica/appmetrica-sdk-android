package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class StartupParamsContainsIdentifiersForPreferencesTest extends CommonTest {

    private final String mInputDeviceId;
    private final String mInputDeviceIdHash;
    private final String mInputUuid;
    private final String mInputGetAdUrl;
    private final String mInputReportAdUrl;
    private final String mGaid;
    private final String mHoaid;
    private final String yandexAdvId;
    private final Map<String, List<String>> customSdkHosts;
    private final IdentifiersResult sslFeature;
    private final String mInputResponseClids;
    private final String mInputClientClids;
    private final List<String> mInputCustomHosts;
    private final List<String> mRequestedIdentifiers;
    private final boolean isOutdated;
    private final boolean mExpectedContainsIdentifiers;
    private final boolean mExpectedShouldSendStartupForAll;
    private final boolean mExpectedShouldSendStartup;

    public StartupParamsContainsIdentifiersForPreferencesTest(String inputDeviceId,
                                                              String inputDeviceIdHash,
                                                              String inputUuid,
                                                              String inputGetAdUrl,
                                                              String inputReportAdUrl,
                                                              String gaid,
                                                              String hoaid,
                                                              String yandexAdvId,
                                                              IdentifiersResult sslFeature,
                                                              Map<String, List<String>> customSdkHosts,
                                                              String inputResponseClids,
                                                              String inputClientClids,
                                                              List<String> inputCustomHosts,
                                                              List<String> requestedIdentifiers,
                                                              boolean isOutdated,
                                                              boolean expectedContainsIdentifiers,
                                                              boolean expectedShouldSendStartupForAll,
                                                              boolean expectedShouldSendStartup,
                                                              String description) {
        mInputDeviceId = inputDeviceId;
        mInputDeviceIdHash = inputDeviceIdHash;
        mInputUuid = inputUuid;
        mInputGetAdUrl = inputGetAdUrl;
        mInputReportAdUrl = inputReportAdUrl;
        mGaid = gaid;
        mHoaid = hoaid;
        this.yandexAdvId = yandexAdvId;
        this.customSdkHosts = customSdkHosts;
        this.sslFeature = sslFeature;
        mInputResponseClids = inputResponseClids;
        mInputClientClids = inputClientClids;
        mInputCustomHosts = inputCustomHosts;
        mRequestedIdentifiers = requestedIdentifiers;
        this.isOutdated = isOutdated;
        mExpectedContainsIdentifiers = expectedContainsIdentifiers;
        mExpectedShouldSendStartupForAll = expectedShouldSendStartupForAll;
        mExpectedShouldSendStartup = expectedShouldSendStartup;
    }

    @Parameters(name = "[{index}]{18}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            //region all identifiers
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "ImputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE, StartupParamsTestUtils.ALL_IDENTIFIERS, false,
                true, false, true,
                "Request all identifiers for all exists"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "ImputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE, StartupParamsTestUtils.ALL_IDENTIFIERS, true,
                true, true, true,
                "Request all identifiers for all exists but outdated"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY, StartupParamsTestUtils.ALL_IDENTIFIERS, false,
                false, true, true,
                "Request all identifiers for all empty"
            },
            {
                null, null, null, null, null, null, null, null, null, null, null, null,
                null,
                StartupParamsTestUtils.ALL_IDENTIFIERS, false,
                false, true, true,
                "Request all identifiers for all null"
            },
            //endregion
            //region all identifiers except adv
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE, StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV, false,
                true, false, false,
                "Request all identifiers except adv, all exist"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE, StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV, true,
                true, true, true,
                "Request all identifiers except adv, all exist but outdated"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", null, null, null,
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE, StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV, false,
                true, false, false,
                "Request all identifiers except adv, all exist, but adv are null"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", null, null, null,
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE, StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV, true,
                true, true, true,
                "Request all identifiers except adv, all exist, but adv are null and outdated"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE, StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV, false,
                true, false, false,
                "Request all identifiers except adv, all exists, but adv are empty"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE, StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV, true,
                true, true, true,
                "Request all identifiers except adv, all exists, but adv are empty and outdated"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY, StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV, false,
                false, true, true,
                "Request all identifiers except adv for all empty"
            },
            {
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV, false,
                false, true, true,
                "Request all identifiers except adv for all null"
            },
            //endregion
            //region uuid
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID), false,
                true, false, false,
                "Request only uuid for all exists"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID), true,
                true, true, true,
                "Request only uuid for all exists but outdated"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID), false,
                false, true, true,
                "Request only uuid for all empty"
            },
            {
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID), false,
                false, true, true,
                "Request only uuid for all null"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", null, "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID), false,
                false, true, true,
                "Request only uuid if all defined except uuid"
            },
            {
                null, null, "Input uuid", null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID), false,
                true, true, false,
                "Request only uuid if only uuid defined"
            },
            {
                null, null, "Input uuid", null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID), true,
                true, true, true,
                "Request only uuid if only uuid defined and outdated"
            },
            //endregion
            //region deviceId
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID), false,
                true, false, false,
                "Request only deviceId for all exists"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID), true,
                true, true, true,
                "Request only deviceId for all exists byt outdated"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID), false,
                false, true, true,
                "Request only device id for all empty"
            },
            {
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID), false,
                false, true, true,
                "Request only device id for all null"
            },
            {
                null, "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID), false,
                false, true, true,
                "Request only deviceId if all defined except deviceId"
            },
            {
                "Input deviceId", null, null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID), false,
                true, true, false,
                "Request only deviceId if only deviceId defined"
            },
            {
                "Input deviceId", null, null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID), true,
                true, true, true,
                "Request only deviceId if only deviceId defined and outdated"
            },
            //endregion
            //region deviceIdHash
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH), false,
                true, false, false,
                "Request only deviceId hash for all exists"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH), true,
                true, true, true,
                "Request only deviceId hash for all exists but outdated"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH), false,
                false, true, true,
                "Request only device id hash for all empty"
            },
            {
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH), false,
                false, true, true,
                "Request only device id hash for all null"
            },
            {
                "Input deviceId", null, "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH), false,
                false, true, true,
                "Request only deviceIdHash if all defined except deviceIdHash"
            },
            {
                null, "Input DeviceIdHash", null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH), false,
                true, true, false,
                "Request only deviceIdHash if only deviceIdHash defined"
            },
            {
                null, "Input DeviceIdHash", null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH), true,
                true, true, true,
                "Request only deviceIdHash if only deviceIdHash defined and outdated"
            },
            //endregion
            //region gaid
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID), false,
                true, false, true,
                "Request only gaid for all exists"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID), true,
                true, true, true,
                "Request only gaid for all exists but outdated"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID), false,
                false, true, true,
                "Request only gaid for all empty"
            },
            {
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID), false,
                false, true, true,
                "Request only gaid for all null"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", null, "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID), false,
                false, false, true,
                "Request only gaid if all defined except gaid"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", null, "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID), true,
                false, true, true,
                "Request only gaid if all defined except gaid and outdated"
            },
            {
                null, null, null, null, null, "InputGaid", null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID), false,
                true, true, true,
                "Request only gaid if only gaid defined"
            },
            //endregion
            //region hoaid
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID), false,
                true, false, true,
                "Request only hoaid for all exists"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID), true,
                true, true, true,
                "Request only hoaid for all exists but outdated"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID), false,
                false, true, true,
                "Request only hoaid for all empty"
            },
            {
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID), false,
                false, true, true,
                "Request only hoaid for all null"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", null, "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID), false,
                false, false, true,
                "Request only hoaid if all defined except hoaid"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", null, "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID), true,
                false, true, true,
                "Request only hoaid if all defined except hoaid but outdated"
            },
            {
                null, null, null, null, null, null, "InputHoaid", null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID), false,
                true, true, true,
                "Request only hoaid if only hoaid defined"
            },
            //endregion
            //region yandex adv id
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID), false,
                true, false, true,
                "Request only yandex adv id for all exists"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID), true,
                true, true, true,
                "Request only yandex adv id for all exists but outdated"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID), false,
                false, true, true,
                "Request only yandex adv id for all empty"
            },
            {
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID), false,
                false, true, true,
                "Request only yandex adv id for all null"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", null,
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID), false,
                false, false, true,
                "Request only yandex adv id if all defined except yandex adv id"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", null,
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID), true,
                false, true, true,
                "Request only yandex adv id if all defined except yandex adv id and outdated"
            },
            {
                null, null, null, null, null, null, null, "InputYandexAdvId", null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID), false,
                true, true, true,
                "Request only yandex adv id if only yandex adv id defined"
            },
            //endregion
            //region response clids
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), false,
                true, false, false,
                "Request only clids for all exists"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), true,
                true, true, true,
                "Request only clids for all exists but outdated"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), false,
                true, true, false,
                "Request only clids for all empty"
            },
            {
                "", "", "", "", "", "", "", "",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_EMPTY, StartupParamsTestUtils.CLIDS_EMPTY,
                StartupParamsTestUtils.CUSTOM_HOST_EMPTY,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), true,
                true, true, true,
                "Request only clids for all empty but outdated"
            },
            {
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), false,
                false, true, true,
                "Request only clids for all null"
            },
            {
                null, null, null, null, null, null, null, null, null, null,
                StartupParamsTestUtils.CLIDS_1,
                null, null, Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), false,
                true, true, false,
                "Request clids for only clids exists"
            },
            {
                null, null, null, null, null, null, null, null, null, null,
                StartupParamsTestUtils.CLIDS_1,
                null, null, Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), true,
                true, true, true,
                "Request clids for only clids exists but outdated"
            },
            {
                "Input deviceId", "Input deviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                null, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), false,
                false, true, true,
                "Request only clids if all defined except response clids"
            },
            {
                null, null, null, null, null, null, null, null,
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), false,
                true, true, false,
                "Request only clids if only response clids defined"
            },
            {
                null, null, null, null, null, null, null, null,
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, null, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), true,
                true, true, true,
                "Request only clids if only response clids defined but outdated"
            },
            {
                "Input deviceId", "Input deviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, null,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), false,
                true, false, false,
                "Request only clids if all defined except client clids"
            },
            {
                "Input deviceId", "Input deviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, null,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), true,
                true, true, true,
                "Request only clids if all defined except client clids but outdated"
            },
            {
                null, null, null, null, null, null, null, null, null, null,
                null,
                StartupParamsTestUtils.CLIDS_1, null,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), false,
                false, true, true,
                "Request only clids if only client clids defined"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_2,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), false,
                true, false, false,
                "Request only clids if client and response clids are different"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                new HashMap<String, List<String>>(),
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_2,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS), true,
                true, true, true,
                "Request only clids if client and response clids are different and outdated"
            },
            // region custom identifiers
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS, false,
                true, false, false,
                "Request only custom identifiers has all"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS, true,
                true, true, true,
                "Request only custom identifiers has all but outdated"
            },
            {
                null, null, null, null, null, null, null, null, null,
                null,
                null, null, null,
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS, false,
                false, true, false,
                "Request only custom identifiers has none"
            },
            {
                null, null, null, null, null, null, null, null, null,
                null,
                null, null, null,
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS, true,
                false, true, true,
                "Request only custom identifiers has none but outdated"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                null, StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CUSTOM_HOSTS,
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS, false,
                false, false, false,
                "Request only custom identifiers has all but custom"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                null, StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CUSTOM_HOSTS,
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS, true,
                false, true, true,
                "Request only custom identifiers has all but custom but outdated"
            },
            {
                null, null, null, null, null, null, null, null,
                null,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP, null, null, null,
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS, false,
                true, true, false,
                "Request only custom identifiers has only custom"
            },
            {
                null, null, null, null, null, null, null, null,
                null,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP, null, null, null,
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS, true,
                true, true, true,
                "Request only custom identifiers has only custom but outdated"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE, false,
                true, false, true,
                "Request all identifiers with custom has all"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE, true,
                true, true, true,
                "Request all identifiers with custom has all but outdated"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV, false,
                true, false, false,
                "Request all identifiers with custom except adv has all"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV, true,
                true, true, true,
                "Request all identifiers with custom except adv has all but outdated"
            },
            // end region
            // region ssl_enabled feature
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE, false,
                true, false, false,
                "Request only ssl feature has all"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE, true,
                true, true, true,
                "Request only ssl feature has all but outdated"
            },
            {
                null, null, null, null, null, null, null, null, null,
                null,
                null, null, null,
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE, false,
                false, true, false,
                "Request only ssl feature has none"
            },
            {
                null, null, null, null, null, null, null, null, null,
                null,
                null, null, null,
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE, true,
                false, true, true,
                "Request only ssl feature has none but outdated"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CUSTOM_HOSTS,
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE, false,
                false, false, false,
                "Request only ssl feature has all but feature"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                null,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CUSTOM_HOSTS,
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE, true,
                false, true, true,
                "Request only ssl feature has all but feature but outdated"
            },
            {
                null, null, null, null, null, null, null, null,
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                null, null, null, null,
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE, false,
                true, true, false,
                "Request only ssl feature has only feature"
            },
            {
                null, null, null, null, null, null, null, null,
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                null, null, null, null,
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE, true,
                true, true, true,
                "Request only ssl feature has only feature but outdated"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE, false,
                true, false, true,
                "Request all identifiers with custom and feature has all"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE, true,
                true, true, true,
                "Request all identifiers with custom and feature has all but outdated"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV, false,
                true, false, false,
                "Request all identifiers with custom and feature except adv has all"
            },
            {
                "InputDeviceId", "InputDeviceIdHash", "Input uuid", "InputGetAdUrl", "InputReportAdUrl", "InputGaid", "InputHoaid", "InputYandexAdvId",
                StartupParamsTestUtils.SSL_ENABLED_FEATURE,
                StartupParamsTestUtils.CUSTOM_HOSTS_MAP,
                StartupParamsTestUtils.CLIDS_1, StartupParamsTestUtils.CLIDS_1,
                StartupParamsTestUtils.CUSTOM_HOST_SINGLE,
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV, true,
                true, true, true,
                "Request all identifiers with custom and feature except adv has all but outdated"
            },
            // end region
        });
    }

    @Mock
    private PreferencesClientDbStorage mPreferencesClientDbStorage;
    @Mock
    private MultiProcessSafeUuidProvider multiProcessSafeUuidProvider;
    @Mock
    private AdvIdentifiersFromIdentifierResultConverter advIdentifiersConverter;
    @Mock
    private ClidsStateChecker clidsStateChecker;
    @Mock
    private FeaturesHolder featuresHolder;
    @Mock
    private UuidValidator uuidValidator;
    @Rule
    public final MockedStaticRule<StartupRequiredUtils> sStartupRequiredUtils = new MockedStaticRule<>(StartupRequiredUtils.class);
    private final long nextStartupTime = 2374687;

    private StartupParams mStartupParams;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(any(Collection.class))).thenCallRealMethod();
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferencesClientDbStorage);

        IdentifiersResult deviceIdResult = new IdentifiersResult(mInputDeviceId, IdentifierStatus.OK, null);
        IdentifiersResult deviceIdHashResult = new IdentifiersResult(mInputDeviceIdHash, IdentifierStatus.OK, null);
        IdentifiersResult uuidResult = new IdentifiersResult(mInputUuid, IdentifierStatus.OK, null);
        IdentifiersResult reportAdResult = new IdentifiersResult(mInputReportAdUrl, IdentifierStatus.OK, null);
        IdentifiersResult getAdResult = new IdentifiersResult(mInputGetAdUrl, IdentifierStatus.OK, null);
        IdentifiersResult gaidResult = new IdentifiersResult(mGaid, IdentifierStatus.OK, null);
        IdentifiersResult hoaidResult = new IdentifiersResult(mHoaid, IdentifierStatus.OK, null);
        IdentifiersResult yandexResult = new IdentifiersResult(yandexAdvId, IdentifierStatus.OK, null);
        IdentifiersResult responseClidsResult = new IdentifiersResult(mInputResponseClids, IdentifierStatus.OK, null);
        IdentifiersResult customSdkHostsResult = new IdentifiersResult(JsonHelper.customSdkHostsToString(customSdkHosts), IdentifierStatus.OK, null);
        when(mPreferencesClientDbStorage.getDeviceIdResult()).thenReturn(deviceIdResult);
        when(mPreferencesClientDbStorage.getDeviceIdHashResult()).thenReturn(deviceIdHashResult);
        when(mPreferencesClientDbStorage.getUuidResult()).thenReturn(uuidResult);
        when(mPreferencesClientDbStorage.getAdUrlGetResult()).thenReturn(getAdResult);
        when(mPreferencesClientDbStorage.getAdUrlReportResult()).thenReturn(reportAdResult);
        when(mPreferencesClientDbStorage.getGaid()).thenReturn(gaidResult);
        when(mPreferencesClientDbStorage.getHoaid()).thenReturn(hoaidResult);
        when(mPreferencesClientDbStorage.getYandexAdvId()).thenReturn(yandexResult);
        when(mPreferencesClientDbStorage.getClientClids(nullable(String.class))).thenReturn(mInputClientClids);
        when(mPreferencesClientDbStorage.getResponseClidsResult()).thenReturn(responseClidsResult);
        when(mPreferencesClientDbStorage.getCustomHosts()).thenReturn(mInputCustomHosts);
        when(mPreferencesClientDbStorage.getClientClidsChangedAfterLastIdentifiersUpdate(true)).thenReturn(false);
        when(mPreferencesClientDbStorage.getCustomSdkHosts()).thenReturn(customSdkHostsResult);
        when(mPreferencesClientDbStorage.getFeatures()).thenReturn(new FeaturesInternal());
        when(featuresHolder.getFeatures()).thenReturn(new FeaturesInternal());
        when(featuresHolder.getFeature(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)).thenReturn(sslFeature);

        when(mPreferencesClientDbStorage.getNextStartupTime()).thenReturn(nextStartupTime);
        when(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(isOutdated);

        when(multiProcessSafeUuidProvider.readUuid()).thenReturn(new IdentifiersResult(mInputUuid, IdentifierStatus.OK, null));
        when(uuidValidator.isValid("Input uuid")).thenReturn(true);

        mStartupParams = new StartupParams(
            mPreferencesClientDbStorage,
            advIdentifiersConverter,
            clidsStateChecker,
            multiProcessSafeUuidProvider,
            new CustomSdkHostsHolder(),
            featuresHolder,
            new FeaturesConverter(),
            uuidValidator
        );
    }

    @Test
    public void testContainsIdentifiersAfterReadingFromPreferences() {
        assertThat(mStartupParams.containsIdentifiers(mRequestedIdentifiers)).isEqualTo(mExpectedContainsIdentifiers);
    }

    @Test
    public void testShouldSendStartupForIdentifiersAfterReadingPreferences() {
        assertThat(mStartupParams.shouldSendStartup(mRequestedIdentifiers)).isEqualTo(mExpectedShouldSendStartup);
    }

    @Test
    public void testShouldSendStartupAfterReadingPreferences() {
        assertThat(mStartupParams.shouldSendStartup()).isEqualTo(mExpectedShouldSendStartupForAll);
    }
}
