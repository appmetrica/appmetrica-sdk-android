package io.appmetrica.analytics.impl.db.state.converter;

import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter;
import io.appmetrica.analytics.coreutils.internal.io.GZIPCompressor;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class BodyDecoderTest extends CommonTest {

    @Mock
    private BodyDecoder.AESEncryptedProvider mEncryptedProvider;
    @Mock
    private GZIPCompressor mGzipCompressor;
    @Mock
    private AESEncrypter mAesEncrypter;

    private BodyDecoder mBodyDecoder;

    final byte[] mEncoded = new byte[]{11, 22, 88, 33};
    final byte[] mCompressed = new byte[]{12, 23};
    final byte[] mExpected = new byte[]{21, 32};
    final String mKey = "key";

    @Before
    public void setUp() throws Throwable {
        MockitoAnnotations.openMocks(this);
        when(mEncryptedProvider.getEncrypter(eq(mKey.getBytes()), any())).thenReturn(mAesEncrypter);
        when(mAesEncrypter.decrypt(eq(mEncoded), anyInt(), anyInt())).thenReturn(mCompressed);
        when(mGzipCompressor.uncompress(mCompressed)).thenReturn(mExpected);
        mBodyDecoder = new BodyDecoder(mEncryptedProvider, mGzipCompressor);
    }

    @Test
    public void testDecodeNull() {
        assertThat(mBodyDecoder.decode(null, "")).isNull();
    }

    @Test
    public void testDecodeEmpty() {
        assertThat(mBodyDecoder.decode(new byte[0], "")).isNull();
    }

    @Test
    public void testDecodeNotNull() throws Exception {
        assertThat(mBodyDecoder.decode(mEncoded, mKey)).isEqualTo(mExpected);
    }

    @Test
    public void testAesEncrypterThrows() throws Throwable {
        when(mAesEncrypter.decrypt(eq(mEncoded), anyInt(), anyInt())).thenThrow(new RuntimeException());
        assertThat(mBodyDecoder.decode(mEncoded, mKey)).isNull();
    }

    @Test
    public void testCompressorThrows() throws Exception {
        when(mGzipCompressor.uncompress(mCompressed)).thenThrow(new IOException());
        assertThat(mBodyDecoder.decode(mEncoded, mKey)).isNull();
    }
}

