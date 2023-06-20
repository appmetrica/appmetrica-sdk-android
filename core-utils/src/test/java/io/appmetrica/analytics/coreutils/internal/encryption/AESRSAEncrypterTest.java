package io.appmetrica.analytics.coreutils.internal.encryption;

import io.appmetrica.analytics.testutils.CommonTest;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AESRSAEncrypterTest extends CommonTest {

    @Test
    public void testEncryption() throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        final byte[] iv = new byte[16];
        final byte[] password = new byte[16];
        random.nextBytes(password);
        random.nextBytes(iv);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.genKeyPair();
        PublicKey publicKey = kp.getPublic();
        PrivateKey privateKey = kp.getPrivate();

        AESRSAEncrypter encrypter = new AESRSAEncrypter("AES/CBC/PKCS5Padding", "RSA/ECB/PKCS1Padding");

        byte[] text = new byte[1024 * 56 - 333];

        random.nextBytes(text);

        byte[] encrypted = encrypter.encryptInternal(text, password, iv, publicKey);
        byte[] decrypted = encrypter.decryptInternal(encrypted, privateKey);
        assertThat(Arrays.equals(text, decrypted)).isTrue();
    }
}
