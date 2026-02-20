package io.appmetrica.analytics.impl.crash.jvm.client;

import io.appmetrica.analytics.ICrashTransformer;
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.UnhandledSituationReporterProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReporterBasedCrashProcessorTest extends CommonTest {

    @Mock
    private IReporterExtended reporter;
    @Mock
    private Throwable originalException;
    @Mock
    private ThrowableModel throwable;
    @Mock
    private CrashProcessor.Rule mRule;
    @Mock
    private UnhandledSituationReporterProvider mReporterProvider;
    @Mock
    private ExtraMetaInfoRetriever extraMetaInfoRetriever;
    @Mock
    private ICrashTransformer mCustomCrashTransformer;
    @Rule
    public final MockedStaticRule<UnhandledExceptionFactory> sUnhandledExceptionFactory =
        new MockedStaticRule<>(UnhandledExceptionFactory.class);
    @Mock
    private UnhandledException mUnhandledException;
    @Mock
    private AllThreads allThreads;
    private final String buildId = "333-444";
    private final boolean isOffline = true;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mUnhandledException = new UnhandledException(
            throwable,
            mock(AllThreads.class),
            Collections.singletonList(mock(StackTraceItemInternal.class)),
            "unity",
            "1.2.3",
            new HashMap<String, String>(),
            "12345",
            false
        );
        when(mReporterProvider.getReporter()).thenReturn(reporter);
        when(extraMetaInfoRetriever.getBuildId()).thenReturn(buildId);
        when(extraMetaInfoRetriever.isOffline()).thenReturn(isOffline);
        when(UnhandledExceptionFactory.getUnhandledExceptionFromJava(
            nullable(Throwable.class),
            any(AllThreads.class),
            nullable(List.class),
            nullable(String.class),
            nullable(Boolean.class)
        )).thenReturn(mUnhandledException);
    }

    @Test
    public void testReporterCalled() {
        when(mRule.shouldReportCrash(any(Throwable.class))).thenReturn(true);

        new ReporterBasedCrashProcessor(mReporterProvider, mRule, null, extraMetaInfoRetriever)
            .processCrash(originalException, allThreads);

        sUnhandledExceptionFactory.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                    originalException,
                    allThreads,
                    null,
                    buildId,
                    isOffline
                );
            }
        });
        verify(reporter, times(1)).reportUnhandledException(mUnhandledException);
    }

    @Test
    public void testReporterShouldNotCall() {
        when(mRule.shouldReportCrash(any(Throwable.class))).thenReturn(false);

        new ReporterBasedCrashProcessor(mReporterProvider, mRule, null, extraMetaInfoRetriever)
            .processCrash(originalException, allThreads);

        verify(reporter, never()).reportUnhandledException(any(UnhandledException.class));
    }

    @Test
    public void testNullThrowable() {
        when(mRule.shouldReportCrash(any())).thenReturn(true);

        new ReporterBasedCrashProcessor(mReporterProvider, mRule, mCustomCrashTransformer, extraMetaInfoRetriever)
            .processCrash(null, allThreads);
        verify(mCustomCrashTransformer, never()).process(any());
        sUnhandledExceptionFactory.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                    null,
                    allThreads,
                    null,
                    buildId,
                    isOffline
                );
            }
        });
        verify(reporter).reportUnhandledException(mUnhandledException);
    }

    @Test
    public void testCustomCrashProcessor() {
        when(mRule.shouldReportCrash(any(Throwable.class))).thenReturn(true);
        final Throwable customThrowable = mock(Throwable.class);
        when(mCustomCrashTransformer.process(originalException)).thenReturn(customThrowable);

        new ReporterBasedCrashProcessor(mReporterProvider, mRule, mCustomCrashTransformer, extraMetaInfoRetriever)
            .processCrash(originalException, allThreads);
        verify(mCustomCrashTransformer).process(originalException);
        verify(reporter).reportUnhandledException(mUnhandledException);
        sUnhandledExceptionFactory.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                    customThrowable,
                    allThreads,
                    null,
                    buildId,
                    isOffline
                );
            }
        });
    }

    @Test
    public void testCustomProcessorReturnedNull() {
        when(mRule.shouldReportCrash(any(Throwable.class))).thenReturn(true);
        when(mCustomCrashTransformer.process(originalException)).thenReturn(null);

        new ReporterBasedCrashProcessor(mReporterProvider, mRule, mCustomCrashTransformer, extraMetaInfoRetriever)
            .processCrash(originalException, allThreads);
        verify(mCustomCrashTransformer).process(originalException);
        verify(reporter, never()).reportUnhandledException(any(UnhandledException.class));
    }
}
