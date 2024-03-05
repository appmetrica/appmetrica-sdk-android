package io.appmetrica.analytics.impl.startup;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.StartupParamsItem;
import io.appmetrica.analytics.StartupParamsItemStatus;
import io.appmetrica.analytics.TestData;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.impl.FeaturesResult;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupParamsTest extends CommonTest {
    private static final IdentifiersResult TEST_AD_URL_GET = new IdentifiersResult("Test adUrlGet", IdentifierStatus.OK, null);
    private static final IdentifiersResult TEST_AD_URL_REPORT = new IdentifiersResult("Test adUrlReport", IdentifierStatus.OK, null);
    private static final long TEST_SERVER_SIDE_OFFSET = -280;

    private Map<String, String> mResponseClids = new HashMap<String, String>();
    private Map<String, String> mRequestClids = new HashMap<String, String>();
    @Mock
    private PreferencesClientDbStorage mPreferences;
    @Mock
    private Bundle mBundle;
    @Mock
    private AdvIdentifiersFromIdentifierResultConverter advIdentifiersConverter;
    @Mock
    private ClidsStateChecker clidsStateChecker;
    @Mock
    private MultiProcessSafeUuidProvider uuidProvider;
    @Mock
    private CustomSdkHostsHolder customSdkHostsHolder;
    @Mock
    private FeaturesHolder featuresHolder;
    @Mock
    private FeaturesConverter featuresConverter;
    @Mock
    private UuidValidator uuidValidator;
    private Context context;

    private StartupParams mStartupParams;

    private List<String> mValidCustomHosts = StartupParamsTestUtils.CUSTOM_HOSTS;
    private String mUuid = "Valid uuid";
    private String mDeviceId = "Valid device id";
    private String mDeviceIdHash = "Valid device id hash";
    private String mAdUrlGet = "https://ad.url.get";
    private String mAdUrlReport = "https://ad.url.report";
    private final String mGaid = "test gaid";
    private final String mHoaid = "test hoaid";
    private final String yandex = "test yandex adv_id";
    private final String customKey1 = "am";
    private final String customKey2 = "ads";
    private final Map<String, List<String>> customSdkHosts = new HashMap<>();
    private String mClidsFromPrefs = JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1);
    private String mClientClidsFromPrefs = StartupParamsTestUtils.CLIDS_2;
    private Map<String, String> clientClids = StartupParamsTestUtils.CLIDS_MAP_3;
    private final long mServerTimeOffset = 39787657;
    private final IdentifiersResult mUuidResult = new IdentifiersResult(mUuid, IdentifierStatus.OK, null);
    private final StartupParamsItem uuidStartupParam = new StartupParamsItem(mUuid, StartupParamsItemStatus.OK, null);
    private final IdentifiersResult mDeviceIdResult = new IdentifiersResult(mDeviceId, IdentifierStatus.OK, null);
    private final StartupParamsItem deviceIdStartupParam = new StartupParamsItem(mDeviceId, StartupParamsItemStatus.OK, null);
    private final IdentifiersResult deviceIdHashResult = new IdentifiersResult(mDeviceIdHash, IdentifierStatus.OK, null);
    private final StartupParamsItem deviceIdHashStartupParam = new StartupParamsItem(mDeviceIdHash, StartupParamsItemStatus.OK, null);
    private final IdentifiersResult adUrlIdentifierResult = new IdentifiersResult(mAdUrlGet, IdentifierStatus.OK, null);
    private final StartupParamsItem adUrlGetStartupParamsItem = new StartupParamsItem(mAdUrlGet, StartupParamsItemStatus.OK, null);
    private final IdentifiersResult mAdUrlReportResult = new IdentifiersResult(mAdUrlReport, IdentifierStatus.OK, null);
    private final StartupParamsItem adUrlReportStartupParamsItem = new StartupParamsItem(mAdUrlReport, StartupParamsItemStatus.OK, null);
    private final IdentifiersResult mGaidResult = new IdentifiersResult(mGaid, IdentifierStatus.OK, null);
    private final StartupParamsItem gaidStartupParamsItem = new StartupParamsItem(mGaid, StartupParamsItemStatus.OK, null);
    private final IdentifiersResult mHoaidResult = new IdentifiersResult(mHoaid, IdentifierStatus.OK, null);
    private final StartupParamsItem hoaidStartupParamsItem = new StartupParamsItem(mHoaid, StartupParamsItemStatus.OK, null);
    private final IdentifiersResult yandexResult = new IdentifiersResult(yandex, IdentifierStatus.OK, null);
    private final StartupParamsItem yandexStartupParamsItem = new StartupParamsItem(yandex, StartupParamsItemStatus.OK, null);
    private final IdentifiersResult customSdkHostsResult1 = new IdentifiersResult(JsonHelper.listToJsonString(Arrays.asList("host1", "host2")), IdentifierStatus.OK, null);
    private final StartupParamsItem customSdkHostsResult1StartupParamsItem = new StartupParamsItem(JsonHelper.listToJsonString(Arrays.asList("host1", "host2")), StartupParamsItemStatus.OK, null);
    private final IdentifiersResult customSdkHostsResult2 = new IdentifiersResult(JsonHelper.listToJsonString(Arrays.asList("host3")), IdentifierStatus.OK, null);
    private final StartupParamsItem customSdkHostsResult2StartupParamsItem = new StartupParamsItem(JsonHelper.listToJsonString(Arrays.asList("host3")), StartupParamsItemStatus.OK, null);
    private final IdentifiersResult customSdkHostsResult = new IdentifiersResult(JsonHelper.customSdkHostsToString(customSdkHosts), IdentifierStatus.OK, null);
    private final IdentifiersResult mClidsFromPrefsResult = new IdentifiersResult(mClidsFromPrefs, IdentifierStatus.OK, null);
    private final StartupParamsItem clidsFromPrefsStartupParamItems = new StartupParamsItem(mClidsFromPrefs, StartupParamsItemStatus.OK, null);
    private final IdentifiersResult sslFeatureResult = new IdentifiersResult("true", IdentifierStatus.OK, null);
    private final StartupParamsItem sslFeatureStartupParam = new StartupParamsItem("true", StartupParamsItemStatus.OK, null);
    private IdentifiersResult mRequestClidsResult;
    private IdentifiersResult mResponseClidsResult;
    private final IdentifiersResult mEmptyIdentifierResult = new IdentifiersResult("", IdentifierStatus.OK, null);
    private final IdentifiersResult mNullIdentifierResult = new IdentifiersResult(null, IdentifierStatus.OK, null);
    private final FeaturesInternal featuresInternal = new FeaturesInternal(true, IdentifierStatus.OK, null);
    private final long nextStartupTime = 327846276;
    private final IdentifiersResult initialCustomSdkHost = new IdentifiersResult(null, IdentifierStatus.OK, null);
    private final StartupParamsItem initialCustomSdkHostStartupParam = new StartupParamsItem(null, StartupParamsItemStatus.OK, null);

    private MockedConstruction.MockInitializer<ClientIdentifiersHolder> additionalInitializer;

    @Rule
    public MockedConstructionRule<ClientIdentifiersHolder> cHolder = new MockedConstructionRule<>(ClientIdentifiersHolder.class,
            new MockedConstruction.MockInitializer<ClientIdentifiersHolder>() {
                @Override
                public void prepare(ClientIdentifiersHolder mock, MockedConstruction.Context context) throws Throwable {
                    if (context.arguments().size() == 1 && context.arguments().contains(mBundle)) {
                        mockClientIdentifiersHolder(mock);
                        if (additionalInitializer != null) {
                            additionalInitializer.prepare(mock, context);
                        }
                    }
                }
            });
    @Rule
    public final MockedStaticRule<StartupRequiredUtils> startupRequiredUtils = new MockedStaticRule<>(StartupRequiredUtils.class);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        customSdkHosts.put(customKey1, Arrays.asList("host1", "host2"));
        customSdkHosts.put(customKey2, Arrays.asList("host3"));
        context = TestUtils.createMockedContext();

        when(uuidProvider.readUuid()).thenReturn(mUuidResult);
        when(mPreferences.getCustomSdkHosts()).thenReturn(initialCustomSdkHost);
        when(mPreferences.getFeatures()).thenReturn(new FeaturesInternal());
        when(featuresHolder.getFeatures()).thenReturn(new FeaturesInternal());

        mResponseClids.put("clid0", "123");
        mResponseClids.put("clid1", "456");
        mRequestClids.put("clid2", "789");
        mRequestClids.put("clid3", "120");
        mRequestClidsResult = new IdentifiersResult(JsonHelper.clidsToString(mRequestClids), IdentifierStatus.OK, null);
        mResponseClidsResult = new IdentifiersResult(JsonHelper.clidsToString(mResponseClids), IdentifierStatus.OK, null);

        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);

        when(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(any(Collection.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0);
            }
        });

        when(uuidValidator.isValid(mUuid)).thenReturn(true);

        mStartupParams = new StartupParams(
            mPreferences,
            advIdentifiersConverter,
            clidsStateChecker,
            uuidProvider,
            customSdkHostsHolder,
            featuresHolder,
            featuresConverter,
            uuidValidator
        );
    }

    @Test
    public void shouldUpdateFeatures() {
        when(mPreferences.getFeatures()).thenReturn(featuresInternal);
        clearInvocations(featuresHolder);
        createStartupParams();
        verify(featuresHolder).setFeatures(featuresInternal);
    }

    @Test
    public void testUuidShouldBeSameAsStoredIfExternalUuidIsEmptyAndStoredUuidIsNotEmpty() {
        String uuidString = UUID.randomUUID().toString();
        when(uuidValidator.isValid(uuidString)).thenReturn(true);
        IdentifiersResult prefUuid = new IdentifiersResult(uuidString, IdentifierStatus.OK, null);
        when(uuidProvider.readUuid()).thenReturn(prefUuid);

        StartupParams startupParams = createStartupParams();

        assertThat(startupParams.getUuid()).isEqualTo(uuidString);
    }

    @Test
    public void testUuidShouldNotBeChangedIfCallbackReturnsDifferentUuid() {
        String initialUuidString = UUID.randomUUID().toString();
        final String anotherUuidString = UUID.randomUUID().toString();
        when(uuidValidator.isValid(initialUuidString)).thenReturn(true);
        when(uuidValidator.isValid(anotherUuidString)).thenReturn(true);
        IdentifiersResult initialUuid = new IdentifiersResult(initialUuidString, IdentifierStatus.OK, null);
        when(uuidProvider.readUuid()).thenReturn(initialUuid);
        StartupParams startupParams = createStartupParams();

        additionalInitializer = new MockedConstruction.MockInitializer<ClientIdentifiersHolder>() {
            @Override
            public void prepare(ClientIdentifiersHolder mock, MockedConstruction.Context context) throws Throwable {
                when(mock.getUuid()).thenReturn(new IdentifiersResult(anotherUuidString, IdentifierStatus.OK, null));
            }
        };
        startupParams.updateAllParamsByReceiver(mBundle);

        assertThat(startupParams.getUuid()).isEqualTo(initialUuidString);
    }

    @Test
    public void testDeviceIdShouldNotBeClearedByReceiver() {
        String deviceId = UUID.randomUUID().toString();
        StartupParams startupParams = new StartupParams(context, mPreferences);
        startupParams.setDeviceId(new IdentifiersResult(deviceId, IdentifierStatus.OK, null));
        additionalInitializer = new MockedConstruction.MockInitializer<ClientIdentifiersHolder>() {
            @Override
            public void prepare(ClientIdentifiersHolder mock, MockedConstruction.Context context) {
                when(mock.getDeviceId()).thenReturn(null);
            }
        };
        startupParams.updateAllParamsByReceiver(mBundle);

        InOrder inOrder = inOrder(mPreferences);
        inOrder.verify(mPreferences).putDeviceIdResult(any(IdentifiersResult.class));
        assertThat(startupParams.getDeviceId()).isNotEmpty();

        additionalInitializer = new MockedConstruction.MockInitializer<ClientIdentifiersHolder>() {
            @Override
            public void prepare(ClientIdentifiersHolder mock, MockedConstruction.Context context) {
                when(mock.getDeviceId()).thenReturn(mEmptyIdentifierResult);
            }
        };
        startupParams.updateAllParamsByReceiver(mBundle);

        inOrder.verify(mPreferences).putDeviceIdResult(any(IdentifiersResult.class));
        assertThat(startupParams.getDeviceId()).isNotEmpty();
    }

    @Test
    public void testHostsShouldNotBeClearedByReceiver() {
        defineTestHostsFromPreferences();

        StartupParams startupParams = new StartupParams(context, mPreferences);
        additionalInitializer = new MockedConstruction.MockInitializer<ClientIdentifiersHolder>() {
            @Override
            public void prepare(ClientIdentifiersHolder mock, MockedConstruction.Context context) {
                when(mock.getGetAdUrl()).thenReturn(null);
                when(mock.getReportAdUrl()).thenReturn(null);
            }
        };
        startupParams.updateAllParamsByReceiver(mBundle);

        InOrder inOrder = inOrder(mPreferences);
        inOrder.verify(mPreferences).putAdUrlGetResult(any(IdentifiersResult.class));
        inOrder.verify(mPreferences).putAdUrlReportResult(any(IdentifiersResult.class));

        additionalInitializer = new MockedConstruction.MockInitializer<ClientIdentifiersHolder>() {
            @Override
            public void prepare(ClientIdentifiersHolder mock, MockedConstruction.Context context) {
                when(mock.getGetAdUrl()).thenReturn(mEmptyIdentifierResult);
                when(mock.getReportAdUrl()).thenReturn(mEmptyIdentifierResult);
            }
        };
        startupParams.updateAllParamsByReceiver(mBundle);

        inOrder.verify(mPreferences).putAdUrlGetResult(any(IdentifiersResult.class));
        inOrder.verify(mPreferences).putAdUrlReportResult(any(IdentifiersResult.class));
    }

    @Test
    public void testSomeStartupParamsShouldBeValidIfAllDefined() {
        defineAllStartupParamsFromPreferences();
        defineEmptyUuidFromPreferences();

        StartupParams startupParams = new StartupParams(context, mPreferences);

        assertThat(startupParams.containsIdentifiers(Arrays.asList(
                Constants.StartupParamsCallbackKeys.DEVICE_ID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH
        ))).isTrue();
    }

    @Test
    public void testSomeStartupParamsShouldNotBeValidIfOnlyOneDefined() {
        defineAllStartupParamsFromPreferences();
        defineEmptyUuidFromPreferences();
        defineEmptyDeviceIdFromPreferences();

        StartupParams startupParams = new StartupParams(context, mPreferences);

        assertThat(startupParams.containsIdentifiers(Arrays.asList(
                Constants.StartupParamsCallbackKeys.DEVICE_ID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH
        ))).isFalse();
    }

    @Test
    public void testSomeStartupParamsShoulNotBeValidIfNoneDefined() {
        StartupParams startupParams = new StartupParams(context, mPreferences);

        assertThat(startupParams.containsIdentifiers(Arrays.asList(
                Constants.StartupParamsCallbackKeys.DEVICE_ID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH
        ))).isFalse();
    }

    @Test
    public void testSomeStartupParamsShoulNotBeValidIfExtraDefined() {
        defineAllStartupParamsFromPreferences();

        StartupParams startupParams = new StartupParams(context, mPreferences);

        assertThat(startupParams.containsIdentifiers(Arrays.asList(
                Constants.StartupParamsCallbackKeys.DEVICE_ID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH
        ))).isTrue();
    }

    @Test
    public void testDeviceIdShouldSaveCorrectly() {
        String deviceId = UUID.randomUUID().toString();
        when(mPreferences.getDeviceIdResult()).thenReturn(new IdentifiersResult(deviceId, IdentifierStatus.OK, null));

        StartupParams startupParams = new StartupParams(context, mPreferences);

        assertThat(startupParams.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    public void testHostsShouldSavedCorrectly() {
        when(mPreferences.getAdUrlGetResult()).thenReturn(TEST_AD_URL_GET);
        when(mPreferences.getAdUrlReportResult()).thenReturn(TEST_AD_URL_REPORT);
        when(mPreferences.getCustomSdkHosts()).thenReturn(customSdkHostsResult);
        StartupParams startupParams = new StartupParams(context, mPreferences);

        verify(mPreferences).putAdUrlGetResult(TEST_AD_URL_GET);
        verify(mPreferences).putAdUrlReportResult(TEST_AD_URL_REPORT);
        verify(mPreferences).putCustomSdkHosts(customSdkHostsResult);
    }

    @Test
    public void featuresShouldBeSavedCorrectly() {
        when(mPreferences.getFeatures()).thenReturn(featuresInternal);
        new StartupParams(context, mPreferences);
        verify(mPreferences).putFeatures(featuresInternal);
    }

    @Test
    public void testServerTimeOffsetShouldSaveCorrectly() {
        when(mPreferences.getServerTimeOffset(nullable(Long.class))).thenReturn(TEST_SERVER_SIDE_OFFSET);
        createStartupParams();

        verify(mPreferences).putServerTimeOffset(TEST_SERVER_SIDE_OFFSET);
    }

    @Test
    public void testPutToMapSomeIdentifiers() {
        final String uuid = "uuid";
        final String getAdUrl = "getAdUrl";
        when(uuidProvider.readUuid()).thenReturn(new IdentifiersResult(uuid, IdentifierStatus.OK, null));
        when(mPreferences.getAdUrlGetResult()).thenReturn(new IdentifiersResult(getAdUrl, IdentifierStatus.OK, null));
        when(uuidValidator.isValid(uuid)).thenReturn(true);

        StartupParams startupParams = createStartupParams();
        List<String> params = Arrays.asList(
                Constants.StartupParamsCallbackKeys.UUID,
                Constants.StartupParamsCallbackKeys.GET_AD_URL
        );
        Map<String, StartupParamsItem> actual = new HashMap<String, StartupParamsItem>();
        startupParams.putToMap(params, actual);
        assertThat(actual).hasSize(2);
        assertThat(actual.get(Constants.StartupParamsCallbackKeys.UUID))
            .isEqualToComparingFieldByField(new StartupParamsItem(uuid, StartupParamsItemStatus.OK, null));
        assertThat(actual.get(Constants.StartupParamsCallbackKeys.GET_AD_URL))
            .isEqualToComparingFieldByField(new StartupParamsItem(getAdUrl, StartupParamsItemStatus.OK, null));
    }

    @Test
    public void testPutToMapInvalidIdentifier() {
        final String uuid = "uuid";
        when(uuidProvider.readUuid()).thenReturn(new IdentifiersResult(uuid, IdentifierStatus.OK, null));
        when(uuidValidator.isValid(uuid)).thenReturn(true);

        StartupParams startupParams = createStartupParams();
        List<String> params = Arrays.asList(
                Constants.StartupParamsCallbackKeys.UUID,
                "invalid param"
        );
        Map<String, StartupParamsItem> actual = new HashMap<String, StartupParamsItem>();
        startupParams.putToMap(params, actual);
        assertThat(actual).containsOnly(
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.UUID,
                        new StartupParamsItem(uuid, StartupParamsItemStatus.OK, null)
                )
        );
    }

    @Test
    public void testPutIdentifiersToMap() {
        final String customIdentifier1 = "custom_id1";
        final String customIdentifier2 = "custom_id2";
        final List<String> identifierKeys = Arrays.asList(
                Constants.StartupParamsCallbackKeys.UUID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                Constants.StartupParamsCallbackKeys.GET_AD_URL,
                Constants.StartupParamsCallbackKeys.CLIDS,
                Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID,
                Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID,
                Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID,
                customIdentifier1,
                customIdentifier2
        );
        mockPreferencesWithValidValues();
        StartupParams startupParams = createStartupParams();
        Map<String, StartupParamsItem> actual = new HashMap<String, StartupParamsItem>();
        final StartupParamsItem customResult1 = mock(StartupParamsItem.class);
        final StartupParamsItem customResult2 = mock(StartupParamsItem.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, StartupParamsItem> map = ((Map<String, StartupParamsItem>) invocation.getArgument(1));
                map.put(customIdentifier1, customResult1);
                map.put(customIdentifier2, customResult2);
                return null;
            }
        }).when(customSdkHostsHolder).putToMap(identifierKeys, actual);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, StartupParamsItem> map = ((Map<String, StartupParamsItem>) invocation.getArgument(1));
                map.put(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED, sslFeatureStartupParam);
                return null;
            }
        }).when(featuresHolder).putToMap(identifierKeys, actual);
        startupParams.putToMap(identifierKeys, actual);
        assertThat(actual).containsOnly(
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.UUID,
                        uuidStartupParam
                ),
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.DEVICE_ID,
                        deviceIdStartupParam
                ),
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                        deviceIdHashStartupParam
                ),
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                        adUrlReportStartupParamsItem
                ),
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    adUrlGetStartupParamsItem
                ),
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.CLIDS,
                        clidsFromPrefsStartupParamItems
                ),
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID,
                        gaidStartupParamsItem
                ),
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID,
                        hoaidStartupParamsItem
                ),
                new AbstractMap.SimpleEntry<String, StartupParamsItem>(
                        Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID,
                        yandexStartupParamsItem
                ),
                new AbstractMap.SimpleEntry<>(customIdentifier1, customResult1),
                new AbstractMap.SimpleEntry<>(customIdentifier2, customResult2),
                new AbstractMap.SimpleEntry<>(
                        Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED,
                        sslFeatureStartupParam
                )
        );
    }

    @Test
    public void testSetDifferentClientClids() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = new StartupParams(context, mPreferences);
        InOrder inOrder = inOrder(mPreferences);
        inOrder.verify(mPreferences).putResponseClidsResult(new IdentifiersResult(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1), IdentifierStatus.OK, null));
        inOrder.verify(mPreferences).putClientClids(StartupParamsTestUtils.CLIDS_2);
        assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1));
        assertThat(mStartupParams.getClientClids()).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2);
        mStartupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_3);
        inOrder.verify(mPreferences).putClientClids(StartupParamsTestUtils.CLIDS_3);
        inOrder.verify(mPreferences).putClientClidsChangedAfterLastIdentifiersUpdate(true);
        assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1));
        assertThat(mStartupParams.getClientClids()).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_3);
    }

    @Test
    public void testSetSameClientClids() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = new StartupParams(context, mPreferences);
        InOrder inOrder = inOrder(mPreferences);
        inOrder.verify(mPreferences).putResponseClidsResult(new IdentifiersResult(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1), IdentifierStatus.OK, null));
        inOrder.verify(mPreferences).putClientClids(StartupParamsTestUtils.CLIDS_2);
        assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1));
        assertThat(mStartupParams.getClientClids()).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2);
        clearInvocations(mPreferences);
        mStartupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_2);
        inOrder.verify(mPreferences, never()).putClientClids(anyString());
        inOrder.verify(mPreferences, never()).putClientClidsChangedAfterLastIdentifiersUpdate(anyBoolean());
        assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1));
        assertThat(mStartupParams.getClientClids()).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2);
    }

    @Test
    public void testSetClientClidsAfterNull() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        when(mPreferences.getClientClids(nullable(String.class))).thenReturn(null);
        mStartupParams = new StartupParams(context, mPreferences);
        InOrder inOrder = inOrder(mPreferences);
        inOrder.verify(mPreferences).putResponseClidsResult(new IdentifiersResult(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1), IdentifierStatus.OK, null));
        inOrder.verify(mPreferences).putClientClids("");
        assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1));
        assertThat(mStartupParams.getClientClids()).isNull();
        mStartupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_2);
        inOrder.verify(mPreferences).putClientClids(StartupParamsTestUtils.CLIDS_2);
        inOrder.verify(mPreferences).putClientClidsChangedAfterLastIdentifiersUpdate(true);
        assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1));
        assertThat(mStartupParams.getClientClids()).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2);
    }

    @Test
    public void testSetNullClientClidsAfterNotNull() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = new StartupParams(context, mPreferences);
        InOrder inOrder = inOrder(mPreferences);
        inOrder.verify(mPreferences).putResponseClidsResult(new IdentifiersResult(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1), IdentifierStatus.OK, null));
        inOrder.verify(mPreferences).putClientClids(StartupParamsTestUtils.CLIDS_2);
        assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1));
        assertThat(mStartupParams.getClientClids()).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2);
        mStartupParams.setClientClids(null);
        inOrder.verify(mPreferences, never()).putClientClids("");
        inOrder.verify(mPreferences, never()).putClientClidsChangedAfterLastIdentifiersUpdate(true);
        assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1));
        assertThat(mStartupParams.getClientClids()).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2);
    }

    @Test
    public void testShouldSendStartupForTheFirstTime() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        when(mPreferences.getClientClidsChangedAfterLastIdentifiersUpdate(true)).thenReturn(true);
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup()).isTrue();
    }

    @Test
    public void testShouldNotSendStartupForAllValid() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup()).isFalse();
    }

    @Test
    public void testShouldSendStartupForOutdated() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        when(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(true);
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup()).isTrue();
    }

    @Test
    public void shouldWriteClidsUpdatedToPreferences() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        clearInvocations(mPreferences);
        mStartupParams.setClientClids(clientClids);
        verify(mPreferences).putClientClidsChangedAfterLastIdentifiersUpdate(true);
        when(clidsStateChecker.doClientClidsMatchClientClidsForRequest(clientClids, mRequestClids)).thenReturn(true);
        ClientIdentifiersHolder mock = mock(ClientIdentifiersHolder.class);
        mockClientIdentifiersHolder(mock);
        mStartupParams.updateAllParamsByReceiver(mock);
        verify(mPreferences).putClientClidsChangedAfterLastIdentifiersUpdate(false);
    }

    @Test
    public void testShouldSendStartupForUpdateOnNewClids() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        mStartupParams.setClientClids(clientClids);
        when(clidsStateChecker.doClientClidsMatchClientClidsForRequest(clientClids, mRequestClids)).thenReturn(false);
        ClientIdentifiersHolder mock = mock(ClientIdentifiersHolder.class);
        mockClientIdentifiersHolder(mock);
        mStartupParams.updateAllParamsByReceiver(mock);
        assertThat(mStartupParams.shouldSendStartup()).isTrue();
    }

    @Test
    public void testShouldSendStartupIfRequestedGaid() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(
                Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID
        ))).isTrue();
    }

    @Test
    public void testShouldSendStartupIfRequestedHoaid() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(
                Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID
        ))).isTrue();
    }

    @Test
    public void testShouldSendStartupIfRequestedYandexAdvId() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(
                Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID
        ))).isTrue();
    }

    @Test
    public void shouldNotSendStartupForCustomKeyIfNotOutdated() {
        final String customKey = "custom key";
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        when(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(false);
        when(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(Arrays.asList(customKey))).thenReturn(Collections.<String>emptyList());
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(customKey))).isFalse();
        startupRequiredUtils.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(Arrays.asList(customKey));
            }
        });
    }

    @Test
    public void shouldNotSendStartupForCustomKeyIfOutdated() {
        final String customKey = "custom key";
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mockPreferencesWithValidValues();
        when(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(true);
        when(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(Arrays.asList(customKey))).thenReturn(Collections.<String>emptyList());
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(customKey))).isTrue();
    }

    @Test
    public void shouldNotSendStartupForSslPinningIfUpToDate() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        when(featuresHolder.getFeature(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)).thenReturn(sslFeatureResult);
        when(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(false);
        when(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(Arrays.asList(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)))
                .thenReturn(Collections.<String>emptyList());
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED))).isFalse();
    }

    @Test
    public void shouldSendStartupForSslPinningIOutdated() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        when(featuresHolder.getFeature(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)).thenReturn(sslFeatureResult);
        when(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(true);
        when(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(Arrays.asList(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)))
                .thenReturn(Collections.<String>emptyList());
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED))).isTrue();
    }

    @Test
    public void testShouldSendStartupForIdentifiersForAllValid() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(
                Constants.StartupParamsCallbackKeys.UUID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID
        ))).isFalse();
    }

    @Test
    public void testShouldSendStartupForIdentifiersForOutdated() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        when(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(true);
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(
                Constants.StartupParamsCallbackKeys.UUID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID
        ))).isTrue();
    }

    @Test
    public void testShouldSendStartupForIdentifiersForUpdateOnNewClids() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        mStartupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_3);
        assertThat(mStartupParams.shouldSendStartup(Arrays.asList(
                Constants.StartupParamsCallbackKeys.UUID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID
        ))).isTrue();
    }

    @Test
    public void testUpdateAllParamsByReceiverEverythingFilled() {
        final List<String> identifierKeys = Arrays.asList(
                Constants.StartupParamsCallbackKeys.GET_AD_URL,
                Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID,
                Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID,
                Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID,
                Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED,
                customKey1,
                customKey2
        );
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        when(clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                StartupUtils.decodeClids(mClientClidsFromPrefs),
                mRequestClids
        )).thenReturn(true);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, StartupParamsItem> map = ((Map<String, StartupParamsItem>) invocation.getArgument(1));
                map.put(customKey1, customSdkHostsResult1StartupParamsItem);
                map.put(customKey2, customSdkHostsResult2StartupParamsItem);
                return null;
            }
        }).when(customSdkHostsHolder).putToMap(eq(identifierKeys), any(Map.class));
        clearInvocations(customSdkHostsHolder, featuresHolder);
        mStartupParams.updateAllParamsByReceiver(mBundle);
        Map<String, StartupParamsItem> identifiers = new HashMap<String, StartupParamsItem>();
        mStartupParams.putToMap(identifierKeys, identifiers);
        verify(customSdkHostsHolder).update(customSdkHostsResult);
        verify(featuresHolder).setFeatures(featuresInternal);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(mStartupParams.getUuid()).isEqualTo(mUuid);
        softly.assertThat(mStartupParams.getDeviceId()).isEqualTo(mDeviceId);
        softly.assertThat(mStartupParams.getDeviceIDHash()).isEqualTo(mDeviceIdHash);
        softly.assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(mResponseClids));
        softly.assertThat(mStartupParams.getServerTimeOffsetSeconds()).isEqualTo(mServerTimeOffset);
        softly.assertThat(identifiers.get(Constants.StartupParamsCallbackKeys.GET_AD_URL))
                .isEqualToComparingFieldByField(adUrlGetStartupParamsItem);
        softly.assertThat(identifiers.get(Constants.StartupParamsCallbackKeys.REPORT_AD_URL))
                .isEqualToComparingFieldByField(adUrlReportStartupParamsItem);
        softly.assertThat(identifiers.get(Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID))
                .isEqualToComparingFieldByField(gaidStartupParamsItem);
        softly.assertThat(identifiers.get(Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID))
                .isEqualToComparingFieldByField(hoaidStartupParamsItem);
        softly.assertThat(identifiers.get(Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID))
                .isEqualToComparingFieldByField(yandexStartupParamsItem);
        softly.assertThat(identifiers.get(customKey1))
            .isEqualToComparingFieldByField(customSdkHostsResult1StartupParamsItem);
        softly.assertThat(identifiers.get(customKey2))
            .isEqualToComparingFieldByField(customSdkHostsResult2StartupParamsItem);
        softly.assertAll();
    }

    @Test
    public void testUpdateClidsAfterStartup() {
        final HashMap<String, String> startupClids = new HashMap<String, String>() {
            {
                put("clid", "from_startup");
            }
        };
        when(mPreferences.getClientClids(nullable(String.class))).thenReturn(StartupUtils.encodeClids(TestData.TEST_CLIDS));
        additionalInitializer = new MockedConstruction.MockInitializer<ClientIdentifiersHolder>() {
            @Override
            public void prepare(ClientIdentifiersHolder mock, MockedConstruction.Context context) {
                when(mock.getResponseClids())
                        .thenReturn(new IdentifiersResult(JsonHelper.clidsToString(startupClids), IdentifierStatus.OK, null));
                when(mock.getClientClidsForRequest())
                        .thenReturn(new IdentifiersResult(JsonHelper.clidsToString(TestData.TEST_CLIDS), IdentifierStatus.OK, null));
            }
        };

        mStartupParams = new StartupParams(context, mPreferences);
        when(clidsStateChecker.doClientClidsMatchClientClidsForRequest(TestData.TEST_CLIDS, TestData.TEST_CLIDS)).thenReturn(true);
        mStartupParams.updateAllParamsByReceiver(mBundle);

        assertThat(mStartupParams.getClids()).isEqualTo(JsonHelper.clidsToString(startupClids));
    }

    @Test
    public void testClidsAreNotUpdatedAfterStartupIfClientDoNotMatchRequest() {
        final HashMap<String, String> startupClids = new HashMap<String, String>() {
            {
                put("clid", "from_startup");
            }
        };
        when(mPreferences.getClientClids(nullable(String.class))).thenReturn(StartupUtils.encodeClids(TestData.TEST_CLIDS));

        additionalInitializer = new MockedConstruction.MockInitializer<ClientIdentifiersHolder>() {
            @Override
            public void prepare(ClientIdentifiersHolder mock, MockedConstruction.Context context) {
                when(mock.getResponseClids())
                        .thenReturn(new IdentifiersResult(JsonHelper.clidsToString(startupClids), IdentifierStatus.OK, null));
                when(mock.getClientClidsForRequest())
                        .thenReturn(new IdentifiersResult(JsonHelper.clidsToString(TestData.TEST_CLIDS), IdentifierStatus.OK, null));
            }
        };

        mStartupParams = createStartupParams();
        when(clidsStateChecker.doClientClidsMatchClientClidsForRequest(TestData.TEST_CLIDS, TestData.TEST_CLIDS)).thenReturn(false);
        mStartupParams.updateAllParamsByReceiver(mBundle);

        assertThat(mStartupParams.getClids()).isNotEqualTo(JsonHelper.clidsToString(startupClids));
    }

    @Test
    public void advIdentifiersAreUpdatedAnyway() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(mPreferences);
        mockPreferencesWithValidValues();
        mStartupParams = new StartupParams(context, mPreferences);
        additionalInitializer = new MockedConstruction.MockInitializer<ClientIdentifiersHolder>() {
            @Override
            public void prepare(ClientIdentifiersHolder mock, MockedConstruction.Context context) {
                when(mock.getGaid()).thenReturn(mNullIdentifierResult);
                when(mock.getHoaid()).thenReturn(mNullIdentifierResult);
                when(mock.getYandexAdvId()).thenReturn(mNullIdentifierResult);
            }
        };
        mStartupParams.updateAllParamsByReceiver(mBundle);
        verify(mPreferences).putGaid(mNullIdentifierResult);
        verify(mPreferences).putHoaid(mNullIdentifierResult);
        verify(mPreferences).putYandexAdvId(mNullIdentifierResult);
    }

    @Test
    public void testContainsIdentifiersClidsChanged() {
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        Map<String, String> newClids = new HashMap<String, String>();
        newClids.put("newclid0", "0");
        mStartupParams.setClientClids(newClids);
        assertThat(mStartupParams.containsIdentifiers(Arrays.asList(Constants.StartupParamsCallbackKeys.CLIDS))).isFalse();
    }

    @Test
    public void testContainsIdentifiersClidsDidNotChangeNewAreNull() {
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        mStartupParams.setClientClids(null);
        assertThat(mStartupParams.containsIdentifiers(Arrays.asList(Constants.StartupParamsCallbackKeys.CLIDS))).isTrue();
    }

    @Test
    public void testContainsIdentifiersClidsDidNotChangeNewAreEmpty() {
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        mStartupParams.setClientClids(new HashMap<String, String>());
        assertThat(mStartupParams.containsIdentifiers(Arrays.asList(Constants.StartupParamsCallbackKeys.CLIDS))).isTrue();
    }

    @Test
    public void testContainsIdentifiersClidsDidNotChangeNewAreTheSame() {
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        mStartupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_2);
        assertThat(mStartupParams.containsIdentifiers(Arrays.asList(Constants.StartupParamsCallbackKeys.CLIDS))).isTrue();
    }

    @Test
    public void testContainsIdentifiersClidsDidNotChange() {
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        assertThat(mStartupParams.containsIdentifiers(Arrays.asList(Constants.StartupParamsCallbackKeys.CLIDS))).isTrue();
    }

    @Test
    public void testListContainsIdentifiersGaid() {
        assertThat(mStartupParams.listContainsAdvIdentifiers(Arrays.asList(Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID))).isTrue();
    }

    @Test
    public void testListContainsIdentifiersHoaid() {
        assertThat(mStartupParams.listContainsAdvIdentifiers(Arrays.asList(Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID))).isTrue();
    }

    @Test
    public void testListContainsIdentifiersYandexAdvId() {
        assertThat(mStartupParams.listContainsAdvIdentifiers(Arrays.asList(Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID))).isTrue();
    }

    @Test
    public void testListDoesNotContainIdentifiers() {
        assertThat(mStartupParams.listContainsAdvIdentifiers(Arrays.asList(
                Constants.StartupParamsCallbackKeys.UUID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                Constants.StartupParamsCallbackKeys.GET_AD_URL,
                Constants.StartupParamsCallbackKeys.CLIDS
        ))).isFalse();
    }

    @Test
    public void testGetCachedAdvIdentifiers() {
        when(mPreferences.getGaid()).thenReturn(mGaidResult);
        when(mPreferences.getHoaid()).thenReturn(mHoaidResult);
        when(mPreferences.getYandexAdvId()).thenReturn(yandexResult);
        mStartupParams = createStartupParams();
        AdvIdentifiersResult converted = mock(AdvIdentifiersResult.class);
        when(advIdentifiersConverter.convert(mGaidResult, mHoaidResult, yandexResult)).thenReturn(converted);
        assertThat(mStartupParams.getCachedAdvIdentifiers()).isSameAs(converted);
    }

    @NonNull
    private StartupParams createStartupParams() {
        return new StartupParams(
            mPreferences,
            advIdentifiersConverter,
            clidsStateChecker,
            uuidProvider,
            customSdkHostsHolder,
            featuresHolder,
            featuresConverter,
            uuidValidator
        );
    }

    private void defineAllStartupParamsFromPreferences() {
        defineRandomUuidFromPrefencesSpy();
        defineRandomDeviceIdFromPreferences();
        defineRandomDeviceIDHashFromPreferences();
        defineTestHostsFromPreferences();
        defineClidsFromPreferences();
        defineCustomSdkHostsFromPreferences();
    }

    private void defineClidsFromPreferences() {
        when(mPreferences.getResponseClidsResult()).thenReturn(mResponseClidsResult);
    }

    private void defineCustomSdkHostsFromPreferences() {
        when(mPreferences.getCustomSdkHosts()).thenReturn(customSdkHostsResult);
    }

    private void defineRandomDeviceIdFromPreferences() {
        defineDeviceIdFromPreferences(new IdentifiersResult(UUID.randomUUID().toString(), IdentifierStatus.OK, null));
    }

    private void defineRandomDeviceIDHashFromPreferences() {
        defineDeviceIdHashFromPreferences(new IdentifiersResult(UUID.randomUUID().toString(), IdentifierStatus.OK, null));
    }

    private void defineEmptyDeviceIdFromPreferences() {
        defineDeviceIdFromPreferences(new IdentifiersResult("", IdentifierStatus.OK, null));
    }

    private void defineDeviceIdFromPreferences(IdentifiersResult deviceId) {
        when(mPreferences.getDeviceIdResult()).thenReturn(deviceId);
    }

    private void defineRandomUuidFromPrefencesSpy() {
        defineUuidFromPreferences(new IdentifiersResult(UUID.randomUUID().toString(), IdentifierStatus.OK, null));
    }

    private void defineUuidFromPreferences(IdentifiersResult prefUuid) {
        doReturn(prefUuid).when(mPreferences).getUuidResult();
    }

    private void defineEmptyUuidFromPreferences() {
        defineUuidFromPreferences(new IdentifiersResult("", IdentifierStatus.OK, null));
    }

    private void defineTestAdUrlGetFromPreferences() {
        defineAdUrlGetFromPrefences(TEST_AD_URL_GET);
    }

    private void defineAdUrlGetFromPrefences(IdentifiersResult adUrlGet) {
        when(mPreferences.getAdUrlGetResult()).thenReturn(adUrlGet);
    }

    private void defineTestAdUrlReportFromPreferences() {
        defineAdUrlReportFromPreferences(TEST_AD_URL_REPORT);
    }

    private void defineAdUrlReportFromPreferences(IdentifiersResult adUrlReport) {
        when(mPreferences.getAdUrlReportResult()).thenReturn(adUrlReport);
    }

    private void defineDeviceIdHashFromPreferences(IdentifiersResult deviceIdHash) {
        when(mPreferences.getDeviceIdHashResult()).thenReturn(deviceIdHash);
    }

    private void defineTestHostsFromPreferences() {
        defineTestAdUrlGetFromPreferences();
        defineTestAdUrlReportFromPreferences();
    }

    private void mockPreferencesWithValidValues() {
        when(mPreferences.getServerTimeOffset(0)).thenReturn(mServerTimeOffset);
        when(mPreferences.getCustomHosts()).thenReturn(mValidCustomHosts);
        when(mPreferences.getDeviceIdResult()).thenReturn(mDeviceIdResult);
        when(mPreferences.getDeviceIdHashResult()).thenReturn(deviceIdHashResult);
        when(mPreferences.getUuidResult()).thenReturn(mUuidResult);
        when(mPreferences.getAdUrlGetResult()).thenReturn(adUrlIdentifierResult);
        when(mPreferences.getAdUrlReportResult()).thenReturn(mAdUrlReportResult);
        when(mPreferences.getGaid()).thenReturn(mGaidResult);
        when(mPreferences.getHoaid()).thenReturn(mHoaidResult);
        when(mPreferences.getYandexAdvId()).thenReturn(yandexResult);
        when(mPreferences.getResponseClidsResult()).thenReturn(mClidsFromPrefsResult);
        when(mPreferences.getClientClids(nullable(String.class))).thenReturn(mClientClidsFromPrefs);
        when(mPreferences.getClientClidsChangedAfterLastIdentifiersUpdate(true)).thenReturn(false);
        when(mPreferences.getCustomSdkHosts()).thenReturn(customSdkHostsResult);
        when(mPreferences.getNextStartupTime()).thenReturn(nextStartupTime);
        when(mPreferences.getFeatures()).thenReturn(featuresInternal);
    }

    private void mockClientIdentifiersHolder(ClientIdentifiersHolder mock) {
        when(mock.getUuid()).thenReturn(mUuidResult);
        when(mock.getDeviceId()).thenReturn(mDeviceIdResult);
        when(mock.getDeviceIdHash()).thenReturn(deviceIdHashResult);
        when(mock.getReportAdUrl()).thenReturn(mAdUrlReportResult);
        when(mock.getGetAdUrl()).thenReturn(adUrlIdentifierResult);
        when(mock.getResponseClids()).thenReturn(mResponseClidsResult);
        when(mock.getClientClidsForRequest()).thenReturn(mRequestClidsResult);
        when(mock.getServerTimeOffset()).thenReturn(mServerTimeOffset);
        when(mock.getGaid()).thenReturn(mGaidResult);
        when(mock.getHoaid()).thenReturn(mHoaidResult);
        when(mock.getYandexAdvId()).thenReturn(yandexResult);
        when(mock.getCustomSdkHosts()).thenReturn(customSdkHostsResult);
        when(mock.getNextStartupTime()).thenReturn(nextStartupTime);
        when(mock.getFeatures()).thenReturn(featuresInternal);
    }

    @Test
    public void shouldUpdateAllParamsInPreferencesOnCreate() {
        mockPreferencesWithValidValues();
        clearInvocations(mPreferences);
        when(customSdkHostsHolder.getCommonResult()).thenReturn(customSdkHostsResult);
        when(featuresHolder.getFeatures()).thenReturn(featuresInternal);
        createStartupParams();
        verify(mPreferences).putUuidResult(mUuidResult);
        verify(mPreferences).putDeviceIdResult(mDeviceIdResult);
        verify(mPreferences).putDeviceIdHashResult(deviceIdHashResult);
        verify(mPreferences).putGaid(mGaidResult);
        verify(mPreferences).putHoaid(mHoaidResult);
        verify(mPreferences).putYandexAdvId(yandexResult);
        verify(mPreferences).putAdUrlReportResult(mAdUrlReportResult);
        verify(mPreferences).putAdUrlGetResult(adUrlIdentifierResult);
        verify(mPreferences).putServerTimeOffset(mServerTimeOffset);
        verify(mPreferences).putResponseClidsResult(mClidsFromPrefsResult);
        verify(mPreferences).putClientClids(mClientClidsFromPrefs);
        verify(mPreferences).putClientClidsChangedAfterLastIdentifiersUpdate(false);
        verify(mPreferences).putCustomSdkHosts(customSdkHostsResult);
        verify(mPreferences).putFeatures(featuresInternal);
        verify(mPreferences).putNextStartupTime(nextStartupTime);
        verify(mPreferences).commit();
    }

    @Test
    public void shouldUpdateAllParamsInPreferencesOnUpdateFromReceiver() {
        mStartupParams.setClientClids(clientClids);
        clearInvocations(mPreferences);
        when(customSdkHostsHolder.getCommonResult()).thenReturn(customSdkHostsResult);
        when(clidsStateChecker.doClientClidsMatchClientClidsForRequest(nullable(Map.class), nullable(Map.class))).thenReturn(true);
        mStartupParams.updateAllParamsByReceiver(mBundle);
        verify(mPreferences).putUuidResult(mUuidResult);
        verify(mPreferences).putDeviceIdResult(mDeviceIdResult);
        verify(mPreferences).putDeviceIdHashResult(deviceIdHashResult);
        verify(mPreferences).putGaid(mGaidResult);
        verify(mPreferences).putHoaid(mHoaidResult);
        verify(mPreferences).putYandexAdvId(yandexResult);
        verify(mPreferences).putAdUrlReportResult(mAdUrlReportResult);
        verify(mPreferences).putAdUrlGetResult(adUrlIdentifierResult);
        verify(mPreferences).putServerTimeOffset(mServerTimeOffset);
        verify(mPreferences).putResponseClidsResult(mResponseClidsResult);
        verify(mPreferences).putClientClids(StartupUtils.encodeClids(clientClids));
        verify(mPreferences).putClientClidsChangedAfterLastIdentifiersUpdate(false);
        verify(mPreferences).putCustomSdkHosts(customSdkHostsResult);
        verify(mPreferences).putNextStartupTime(nextStartupTime);
        verify(mPreferences).commit();
    }

    @Test
    public void shouldUpdateAllParamsInPreferencesOnSetClientClids() {
        final Map<String, String> newClids = new HashMap<>();
        newClids.put("clid878", "999000");
        when(customSdkHostsHolder.getCommonResult()).thenReturn(customSdkHostsResult);
        when(featuresHolder.getFeatures()).thenReturn(featuresInternal);
        mockPreferencesWithValidValues();
        mStartupParams = createStartupParams();
        clearInvocations(mPreferences);
        mStartupParams.setClientClids(newClids);
        verify(mPreferences).putUuidResult(mUuidResult);
        verify(mPreferences).putDeviceIdResult(mDeviceIdResult);
        verify(mPreferences).putDeviceIdHashResult(deviceIdHashResult);
        verify(mPreferences).putGaid(mGaidResult);
        verify(mPreferences).putHoaid(mHoaidResult);
        verify(mPreferences).putYandexAdvId(yandexResult);
        verify(mPreferences).putAdUrlReportResult(mAdUrlReportResult);
        verify(mPreferences).putAdUrlGetResult(adUrlIdentifierResult);
        verify(mPreferences).putServerTimeOffset(mServerTimeOffset);
        verify(mPreferences).putResponseClidsResult(mClidsFromPrefsResult);
        verify(mPreferences).putClientClids(StartupUtils.encodeClids(newClids));
        verify(mPreferences).putClientClidsChangedAfterLastIdentifiersUpdate(true);
        verify(mPreferences).putCustomSdkHosts(customSdkHostsResult);
        verify(mPreferences).putFeatures(featuresInternal);
        verify(mPreferences).putNextStartupTime(nextStartupTime);
        verify(mPreferences).commit();
    }

    @Test
    public void getFeatures() {
        FeaturesResult result = mock(FeaturesResult.class);
        when(featuresHolder.getFeatures()).thenReturn(featuresInternal);
        mStartupParams = createStartupParams();
        when(featuresConverter.convert(featuresInternal)).thenReturn(result);
        assertThat(mStartupParams.getFeatures()).isSameAs(result);
    }
}
