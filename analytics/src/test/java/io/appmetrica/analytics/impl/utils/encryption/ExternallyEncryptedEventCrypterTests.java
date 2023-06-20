package io.appmetrica.analytics.impl.utils.encryption;

import android.util.Base64;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ExternallyEncryptedEventCrypterTests extends CommonTest {

    private EventEncrypter mEventEncrypter;

    @Before
    public void setUp() throws Exception {
        mEventEncrypter = new ExternallyEncryptedEventCrypter();
    }

    @Test
    public void testEncryptionMode() {
        assertThat(mEventEncrypter.getEncryptionMode())
                .isEqualTo(EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEncryptThrowException() {
        mEventEncrypter.encrypt(mock(CounterReport.class));
    }

    @Test
    public void testDecryptReturnExpectedData() {
        byte[] input = new byte[2048];
        new Random().nextBytes(input);
        assertThat(mEventEncrypter.decrypt(Base64.encode(input, Base64.DEFAULT))).isEqualTo(input);
    }

    @Test
    public void testDecryptBadBase64() {
        assertThat(mEventEncrypter.decrypt(new byte[]{1, 2, 3})).isNotNull().isEmpty();
    }
}
