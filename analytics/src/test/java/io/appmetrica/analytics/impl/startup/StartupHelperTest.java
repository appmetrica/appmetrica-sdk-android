package io.appmetrica.analytics.impl.startup;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.StartupParamsItem;
import io.appmetrica.analytics.StartupParamsItemStatus;
import io.appmetrica.analytics.TestData;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.FeaturesResult;
import io.appmetrica.analytics.impl.ReportsHandler;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.db.storage.MockedKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupHelperTest extends CommonTest {

    private static final IdentifiersResult TEST_DEVICE_ID = new IdentifiersResult("deviceid12345", IdentifierStatus.OK, null);
    private static final IdentifiersResult TEST_UUID = new IdentifiersResult("uuid1235", IdentifierStatus.OK, null);
    private static final IdentifiersResult TEST_DEVICE_ID_HASH = new IdentifiersResult("deviceid12345hash", IdentifierStatus.OK, null);

    private PreferencesClientDbStorage mPreferences;
    private List<String> mAllIdentifiers = java.util.Arrays.asList(
            Constants.StartupParamsCallbackKeys.UUID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
            Constants.StartupParamsCallbackKeys.GET_AD_URL,
            Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
            Constants.StartupParamsCallbackKeys.CLIDS
    );

    @Mock
    private Handler mHandler;
    @Mock
    private ReportsHandler mReportsHandler;
    @Mock
    private StartupParamsCallback mCallback;
    @Mock
    private StartupParams mStartupParams;
    @Mock
    private Bundle mBundle;
    private Context context;
    @Captor
    private ArgumentCaptor<ResultReceiver> mReceiverArgumentCaptor;
    private final Map<String, String> mResponseClids = new HashMap<String, String>();
    private final Map<String, String> mClientClids = new HashMap<String, String>();

    private StartupHelper mStartupHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        mResponseClids.put("clid0", "123");
        mResponseClids.put("clid1", "456");
        mClientClids.put("clid0", "222");
        mClientClids.put("clid1", "333");
        IKeyValueTableDbHelper dbHelper = spy(new MockedKeyValueTableDbHelper(null));
        doNothing().when(dbHelper).commit();
        mPreferences = new PreferencesClientDbStorage(dbHelper);
        mPreferences.putClientClidsChangedAfterLastIdentifiersUpdate(false).commit();

        mStartupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
    }

    @Test
    public void requestAdvIdentifiers() {
        List<String> advIdentifiers = Arrays.asList(
                Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID,
                Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID,
                Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID
        );
        when(mStartupParams.shouldSendStartup(advIdentifiers)).thenReturn(true);
        when(mStartupParams.containsIdentifiers(advIdentifiers)).thenReturn(true);
        ArgumentCaptor<ResultReceiver> receiverCaptor = ArgumentCaptor.forClass(ResultReceiver.class);

        mStartupHelper.requestStartupParams(mCallback, advIdentifiers, mClientClids);
        verify(mReportsHandler).reportStartupEvent(eq(advIdentifiers), receiverCaptor.capture(), eq(mClientClids));
        receiverCaptor.getValue().send(0, mBundle);
        verify(mCallback).onReceive(any(StartupParamsCallback.Result.class));
    }

    @Test
    public void testRequestStartupParamsSetClidsIfDefined() {
        testRequestStartupParamsSetClientClids(mClientClids);
    }

    @Test
    public void testRequestStartupParamsSetClidsIfEmpty() {
        testRequestStartupParamsSetClientClids(new HashMap<String, String>());
    }

    @Test
    public void testRequestStartupParamsSetClidsIfNull() {
        testRequestStartupParamsSetClientClids(null);
    }

    private void testRequestStartupParamsSetClientClids(Map<String, String> clids) {
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, clids);
        verify(mStartupParams).setClientClids(clids);
    }

    @Test
    public void testRequestStartupParamsNotifyReportsHandlerOnStart() {
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        verify(mReportsHandler).onStartupRequestStarted();
    }

    @Test
    public void testRequestStartupParamsProvokeStartup() {
        when(mStartupParams.shouldSendStartup(mAllIdentifiers)).thenReturn(true);
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        verify(mReportsHandler).reportStartupEvent(eq(mAllIdentifiers), any(ResultReceiver.class), eq(mClientClids));
    }

    @Test
    public void testRequestStartupParamsDoesNotProvokeStartup() {
        when(mStartupParams.shouldSendStartup(mAllIdentifiers)).thenReturn(false);
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        verify(mReportsHandler, never())
                .reportStartupEvent(
                        ArgumentMatchers.<String>anyList(),
                        any(ResultReceiver.class),
                        ArgumentMatchers.<String, String>anyMap()
                );
    }

    @Test
    public void testSendStartupIfNeededProvokeStartup() {
        when(mStartupParams.shouldSendStartup()).thenReturn(true);
        mStartupHelper.setClids(mClientClids);
        mStartupHelper.sendStartupIfNeeded();
        verify(mReportsHandler).reportStartupEvent(eq(mAllIdentifiers), any(ResultReceiver.class), eq(mClientClids));
    }

    @Test
    public void testSendStartupIfNeededDoesNotProvokeStartup() {
        when(mStartupParams.shouldSendStartup()).thenReturn(false);
        mStartupHelper.setClids(mClientClids);
        mStartupHelper.sendStartupIfNeeded();
        verify(mReportsHandler, never())
                .reportStartupEvent(
                        ArgumentMatchers.<String>anyList(),
                        any(ResultReceiver.class),
                        ArgumentMatchers.<String, String>anyMap()
                );
    }

    @Test
    public void testRequestStartupParamsProvokeStartupEventIfStartupOutdated() {
        when(mStartupParams.shouldSendStartup(mAllIdentifiers)).thenReturn(true);
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        verify(mReportsHandler).reportStartupEvent(eq(mAllIdentifiers), any(ResultReceiver.class), eq(mClientClids));
    }

    @Test
    public void testRequestStartupParamsOnResult() {
        when(mStartupParams.shouldSendStartup(ArgumentMatchers.<String>anyList())).thenReturn(true);
        when(mStartupParams.containsIdentifiers(mAllIdentifiers)).thenReturn(true);
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        interceptReceiver().send(DataResultReceiver.RESULT_CODE_STARTUP_PARAMS_UPDATED, mBundle);
        verify(mCallback).onReceive(any(StartupParamsCallback.Result.class));
        verify(mReportsHandler).onStartupRequestFinished();
        assertThat(mStartupHelper.getStartupAllParamsCallbacks()).isEmpty();
    }

    @Test
    public void testRequestStartupParamsIfAllAvailable() {
        when(mStartupParams.shouldSendStartup(ArgumentMatchers.<String>anyList())).thenReturn(true);
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        when(mStartupParams.containsIdentifiers(ArgumentMatchers.<String>anyList())).thenReturn(true);
        interceptReceiver().send(DataResultReceiver.RESULT_CODE_STARTUP_PARAMS_UPDATED, mBundle);
        verify(mCallback).onReceive(any(StartupParamsCallback.Result.class));
        verify(mReportsHandler).onStartupRequestFinished();
        assertThat(mStartupHelper.getStartupAllParamsCallbacks()).isEmpty();
    }

    @Test
    public void testUpdateParametersOnReceive() {
        when(mStartupParams.shouldSendStartup(mAllIdentifiers)).thenReturn(true);
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        interceptReceiver().send(DataResultReceiver.RESULT_CODE_STARTUP_PARAMS_UPDATED, mBundle);
        verify(mStartupParams).updateAllParamsByReceiver(mBundle);
    }

    @Test
    public void testRequestStartupParamsForInvalidReceivedParams() {
        when(mStartupParams.shouldSendStartup(mAllIdentifiers)).thenReturn(true);
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        interceptReceiver().send(DataResultReceiver.RESULT_CODE_STARTUP_PARAMS_UPDATED, mBundle);
        verify(mCallback).onRequestError(
                eq(new StartupParamsCallback.Reason("INCONSISTENT_CLIDS")),
                any(StartupParamsCallback.Result.class)
        );
        assertThat(mStartupHelper.getStartupAllParamsCallbacks().containsKey(mCallback)).isFalse();
    }

    @Test
    public void testRequestStartupParamsForInvalidResultReceiverError() {
        when(mStartupParams.shouldSendStartup(mAllIdentifiers)).thenReturn(true);
        when(mStartupParams.containsIdentifiers(mAllIdentifiers)).thenReturn(true);
        mStartupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        interceptReceiver().send(DataResultReceiver.RESULT_CODE_STARTUP_ERROR, mBundle);
        verify(mStartupParams).updateAllParamsByReceiver(mBundle);
        verify(mCallback).onReceive(any(StartupParamsCallback.Result.class));
        verify(mReportsHandler).onStartupRequestFinished();
    }

    private ResultReceiver interceptReceiver() {
        verify(mReportsHandler).reportStartupEvent(
                ArgumentMatchers.<String>anyList(),
                mReceiverArgumentCaptor.capture(),
                ArgumentMatchers.<String, String>anyMap()
        );

        return mReceiverArgumentCaptor.getValue();
    }

    @Test
    public void testStartupRequestedIfNoIdentifiers() {
        new StartupHelper(context, mReportsHandler, mPreferences, mHandler)
                .requestStartupParams(mCallback, mAllIdentifiers, mClientClids);

        verify(mReportsHandler).reportStartupEvent(same(mAllIdentifiers), any(DataResultReceiver.class), same(mClientClids));
    }

    @Test
    public void testStartupRequestedIfRequestedIdentifierIsNotInPreferences() {
        List<String> identifiers = Collections.singletonList(Constants.StartupParamsCallbackKeys.UUID);
        mPreferences.putDeviceIdResult(TEST_DEVICE_ID).putDeviceIdHashResult(TEST_DEVICE_ID_HASH).commit();

        new StartupHelper(context, mReportsHandler, mPreferences, mHandler)
                .requestStartupParams(mock(StartupParamsCallback.class), identifiers, mClientClids);

        verify(mReportsHandler).reportStartupEvent(same(identifiers), any(DataResultReceiver.class), same(mClientClids));
    }

    @Test
    public void testStartupRequestedIfNotAllRequestedIdentifierInPreferences() {
        mPreferences.putDeviceIdResult(TEST_DEVICE_ID).putDeviceIdHashResult(TEST_DEVICE_ID_HASH).commit();

        new StartupHelper(context, mReportsHandler, mPreferences, mHandler)
                .requestStartupParams(mock(StartupParamsCallback.class), mAllIdentifiers, mClientClids);

        verify(mReportsHandler).reportStartupEvent(same(mAllIdentifiers), any(DataResultReceiver.class), same(mClientClids));
    }

    @Test
    public void testCallbacksNotifiedOnlyIfIdentifiersValidSome() {
        List<String> identifiers = Collections.singletonList(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH);

        mPreferences.putNextStartupTime(System.currentTimeMillis() / 1000 + 1000000);
        mPreferences.putClientClidsChangedAfterLastIdentifiersUpdate(false).commit();
        new StartupHelper(context, mReportsHandler, mPreferences, mHandler).requestStartupParams(mCallback, identifiers, mClientClids);
        verify(mCallback, never()).onReceive(any(StartupParamsCallback.Result.class));

        mPreferences.putUuidResult(TEST_UUID).commit();
        mPreferences.putClientClidsChangedAfterLastIdentifiersUpdate(false).commit();
        new StartupHelper(context, mReportsHandler, mPreferences, mHandler).requestStartupParams(mCallback, identifiers, mClientClids);
        verify(mCallback, never()).onReceive(any(StartupParamsCallback.Result.class));

        mPreferences.putDeviceIdHashResult(TEST_DEVICE_ID_HASH).commit();
        mPreferences.putClientClidsChangedAfterLastIdentifiersUpdate(false).commit();
        new StartupHelper(context, mReportsHandler, mPreferences, mHandler).requestStartupParams(mCallback, identifiers, mClientClids);
        verify(mCallback).onReceive(any(StartupParamsCallback.Result.class));
    }

    @Test
    public void testParametersNotClearedSome() {
        List<String> identifiers = Arrays.asList(
                Constants.StartupParamsCallbackKeys.DEVICE_ID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH
        );

        new StartupHelper(context, mReportsHandler, mPreferences, mHandler).requestStartupParams(mCallback, identifiers, mClientClids);

        mPreferences.putNextStartupTime(System.currentTimeMillis() / 1000 + 1000000);
        mPreferences.putDeviceIdResult(TEST_DEVICE_ID).commit();
        mPreferences.putDeviceIdHashResult(TEST_DEVICE_ID_HASH).commit();
        mPreferences.putClientClidsChangedAfterLastIdentifiersUpdate(false).commit();

        new StartupHelper(context, mReportsHandler, mPreferences, mHandler).requestStartupParams(mCallback, identifiers, mClientClids);
        ArgumentCaptor<StartupParamsCallback.Result> parameters =
                ArgumentCaptor.forClass(StartupParamsCallback.Result.class);
        verify(mCallback).onReceive(parameters.capture());
        assertThat(parameters.getValue().parameters).containsOnly(
                new AbstractMap.SimpleEntry(
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    new StartupParamsItem(TEST_DEVICE_ID.id, StartupParamsItemStatus.OK, null
                    )),
                new AbstractMap.SimpleEntry(
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    new StartupParamsItem(TEST_DEVICE_ID_HASH.id, StartupParamsItemStatus.OK, null)
                    ),
                new AbstractMap.SimpleEntry(
                        Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS,
                        new StartupParamsItem("{}", StartupParamsItemStatus.UNKNOWN_ERROR, "no identifier in preferences")
                )
        );
    }

    @Test
    public void testCallbacksOnErrorCalledNetwork() {
        verifyCallbackCalledOnError(StartupError.NETWORK, StartupParamsCallback.Reason.NETWORK);
    }

    @Test
    public void testCallbacksOnErrorCalledParse() {
        verifyCallbackCalledOnError(StartupError.PARSE, StartupParamsCallback.Reason.INVALID_RESPONSE);
    }

    @Test
    public void testCallbacksOnErrorCalledUnknown() {
        verifyCallbackCalledOnError(StartupError.UNKNOWN, StartupParamsCallback.Reason.UNKNOWN);
    }

    @Test
    public void testCallbacksOnErrorCalledNull() {
        verifyCallbackCalledOnError(null, new StartupParamsCallback.Reason("INCONSISTENT_CLIDS"));
    }

    private void verifyCallbackCalledOnError(final StartupError error,
                                             final StartupParamsCallback.Reason reason) {
        StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        when(mStartupParams.shouldSendStartup(ArgumentMatchers.<String>anyList())).thenReturn(true);

        verify(mCallback, never()).onReceive(any(StartupParamsCallback.Result.class));

        Bundle bundle = new Bundle();
        if (error != null) {
            error.toBundle(bundle);
        }
        startupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        interceptReceiver().send(0, bundle);

        verify(mCallback, times(1)).onRequestError(
                eq(reason),
                any(StartupParamsCallback.Result.class)
        );
    }

    @Test
    public void testStartupErrorSerialization() {
        Bundle bundle = new Bundle();

        for (StartupError error : StartupError.values()) {
            error.toBundle(bundle);
            assertThat(StartupError.fromBundle(bundle).equals(error)).isTrue();
        }
    }

    @Test
    public void testSetInstallReferrerSource() {
        String source = "api";
        mStartupHelper.setInstallReferrerSource(source);
        verify(mReportsHandler).setInstallReferrerSource(source);
    }

    @Test
    public void testHelperReturnPassedDistributionInfoIfNoStartupData() {
        StartupHelper startupHelper = new StartupHelper(context, mReportsHandler, mPreferences, mHandler);

        startupHelper.setClids(TestData.TEST_CLIDS);

        assertThat(startupHelper.getClids()).isEqualTo(TestData.TEST_CLIDS);
    }

    @Test
    public void testHelperReturnEmptyDistributionInfoIfNoData() {
        StartupHelper startupHelper = new StartupHelper(context, mReportsHandler, mPreferences, mHandler);

        assertThat(startupHelper.getClids()).isNull();
    }

    @Test
    public void testRequestStartupAllParamsStarted() {
        StartupHelper startupHelper = new StartupHelper(context, mReportsHandler, mPreferences, mHandler);
        startupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        verify(mReportsHandler).onStartupRequestStarted();
    }

    @Test
    public void testCallbackNotRemovedIfNotNotifiedIdentifiers() {
        StartupParams startupParams = mock(StartupParams.class);
        StartupHelper startupHelper = new StartupHelper(mReportsHandler, startupParams, mHandler);
        StartupError startupError = StartupError.NETWORK;
        when(startupParams.shouldSendStartup(ArgumentMatchers.<String>anyList())).thenReturn(true);
        startupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        verify(mCallback, never()).onReceive(any(StartupParamsCallback.Result.class));
        Bundle bundle = new Bundle();
        startupError.toBundle(bundle);
        interceptReceiver().send(0, bundle);
        verify(mCallback).onRequestError(
                eq(StartupParamsCallback.Reason.NETWORK),
                any(StartupParamsCallback.Result.class)
        );
    }

    @Test
    public void testOnlyRequestedIdentifiersReturned() {
        List<String> identifiers = Arrays.asList(
                Constants.StartupParamsCallbackKeys.UUID,
                Constants.StartupParamsCallbackKeys.DEVICE_ID
        );
        StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        when(mStartupParams.shouldSendStartup(identifiers)).thenReturn(true);
        startupHelper.requestStartupParams(mCallback, identifiers, mClientClids);
        when(mStartupParams.containsIdentifiers(identifiers)).thenReturn(true);

        startupHelper.processResultFromResultReceiver(mBundle);
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mStartupParams).putToMap(eq(identifiers), mapCaptor.capture());
        ArgumentCaptor<StartupParamsCallback.Result> resultCaptor =
                ArgumentCaptor.forClass(StartupParamsCallback.Result.class);
        verify(mCallback).onReceive(resultCaptor.capture());
        assertThat(resultCaptor.getValue().parameters)
                .containsExactlyInAnyOrderEntriesOf(mapCaptor.getValue());
    }

    @Test
    public void testReceiverOnError() {
        StartupParams startupParams = mock(StartupParams.class);
        when(startupParams.containsIdentifiers(mAllIdentifiers)).thenReturn(false);
        StartupHelper startupHelper = new StartupHelper(context, mReportsHandler, mPreferences, mHandler);
        startupHelper.requestStartupParams(mCallback, mAllIdentifiers, mClientClids);
        ArgumentCaptor<DataResultReceiver> captor = ArgumentCaptor.forClass(DataResultReceiver.class);
        verify(mReportsHandler).reportStartupEvent(same(mAllIdentifiers), captor.capture(), same(mClientClids));
        DataResultReceiver receiver = captor.getValue();
        DataResultReceiver.notifyOnStartupError(
                receiver,
                StartupError.NETWORK,
                mock(ClientIdentifiersHolder.class)
        );
        verify(mCallback).onRequestError(
                eq(StartupParamsCallback.Reason.NETWORK),
                any(StartupParamsCallback.Result.class)
        );
    }

    @Test
    public void testReceiverSuccess() {
        StartupParamsCallback callback1 = mock(StartupParamsCallback.class);
        StartupParamsCallback callback2 = mock(StartupParamsCallback.class);
        StartupParams startupParams = mock(StartupParams.class);
        when(startupParams.shouldSendStartup(mAllIdentifiers)).thenReturn(true);
        StartupHelper startupHelper = new StartupHelper(mReportsHandler, startupParams, mHandler);
        startupHelper.requestStartupParams(callback1, mAllIdentifiers, mClientClids);
        ArgumentCaptor<DataResultReceiver> captor = ArgumentCaptor.forClass(DataResultReceiver.class);
        verify(mReportsHandler).reportStartupEvent(same(mAllIdentifiers), captor.capture(), same(mClientClids));
        DataResultReceiver receiver = captor.getValue();
        startupHelper.requestStartupParams(callback2, mAllIdentifiers, mClientClids);
        when(startupParams.containsIdentifiers(any(List.class))).thenReturn(true);
        DataResultReceiver.notifyOnStartupUpdated(receiver, mock(ClientIdentifiersHolder.class));
        verify(callback1).onReceive(any(StartupParamsCallback.Result.class));
        verify(callback2).onReceive(any(StartupParamsCallback.Result.class));
    }

    @Test
    public void testSendStartupWithStubReceiver() {
        final StartupHelper startupHelper = new StartupHelper(context, mReportsHandler, mPreferences, mHandler);
        startupHelper.setClids(mClientClids);
        startupHelper.sendStartupIfNeeded();
        verify(mReportsHandler).reportStartupEvent(eq(mAllIdentifiers),
                argThat(new ArgumentMatcher<ResultReceiver>() {
                    @Override
                    public boolean matches(ResultReceiver argument) {
                        return ((DataResultReceiver) argument).getReceiver() == startupHelper.getStubReceiver();
                    }
                }),
                eq(mClientClids)
        );
    }

    @Test
    public void testNotifyCallbackOnError() {
        final String uuid = UUID.randomUUID().toString();
        final String deviceId = "12345678";
        final String deviceIdHash = "qwertyytrewq";
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid0", "123");
        clids.put("clid1", "456");

        StartupParamsCallback callback = mock(StartupParamsCallback.class);
        final StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        when(mStartupParams.shouldSendStartup(ArgumentMatchers.<String>anyList())).thenReturn(true);
        startupHelper.requestStartupParams(callback, Arrays.asList(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH), mClientClids);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, StartupParamsItem> targetMap = invocation.getArgument(1);
                targetMap.put(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH, new StartupParamsItem(deviceIdHash, StartupParamsItemStatus.OK, null));
                return targetMap;
            }
        }).when(mStartupParams).putToMap(ArgumentMatchers.<String>anyList(), ArgumentMatchers.<String, StartupParamsItem>anyMap());
        when(mBundle.containsKey("startup_error_key_code")).thenReturn(true);
        when(mBundle.getInt("startup_error_key_code")).thenReturn(1); // stub network error code
        when(mStartupParams.areResponseClidsConsistent()).thenReturn(true);
        when(mStartupParams.getUuid()).thenReturn(uuid);
        when(mStartupParams.getDeviceId()).thenReturn(deviceId);
        when(mStartupParams.getDeviceIDHash()).thenReturn(deviceIdHash);

        interceptReceiver().send(0, mBundle);

        ArgumentCaptor<StartupParamsCallback.Result> captor = ArgumentCaptor.forClass(StartupParamsCallback.Result.class);
        verify(callback).onRequestError(eq(StartupParamsCallback.Reason.NETWORK), captor.capture());
        StartupParamsCallback.Result result = captor.getValue();

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result.deviceId).isNull();
        assertions.assertThat(result.uuid).isNull();
        assertions.assertThat(result.deviceIdHash).isEqualTo(deviceIdHash);

        assertions.assertAll();
    }

    @Test
    public void testNotifyCallbackOnErrorInconsistentClids() {
        StartupParamsCallback callback = mock(StartupParamsCallback.class);
        PublicLogger publicLogger = mock(PublicLogger.class);
        final StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        startupHelper.setPublicLogger(publicLogger);
        when(mStartupParams.shouldSendStartup(ArgumentMatchers.<String>anyList())).thenReturn(true);
        startupHelper.requestStartupParams(callback, Arrays.asList(Constants.StartupParamsCallbackKeys.CLIDS), mClientClids);
        interceptReceiver().send(0, mBundle);

        verify(callback).onRequestError(
                eq(new StartupParamsCallback.Reason("INCONSISTENT_CLIDS")),
                any(StartupParamsCallback.Result.class));
        verify(publicLogger).warning(anyString(), any());
    }

    @Test
    public void testNotifyCallbackOnErrorConsistentClids() {
        StartupParamsCallback callback = mock(StartupParamsCallback.class);
        PublicLogger publicLogger = mock(PublicLogger.class);
        final StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        startupHelper.setPublicLogger(publicLogger);
        when(mStartupParams.shouldSendStartup(ArgumentMatchers.<String>anyList())).thenReturn(true);
        when(mStartupParams.areResponseClidsConsistent()).thenReturn(true);
        startupHelper.requestStartupParams(callback, Arrays.asList(Constants.StartupParamsCallbackKeys.CLIDS), StartupParamsTestUtils.CLIDS_MAP_1);
        interceptReceiver().send(0, mBundle);

        verify(callback).onRequestError(
                eq(StartupParamsCallback.Reason.UNKNOWN),
                any(StartupParamsCallback.Result.class)
        );
        verifyNoMoreInteractions(publicLogger);
    }

    @Test
    public void testNotifyCallbackWithStartupParamsCallbackOnError() {
        final String uuid = UUID.randomUUID().toString();
        final String deviceId = "12345678";
        final String deviceIdHash = "qwertyytrewq";
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid0", "123");
        clids.put("clid1", "456");

        StartupParamsCallback callback = mock(StartupParamsCallback.class);
        final StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        when(mStartupParams.shouldSendStartup(ArgumentMatchers.<String>anyList())).thenReturn(true);
        startupHelper.requestStartupParams(callback, Arrays.asList(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH), mClientClids);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, StartupParamsItem> targetMap = invocation.getArgument(1);
                targetMap.put(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH, new StartupParamsItem(deviceIdHash, StartupParamsItemStatus.OK, null));
                return targetMap;
            }
        }).when(mStartupParams).putToMap(ArgumentMatchers.<String>anyList(), ArgumentMatchers.<String, StartupParamsItem>anyMap());
        when(mBundle.containsKey("startup_error_key_code")).thenReturn(true);
        when(mBundle.getInt("startup_error_key_code")).thenReturn(1); // stub network error code
        when(mStartupParams.areResponseClidsConsistent()).thenReturn(true);
        when(mStartupParams.getUuid()).thenReturn(uuid);
        when(mStartupParams.getDeviceId()).thenReturn(deviceId);
        when(mStartupParams.getDeviceIDHash()).thenReturn(deviceIdHash);

        interceptReceiver().send(0, mBundle);

        ArgumentCaptor<StartupParamsCallback.Result> captor = ArgumentCaptor.forClass(StartupParamsCallback.Result.class);
        verify(callback).onRequestError(eq(StartupParamsCallback.Reason.NETWORK), captor.capture());
        StartupParamsCallback.Result result = captor.getValue();

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result.deviceId).isNull();
        assertions.assertThat(result.uuid).isNull();
        assertions.assertThat(result.deviceIdHash).isEqualTo(deviceIdHash);

        assertions.assertAll();
    }

    @Test
    public void testNotifyCallbackWithStartupParamsCallbackOnErrorConsistentClids() {
        StartupParamsCallback callback = mock(StartupParamsCallback.class);
        PublicLogger publicLogger = mock(PublicLogger.class);
        final StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        startupHelper.setPublicLogger(publicLogger);
        when(mStartupParams.shouldSendStartup(ArgumentMatchers.<String>anyList())).thenReturn(true);
        when(mStartupParams.areResponseClidsConsistent()).thenReturn(true);
        startupHelper.requestStartupParams(callback, Arrays.asList(Constants.StartupParamsCallbackKeys.CLIDS), StartupParamsTestUtils.CLIDS_MAP_1);
        interceptReceiver().send(0, mBundle);

        verify(callback).onRequestError(eq(StartupParamsCallback.Reason.UNKNOWN), any(StartupParamsCallback.Result.class));
        verifyNoMoreInteractions(publicLogger);
    }

    @Test
    public void testSetClids() {
        StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        startupHelper.setClids(StartupParamsTestUtils.CLIDS_MAP_3);
        verify(mStartupParams).setClientClids(StartupParamsTestUtils.CLIDS_MAP_3);
        verify(mReportsHandler).setClids(StartupParamsTestUtils.CLIDS_MAP_3);
    }

    @Test
    public void testSetClidsForEmpty() {
        StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        startupHelper.setClids(new HashMap<String, String>());
        verifyNoMoreInteractions(mStartupParams, mReportsHandler);
    }

    @Test
    public void testSetClidsForNull() {
        StartupHelper startupHelper = new StartupHelper(mReportsHandler, mStartupParams, mHandler);
        startupHelper.setClids(null);
        verifyNoMoreInteractions(mStartupParams, mReportsHandler);
    }

    @Test
    public void testSendStartupIfNeededShouldNot() {
        when(mStartupParams.shouldSendStartup()).thenReturn(false);
        mStartupHelper.sendStartupIfNeeded();
        verifyReportingStartupEvent(0);
    }

    @Test
    public void testSendStartupIfNeededShould() {
        when(mStartupParams.shouldSendStartup()).thenReturn(true);
        mStartupHelper.sendStartupIfNeeded();
        verifyReportingStartupEvent(1);
    }

    private void verifyReportingStartupEvent(int numOfTimes) {
        verify(mReportsHandler, times(numOfTimes))
                .reportStartupEvent(
                        ArgumentMatchers.nullable(List.class),
                        nullable(ResultReceiver.class),
                        nullable(Map.class)
                );
    }

    @Test
    public void testGetUuid() {
        String uuid = "test uuid";
        when(mStartupParams.getUuid()).thenReturn(uuid);
        assertThat(mStartupHelper.getUuid()).isEqualTo(uuid);
    }

    @Test
    public void testGetDeviceId() {
        String deviceId = "test device id";
        when(mStartupParams.getDeviceId()).thenReturn(deviceId);
        assertThat(mStartupHelper.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    public void testGetClids() {
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid0", "0");
        clids.put("clid1", "1");
        when(mStartupParams.getClids()).thenReturn(JsonHelper.clidsToString(clids));
        assertThat(mStartupHelper.getClids()).isEqualTo(clids);
    }

    @Test
    public void getCachedAdvIdentifiers() {
        AdvIdentifiersResult result = mock(AdvIdentifiersResult.class);
        when(mStartupParams.getCachedAdvIdentifiers()).thenReturn(result);
        assertThat(mStartupHelper.getCachedAdvIdentifiers()).isEqualTo(result);
    }

    @Test
    public void getServerTimeOffsetSeconds() {
        long offset = 8899;
        when(mStartupParams.getServerTimeOffsetSeconds()).thenReturn(offset);
        assertThat(mStartupHelper.getServerTimeOffsetSeconds()).isEqualTo(offset);
    }

    @Test
    public void getFeatures() {
        FeaturesResult featuresResult = mock(FeaturesResult.class);
        when(mStartupParams.getFeatures()).thenReturn(featuresResult);
        assertThat(mStartupHelper.getFeatures()).isSameAs(featuresResult);
    }
}
