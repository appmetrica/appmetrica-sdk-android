package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.crash.AppMetricaThrowable;
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.Anr;
import io.appmetrica.analytics.impl.crash.client.CustomError;
import io.appmetrica.analytics.impl.crash.client.RegularError;
import io.appmetrica.analytics.impl.crash.client.ThreadState;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.client.UnhandledExceptionFactory;
import io.appmetrica.analytics.impl.crash.client.converter.AnrConverter;
import io.appmetrica.analytics.impl.crash.client.converter.CustomErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.RegularErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.UnhandledExceptionConverter;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.revenue.ad.AdRevenueWrapper;
import io.appmetrica.analytics.impl.service.AppMetricaServiceDataReporter;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.impl.utils.validation.revenue.RevenueValidator;
import io.appmetrica.analytics.plugins.PluginErrorDetails;
import io.appmetrica.analytics.profile.Attribute;
import io.appmetrica.analytics.profile.UserProfile;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static io.appmetrica.analytics.impl.TestsData.TEST_ENVIRONMENT_KEY;
import static io.appmetrica.analytics.impl.TestsData.TEST_ENVIRONMENT_VALUE;
import static io.appmetrica.analytics.impl.TestsData.TEST_ERROR_ENVIRONMENT_KEY;
import static io.appmetrica.analytics.impl.TestsData.TEST_ERROR_ENVIRONMENT_VALUE;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class BaseReporterTest extends BaseReporterData {

    @Rule
    public final ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    protected BaseReporter mReporter;

    private final String processName = "processName";

    @Mock
    protected ExtraMetaInfoRetriever mExtraMetaInfoRetriever;
    @Mock
    private CrashAndroid.Error mockedProtoError;

    @Rule
    public final MockedStaticRule<LoggerStorage> sLoggerStorage = new MockedStaticRule<>(LoggerStorage.class);
    @Rule
    public final MockedStaticRule<EventsManager> sEventsManager = new MockedStaticRule<>(EventsManager.class);
    @Rule
    public final MockedStaticRule<CounterReport> sCounterReport = new MockedStaticRule<>(CounterReport.class);
    @Rule
    public final MockedStaticRule<MessageNano> sMessageNano = new MockedStaticRule<>(MessageNano.class);
    @Rule
    public final MockedStaticRule<UnhandledExceptionFactory> sUnhandledExceptionFactory = new MockedStaticRule<>(UnhandledExceptionFactory.class);
    @Rule
    public final MockedStaticRule<UnhandledException> sUnhandledException = new MockedStaticRule<>(UnhandledException.class);
    @Rule
    public final MockedConstructionRule<AdRevenueWrapper> adRevenueWrapperConstructor =
            new MockedConstructionRule<>(AdRevenueWrapper.class);

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    protected AnrConverter anrConverter = mock(AnrConverter.class);
    protected UnhandledExceptionConverter unhandledExceptionConverter = mock(UnhandledExceptionConverter.class);
    protected RegularErrorConverter regularErrorConverter = mock(RegularErrorConverter.class);
    protected CustomErrorConverter customErrorConverter = mock(CustomErrorConverter.class);
    protected PluginErrorDetailsConverter pluginErrorDetailsConverter = mock(PluginErrorDetailsConverter.class);
    protected byte[] serializedBytes = new byte[] { 1, 3, 5, 7 };
    protected CounterReport mockedEvent = mock(CounterReport.class);

    @Before
    @Override
    public void setUp() {
        super.setUp();
        when(LoggerStorage.getOrCreatePublicLogger(apiKey)).thenReturn(mPublicLogger);
        when(MessageNano.toByteArray(any(MessageNano.class))).thenReturn(serializedBytes);
        mReporter = getReporter();
        when(mReporterEnvironment.getReporterConfiguration()).thenReturn(mCounterConfiguration);
        mReporter.setKeepAliveHandler(mKeepAliveHandler);
        doReturn(processName).when(processDetector).getProcessName();
    }

    @Test
    public void testReportAnrNoResources() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final CrashAndroid.Anr anr = mock(CrashAndroid.Anr.class);
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(null);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(null);
        when(anrConverter.fromModel(any(Anr.class))).thenReturn(anr);
        when(EventsManager.anrEntry(eq(serializedBytes), any(PublicLogger.class))).thenReturn(mockedEvent);
        final AllThreads allThreads = new AllThreads(new ThreadState(
                "name", 1, 1, "group", 1, new ArrayList<StackTraceElement>()
        ), new ArrayList<ThreadState>(), "process");

        getReporter().reportAnr(allThreads);

        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(anr);
            }
        });
        ArgumentCaptor<Anr> anrCaptor = ArgumentCaptor.forClass(Anr.class);
        verify(anrConverter).fromModel(anrCaptor.capture());
        ObjectPropertyAssertions(anrCaptor.getValue())
                .checkField("mAllThreads", allThreads)
                .checkFieldsAreNull("mBuildId", "mIsOffline")
                .checkAll();
    }

    @Test
    public void testReportAnr() throws IllegalAccessException {
        final CrashAndroid.Anr anr = mock(CrashAndroid.Anr.class);
        final String buildId = "buildId";
        final Boolean isOffline = false;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);
        when(anrConverter.fromModel(any(Anr.class))).thenReturn(anr);
        when(EventsManager.anrEntry(eq(serializedBytes), any(PublicLogger.class))).thenReturn(mockedEvent);
        final AllThreads allThreads = new AllThreads(new ThreadState(
                "name", 1, 1, "group", 1, new ArrayList<StackTraceElement>()
        ), new ArrayList<ThreadState>(), "process");
        getReporter().reportAnr(allThreads);

        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(anr);
            }
        });
        ArgumentCaptor<Anr> anrCaptor = ArgumentCaptor.forClass(Anr.class);
        verify(anrConverter).fromModel(anrCaptor.capture());
        ObjectPropertyAssertions(anrCaptor.getValue())
                .checkField("mAllThreads", allThreads)
                .checkField("mBuildId", buildId)
                .checkField("mIsOffline", isOffline)
                .checkAll();
    }

    @Test
    public void testSendEventBufferShouldDispatchToReportsHandler() {
        when(EventsManager.reportEntry(eq(InternalEvents.EVENT_TYPE_PURGE_BUFFER), any(PublicLogger.class))).thenReturn(mockedEvent);
        mReporter.sendEventsBuffer();
        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
    }

    @Test
    public void testReportErrorNullException() throws IllegalAccessException {
        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        final String message = "message";
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);
        when(regularErrorConverter.fromModel(any(RegularError.class))).thenReturn(mockedProtoError);
        UnhandledException unhandledException = mock(UnhandledException.class);
        when(UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                same((Throwable) null),
                argThat(new ArgumentMatcher<AllThreads>() {
                    @Override
                    public boolean matches(AllThreads argument) {
                        return Objects.equals(argument.processName, processName);
                    }
                }),
                same((List) null),
                eq(buildId),
                eq(isOffline)
        )).thenReturn(unhandledException);
        when(EventsManager.regularErrorReportEntry(eq(message), eq(serializedBytes), any(PublicLogger.class))).thenReturn(mockedEvent);

        mReporter.reportError(message, (Throwable) null);

        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(mockedProtoError);
            }
        });
        ArgumentCaptor<RegularError> captor = ArgumentCaptor.forClass(RegularError.class);
        verify(regularErrorConverter).fromModel(captor.capture());
        ObjectPropertyAssertions(captor.getValue())
                .checkField("message", message)
                .checkField("exception", unhandledException)
                .checkAll();
    }

    @Test
    public void testReportErrorExceptionFormedByAppMetrica() throws IllegalAccessException {
        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);
        final String message = "message";
        Throwable throwable = mock(AppMetricaThrowable.class);
        StackTraceElement[] stacktrace = new StackTraceElement[] { mock(StackTraceElement.class) };
        when(throwable.getStackTrace()).thenReturn(stacktrace);
        when(regularErrorConverter.fromModel(any(RegularError.class))).thenReturn(mockedProtoError);
        when(EventsManager.regularErrorReportEntry(eq(message), eq(serializedBytes), any(PublicLogger.class))).thenReturn(mockedEvent);
        UnhandledException unhandledException = mock(UnhandledException.class);
        when(UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                same((Throwable) null),
                argThat(new ArgumentMatcher<AllThreads>() {
                    @Override
                    public boolean matches(AllThreads argument) {
                        return Objects.equals(argument.processName, processName);
                    }
                }),
                eq(Arrays.asList(stacktrace)),
                eq(buildId),
                eq(isOffline)
        )).thenReturn(unhandledException);

        mReporter.reportError(message, throwable);

        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(mockedProtoError);
            }
        });
        ArgumentCaptor<RegularError> captor = ArgumentCaptor.forClass(RegularError.class);
        verify(regularErrorConverter).fromModel(captor.capture());
        ObjectPropertyAssertions(captor.getValue())
                .checkField("message", message)
                .checkField("exception", unhandledException)
                .checkAll();
    }

    @Test
    public void testReportErrorWithNonNullException() throws IllegalAccessException {
        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);

        final String message = "message";
        final IllegalStateException exception = new IllegalStateException();
        when(regularErrorConverter.fromModel(any(RegularError.class))).thenReturn(mockedProtoError);
        when(EventsManager.regularErrorReportEntry(eq(message), eq(serializedBytes), any(PublicLogger.class))).thenReturn(mockedEvent);
        UnhandledException unhandledException = mock(UnhandledException.class);
        when(UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                same(exception),
                argThat(new ArgumentMatcher<AllThreads>() {
                    @Override
                    public boolean matches(AllThreads argument) {
                        return Objects.equals(argument.processName, processName);
                    }
                }),
                same((List) null),
                eq(buildId),
                eq(isOffline)
        )).thenReturn(unhandledException);

        mReporter.reportError(message, exception);

        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(mockedProtoError);
            }
        });
        ArgumentCaptor<RegularError> captor = ArgumentCaptor.forClass(RegularError.class);
        verify(regularErrorConverter).fromModel(captor.capture());
        ObjectPropertyAssertions(captor.getValue())
                .checkField("message", message)
                .checkField("exception", unhandledException)
                .checkAll();
    }

    @Test
    public void testReportCustomErrorWithNonNullException() throws Exception {
        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);

        final String id = "identifier";
        final String message = "message";
        final IllegalStateException exception = new IllegalStateException();
        when(customErrorConverter.fromModel(any(CustomError.class))).thenReturn(mockedProtoError);
        when(EventsManager.customErrorReportEntry(eq(message), eq(serializedBytes), any(PublicLogger.class))).thenReturn(mockedEvent);
        final UnhandledException unhandledException = mock(UnhandledException.class);
        when(UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                same(exception),
                argThat(new ArgumentMatcher<AllThreads>() {
                    @Override
                    public boolean matches(AllThreads argument) {
                        return Objects.equals(argument.processName, processName);
                    }
                }),
                same((List) null),
                eq(buildId),
                eq(isOffline)
        )).thenReturn(unhandledException);

        mReporter.reportError(id, message, exception);

        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(mockedProtoError);
            }
        });
        ArgumentCaptor<CustomError> captor = ArgumentCaptor.forClass(CustomError.class);
        verify(customErrorConverter).fromModel(captor.capture());
        ObjectPropertyAssertions(captor.getValue())
                .checkField("identifier", id)
                .checkFieldRecursively("regularError", new Consumer<ObjectPropertyAssertions<RegularError>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<RegularError> assertions) {
                        assertions
                                .checkField("message", message)
                                .checkField("exception", unhandledException);
                    }
                })
                .checkAll();
    }

    @Test
    public void testReportCustomErrorWithNullException() throws Exception {
        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);

        final String id = "identifier";
        final String message = "message";
        when(customErrorConverter.fromModel(any(CustomError.class))).thenReturn(mockedProtoError);
        when(EventsManager.customErrorReportEntry(eq(message), eq(serializedBytes), any(PublicLogger.class))).thenReturn(mockedEvent);
        final UnhandledException unhandledException = mock(UnhandledException.class);
        when(UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                same((Throwable) null),
                argThat(new ArgumentMatcher<AllThreads>() {
                    @Override
                    public boolean matches(AllThreads argument) {
                        return Objects.equals(argument.processName, processName);
                    }
                }),
                same((List) null),
                eq(buildId),
                eq(isOffline)
        )).thenReturn(unhandledException);
        mReporter.reportError(id, message, (Throwable) null);

        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(mockedProtoError);
            }
        });
        ArgumentCaptor<CustomError> captor = ArgumentCaptor.forClass(CustomError.class);
        verify(customErrorConverter).fromModel(captor.capture());
        ObjectPropertyAssertions(captor.getValue())
                .checkField("identifier", id)
                .checkFieldRecursively("regularError", new Consumer<ObjectPropertyAssertions<RegularError>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<RegularError> assertions) {
                        assertions
                                .checkField("message", message)
                                .checkField("exception", unhandledException);
                    }
                })
                .checkAll();
    }

    @Test
    public void testReportCustomErrorWithoutException() throws Exception {
        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);

        final String id = "identifier";
        final String message = "message";
        when(customErrorConverter.fromModel(any(CustomError.class))).thenReturn(mockedProtoError);
        when(EventsManager.customErrorReportEntry(eq(message), eq(serializedBytes), any(PublicLogger.class))).thenReturn(mockedEvent);
        final UnhandledException unhandledException = mock(UnhandledException.class);
        when(UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                same((Throwable) null),
                argThat(new ArgumentMatcher<AllThreads>() {
                    @Override
                    public boolean matches(AllThreads argument) {
                        return Objects.equals(argument.processName, processName);
                    }
                }),
                same((List) null),
                eq(buildId),
                eq(isOffline)
        )).thenReturn(unhandledException);
        mReporter.reportError(id, message);

        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(mockedProtoError);
            }
        });
        ArgumentCaptor<CustomError> captor = ArgumentCaptor.forClass(CustomError.class);
        verify(customErrorConverter).fromModel(captor.capture());
        ObjectPropertyAssertions(captor.getValue())
                .checkField("identifier", id)
                .checkFieldRecursively("regularError", new Consumer<ObjectPropertyAssertions<RegularError>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<RegularError> assertions) {
                        assertions
                                .checkField("message", message)
                                .checkField("exception", unhandledException);

                    }
                })
                .checkAll();
    }

    @Test
    public void testReportUnhandledExceptionThrowable() {
        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);

        final IllegalStateException exception = new IllegalStateException();
        UnhandledException unhandledException = mock(UnhandledException.class);
        when(UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                same(exception),
                argThat(new ArgumentMatcher<AllThreads>() {
                    @Override
                    public boolean matches(AllThreads argument) {
                        return Objects.equals(argument.processName, processName);
                    }
                }),
                same((List) null),
                eq(buildId),
                eq(isOffline)
        )).thenReturn(unhandledException);

        mReporter.reportUnhandledException(exception);

        verify(mReportsHandler).reportUnhandledException(unhandledException, mReporterEnvironment);
    }

    @Test
    public void testSetEnvironmentValueShouldForwardAllToReporterEnvironment() {
        mReporter.putErrorEnvironmentValue(TestsData.TEST_ENVIRONMENT_KEY, TestsData.TEST_ENVIRONMENT_VALUE);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

        verify(mReporterEnvironment, times(1)).putErrorEnvironmentValue(arg1.capture(), arg2.capture());
        assertThat(arg1.getValue()).isEqualTo(TestsData.TEST_ENVIRONMENT_KEY);
        assertThat(arg2.getValue()).isEqualTo(TestsData.TEST_ENVIRONMENT_VALUE);
    }

    @Test
    public void testShouldInitializeReportEnvironmentAfterCreation() {
        verify(mReporterEnvironment, times(1)).initialize(any(SimpleMapLimitation.class));
    }

    @Test
    public void testStartShouldReportFirstEvent() {
        mReporter.start();
        verify(mReportsHandler, times(1)).reportActivationEvent(any(ReporterEnvironment.class));
    }

    protected abstract BaseReporter getReporter();

    @Test
    public void testSendAppEnvironmentValueAddedAllValuesFromMap() throws Exception {
        Map<String, String> env = new HashMap<String, String>();
        env.put(TEST_ENVIRONMENT_KEY, TEST_ENVIRONMENT_VALUE);
        env.put(TEST_ERROR_ENVIRONMENT_KEY, TEST_ERROR_ENVIRONMENT_VALUE);

        ArgumentCaptor<String> argKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argValue = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ReporterEnvironment> argReporterEnv = ArgumentCaptor.forClass(ReporterEnvironment.class);

        getReporter().putAllToAppEnvironment(env);

        verify(mReportsHandler, times(2)).sendAppEnvironmentValue(argKey.capture(), argValue.capture(), argReporterEnv.capture());
        assertThat(argReporterEnv.getValue()).isEqualTo(mReporterEnvironment);
        assertTrue(argKey.getAllValues().contains(TEST_ENVIRONMENT_KEY));
        assertTrue(argKey.getAllValues().contains(TEST_ERROR_ENVIRONMENT_KEY));

        assertTrue(argValue.getAllValues().contains(TEST_ENVIRONMENT_VALUE));
        assertTrue(argValue.getAllValues().contains(TEST_ERROR_ENVIRONMENT_VALUE));
    }

    @Test
    public void testPutAllToAppEnvironmentNotAddEmptyAppEnvironment() throws Exception {
        Map<String, String> env = new HashMap<String, String>();
        env.put(null, TEST_ENVIRONMENT_VALUE);
        env.put("", TEST_ERROR_ENVIRONMENT_VALUE);

        ArgumentCaptor<String> argKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argValue = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ReporterEnvironment> argReporterEnv = ArgumentCaptor.forClass(ReporterEnvironment.class);

        getReporter().putAllToAppEnvironment(env);

        verify(mReportsHandler, never()).sendAppEnvironmentValue(argKey.capture(), argValue.capture(), argReporterEnv.capture());
    }

    @Test
    public void testPutAllToAppEnvironmentNotAddEmptyMap() throws Exception {
        Map<String, String> env = new HashMap<String, String>();

        getReporter().putAllToAppEnvironment(env);
        getReporter().putAllToAppEnvironment(null);

        verify(mReportsHandler, never()).sendAppEnvironmentValue(anyString(), anyString(), any(ReporterEnvironment.class));
    }

    @Test
    public void testPutAllToErrorEnvironmentAddAllValues() throws Exception {
        Map<String, String> env = new HashMap<String, String>();
        env.put(TEST_ENVIRONMENT_KEY, TEST_ENVIRONMENT_VALUE);
        env.put(TEST_ERROR_ENVIRONMENT_KEY, TEST_ERROR_ENVIRONMENT_VALUE);

        ArgumentCaptor<String> argKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argValue = ArgumentCaptor.forClass(String.class);

        getReporter().putAllToErrorEnvironment(env);

        verify(mReporterEnvironment, times(2)).putErrorEnvironmentValue(argKey.capture(), argValue.capture());
        assertTrue(argKey.getAllValues().contains(TEST_ENVIRONMENT_KEY));
        assertTrue(argKey.getAllValues().contains(TEST_ERROR_ENVIRONMENT_KEY));

        assertTrue(argValue.getAllValues().contains(TEST_ENVIRONMENT_VALUE));
        assertTrue(argValue.getAllValues().contains(TEST_ERROR_ENVIRONMENT_VALUE));
    }

    @Test
    public void testPutAllToErrorEnvironmentNotAddEmptyValues() throws Exception {
        Map<String, String> env = new HashMap<String, String>();
        env.put("", TEST_ENVIRONMENT_VALUE);
        env.put(null, TEST_ERROR_ENVIRONMENT_VALUE);

        ArgumentCaptor<String> argKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argValue = ArgumentCaptor.forClass(String.class);

        getReporter().putAllToErrorEnvironment(env);

        verify(mReporterEnvironment, never()).putErrorEnvironmentValue(argKey.capture(), argValue.capture());
    }

    @Test
    public void testPutAllToErrorEnvironmentNotAddEmptyMap() throws Exception {
        getReporter().putAllToErrorEnvironment(new HashMap<String, String>());
        getReporter().putAllToErrorEnvironment(null);

        verify(mReporterEnvironment, never()).putErrorEnvironmentValue(anyString(), anyString());
    }

    @Test
    public void testReportCustomEvent() {
        String eventName = randomString();
        String eventValue = randomString();
        int eventType = 14;
        int serviceDataReporterType = AppMetricaServiceDataReporter.TYPE_CORE;
        Map<String, Object> environment = new HashMap<>();
        Map<String, byte[]> extras = Collections.singletonMap("Key", new byte[]{1, 2, 3, 7, 8});
        Map<String, Object> attributes = new HashMap<>();
        String key = randomString();
        String value = randomString();
        environment.put(key, value);
        Map<String, Object> expectedEnvironment = new HashMap<String, Object>();
        expectedEnvironment.put(key, value);

        final ModuleEvent moduleEvent = ModuleEvent.newBuilder(eventType)
            .withName(eventName)
            .withValue(eventValue)
            .withServiceDataReporterType(serviceDataReporterType)
            .withEnvironment(environment)
            .withExtras(extras)
            .withAttributes(attributes)
            .build();

        when(EventsManager.customEventReportEntry(eventType,
                eventName, eventValue, expectedEnvironment, extras, mPublicLogger)).thenReturn(mockedEvent);
        getReporter().reportEvent(moduleEvent);
        verify(mReportsHandler).reportEvent(
            same(mockedEvent),
            any(ReporterEnvironment.class),
            eq(serviceDataReporterType),
            eq(attributes)
        );
    }

    @Test
    public void reportCustomEventNullEnvironmentAndExtras() {
        String eventName = randomString();
        String eventValue = randomString();
        int eventType = 14;
        int serviceDataReporterType = AppMetricaServiceDataReporter.TYPE_CORE;
        Map<String, Object> attributes = new HashMap<>();

        final ModuleEvent moduleEvent = ModuleEvent.newBuilder(eventType)
            .withName(eventName)
            .withValue(eventValue)
            .withServiceDataReporterType(serviceDataReporterType)
            .withAttributes(attributes)
            .build();

        when(EventsManager.customEventReportEntry(eventType,
                eventName, eventValue, null, null, mPublicLogger)).thenReturn(mockedEvent);
        getReporter().reportEvent(moduleEvent);
        verify(mReportsHandler).reportEvent(
            same(mockedEvent),
            any(ReporterEnvironment.class),
            eq(serviceDataReporterType),
            eq(attributes)
        );
    }

    @Test
    public void setSessionExtra() {
        String key = "Key";
        byte[] value = new byte[] {2, 6, 11};
        when(EventsManager.setSessionExtraReportEntry(key, value, mPublicLogger)).thenReturn(mockedEvent);

        getReporter().setSessionExtra(key, value);

        verify(mReportsHandler).reportEvent(
            same(mockedEvent),
            any(ReporterEnvironment.class)
        );
    }

    public static abstract class ReporterReportCustomEventEventTypeTests extends BaseReporterData {

        private static final Collection<Integer> RESERVED_EVENT_TYPE_EXCEPTIONS = Arrays.asList(1, 13);

        private static HashSet<Integer> getReservedEventTypes() {
            HashSet<Integer> types = new HashSet<Integer>();
            for (int i = 1; i < 100; i++) {
                if (RESERVED_EVENT_TYPE_EXCEPTIONS.contains(i)) {
                    types.add(i);
                }
            }

            return types;
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "Report custom event with type={0}")
        public static Collection<Object[]> data() {
            ArrayList<Object[]> data = new ArrayList<Object[]>();
            HashSet<Integer> reserved = getReservedEventTypes();
            for (int i = -1; i < 102; i++) {
                data.add(new Object[]{i, reserved.contains(i) ? 0 : 1});
            }
            return data;
        }

        final int mEventType;
        final int mWantedNumbersOfInvocation;

        @Rule
        public final MockedStaticRule<EventsManager> sEventsManager = new MockedStaticRule<>(EventsManager.class);
        private CounterReport mockedEvent = mock(CounterReport.class);
        protected BaseReporter mReporter;

        @Before
        @Override
        public void setUp() {
            super.setUp();
            mReporter = getReporter();
            when(mReporterEnvironment.getReporterConfiguration()).thenReturn(mCounterConfiguration);
            mReporter.setKeepAliveHandler(mKeepAliveHandler);
        }

        public ReporterReportCustomEventEventTypeTests(int eventType, int wantedNumberOfInvocations) {
            mEventType = eventType;
            mWantedNumbersOfInvocation = wantedNumberOfInvocations;
        }

        @Test
        public void testShouldIgnoringMetricaInternalEventTypes() {
            int serviceDataReporterType = AppMetricaServiceDataReporter.TYPE_CORE;

            final ModuleEvent moduleEvent = ModuleEvent.newBuilder(mEventType)
                .withName(randomString())
                .withValue(randomString())
                .withServiceDataReporterType(serviceDataReporterType)
                .withEnvironment(new HashMap<String, Object>())
                .withExtras(new HashMap<String, byte[]>())
                .withAttributes(new HashMap<String, Object>())
                .build();

            when(EventsManager.customEventReportEntry(anyInt(), anyString(), anyString(), nullable(Map.class),
                nullable(Map.class), any(PublicLogger.class)))
                .thenReturn(mockedEvent);
            mReporter.reportEvent(moduleEvent);
            verify(mReportsHandler, times(mWantedNumbersOfInvocation))
                .reportEvent(
                    same(mockedEvent),
                    any(ReporterEnvironment.class),
                    same(serviceDataReporterType),
                    any(Map.class)
                );
        }

        public abstract BaseReporter getReporter();
    }

    private static String randomString() {
        return new RandomStringGenerator(new Random().nextInt(100) + 1).nextString();
    }

    @Test
    public void testReportEventWithNameShouldBeSentToReportsHandler() {
        String eventName = "EventName";
        when(EventsManager.regularEventReportEntry(eq(eventName), any(PublicLogger.class))).thenReturn(mockedEvent);
        getReporter().reportEvent(eventName);
        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
    }

    @Test
    public void testReportEventWithNullJsonAttributesShouldBeSentToReportsHandler() {
        String eventName = "EventName";
        String value = null;
        when(EventsManager.regularEventReportEntry(eq(eventName), same(value), any(PublicLogger.class))).thenReturn(mockedEvent);
        getReporter().reportEvent(eventName, value);
        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
    }

    @Test
    public void testReportEventWithValidJsonShouldBeSentToReportsHandler() {
        String eventName = "EventName";
        String json = "{\"name\" : \"value\"}";
        when(EventsManager.regularEventReportEntry(eq(eventName), eq(json), any(PublicLogger.class))).thenReturn(mockedEvent);
        getReporter().reportEvent(eventName, json);
        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
    }

    @Test
    public void testReportEventWithMapShouldBeSentToReportsHandler() {
        String eventName = "EventName";
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("Key", "Value");
        when(EventsManager.regularEventReportEntry(eq(eventName), any(PublicLogger.class))).thenReturn(mockedEvent);
        getReporter().reportEvent(eventName, attributes);
        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class), eq(attributes));
    }

    @Test
    public void testReportEventWithMapShouldBeSentToReportsHandlerIfMapIsNull() {
        String eventName = "EventName";
        when(EventsManager.regularEventReportEntry(eq(eventName), any(PublicLogger.class))).thenReturn(mockedEvent);
        getReporter().reportEvent(eventName, (Map) null);
        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class), same((Map) null));
    }

    @Test
    public void testReportEventWithMapShouldReportMapCopyToReportsHandler() {
        String eventName = "EventName";
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("Key", "Value");
        when(EventsManager.regularEventReportEntry(eq(eventName), any(PublicLogger.class))).thenReturn(mockedEvent);
        getReporter().reportEvent(eventName, attributes);
        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class), not(same(attributes)));
    }

    @Test
    public void testReportEventWithMapShouldReportEventWithNullEnvironmentIfEnvironmentIsEmpty() {
        String eventName = "EventName";
        when(EventsManager.regularEventReportEntry(eq(eventName), any(PublicLogger.class))).thenReturn(mockedEvent);
        getReporter().reportEvent(eventName, new HashMap<String, Object>());
        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class), same((Map) null));
    }

    @Test
    public void testUserProfileSent() {
        UserProfile profile = UserProfile.newBuilder().apply(
                Attribute.customString("string").withValue("value")
        ).build();
        getReporter().reportUserProfile(profile);
        verify(mReportsHandler, times(1))
                .sendUserProfile(any(Userprofile.Profile.class), any(ReporterEnvironment.class));
    }

    @Test
    public void testUserProfileNotSentIfListIsEmpty() {
        UserProfile profile = UserProfile.newBuilder().build();
        getReporter().reportUserProfile(profile);
        verify(mReportsHandler, never()).sendUserProfile(any(Userprofile.Profile.class), any(ReporterEnvironment.class));
    }

    @Test
    public void testSetUserProfileID() {
        String profileID = "profileid";
        getReporter().setUserProfileID(profileID);
        verify(mReportsHandler, times(1))
                .setUserProfileID(eq(profileID), any(ReporterEnvironment.class));
    }

    @Test
    public void testAdRevenueSent() {
        final AdRevenue mock = mock(AdRevenue.class);
        getReporter().reportAdRevenue(mock);
        assertThat(adRevenueWrapperConstructor.getArgumentInterceptor().getArguments().get(0).get(0)).isSameAs(mock);
        verify(mReportsHandler).sendAdRevenue(
                same(adRevenueWrapperConstructor.getConstructionMock().constructed().get(0)),
                any(ReporterEnvironment.class)
        );
    }

    @Test
    public void testRevenueSent() {
        getReporter().reportRevenue(mock(Revenue.class));
        verify(mReportsHandler, times(1))
                .sendRevenue(any(RevenueWrapper.class), any(ReporterEnvironment.class));
    }

    @Test
    public void logRevenueWithPriceInMicros() {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        Revenue revenue = Revenue.newBuilderWithMicros(12000000, Currency.getInstance("USD")).build();
        getReporter().reportRevenue(revenue);
        verify(mPublicLogger).i("Revenue received for productID: <null> of quantity: <null> with price (in micros): 12000000 USD");
    }

    @Test
    public void logRevenueWithDeprecatedPrice() {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        Revenue revenue = Revenue.newBuilder(12, Currency.getInstance("USD")).build();
        getReporter().reportRevenue(revenue);
        verify(mPublicLogger).i("Revenue received for productID: <null> of quantity: <null> with price: 12.0 USD");
    }

    @Test
    public void logRevenueFilled() {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        Revenue revenue = Revenue.newBuilderWithMicros(12000000, Currency.getInstance("USD"))
                .withProductID("12abc")
                .withQuantity(3)
                .withPayload("payload")
                .withReceipt(Revenue.Receipt.newBuilder().withData("mydata").withSignature("mysignature").build())
                .build();
        getReporter().reportRevenue(revenue);
        verify(mPublicLogger).i("Revenue received for productID: 12abc of quantity: 3 with price (in micros): 12000000 USD");
    }

    @Test
    public void reportECommerce() {
        ECommerceEvent event = mock(ECommerceEvent.class);
        getReporter().reportECommerce(event);
        verify(mReportsHandler).sendECommerce(eq(event), any(ReporterEnvironment.class));
    }

    @Test
    public void reportECommercePublicLogIfEnabled() {
        reportECommercePublicLog(true);
    }

    @Test
    public void reportECommercePublicLogIfDisabled() {
        reportECommercePublicLog(false);
    }

    private void reportECommercePublicLog(boolean enabled) {
        when(mPublicLogger.isEnabled()).thenReturn(enabled);

        String eventDescription = "event description";
        ECommerceEvent event = mock(ECommerceEvent.class);
        when(event.getPublicDescription()).thenReturn(eventDescription);

        getReporter().reportECommerce(event);
        verify(mPublicLogger, enabled ? times(1) : never()).i("E-commerce event received: " + eventDescription);
    }

    @Test
    public void testName() {
        mReporter.reportEvent("name");
    }

    @Test
    public void testValidRevenue() {
        try (MockedStatic<BaseReporter.ValidatorProvider> ignored = Mockito.mockStatic(BaseReporter.ValidatorProvider.class)) {
            Validator<Revenue> validator = mock(RevenueValidator.class);
            when(BaseReporter.ValidatorProvider.getRevenueValidator()).thenReturn(validator);
            doReturn(ValidationResult.successful(validator)).when(validator).validate(any(Revenue.class));
            mReporter.reportRevenue(mock(Revenue.class));
            verify(mReportsHandler, times(1)).sendRevenue(any(RevenueWrapper.class), any(ReporterEnvironment.class));
        }
    }

    @Test
    public void testInvalidRevenue() {
        try (MockedStatic<BaseReporter.ValidatorProvider> ignored = Mockito.mockStatic(BaseReporter.ValidatorProvider.class)) {
            Validator<Revenue> validator = mock(RevenueValidator.class);
            when(BaseReporter.ValidatorProvider.getRevenueValidator()).thenReturn(validator);
            doReturn(ValidationResult.failed(validator, "error")).when(validator).validate(any(Revenue.class));
            mReporter.reportRevenue(mock(Revenue.class));
            verify(mReportsHandler, never()).sendRevenue(any(RevenueWrapper.class), any(ReporterEnvironment.class));
        }
    }

    @Test
    public void testOnResumeDoesNotSendEventsBuffer() {
        final String activity = "some activity";
        when(EventsManager.reportEntry(eq(InternalEvents.EVENT_TYPE_PURGE_BUFFER), any(PublicLogger.class))).thenReturn(mockedEvent);
        mReporter.onResumeForegroundSession(activity);
        verify(mReportsHandler, never()).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
    }

    @Test
    public void reportJsEvent() {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        ClientCounterReport report = mock(ClientCounterReport.class);
        try (MockedStatic<ClientCounterReport> sClientCounterReport = Mockito.mockStatic(ClientCounterReport.class)) {
            final String eventName = "Event name";
            final String eventValue = "Event value";
            when(ClientCounterReport.formJsEvent(eventName, eventValue, mPublicLogger)).thenReturn(report);
            mReporter.reportJsEvent(eventName, eventValue);
            verify(mPublicLogger).i("Event received: Event name. With value: Event value");
            verify(mReportsHandler).reportEvent(report, mReporterEnvironment);
        }
    }

    @Test
    public void reportJsInitEvent() {
        final String eventValue = "Event value";
        when(CounterReport.formJsInitEvent(eventValue)).thenReturn(mockedEvent);
        mReporter.reportJsInitEvent(eventValue);
        verify(mReportsHandler).reportEvent(mockedEvent, mReporterEnvironment);
        verify(mPublicLogger, never()).i(anyString());
    }

    @Test
    public void reportUnhandledPluginException() {
        final String errorName = "error name";
        ClientCounterReport clientCounterReport = mock(ClientCounterReport.class);
        PluginErrorDetails errorDetails = mock(PluginErrorDetails.class);
        UnhandledException unhandledException = mock(UnhandledException.class);
        final CrashAndroid.Crash crash = mock(CrashAndroid.Crash.class);

        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);

        when(pluginErrorDetailsConverter.toUnhandledException(errorDetails)).thenReturn(unhandledException);
        when(unhandledExceptionConverter.fromModel(unhandledException)).thenReturn(crash);
        when(UnhandledException.getErrorName(unhandledException)).thenReturn(errorName);
        when(EventsManager.unhandledExceptionReportEntry(eq(errorName), eq(serializedBytes), any(PublicLogger.class))).thenReturn(clientCounterReport);

        getReporter().reportUnhandledException(errorDetails);

        verify(mReportsHandler).reportEvent(same(clientCounterReport), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(crash);
            }
        });
    }

    @Test
    public void reportPluginError() {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        final String message = "message";
        ClientCounterReport clientCounterReport = mock(ClientCounterReport.class);
        PluginErrorDetails errorDetails = mock(PluginErrorDetails.class);
        RegularError regularError = new RegularError(message, mock(UnhandledException.class));
        final CrashAndroid.Error error = mock(CrashAndroid.Error.class);

        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);
        when(pluginErrorDetailsConverter.toRegularError(message, errorDetails)).thenReturn(regularError);
        when(regularErrorConverter.fromModel(regularError)).thenReturn(error);
        when(EventsManager.customErrorReportEntry(eq(message), eq(serializedBytes), any(PublicLogger.class))).thenReturn(clientCounterReport);

        getReporter().reportError(errorDetails, message);

        verify(mReportsHandler).reportEvent(same(clientCounterReport), any(ReporterEnvironment.class));
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(error);
            }
        });
        verify(mPublicLogger).fi("Error from plugin received: %s", message);
    }

    @Test
    public void reportPluginErrorWithIdentifier() throws IllegalAccessException {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        String identifier = "id";
        final String message = "message";
        ClientCounterReport clientCounterReport = mock(ClientCounterReport.class);
        PluginErrorDetails errorDetails = mock(PluginErrorDetails.class);
        UnhandledException unhandledException = mock(UnhandledException.class);
        RegularError regularError = new RegularError(message, unhandledException);
        when(UnhandledException.getErrorName(unhandledException)).thenReturn(message);
        final CrashAndroid.Error error = mock(CrashAndroid.Error.class);

        final String buildId = UUID.randomUUID().toString();
        final boolean isOffline = true;
        when(mExtraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(mExtraMetaInfoRetriever.isOffline()).thenReturn(isOffline);

        when(pluginErrorDetailsConverter.toRegularError(message, errorDetails)).thenReturn(regularError);
        when(customErrorConverter.fromModel(any(CustomError.class))).thenReturn(error);
        when(EventsManager.customErrorReportEntry(eq(message), eq(serializedBytes), any(PublicLogger.class))).thenReturn(clientCounterReport);

        getReporter().reportError(identifier, message, errorDetails);

        verify(mReportsHandler).reportEvent(same(clientCounterReport), any(ReporterEnvironment.class));
        ArgumentCaptor<CustomError> customErrorCaptor = ArgumentCaptor.forClass(CustomError.class);
        verify(customErrorConverter).fromModel(customErrorCaptor.capture());
        ObjectPropertyAssertions(customErrorCaptor.getValue())
                .checkField("identifier", identifier)
                .checkField("regularError", regularError)
                .checkAll();
        sMessageNano.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                MessageNano.toByteArray(error);
            }
        });
        verify(mPublicLogger).fi("Error with identifier: %s from plugin received: %s", identifier, message);
    }

    @Test
    public void getPluginExtension() {
        assertThat(mReporter.getPluginExtension()).isSameAs(mReporter);
    }
}
