package io.appmetrica.analytics.impl.utils.encryption;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class EncryptionCounterReportTests extends CommonTest {

    private EncryptedCounterReport mEncryptedCounterReport;

    @Test
    public void testEncryptedCounterReportContainsCounterReportFromConstructor() {
        CounterReport counterReport = mock(CounterReport.class);
        mEncryptedCounterReport = new EncryptedCounterReport(counterReport, EventEncryptionMode.NONE);
        assertThat(mEncryptedCounterReport.mCounterReport).isEqualTo(counterReport);
    }

    @Test
    public void testEncryptedCounterReportContainsEncryptionModeFromConstructor() {
        EventEncryptionMode eventEncryptionMode = EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER;
        mEncryptedCounterReport = new EncryptedCounterReport(null, eventEncryptionMode);
        assertThat(mEncryptedCounterReport.mEventEncryptionMode).isEqualTo(eventEncryptionMode);
    }
}
