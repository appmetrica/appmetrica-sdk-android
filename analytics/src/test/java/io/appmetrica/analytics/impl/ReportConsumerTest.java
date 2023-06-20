package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Bundle;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.component.clients.ClientUnit;
import io.appmetrica.analytics.impl.crash.ReadAndReportRunnable;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashSource;
import io.appmetrica.analytics.impl.crash.ndk.NativeDumpHandlerWrapper;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadCrash;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadCrashReport;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadCrashReporter;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.RuntimeConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportConsumerTest extends CommonTest {

    private Context mContext;
    @Mock
    private ClientRepository mClientRepository;
    @Mock
    private ICommonExecutor mTasksExcutor;
    @Mock
    private FileProvider fileProvider;
    private String dumpFile = "dump file";
    private ReportConsumer mReportConsumer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(fileProvider.getFileByNonNullPath(anyString())).thenReturn(new File(RuntimeEnvironment.getApplication().getFilesDir(), dumpFile));
        mReportConsumer = new ReportConsumer(mContext, mClientRepository, mTasksExcutor, fileProvider);
    }

    @Test
    public void testConsumeReport() {
        CounterReport counterReport = mock(CounterReport.class);
        mReportConsumer.consumeReport(counterReport, new Bundle());
        verify(mTasksExcutor).execute(any(ReportRunnable.class));
    }

    @Test
    public void testConsumeReportUndefinedType() {
        CounterReport counterReport = mock(CounterReport.class);
        when(counterReport.isUndefinedType()).thenReturn(true);
        mReportConsumer.consumeReport(counterReport, new Bundle());
        verify(mTasksExcutor, never()).execute(any(ReportRunnable.class));
    }

    @Test
    public void testConsumeCrashFromFile() {
        mReportConsumer.consumeCrashFromFile(mock(File.class));
        verify(mTasksExcutor).execute(any(ReadAndReportRunnable.class));
    }

    @Test
    public void testConsumeCrash() {
        final String packageName = "package";
        final int pid = 10;
        final String psid = "11";
        ClientDescription clientDescription = mock(ClientDescription.class);
        when(clientDescription.getPackageName()).thenReturn(packageName);
        when(clientDescription.getProcessID()).thenReturn(pid);
        when(clientDescription.getProcessSessionID()).thenReturn(psid);
        CounterReport counterReport = mock(CounterReport.class);
        CommonArguments commonArguments = mock(CommonArguments.class);
        ClientUnit clientUnit = mock(ClientUnit.class);
        when(mClientRepository.getOrCreateClient(clientDescription, commonArguments)).thenReturn(clientUnit);
        mReportConsumer.consumeCrash(clientDescription, counterReport, commonArguments);
        verify(clientUnit).handle(counterReport, commonArguments);
        verify(mClientRepository).remove(packageName, pid, psid);
    }

    @Test
    public void testCrashpadCrash() {
        ClientDescription description = mock(ClientDescription.class);
        final CrashpadCrashReport report = new CrashpadCrashReport("uuid", "dumpFile", 12321321L);
        Function<String, CounterReport> reportFunction = mock(Function.class);
        String version = "someVersion";
        mReportConsumer.consumeCrashpadCrash(new CrashpadCrash(report, description, new RuntimeConfig(null, version)), reportFunction);

        ArgumentCaptor<ReadAndReportRunnable<String>> captor = ArgumentCaptor.forClass(ReadAndReportRunnable.class);
        verify(mTasksExcutor).execute(captor.capture());

        SoftAssertions assertions = new SoftAssertions();

        List<ReadAndReportRunnable<String>> values = captor.getAllValues();

        assertions.assertThat(values).extracting("crashFile").extracting("name")
                .containsExactly(dumpFile);
        assertions.assertThat(values).extracting("finalizator").extracting("class").as("finalizator")
                .containsExactly(CrashpadCrashReporter.CrashCompletedConsumer.class);
        assertions.assertThat(values).extracting("fileReader").extracting("class").as("fileReader")
                .containsExactly(NativeDumpHandlerWrapper.class);
        assertions.assertThat(values).extracting("fileReader").extracting("description").extracting("source", "handlerVersion").as("description ")
                .containsExactly(tuple(NativeCrashSource.CRASHPAD, version));
        assertions.assertThat(values).extracting("crashConsumer").extracting("class").as("consumer")
                .containsExactly(ReportConsumer.CrashpadConsumer.class);

        assertions.assertAll();
    }

}
