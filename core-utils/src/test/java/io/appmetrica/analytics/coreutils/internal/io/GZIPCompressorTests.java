package io.appmetrica.analytics.coreutils.internal.io;

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
public class GZIPCompressorTests extends CommonTest {

    GZIPCompressor mGZIPCompressor;

    private byte[] mInput;
    private byte[] mCompressedValue;

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte[] NON_EMPTY_BYTE_ARRAY = "Test input value".getBytes();
    private static final byte[] EMPTY_BYTE_ARRAY_COMPRESSED =
            {31, -117, 8, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] NON_EMPTY_BYTE_ARRAY_COMPRESSED =
            {31, -117, 8, 0, 0, 0, 0, 0, 0, 0, 11, 73, 45, 46, 81, -56, -52, 43, 40, 45, 81, 40, 75, -52, 41, 77, 5,
                    0, -46, -15, 98, 65, 16, 0, 0, 0};

    @ParameterizedRobolectricTestRunner.Parameters(name = "for input = {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, null, "null"},
                {EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY_COMPRESSED, "empty byte array"},
                {NON_EMPTY_BYTE_ARRAY, NON_EMPTY_BYTE_ARRAY_COMPRESSED, "non empty byte array"}
        });
    }

    public GZIPCompressorTests(final byte[] input,
                               final byte[] compressedValue,
                               final String description) {
        mInput = input;
        mCompressedValue = compressedValue;
    }

    @Before
    public void setUp() {
        mGZIPCompressor = new GZIPCompressor();
        new Random().nextBytes(NON_EMPTY_BYTE_ARRAY);
    }

    @Test
    public void testCompressAndUnCompress() throws Exception {
        byte[] compressed = mGZIPCompressor.compress(mInput);
        byte[] unCompressed = mGZIPCompressor.uncompress(compressed);
        assertThat(unCompressed).isEqualTo(mInput);
    }
}
