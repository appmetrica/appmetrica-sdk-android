package io.appmetrica.analytics.impl.crash;

import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ClientConfigurationTestUtils;
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReadAndReportRunnableTest extends CommonTest {

    @Mock
    private File crashFile;
    @Mock
    private Function<File, JvmCrash> fileReader;
    @Mock
    private Consumer<File> finalizator;
    @Mock
    private Consumer<JvmCrash> crashConsumer;
    private ReadAndReportRunnable<JvmCrash> readAndReportRunnable;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(true).when(crashFile).exists();
        readAndReportRunnable = new ReadAndReportRunnable<JvmCrash>(crashFile, fileReader, finalizator, crashConsumer);
    }

    @Test
    public void testEmpty() {
        doReturn(null).when(fileReader).apply(any(File.class));
        readAndReportRunnable.run();
        verify(fileReader).apply(crashFile);
        verify(crashConsumer, never()).consume(any(JvmCrash.class));
    }

    @Test
    public void testCrashHandled() {
        ClientConfiguration stubbedConfiguration = ClientConfigurationTestUtils.createStubbedConfiguration();
        stubbedConfiguration.getReporterConfiguration().setApiKey(TestsData.generateApiKey());
        final JvmCrash crash = new JvmCrash(
                EventsManager.unhandledExceptionReportEntry("crash name", "crash value".getBytes(), mock(PublicLogger.class)),
                stubbedConfiguration,
                mock(HashMap.class)
        );
        doReturn(crash).when(fileReader).apply(any(File.class));
        readAndReportRunnable.run();
        ArgumentCaptor<JvmCrash> newCrash = ArgumentCaptor.forClass(JvmCrash.class);
        verify(crashConsumer).consume(newCrash.capture());
        assertThat(crash).usingRecursiveComparison().isEqualTo(newCrash.getValue());
    }

    @Test
    public void testCrashFileDeleted() {
        when(crashFile.exists()).thenReturn(true);
        readAndReportRunnable.run();
        verify(finalizator).consume(crashFile);
    }
}
