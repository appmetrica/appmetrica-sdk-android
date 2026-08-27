package io.appmetrica.analytics.impl.utils.encryption;

import io.appmetrica.analytics.impl.ServiceEvent;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EncryptionCounterReportTests extends CommonTest {

    private EncryptedCounterReport mEncryptedCounterReport;

    @Test
    public void testEncryptedCounterReportContainsCounterReportFromConstructor() {
        ServiceEvent serviceEvent = mock(ServiceEvent.class);
        mEncryptedCounterReport = new EncryptedCounterReport(serviceEvent, EventEncryptionMode.NONE);
        assertThat(mEncryptedCounterReport.mServiceEvent).isEqualTo(serviceEvent);
    }

    @Test
    public void testEncryptedCounterReportContainsEncryptionModeFromConstructor() {
        EventEncryptionMode eventEncryptionMode = EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER;
        mEncryptedCounterReport = new EncryptedCounterReport(null, eventEncryptionMode);
        assertThat(mEncryptedCounterReport.mEventEncryptionMode).isEqualTo(eventEncryptionMode);
    }
}
