package io.appmetrica.analytics.coreutils.internal.encryption;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class AESEncrypterTests extends CommonTest {

    private int mPasswordLength;
    private int mIVLength;

    private int mDataLength;

    private boolean mExpectedException;

    private byte[] mPassword;
    private byte[] mIV;
    private byte[] mData;
    private AESEncrypter mAESEncrypter;

    @ParameterizedRobolectricTestRunner.Parameters(name = "Throw exception ? {3} for password length = {0}, " +
            "IV length = {1}, data length = {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {16, 16, 0, false},
                {16, 16, 8, false},
                {16, 16, 16, false},
                {16, 16, 1024, false},
                {16, 16, 64 * 1024, false},
                {16, 16, 128 * 1024, false},
                {16, 16, 256 * 1024, false},
                {16, 16, 512 * 1024, false},
                {16, 16, 1024 * 1024, false},
                {32, 16, 1024, true},
                {16, 32, 1024, true},
                {8, 8, 1024, true},
                {16, 8, 1024, true},
                {8, 16, 1024, true},
                {16, 16, 0, false},
                {16, 16, -1, true},
                {-1, 16, 1024, true},
                {16, -1, 1024, true},
                {-1, -1, 1024, true},
        });
    }

    public AESEncrypterTests(int passwordLength, int ivLength, int dataLength, boolean expectedException) {
        mPasswordLength = passwordLength;
        mIVLength = ivLength;
        mDataLength = dataLength;
        mExpectedException = expectedException;
    }

    @Before
    public void setUp() throws Exception {
        mPassword = randomBytes(mPasswordLength);
        mIV = randomBytes(mIVLength);
        mData = randomBytes(mDataLength);
        mAESEncrypter = new AESEncrypter("AES/CBC/PKCS5Padding", mPassword, mIV);
    }

    @Test
    public void testEncryption() {
        try {
            byte[] encryptedBytes = mAESEncrypter.encrypt(mData);
            assertThat(mAESEncrypter.decrypt(encryptedBytes)).isEqualTo(mData);
        } catch (Throwable e) {
            assertThat(mExpectedException).as("Exception: " + e.getMessage()).isTrue();
        }
    }

    private byte[] randomBytes(int length) {
        if (length < 0) {
            return null;
        }
        byte[] result = new byte[length];
        new Random().nextBytes(result);
        return result;
    }
}
