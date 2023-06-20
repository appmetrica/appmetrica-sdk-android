package io.appmetrica.analytics.impl.crash;

import io.appmetrica.analytics.CounterConfiguration;
import io.appmetrica.analytics.impl.ClientCounterReport;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.ReportToSend;
import io.appmetrica.analytics.impl.ReporterEnvironment;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.client.ThrowableModel;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.client.converter.JvmCrashConverter;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.HashMap;
import java.util.Random;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UnhandledExceptionEventFormerTest extends CommonTest {

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
        mEventFormer = new UnhandledExceptionEventFormer(mJvmCrashConverter);
    }

    @Test
    public void formEvent() {
        try (MockedStatic<EventsManager> sEventsManager = Mockito.mockStatic(EventsManager.class)) {
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
            when(mReporterEnvironment.getProcessConfiguration()).thenReturn(new ProcessConfiguration(RuntimeEnvironment.getApplication(), null));
            when(mReporterEnvironment.getReporterConfiguration()).thenReturn(new CounterConfiguration());

            ClientCounterReport clientCounterReport = mock(ClientCounterReport.class);
            when(
                    EventsManager.unhandledExceptionReportEntry(
                            eq(errorName),
                            same(eventValueBytes),
                            any(PublicLogger.class)
                    )
            ).thenReturn(clientCounterReport);
            HashMap<ClientCounterReport.TrimmedField, Integer> trimmedFields = new HashMap<ClientCounterReport.TrimmedField, Integer>();
            trimmedFields.put(ClientCounterReport.TrimmedField.VALUE, 20);
            trimmedFields.put(ClientCounterReport.TrimmedField.NAME, 10);
            when(clientCounterReport.getTrimmedFields()).thenReturn(trimmedFields);
            ReportToSend report = mEventFormer.formEvent(unhandledException, mReporterEnvironment);
            verify(clientCounterReport).setEventEnvironment(environment);
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(report.getEnvironment().getReporterConfiguration())
                    .isEqualToComparingFieldByFieldRecursively(mReporterEnvironment.getReporterConfiguration());
            softly.assertThat(report.getEnvironment().getProcessConfiguration())
                    .isEqualToComparingFieldByFieldRecursively(mReporterEnvironment.getProcessConfiguration());
            softly.assertThat(report.isCrashReport()).isTrue();
            softly.assertThat(report.getTrimmedFields()).isEqualTo(trimmedFields);
            softly.assertThat(report.getReport()).isSameAs(clientCounterReport);
            softly.assertAll();
        }
    }
}
