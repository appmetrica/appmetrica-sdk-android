package io.appmetrica.analytics.impl.crash;

import android.content.Context;
import io.appmetrica.analytics.impl.CoreClientEvent;
import io.appmetrica.analytics.impl.EventTrimPolicy;
import io.appmetrica.analytics.impl.ReportToSend;
import io.appmetrica.analytics.impl.ReporterEnvironment;
import io.appmetrica.analytics.impl.TrimmedCoreClientEvent;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.jvm.client.ThrowableModel;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.jvm.converter.JvmCrashConverter;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.androidtestutils.rules.ContextRule;
import io.appmetrica.gradle.testutils.rules.MockedStaticRule;
import java.util.Random;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnhandledExceptionEventFormerTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();
    private Context context;

    private final String errorName = "some error";
    @Mock
    private JvmCrashConverter mJvmCrashConverter;
    @Mock
    private ReporterEnvironment mReporterEnvironment;
    @Rule
    public final MockedStaticRule<UnhandledException> sUnhandledException = new MockedStaticRule<>(UnhandledException.class);
    private UnhandledExceptionEventFormer mEventFormer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = contextRule.getContext();
        mEventFormer = new UnhandledExceptionEventFormer(mJvmCrashConverter);
    }

    @Test
    public void formEvent() {
        try (MockedStatic<CoreClientEvent> sCoreClientEvent = Mockito.mockStatic(CoreClientEvent.class)) {
            final byte[] eventValueBytes = new byte[1024];
            String environment = "environment";
            new Random().nextBytes(eventValueBytes);
            UnhandledException unhandledException = new UnhandledException(
                mock(ThrowableModel.class),
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );
            when(UnhandledException.getErrorName(unhandledException)).thenReturn(errorName);
            when(mJvmCrashConverter.fromModel(unhandledException)).thenReturn(eventValueBytes);
            when(mReporterEnvironment.getErrorEnvironment()).thenReturn(environment);
            ProcessConfiguration processConfiguration = new ProcessConfiguration(context, null);
            when(mReporterEnvironment.getProcessConfiguration()).thenReturn(processConfiguration);
            when(mReporterEnvironment.getReporterConfiguration()).thenReturn(new CounterConfiguration());

            CoreClientEvent clientCounterReport = mock(CoreClientEvent.class);
            when(clientCounterReport.getTrimPolicy()).thenReturn(EventTrimPolicy.STANDARD);
            when(clientCounterReport.getBytesTruncated()).thenReturn(0);
            when(clientCounterReport.getExtras()).thenReturn(new java.util.HashMap<>());
            when(
                CoreClientEvent.unhandledExceptionReportEntry(
                    eq(errorName),
                    same(eventValueBytes),
                    same(mReporterEnvironment)
                )
            ).thenReturn(clientCounterReport);
            ReportToSend report = mEventFormer.formEvent(unhandledException, mReporterEnvironment);
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(report.getEnvironment().getReporterConfiguration())
                .usingRecursiveComparison().isEqualTo(mReporterEnvironment.getReporterConfiguration());
            softly.assertThat(report.getEnvironment().getProcessConfiguration())
                .usingRecursiveComparison().isEqualTo(mReporterEnvironment.getProcessConfiguration());
            softly.assertThat(report.isCrashReport()).isTrue();
            softly.assertThat(report.getReport()).isInstanceOf(TrimmedCoreClientEvent.class);
            softly.assertAll();
        }
    }
}
