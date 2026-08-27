package io.appmetrica.analytics.impl.utils.encryption;

import io.appmetrica.analytics.impl.ServiceEvent;
import io.appmetrica.gradle.testutils.CommonTest;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DummyEventEncrypterTests extends CommonTest {

    DummyEventEncrypter mDummyEventEncrypter;

    @Before
    public void setUp() throws Exception {
        mDummyEventEncrypter = new DummyEventEncrypter();
    }

    @Test
    public void testEncryptionMode() {
        assertThat(mDummyEventEncrypter.getEncryptionMode()).isEqualTo(EventEncryptionMode.NONE);
    }

    @Test
    public void testEncryptDoesNotModifyCounterReport() {
        ServiceEvent serviceEvent = mock(ServiceEvent.class);
        assertThat(mDummyEventEncrypter.encrypt(serviceEvent).mServiceEvent).isEqualTo(serviceEvent);
    }

    @Test
    public void testEncryptSetExpectedEncryptionMode() {
        assertThat(mDummyEventEncrypter.encrypt(mock(ServiceEvent.class)).mEventEncryptionMode)
            .isEqualTo(EventEncryptionMode.NONE);
    }

    @Test
    public void testDecryptReturnInputData() {
        byte[] input = new byte[1024];
        new Random().nextBytes(input);
        assertThat(mDummyEventEncrypter.decrypt(input)).isEqualTo(input);
    }
}
