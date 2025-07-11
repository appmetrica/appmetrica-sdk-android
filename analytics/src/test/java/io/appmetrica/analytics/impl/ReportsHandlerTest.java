package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Base64;
import android.util.Pair;
import io.appmetrica.analytics.TestData;
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.UnhandledExceptionEventFormer;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.ThreadStateTest;
import io.appmetrica.analytics.impl.crash.jvm.client.ThrowableModel;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.db.DatabaseStorageFactoryTestUtils;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProtoSerializable;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.referrer.common.ReferrerResultReceiver;
import io.appmetrica.analytics.impl.revenue.ad.AdRevenueWrapper;
import io.appmetrica.analytics.impl.service.commands.ServiceCallableFactory;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.internal.IAppMetricaService;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ACTIVATION;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_REQUEST_REFERRER;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_STARTUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportsHandlerTest extends CommonTest {

    @Mock
    private Context mContext;
    @Mock
    private AppMetricaConnector mConnector;
    @Mock
    private MainReporter mMainReporter;
    @Mock
    private IAppMetricaService mMetricaService;
    @Mock
    private DataResultReceiver mDataResultReceiver;
    @Mock
    private ProcessConfiguration mProcessConfiguration;
    @Mock
    private UnhandledExceptionEventFormer mEventFormer;
    @Mock
    private ServiceCallableFactory serviceCallableFactory;
    @Mock
    private CommutationReporterEnvironment commutationReporterEnvironment;
    @Mock
    private ReportsSender mReportsSender;
    @Mock
    private ErrorEnvironment mainErrorEnvironment;
    @Mock
    private ErrorEnvironment reporterErrorEnvironment;

    private ReporterEnvironment mMainReporterEnvironment;
    private ReporterEnvironment mArgReporterEnvironment;

    private ReportsHandler mReportsHandler;
    private ReportsHandler mReportsHandlerSpy;
    private ProcessConfiguration processConfiguration;
    private CounterConfiguration counterConfiguration;

    @Rule
    public MockedConstructionRule<ReportsSender> reportsSenderMockedConstructionRule = new MockedConstructionRule<>(ReportsSender.class);

    private final String userProfileID = "user_profile_id";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mConnector.getService()).thenReturn(mMetricaService);
        when(mConnector.isConnected()).thenReturn(true);
        StartupHelper startupParamsProvider = mock(StartupHelper.class);
        mMainReporterEnvironment = spy(new ReporterEnvironment(
            new ProcessConfiguration(RuntimeEnvironment.getApplication(), mDataResultReceiver),
            new CounterConfiguration(),
            mainErrorEnvironment,
            userProfileID
        ));
        mArgReporterEnvironment = spy(new ReporterEnvironment(
            new ProcessConfiguration(RuntimeEnvironment.getApplication(), mDataResultReceiver),
            new CounterConfiguration(),
            reporterErrorEnvironment,
            userProfileID
        ));
        when(mMainReporter.getEnvironment()).thenReturn(mMainReporterEnvironment);

        DatabaseStorageFactoryTestUtils.mockNonComponentDatabases(mContext);
        processConfiguration = new ProcessConfiguration(RuntimeEnvironment.getApplication(), mDataResultReceiver);
        counterConfiguration = new CounterConfiguration();
        when(commutationReporterEnvironment.getProcessConfiguration()).thenReturn(processConfiguration);
        when(commutationReporterEnvironment.getReporterConfiguration()).thenReturn(counterConfiguration);

        // Create real reports handler based on mocks/spies
        mReportsHandler = new ReportsHandler(
            mConnector,
            mEventFormer,
            commutationReporterEnvironment,
            serviceCallableFactory,
            mReportsSender
        );
        mReportsHandler.setStartupParamsProvider(startupParamsProvider);
        mReportsHandlerSpy = spy(mReportsHandler);
    }

    @Test
    public void testReportStartupExecutorCalls() {
        // Call method
        mReportsHandlerSpy.reportStartupEvent(Collections.EMPTY_LIST, mDataResultReceiver, null, true);

        // Verify calls for executor
        ArgumentCaptor<ReportToSend> captor = ArgumentCaptor.forClass(ReportToSend.class);
        verify(mReportsSender, times(1)).queueReport(captor.capture());
        assertThat(captor.getValue().isCrashReport()).isFalse();

        // Verify calls for connector
        verify(mConnector, times(1)).removeScheduleDisconnect();
        verify(mConnector, never()).scheduleDisconnect();
    }

    @Test
    public void testReportStartupHasProcessReceiver() {
        final List<String> identifiers = Arrays.asList("uuid", "deviceid", "deviceid hash");

        mReportsHandlerSpy.reportStartupEvent(identifiers, mock(DataResultReceiver.class), null, true);
        verify(mReportsHandlerSpy).reportEvent(any(CounterReport.class), any(ReporterEnvironment.class));
    }

    @Test
    public void testReportStartupWithReceiver() {
        final List<String> identifiers = Arrays.asList("uuid", "deviceid", "deviceid hash");
        final ResultReceiver resultReceiver = mock(ResultReceiver.class);
        final Map<String, String> clientClids = new HashMap<String, String>();
        clientClids.put("clid0", "0");
        clientClids.put("clid1", "1");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                CounterReport counterReport = (CounterReport) invocationOnMock.getArguments()[0];
                checkReportData(counterReport, EVENT_TYPE_STARTUP);
                IdentifiersData identifiersData = counterReport.getPayload().getParcelable(IdentifiersData.BUNDLE_KEY);
                assertThat(identifiersData.getIdentifiersList()).containsExactlyInAnyOrderElementsOf(identifiers);
                assertThat(identifiersData.getResultReceiver()).isEqualTo(resultReceiver);
                assertThat(identifiersData.getClidsFromClientForVerification()).isEqualTo(clientClids);
                return null;
            }
        }).when(mReportsHandlerSpy).reportEvent(any(CounterReport.class), any(ReporterEnvironment.class));

        mReportsHandlerSpy.reportStartupEvent(identifiers, resultReceiver, clientClids, true);
        verify(mReportsHandlerSpy).reportEvent(any(CounterReport.class), any(ReporterEnvironment.class));
    }

    @Test
    public void testReportCrash() {
        UnhandledException unhandledException = new UnhandledException(
            mock(ThrowableModel.class),
            new AllThreads(ThreadStateTest.createEmpty(), Collections.emptyList(), "process"),
            null,
            null,
            null,
            null,
            null,
            null
        );
        ReportToSend reportToSend = mock(ReportToSend.class);
        when(reportToSend.getEnvironment()).thenReturn(mock(ReporterEnvironment.class));
        when(mEventFormer.formEvent(unhandledException, mArgReporterEnvironment)).thenReturn(reportToSend);
        mReportsHandlerSpy.reportCrash(unhandledException, mArgReporterEnvironment);

        verify(mReportsSender, times(1)).sendCrash(reportToSend);

        verify(mConnector, times(1)).removeScheduleDisconnect();
        verify(mConnector, never()).scheduleDisconnect();
    }

    @Test
    public void testReportUnhandledException() {
        UnhandledException unhandledException = new UnhandledException(
            mock(ThrowableModel.class),
            new AllThreads(ThreadStateTest.createEmpty(), Collections.emptyList(), "process"),
            null,
            null,
            null,
            null,
            null,
            null
        );
        ReportToSend reportToSend = mock(ReportToSend.class);
        when(reportToSend.getEnvironment()).thenReturn(mock(ReporterEnvironment.class));
        when(mEventFormer.formEvent(unhandledException, mArgReporterEnvironment)).thenReturn(reportToSend);
        mReportsHandlerSpy.reportUnhandledException(unhandledException, mArgReporterEnvironment);

        verify(mReportsSender, times(1)).queueReport(reportToSend);

        verify(mConnector, times(1)).removeScheduleDisconnect();
        verify(mConnector, never()).scheduleDisconnect();
    }

    @Test
    public void testReportErrorProtobufWithEnvironment() {
        final CounterReport reportData = createReportDataWithTypeMock(EVENT_TYPE_EXCEPTION_USER_PROTOBUF);

        // Call method
        mReportsHandlerSpy.reportEvent(reportData, mArgReporterEnvironment);

        // Verify calls for executor
        verify(mReportsSender, times(1)).queueReport(any(ReportToSend.class));

        // Verify calls for environment
        verify(mArgReporterEnvironment, times(1)).getErrorEnvironment();

        // Verify calls for connector
        verify(mConnector, times(1)).removeScheduleDisconnect();
        verify(mConnector, never()).scheduleDisconnect();
    }

    @Test
    public void testReportCustomErrorProtobufWithEnvironment() {
        final CounterReport reportData = createReportDataWithTypeMock(EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF);

        // Call method
        mReportsHandlerSpy.reportEvent(reportData, mArgReporterEnvironment);

        // Verify calls for executor
        verify(mReportsSender, times(1)).queueReport(any(ReportToSend.class));

        // Verify calls for environment
        verify(mArgReporterEnvironment, times(1)).getErrorEnvironment();

        // Verify calls for connector
        verify(mConnector, times(1)).removeScheduleDisconnect();
        verify(mConnector, never()).scheduleDisconnect();
    }

    @Test
    public void testSendUserProfile() throws InvalidProtocolBufferNanoException {
        final Userprofile.Profile profile = new Userprofile.Profile();
        mReportsHandlerSpy.sendUserProfile(profile, mMainReporterEnvironment);
        ArgumentCaptor<ReportToSend> reportToSend = ArgumentCaptor.forClass(ReportToSend.class);
        verify(mReportsSender, times(1)).queueReport(reportToSend.capture());
        CounterReport report = reportToSend.getValue().getReport();
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_SEND_USER_PROFILE.getTypeId());
    }

    @Test
    public void testSetUserProfileID() {
        String profileID = "profileid";
        mReportsHandlerSpy.setUserProfileID(profileID, mMainReporterEnvironment);
        ArgumentCaptor<ReportToSend> reportToSend = ArgumentCaptor.forClass(ReportToSend.class);
        verify(mReportsSender, times(1)).queueReport(reportToSend.capture());
        CounterReport report = reportToSend.getValue().getReport();
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.getTypeId());
        assertThat(report.getValue()).isEqualTo(profileID);
    }

    @Test
    public void testSendRevenue() {
        RevenueWrapper wrapper = mock(RevenueWrapper.class);
        Pair<byte[], Integer> result = new Pair<byte[], Integer>(
            "teststring".getBytes(),
            300
        );
        doReturn(result).when(wrapper).getDataToSend();
        mReportsHandlerSpy.sendRevenue(wrapper, mMainReporterEnvironment);
        ArgumentCaptor<ReportToSend> reportToSend = ArgumentCaptor.forClass(ReportToSend.class);
        verify(mReportsSender, times(1)).queueReport(reportToSend.capture());
        CounterReport report = reportToSend.getValue().getReport();
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.getTypeId());
        assertThat(report.getValue()).isEqualTo(new String(Base64.encode(result.first, 0)));
        assertThat(report.getBytesTruncated()).isEqualTo(300);
    }

    @Test
    public void testSendAdRevenue() {
        AdRevenueWrapper wrapper = mock(AdRevenueWrapper.class);
        kotlin.Pair<byte[], Integer> result = new kotlin.Pair<>(
            "teststring".getBytes(),
            300
        );
        doReturn(result).when(wrapper).getDataToSend();
        mReportsHandlerSpy.sendAdRevenue(wrapper, mMainReporterEnvironment);
        ArgumentCaptor<ReportToSend> reportToSend = ArgumentCaptor.forClass(ReportToSend.class);
        verify(mReportsSender, times(1)).queueReport(reportToSend.capture());
        CounterReport report = reportToSend.getValue().getReport();
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT.getTypeId());
        assertThat(report.getValue()).isEqualTo(new String(Base64.encode(result.getFirst(), 0)));
        assertThat(report.getBytesTruncated()).isEqualTo(300);
    }

    @Test
    public void sendECommerceForSinglePartEvent() throws Exception {
        try (MockedStatic<MessageNano> sMessageNano = Mockito.mockStatic(MessageNano.class)) {
            byte[] value = randomBytes(1024);
            Ecommerce.ECommerceEvent singleEvent = prepareMockedECommerceProto(value);
            int bytesTruncated = 140;

            ProtoSerializable event = mock(ProtoSerializable.class);
            when(event.toProto())
                .thenReturn(Collections.singletonList(new Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>(
                    singleEvent,
                    new BytesTruncatedInfo(bytesTruncated)
                )));
            mReportsHandlerSpy.sendECommerce(event, mMainReporterEnvironment);
            ArgumentCaptor<ReportToSend> reportToSend = ArgumentCaptor.forClass(ReportToSend.class);
            verify(mReportsSender).queueReport(reportToSend.capture());
            assertECommerceCounterReport(reportToSend.getValue(), value, bytesTruncated);
        }
    }

    @Test
    public void sendECommerceForMultipartEvent() throws Exception {
        try (MockedStatic<MessageNano> sMessageNano = Mockito.mockStatic(MessageNano.class)) {
            byte[] firstValue = randomBytes(150 * 1024);
            byte[] secondValue = randomBytes(100 * 1024);
            byte[] thirdValue = randomBytes(50 * 1024);
            Ecommerce.ECommerceEvent firstProto = prepareMockedECommerceProto(firstValue);
            Ecommerce.ECommerceEvent secondProto = prepareMockedECommerceProto(secondValue);
            Ecommerce.ECommerceEvent thirdProto = prepareMockedECommerceProto(thirdValue);

            int firstBytesTruncated = 18;
            int secondBytesTruncated = 100;
            int thirdBytesTruncated = 140;

            ProtoSerializable event = mock(ProtoSerializable.class);
            when(event.toProto())
                .thenReturn(Arrays.asList(
                    new Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>(
                        firstProto,
                        new BytesTruncatedInfo(firstBytesTruncated)
                    ),
                    new Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>(
                        secondProto,
                        new BytesTruncatedInfo(secondBytesTruncated)
                    ),
                    new Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>(
                        thirdProto,
                        new BytesTruncatedInfo(thirdBytesTruncated)
                    )
                ));
            mReportsHandlerSpy.sendECommerce(event, mMainReporterEnvironment);
            ArgumentCaptor<ReportToSend> reportToSend =
                ArgumentCaptor.forClass(ReportToSend.class);
            verify(mReportsSender, times(3)).queueReport(reportToSend.capture());
            List<ReportToSend> capturedValues = reportToSend.getAllValues();
            assertThat(capturedValues).hasSize(3);
            assertECommerceCounterReport(capturedValues.get(0), firstValue, firstBytesTruncated);
            assertECommerceCounterReport(capturedValues.get(1), secondValue, secondBytesTruncated);
            assertECommerceCounterReport(capturedValues.get(2), thirdValue, thirdBytesTruncated);
        }
    }

    private Ecommerce.ECommerceEvent prepareMockedECommerceProto(byte[] value) throws Exception {
        Ecommerce.ECommerceEvent mock = mock(Ecommerce.ECommerceEvent.class);
        when(MessageNano.toByteArray(mock)).thenReturn(value);
        return mock;
    }

    private byte[] randomBytes(int length) {
        byte[] byteValue = new byte[length];
        new Random().nextBytes(byteValue);
        return byteValue;
    }

    private void assertECommerceCounterReport(ReportToSend reportToSend,
                                              byte[] expectedBytes,
                                              int bytesTruncated) {
        CounterReport report = reportToSend.getReport();
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.getTypeId());
        assertThat(Base64Utils.decompressBase64GzipAsBytes(report.getValue())).isEqualTo(expectedBytes);
        assertThat(report.getBytesTruncated()).isEqualTo(bytesTruncated);
    }

    @Test
    public void testRemoveScheduleDisconnectOnResume() {
        // Call method
        mReportsHandlerSpy.onResumeForegroundSession();

        // Verify calls for connector
        verify(mConnector, times(1)).removeScheduleDisconnect();
        verify(mConnector, never()).scheduleDisconnect();
    }

    @Test
    public void testScheduleDisconnectOnPause() {
        // Call method
        mReportsHandlerSpy.onPauseForegroundSession();

        // Verify calls for connector
        verify(mConnector, times(1)).scheduleDisconnect();
        verify(mConnector, never()).removeScheduleDisconnect();
    }

    @Test
    public void testReportFirstEventSendsExpectedEventType() throws RemoteException {
        mReportsHandlerSpy.reportActivationEvent(mMainReporterEnvironment);

        ArgumentCaptor<CounterReport> arg1 = ArgumentCaptor.forClass(CounterReport.class);
        ArgumentCaptor<ReporterEnvironment> arg2 = ArgumentCaptor.forClass(ReporterEnvironment.class);

        verify(mReportsHandlerSpy, times(1)).reportEvent(arg1.capture(), arg2.capture());
        assertThat(arg1.getValue().getType()).isEqualTo(EVENT_TYPE_ACTIVATION.getTypeId());
        assertThat(arg1.getValue().getProfileID()).isEqualTo(userProfileID);

        doAnswer(new Answer() {
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                ReportToSend report = (ReportToSend) invocation.getArguments()[0];
                assertThat(report.getReport().getType()).isEqualTo(EVENT_TYPE_ACTIVATION.getTypeId());
                assertThat(report.getEnvironment().getProcessConfiguration().getClientClids()).isEqualTo(TestData.TEST_CLIDS);
                return null;
            }
        }).when(mReportsSender).queueReport(any(ReportToSend.class));
    }

    @Test
    public void testStartupConfigurationHasDistributionInfo() {
        mReportsHandlerSpy.setClids(TestData.TEST_CLIDS);
        mReportsHandlerSpy.reportStartupEvent(Collections.EMPTY_LIST, mDataResultReceiver, null, true);

        doAnswer(new Answer() {
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                ReportToSend report = (ReportToSend) invocation.getArguments()[0];
                assertThat(report.getEnvironment().getProcessConfiguration().getClientClids()).isEqualTo(TestData.TEST_CLIDS);
                return null;
            }
        }).when(mReportsSender).queueReport(any(ReportToSend.class));
    }

    @Test
    public void testStartupConfigurationHasnotDistributionInfo() {
        mReportsHandlerSpy.reportStartupEvent(Collections.EMPTY_LIST, mDataResultReceiver, null, true);

        doAnswer(new Answer() {
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                ReportToSend report = (ReportToSend) invocation.getArguments()[0];
                assertThat(report.getEnvironment().getProcessConfiguration().getClientClids()).isNull();
                return null;
            }
        }).when(mReportsSender).queueReport(any(ReportToSend.class));
    }

    private CounterReport createReportDataWithTypeMock(final InternalEvents eventType) {
        final CounterReport reportData = mock(CounterReport.class);
        when(reportData.getType()).thenReturn(eventType.getTypeId());
        return reportData;
    }

    private void checkReportData(final Object reportData, final InternalEvents event) {
        assertThat(reportData).isInstanceOf(CounterReport.class);
        assertThat(((CounterReport) reportData).getType()).isEqualTo(event.getTypeId());
    }

    @Test
    public void testUpdatePreActivationWithoutValues() {
        mReportsHandler.updatePreActivationConfig(null, null, null);
        verify(mReportsSender).queueReport(argThat(new ArgumentMatcher<ReportToSend>() {
            @Override
            public boolean matches(ReportToSend argument) {
                CounterConfiguration configuration = argument.getEnvironment().getReporterConfiguration();

                return configuration.getDataSendingEnabled() == null
                    && configuration.isLocationTrackingEnabled() == null
                    && configuration.getManualLocation() == null
                    && configuration.isAdvIdentifiersTrackingEnabled() == null;
            }
        }));
    }

    @Test
    public void testUpdatePreActivationWithLocation() {
        final boolean locationTracking = false;
        mReportsHandler.updatePreActivationConfig(locationTracking, null, null);
        verify(mReportsSender).queueReport(argThat(new ArgumentMatcher<ReportToSend>() {
            @Override
            public boolean matches(ReportToSend argument) {
                CounterConfiguration configuration = argument.getEnvironment().getReporterConfiguration();

                return configuration.isLocationTrackingEnabled() == locationTracking;
            }
        }));
    }

    @Test
    public void testUpdatePreActivationWithDataSendingEnabled() {
        final boolean dataSendingEnabled = true;
        mReportsHandler.updatePreActivationConfig(null, dataSendingEnabled, null);

        verify(mReportsSender).queueReport(argThat(new ArgumentMatcher<ReportToSend>() {
            @Override
            public boolean matches(ReportToSend argument) {
                CounterConfiguration configuration = argument.getEnvironment().getReporterConfiguration();

                return configuration.getDataSendingEnabled() == dataSendingEnabled;
            }
        }));
    }

    @Test
    public void updatePreActivationWithAdvIdentifiersTrackingEnabled() {
        boolean advIdentifiersTrackingEnabled = true;
        mReportsHandler.updatePreActivationConfig(null, null, advIdentifiersTrackingEnabled);

        verify(mReportsSender).queueReport(argThat(new ArgumentMatcher<ReportToSend>() {
            @Override
            public boolean matches(ReportToSend argument) {
                CounterConfiguration configuration = argument.getEnvironment().getReporterConfiguration();

                return configuration.isAdvIdentifiersTrackingEnabled() == advIdentifiersTrackingEnabled;
            }
        }));
    }

    @Test
    public void testOnStartupRequestStarted() {
        mReportsHandler.onStartupRequestStarted();
        verify(mConnector).forbidDisconnect();
    }

    @Test
    public void testOnStartupRequestFinished() {
        mReportsHandler.onStartupRequestFinished();
        verify(mConnector).allowDisconnect();
    }

    @Test
    public void testReportResumeUserSession() {
        mReportsHandler.reportResumeUserSession(mProcessConfiguration);
        verify(mReportsSender).queueResumeUserSession(mProcessConfiguration);
    }

    @Test
    public void testReportPauseUserSession() {
        mReportsHandler.reportPauseUserSession(mProcessConfiguration);
        verify(mReportsSender).queuePauseUserSession(mProcessConfiguration);
    }

    @Test
    public void testSetInstallReferrerSource() {
        String source = "api";
        mReportsHandler.setInstallReferrerSource(source);
        assertThat(mReportsHandler.getCommutationReporterEnvironment().getProcessConfiguration().getInstallReferrerSource()).isEqualTo(source);
    }

    @Test
    public void reportRequestReferrerEvent() {
        ReferrerResultReceiver receiver = mock(ReferrerResultReceiver.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                CounterReport counterReport = (CounterReport) invocationOnMock.getArguments()[0];
                checkReportData(counterReport, EVENT_TYPE_REQUEST_REFERRER);
                ReferrerResultReceiver receiver = counterReport.getPayload().getParcelable(ReferrerResultReceiver.BUNDLE_KEY);
                assertThat(receiver).isNotNull();
                return null;
            }
        }).when(mReportsHandlerSpy).reportEvent(any(CounterReport.class), any(ReporterEnvironment.class));

        mReportsHandlerSpy.reportRequestReferrerEvent(receiver);
        verify(mReportsHandlerSpy).reportEvent(any(CounterReport.class), any(ReporterEnvironment.class));
    }
}
