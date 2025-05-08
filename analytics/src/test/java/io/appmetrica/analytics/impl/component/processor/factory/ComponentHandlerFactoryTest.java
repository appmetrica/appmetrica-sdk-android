package io.appmetrica.analytics.impl.component.processor.factory;

import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.processor.event.ApplySettingsFromActivationConfigHandler;
import io.appmetrica.analytics.impl.component.processor.event.ExternalAttributionHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppOpenHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportCrashMetaInformation;
import io.appmetrica.analytics.impl.component.processor.event.ReportFeaturesHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportFirstHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportFirstOccurrenceStatusHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPermissionHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPrevSessionEventHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPurgeBufferHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportSaveToDatabaseHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportSessionHandler;
import io.appmetrica.analytics.impl.component.processor.event.SaveInitialUserProfileIDHandler;
import io.appmetrica.analytics.impl.component.processor.event.SavePreloadInfoHandler;
import io.appmetrica.analytics.impl.component.processor.event.SubscribeForReferrerHandler;
import io.appmetrica.analytics.impl.component.processor.event.UpdateUserProfileIDHandler;
import io.appmetrica.analytics.impl.component.processor.event.modules.ModulesEventHandler;
import io.appmetrica.analytics.impl.component.processor.session.ReportSessionStopHandler;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ComponentHandlerFactoryTest extends CommonTest {

    @Mock
    private ReportingHandlerProvider mProvider;
    @Mock
    private ReportPurgeBufferHandler mReportPurgeBufferHandler;
    @Mock
    private ReportSaveToDatabaseHandler mReportSaveToDatabaseHandler;
    @Mock
    private ReportSessionHandler mReportSessionHandler;
    @Mock
    private ReportSessionStopHandler mReportSessionStopHandler;
    @Mock
    private ReportCrashMetaInformation reportCrashMetaInformation;
    @Mock
    private ReportFirstHandler mReportFirstHandler;
    @Mock
    private ReportPrevSessionEventHandler mReportPrevSessionEventHandler;
    @Mock
    private ReportPermissionHandler mReportPermissionsHandler;
    @Mock
    private ReportFeaturesHandler mReportFeaturesHandler;
    @Mock
    private ReportAppOpenHandler mReportAppOpenHandler;
    @Mock
    private ReportFirstOccurrenceStatusHandler mReportFirstOccurrenceStatusHandler;
    @Mock
    private SavePreloadInfoHandler mSavePreloadInfoHandler;
    @Mock
    private ApplySettingsFromActivationConfigHandler mApplySettingsFromActivationConfigHandler;
    @Mock
    private SubscribeForReferrerHandler mSubscribeForReferrerHandler;
    @Mock
    private UpdateUserProfileIDHandler updateUserProfileIDHandler;
    @Mock
    private SaveInitialUserProfileIDHandler saveInitialUserProfileIDHandler;
    @Mock
    private ModulesEventHandler moduleEventHandler;
    @Mock
    private ExternalAttributionHandler externalAttributionHandler;

    List<ReportComponentHandler> mHandlersList = new ArrayList<ReportComponentHandler>();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mProvider.getReportPurgeBufferHandler()).thenReturn(mReportPurgeBufferHandler);
        when(mProvider.getReportSaveToDatabaseHandler()).thenReturn(mReportSaveToDatabaseHandler);
        when(mProvider.getReportSessionHandler()).thenReturn(mReportSessionHandler);
        when(mProvider.getReportSessionStopHandler()).thenReturn(mReportSessionStopHandler);
        when(mProvider.getReportFirstHandler()).thenReturn(mReportFirstHandler);
        when(mProvider.getReportPrevSessionEventHandler()).thenReturn(mReportPrevSessionEventHandler);
        when(mProvider.getReportPermissionsHandler()).thenReturn(mReportPermissionsHandler);
        when(mProvider.getReportFeaturesHandler()).thenReturn(mReportFeaturesHandler);
        when(mProvider.getReportAppOpenHandler()).thenReturn(mReportAppOpenHandler);
        when(mProvider.getReportFirstOccurrenceStatusHandler()).thenReturn(mReportFirstOccurrenceStatusHandler);
        when(mProvider.getSavePreloadInfoHandler()).thenReturn(mSavePreloadInfoHandler);
        when(mProvider.getApplySettingsFromActivationConfigHandler()).thenReturn(mApplySettingsFromActivationConfigHandler);
        when(mProvider.getSubscribeForReferrerHandler()).thenReturn(mSubscribeForReferrerHandler);
        when(mProvider.getUpdateUserProfileIDHandler()).thenReturn(updateUserProfileIDHandler);
        when(mProvider.getSaveInitialUserProfileIDHandler()).thenReturn(saveInitialUserProfileIDHandler);
        when(mProvider.getModulesEventHandler()).thenReturn(moduleEventHandler);
        when(mProvider.getExternalAttributionHandler()).thenReturn(externalAttributionHandler);
        doReturn(reportCrashMetaInformation).when(mProvider).getReportCrashMetaInformation();
    }

    @Test
    public void testActivationFactory() {
        ActivationFactory factory = new ActivationFactory(mProvider);
        factory.addHandlers(mHandlersList);

        assertThat(mHandlersList).containsExactly(
            mApplySettingsFromActivationConfigHandler,
            mSavePreloadInfoHandler,
            saveInitialUserProfileIDHandler,
            mReportFirstHandler,
            mSubscribeForReferrerHandler
        );
    }

    @Test
    public void testCommonConditionalFactoryAll() {
        InternalEvents event = InternalEvents.EVENT_TYPE_CUSTOM_EVENT;
        CommonConditionalFactory factory = new CommonConditionalFactory(mProvider);

        factory.addHandlers(event, mHandlersList);

        assertThat(EventsManager.affectSessionState(event)).isTrue();

        assertThat(mHandlersList)
            .containsExactly(moduleEventHandler, mReportSessionHandler);
    }

    @Test
    public void testCommonConditionalFactoryWithoutSessionState() {
        InternalEvents event = InternalEvents.EVENT_TYPE_PURGE_BUFFER;
        CommonConditionalFactory factory = new CommonConditionalFactory(mProvider);

        factory.addHandlers(event, mHandlersList);

        assertThat(EventsManager.affectSessionState(event)).isFalse();

        assertThat(mHandlersList).containsExactly();
    }

    @Test
    public void testJustSaveToDatabaseFactory() {
        JustSaveToDataBaseFactory factory = new JustSaveToDataBaseFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(mReportSaveToDatabaseHandler);
    }

    @Test
    public void testPurgeBUfferFactory() {
        PurgeBufferFactory factory = new PurgeBufferFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(mReportPurgeBufferHandler);
    }

    @Test
    public void testRegularFactory() {
        RegularFactory factory = new RegularFactory(mProvider);

        factory.addHandlers(mHandlersList);

        assertThat(mHandlersList).containsExactly(
            mReportFirstOccurrenceStatusHandler,
            mReportSaveToDatabaseHandler
        );
    }

    @Test
    public void testRegularMainReporterFactory() {
        RegularMainReporterFactory factory = new RegularMainReporterFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(
            mReportPermissionsHandler,
            mReportFeaturesHandler,
            mReportFirstOccurrenceStatusHandler,
            mReportSaveToDatabaseHandler
        );
    }

    @Test
    public void testSingleHandlerFactory() {
        ReportComponentHandler handler = mock(ReportComponentHandler.class);
        SingleHandlerFactory factory = new SingleHandlerFactory(mProvider, handler);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(handler);
    }

    @Test
    public void testStartFactory() {
        StartFactory factory = new StartFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly();
    }

    @Test
    public void testUnhandledExceptionFactory() {
        UnhandledExceptionFactory factory = new UnhandledExceptionFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(
            mReportPurgeBufferHandler,
            mReportSaveToDatabaseHandler,
            mReportSessionStopHandler,
            reportCrashMetaInformation
        );
    }

    @Test
    public void testReportAppOpenFactory() {
        ReportAppOpenFactory factory = new ReportAppOpenFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(mReportAppOpenHandler, mReportSaveToDatabaseHandler);
    }

    @Test
    public void testCurrentSessionNativeCrashHandlerFactory() {
        CurrentSessionNativeCrashHandlerFactory factory = new CurrentSessionNativeCrashHandlerFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(
            mReportSaveToDatabaseHandler,
            mReportPurgeBufferHandler,
            mReportSessionStopHandler
        );
    }

    @Test
    public void testPrevSessionNativeCrashHandlerFactory() {
        PrevSessionNativeCrashHandlerFactory factory = new PrevSessionNativeCrashHandlerFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(
            mReportPrevSessionEventHandler,
            mReportPurgeBufferHandler
        );
    }

    @Test
    public void testUnhandledExceptionFromFileFactory() {
        UnhandledExceptionFromFileFactory factory = new UnhandledExceptionFromFileFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(
            mReportPrevSessionEventHandler,
            reportCrashMetaInformation
        );
    }

    @Test
    public void externalAttributionFactory() {
        ExternalAttributionFactory factory = new ExternalAttributionFactory(mProvider);

        factory.addHandlers(mHandlersList);
        assertThat(mHandlersList).containsExactly(
            externalAttributionHandler,
            mReportSaveToDatabaseHandler
        );
    }
}
