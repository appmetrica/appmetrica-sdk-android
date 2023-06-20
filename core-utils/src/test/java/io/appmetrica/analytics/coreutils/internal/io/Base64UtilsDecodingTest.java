package io.appmetrica.analytics.coreutils.internal.io;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class Base64UtilsDecodingTest extends CommonTest {

    private final String input;
    private final String expectedString;
    private final byte[] expectedBytes;

    public Base64UtilsDecodingTest(String input, String expectedString, byte[] expectedBytes) {
        this.input = input;
        this.expectedString = expectedString;
        this.expectedBytes = expectedBytes;
    }

    private static final String SOME_TEST_STRING = "Some test string";
    private static final String SINGLE_SYMBOL_STRING = "g";
    private static final String NON_ASCII_STRING = "афд";
    private static final String SPECIAL_SYMBOL_STRING = "#4(8%";

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {SOME_TEST_STRING, SOME_TEST_STRING, SOME_TEST_STRING.getBytes("UTF-8")},
                {SINGLE_SYMBOL_STRING, SINGLE_SYMBOL_STRING, SINGLE_SYMBOL_STRING.getBytes("UTF-8")},
                {NON_ASCII_STRING, NON_ASCII_STRING, NON_ASCII_STRING.getBytes("UTF-8")},
                {SPECIAL_SYMBOL_STRING, SPECIAL_SYMBOL_STRING, SPECIAL_SYMBOL_STRING.getBytes("UTF-8")},
                {"", "", new byte[0]},
                {null, "", new byte[0]}
        });
    }

    private String compressedString;
    private String compressedBytes;

    @Before
    public void setUp() throws Exception {
        final byte[] inputBytes = input == null ? null : input.getBytes("UTF-8");
        compressedString = Base64Utils.compressBase64String(input);
        compressedBytes = Base64Utils.compressBase64(inputBytes);
    }

    @Test
    public void decompressBase64GzipAsStringFromCompressedString() throws Exception {
        assertThat(Base64Utils.decompressBase64GzipAsString(compressedString)).isEqualTo(expectedString);
    }

    @Test
    public void decompressBase64GzipAsBytesFromCompressedString() throws Exception {
        assertThat(Base64Utils.decompressBase64GzipAsBytes(compressedString)).isEqualTo(expectedBytes);
    }

    @Test
    public void decompressBase64GzipAsStringFromCompressedBytes() throws Exception {
        assertThat(Base64Utils.decompressBase64GzipAsString(compressedBytes)).isEqualTo(expectedString);
    }

    @Test
    public void decompressBase64GzipAsBytesFromCompressedBytes() throws Exception {
        assertThat(Base64Utils.decompressBase64GzipAsBytes(compressedBytes)).isEqualTo(expectedBytes);
    }
}
