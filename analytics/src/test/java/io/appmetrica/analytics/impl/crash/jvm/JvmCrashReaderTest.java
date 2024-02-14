package io.appmetrica.analytics.impl.crash.jvm;

import android.os.ResultReceiver;
import io.appmetrica.analytics.TestData;
import io.appmetrica.analytics.impl.ClientCounterReport;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.File;
import java.util.HashMap;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class JvmCrashReaderTest extends CommonTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private File crashFile;
    @InjectMocks
    private JvmCrashReader crashReader;

    @Test
    public void hasData() throws JSONException {
        try (MockedStatic<IOUtils> sIoUtils = Mockito.mockStatic(IOUtils.class)) {
            CounterReport counterReport = new CounterReport();
            counterReport.setName("crash");
            String crashValue = new JvmCrash(
                    counterReport,
                    new ClientConfiguration(new ProcessConfiguration(
                            RuntimeEnvironment.getApplication(),
                            mock(ResultReceiver.class)
                    ), new CounterConfiguration(TestData.TEST_UUID)),
                    new HashMap<ClientCounterReport.TrimmedField, Integer>()
            ).toJSONString();
            when(IOUtils.getStringFileLocked(crashFile)).thenReturn(crashValue);
            assertThat(crashReader.apply(crashFile).toJSONString()).isEqualTo(crashValue);
        }
    }

    @Test
    public void noData() {
        try (MockedStatic<IOUtils> sIoUtils = Mockito.mockStatic(IOUtils.class)) {
            when(IOUtils.getStringFileLocked(crashFile)).thenReturn(null);
            assertThat(crashReader.apply(crashFile)).isNull();
        }
    }

    @Test
    public void emptyData() {
        try (MockedStatic<IOUtils> sIoUtils = Mockito.mockStatic(IOUtils.class)) {
            when(IOUtils.getStringFileLocked(crashFile)).thenReturn("");
            assertThat(crashReader.apply(crashFile)).isNull();
        }
    }

    @Test
    public void deleteFile() {
        crashReader.consume(crashFile);
        verify(crashFile).delete();
    }
}
