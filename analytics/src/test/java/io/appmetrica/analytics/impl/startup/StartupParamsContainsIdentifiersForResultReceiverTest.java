package io.appmetrica.analytics.impl.startup;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
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
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class StartupParamsContainsIdentifiersForResultReceiverTest extends CommonTest {

    private ClientIdentifiersHolder mClientIdentifiersHolder;
    private List<String> mRequestedIdentifiers;
    private Map<String, String> mClientClids;
    private final boolean shouldUpdateClids;
    private final boolean isOutdated;
    private boolean mExpectedContainsIdentifiers;
    private boolean mExpectedShouldSendStartupForAll;
    private boolean mExpectedShouldSendStartup;

    private static final Map<String, String> CLIENT_CLIDS = StartupParamsTestUtils.CLIDS_MAP_1;
    private static final IdentifiersResult REQUEST_CLIDS = new IdentifiersResult(
            JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1),
            IdentifierStatus.OK,
            null
    );
    private static final IdentifiersResult RESPONSE_CLIDS = new IdentifiersResult(
            JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_2),
            IdentifierStatus.OK,
            null
    );

    private static final IdentifiersResult EMPTY_IDENTIFIER = new IdentifiersResult("", IdentifierStatus.OK, null);
    private static final IdentifiersResult NULL_IDENTIFIER = new IdentifiersResult(null, IdentifierStatus.OK, null);
    private static final IdentifiersResult UUID = new IdentifiersResult("uuid", IdentifierStatus.OK, null);
    private static final IdentifiersResult DEVICE_ID = new IdentifiersResult("device id", IdentifierStatus.OK, null);
    private static final IdentifiersResult DEVICE_ID_HASH = new IdentifiersResult("device id hash", IdentifierStatus.OK, null);
    private static final IdentifiersResult GET_AD_URL = new IdentifiersResult("https:\\\\get.ad.url", IdentifierStatus.OK, null);
    private static final IdentifiersResult REPORT_AD_URL = new IdentifiersResult("https:\\\\report.ad.url", IdentifierStatus.OK, null);
    private static final IdentifiersResult HOAID = new IdentifiersResult("hoaid", IdentifierStatus.OK, null);
    private static final IdentifiersResult GAID = new IdentifiersResult("gaid", IdentifierStatus.OK, null);
    private static final IdentifiersResult YANDEX_ADV_ID = new IdentifiersResult("yandex_adv_id", IdentifierStatus.OK, null);
    private static final List<String> CUSTOM_HOSTS1 = Arrays.asList("host1");
    private static final List<String> CUSTOM_HOSTS2 = Arrays.asList("host2");
    private static final IdentifiersResult CUSTOM_SDK_HOSTS;
    private static final FeaturesInternal FEATURES = new FeaturesInternal(false, IdentifierStatus.OK, null);

    static {
        Map<String, List<String>> customSdkHostsMap = new HashMap<>();
        customSdkHostsMap.put(StartupParamsTestUtils.CUSTOM_IDENTIFIER1, CUSTOM_HOSTS1);
        customSdkHostsMap.put(StartupParamsTestUtils.CUSTOM_IDENTIFIER2, CUSTOM_HOSTS2);
        CUSTOM_SDK_HOSTS = new IdentifiersResult(JsonHelper.customSdkHostsToString(customSdkHostsMap), IdentifierStatus.OK, null);
    }

    private static final Long SERVER_TIME_OFFSET = 123L;

    public StartupParamsContainsIdentifiersForResultReceiverTest(ClientIdentifiersHolder clientIdentifiersHolder,
                                                                 Consumer<ClientIdentifiersHolder> behaviorChanger,
                                                                 boolean isOutdated,
                                                                 List<String> requestedIdentifiers,
                                                                 Map<String, String> clientClids,
                                                                 boolean shouldUpdateClids,
                                                                 boolean expectedContainsIdentifiers,
                                                                 boolean expectedShouldSendStartupForAll,
                                                                 boolean shouldSendStartup) {
        mClientIdentifiersHolder = clientIdentifiersHolder;
        behaviorChanger.consume(mClientIdentifiersHolder);
        mRequestedIdentifiers = requestedIdentifiers;
        this.isOutdated = isOutdated;
        mClientClids = clientClids;
        this.shouldUpdateClids = shouldUpdateClids;
        mExpectedContainsIdentifiers = expectedContainsIdentifiers;
        mExpectedShouldSendStartupForAll = expectedShouldSendStartupForAll;
        mExpectedShouldSendStartup = shouldSendStartup;
    }

    private static ClientIdentifiersHolder prepareEmptyIdentifiersHolderMock() {
        IdentifiersResult emptyIdentifiers = new IdentifiersResult(null, IdentifierStatus.UNKNOWN, null);
        ClientIdentifiersHolder emptyClientIdentifiersHolder = mock(ClientIdentifiersHolder.class);
        when(emptyClientIdentifiersHolder.getResponseClids()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getClientClidsForRequest()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getReportAdUrl()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getGetAdUrl()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getDeviceId()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getUuid()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getDeviceIdHash()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getGaid()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getHoaid()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getYandexAdvId()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getCustomSdkHosts()).thenReturn(emptyIdentifiers);
        when(emptyClientIdentifiersHolder.getFeatures()).thenReturn(new FeaturesInternal());
        return emptyClientIdentifiersHolder;
    }

    private static ClientIdentifiersHolder prepareFilledIdentifiersHolderMock() {
        ClientIdentifiersHolder filledClientIdentifiersHolder = mock(ClientIdentifiersHolder.class);
        when(filledClientIdentifiersHolder.getResponseClids()).thenReturn(RESPONSE_CLIDS);
        when(filledClientIdentifiersHolder.getClientClidsForRequest()).thenReturn(REQUEST_CLIDS);
        when(filledClientIdentifiersHolder.getReportAdUrl()).thenReturn(REPORT_AD_URL);
        when(filledClientIdentifiersHolder.getGetAdUrl()).thenReturn(GET_AD_URL);
        when(filledClientIdentifiersHolder.getDeviceId()).thenReturn(DEVICE_ID);
        when(filledClientIdentifiersHolder.getUuid()).thenReturn(UUID);
        when(filledClientIdentifiersHolder.getDeviceIdHash()).thenReturn(DEVICE_ID_HASH);
        when(filledClientIdentifiersHolder.getGaid()).thenReturn(GAID);
        when(filledClientIdentifiersHolder.getHoaid()).thenReturn(HOAID);
        when(filledClientIdentifiersHolder.getYandexAdvId()).thenReturn(YANDEX_ADV_ID);
        when(filledClientIdentifiersHolder.getServerTimeOffset()).thenReturn(SERVER_TIME_OFFSET);
        when(filledClientIdentifiersHolder.getCustomSdkHosts()).thenReturn(CUSTOM_SDK_HOSTS);
        when(filledClientIdentifiersHolder.getFeatures()).thenReturn(FEATURES);
        return filledClientIdentifiersHolder;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        final ClientIdentifiersHolder emptyClientIdentifiersHolder = prepareEmptyIdentifiersHolderMock();
        ClientIdentifiersHolder filledClientIdentifiersHolder = prepareFilledIdentifiersHolderMock();

        Consumer<ClientIdentifiersHolder> doNothingConsumer = new Consumer<ClientIdentifiersHolder>() {
            @Override
            public void consume(@Nullable ClientIdentifiersHolder data) {

            }
        };

        return Arrays.asList(new Object[][]{
                //region All
                //#0
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.ALL_IDENTIFIERS,
                        null, true,
                        true, false, true
                },
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.ALL_IDENTIFIERS,
                        null, true,
                        true, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.ALL_IDENTIFIERS,
                        null, false,
                        false, true, true
                },
                //endregion
                //region All except ads
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV,
                        null, true,
                        true, false, false
                },
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV,
                        null, true,
                        true, true, true
                },
                //#5
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_EXCEPT_ADV,
                        null, false,
                        false, true, true
                },
                //endregion
                //region UUID
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getUuid()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID),
                        null, true,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getUuid()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID),
                        null, true,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getUuid()).thenReturn(UUID);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID),
                        null, false,
                        true, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getUuid()).thenReturn(UUID);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID),
                        null, false,
                        true, true, true
                },
                //#10
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_UUID),
                        null, false,
                        false, true, true
                },
                //endregion
                //region Device ID
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getDeviceId()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID),
                        null, true,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getDeviceId()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID),
                        null, true,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getDeviceId()).thenReturn(DEVICE_ID);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID),
                        null, false,
                        true, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getDeviceId()).thenReturn(DEVICE_ID);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID),
                        null, false,
                        true, true, true
                },
                //#15
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID),
                        null, false,
                        false, true, true
                },
                //endregion
                //region Device ID Hash
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getDeviceIdHash()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH),
                        null, true,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getDeviceIdHash()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH),
                        null, true,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getDeviceIdHash()).thenReturn(DEVICE_ID_HASH);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH),
                        null, false,
                        true, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getDeviceIdHash()).thenReturn(DEVICE_ID_HASH);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH),
                        null, false,
                        true, true, true
                },
                //#20
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_DEVICE_ID_HASH),
                        null, false,
                        false, true, true
                },
                //endregion
                //region Get AD Url
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getGetAdUrl()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GET_AD_URL),
                        null, true,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getGetAdUrl()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GET_AD_URL),
                        null, true,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getGetAdUrl()).thenReturn(GET_AD_URL);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GET_AD_URL),
                        null, false,
                        true, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getGetAdUrl()).thenReturn(GET_AD_URL);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GET_AD_URL),
                        null, false,
                        true, true, true
                },
                //#25
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GET_AD_URL),
                        null, false,
                        false, true, true
                },
                //endregion
                //region Report AD Url
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getReportAdUrl()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_REPORT_AD_URL),
                        null, true,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getReportAdUrl()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_REPORT_AD_URL),
                        null, true,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getReportAdUrl()).thenReturn(REPORT_AD_URL);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_REPORT_AD_URL),
                        null, false,
                        true, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getReportAdUrl()).thenReturn(REPORT_AD_URL);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_REPORT_AD_URL),
                        null, false,
                        true, true, true
                },
                //#30
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_REPORT_AD_URL),
                        null, false,
                        false, true, true
                },
                //endregion
                //region Gaid
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getGaid()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID),
                        null, true,
                        false, false, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getGaid()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID),
                        null, true,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getGaid()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID),
                        null, true,
                        false, false, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getGaid()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID),
                        null, true,
                        false, true, true
                },
                //#35
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getGaid()).thenReturn(GAID);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID),
                        null, false,
                        true, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_GOOGLE_ADV_ID),
                        null, false,
                        false, true, true
                },
                //endregion
                //region Hoaid
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getHoaid()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID),
                        null, true,
                        false, false, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getHoaid()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID),
                        null, true,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getHoaid()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID),
                        null, true,
                        false, false, true
                },
                //#40
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getHoaid()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID),
                        null, true,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getHoaid()).thenReturn(HOAID);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID),
                        null, false,
                        true, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_HUAWEI_ADV_ID),
                        null, false,
                        false, true, true
                },
                //endregion
                //region Yandex Adv Id
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getYandexAdvId()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID),
                        null, true,
                        false, false, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getYandexAdvId()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID),
                        null, true,
                        false, true, true
                },
                //#45
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getYandexAdvId()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID),
                        null, true,
                        false, false, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getYandexAdvId()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID),
                        null, true,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getYandexAdvId()).thenReturn(HOAID);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID),
                        null, false,
                        true, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_YANDEX_ADV_ID),
                        null, false,
                        false, true, true
                },
                //endregion
                //region Clids
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getResponseClids()).thenReturn(NULL_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        null, true,
                        false, true, true
                },
                //#50
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getResponseClids()).thenReturn(EMPTY_IDENTIFIER);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        null, true,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getResponseClids()).thenReturn(RESPONSE_CLIDS);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        null, false,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        null, false,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getResponseClids()).thenReturn(RESPONSE_CLIDS);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        null, false,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getResponseClids()).thenReturn(RESPONSE_CLIDS);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        StartupParamsTestUtils.CLIDS_MAP_1, false,
                        false, true, true
                },
                //#55
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getResponseClids()).thenReturn(RESPONSE_CLIDS);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, false, false
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getResponseClids()).thenReturn(RESPONSE_CLIDS);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getResponseClids()).thenReturn(RESPONSE_CLIDS);
                            }
                        },
                        false,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getResponseClids()).thenReturn(RESPONSE_CLIDS);
                            }
                        },
                        true,
                        Collections.singletonList(StartupParamsTestUtils.IDENTIFIER_KEY_CLIDS),
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                //endregion
                // region custom identifiers
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, false, false
                },
                //#60
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        false, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        false, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getCustomSdkHosts()).thenReturn(new IdentifiersResult(null, IdentifierStatus.UNKNOWN, null));
                            }
                        },
                        false,
                        StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        false, false, false
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getCustomSdkHosts()).thenReturn(new IdentifiersResult(null, IdentifierStatus.UNKNOWN, null));
                            }
                        },
                        true,
                        StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        false, true, true
                },
                //#65
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getCustomSdkHosts()).thenReturn(CUSTOM_SDK_HOSTS);
                            }
                        },
                        false,
                        StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getCustomSdkHosts()).thenReturn(CUSTOM_SDK_HOSTS);
                            }
                        },
                        true,
                        StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, false, true
                },
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_EXCEPT_ADS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, false, false
                },
                //#70
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_EXCEPT_ADS,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                //endregion
                // region features
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, false, false
                },
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        false, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        false, true, true
                },
                // #75
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getFeatures()).thenReturn(new FeaturesInternal(null, IdentifierStatus.UNKNOWN, null));
                            }
                        },
                        false,
                        StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        false, false, false
                },
                {
                        filledClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getFeatures()).thenReturn(new FeaturesInternal(null, IdentifierStatus.UNKNOWN, null));
                            }
                        },
                        true,
                        StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        false, true, true
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getFeatures()).thenReturn(FEATURES);
                            }
                        },
                        false,
                        StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, false
                },
                {
                        emptyClientIdentifiersHolder,
                        new Consumer<ClientIdentifiersHolder>() {
                            @Override
                            public void consume(@Nullable ClientIdentifiersHolder data) {
                                when(data.getFeatures()).thenReturn(FEATURES);
                            }
                        },
                        true,
                        StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, false, true
                },
                // #80
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        false,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, false, false
                },
                {
                        filledClientIdentifiersHolder,
                        doNothingConsumer,
                        true,
                        StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV,
                        StartupParamsTestUtils.CLIDS_MAP_1, true,
                        true, true, true
                },
                //endregion
        });
    }

    @Mock
    private PreferencesClientDbStorage mStorage;
    @Mock
    private MultiProcessSafeUuidProvider multiProcessSafeUuidProvider;
    @Mock
    private AdvIdentifiersFromIdentifierResultConverter advIdentifiersConverter;
    @Mock
    private ClidsStateChecker clidsStateChecker;
    @Rule
    public final MockedStaticRule<StartupRequiredUtils> sStartupRequiredUtils = new MockedStaticRule<>(StartupRequiredUtils.class);

    private StartupParams mStartupParams;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mStorage);
        when(mStorage.getClientClids(nullable(String.class))).thenReturn(StartupUtils.encodeClids(CLIENT_CLIDS));
        long nextStartupTime = 374687;
        when(mClientIdentifiersHolder.getNextStartupTime()).thenReturn(nextStartupTime);
        when(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(isOutdated);
        when(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(any(Collection.class))).thenCallRealMethod();
        when(mStorage.getClientClidsChangedAfterLastIdentifiersUpdate(true)).thenReturn(false);
        when(mStorage.getCustomSdkHosts()).thenReturn(new IdentifiersResult(null, IdentifierStatus.UNKNOWN, null));
        when(mStorage.getFeatures()).thenReturn(new FeaturesInternal());

        mStartupParams = new StartupParams(
                mStorage,
            advIdentifiersConverter,
                clidsStateChecker,
                multiProcessSafeUuidProvider,
                new CustomSdkHostsHolder(),
                new FeaturesHolder(),
                new FeaturesConverter()
        );
        mStartupParams.setClientClids(mClientClids);
        Map<String, String> requestClids = JsonHelper.clidsFromString(mClientIdentifiersHolder.getClientClidsForRequest().id);
        when(clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                mClientClids,
                requestClids
        )).thenReturn(shouldUpdateClids);
        when(clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                CLIENT_CLIDS,
                requestClids
        )).thenReturn(shouldUpdateClids);
        mStartupParams.updateAllParamsByReceiver(mClientIdentifiersHolder);
    }

    @Test
    public void testContainsIdentifiersAfterReceivingResult() {
        assertThat(mStartupParams.containsIdentifiers(mRequestedIdentifiers)).isEqualTo(mExpectedContainsIdentifiers);
    }

    @Test
    public void testShouldSendStartupForIdentifiersAfterReceivingResult() {
        assertThat(mStartupParams.shouldSendStartup(mRequestedIdentifiers)).isEqualTo(mExpectedShouldSendStartup);
    }

    @Test
    public void testShouldSendStartupAfterReceivingResult() {
        assertThat(mStartupParams.shouldSendStartup()).isEqualTo(mExpectedShouldSendStartupForAll);
    }
}
