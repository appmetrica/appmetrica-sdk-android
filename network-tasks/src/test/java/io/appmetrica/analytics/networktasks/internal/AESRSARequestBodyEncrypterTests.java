package io.appmetrica.analytics.networktasks.internal;

import io.appmetrica.analytics.coreutils.internal.encryption.AESRSAEncrypter;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AESRSARequestBodyEncrypterTests extends CommonTest {

    @Mock
    AESRSAEncrypter mAESRSAEncrypter;
    AESRSARequestBodyEncrypter mAESRSARequestBodyEncrypter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mAESRSARequestBodyEncrypter = new AESRSARequestBodyEncrypter(mAESRSAEncrypter);
    }

    @Test
    public void testGetEncryptionModeReturnExpectedValue() {
        assertThat(mAESRSARequestBodyEncrypter.getEncryptionMode()).isEqualTo(RequestBodyEncryptionMode.AES_RSA);
    }

    @Test
    public void testEncryptDelegateEncryptionToInternalEncrypter() {
        byte[] expectedResult = new byte[1024];
        new Random().nextBytes(expectedResult);
        byte[] input = new byte[2048];
        new Random().nextBytes(input);
        when(mAESRSAEncrypter.encrypt(input)).thenReturn(expectedResult);
        assertThat(mAESRSARequestBodyEncrypter.encrypt(input)).isEqualTo(expectedResult);
    }

    @Test
    public void testEncryptDelegateEncryptionToInternalEncrypterForNull() {
        when(mAESRSAEncrypter.encrypt(null)).thenReturn(null);
        assertThat(mAESRSARequestBodyEncrypter.encrypt(null)).isNull();
    }
}
