package io.appmetrica.analytics.coreutils.internal;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class StringUtilsGetUtf8BytesForStringListTest extends CommonTest {

    private List<String> input;
    private byte[][] expected;

    public StringUtilsGetUtf8BytesForStringListTest(List<String> input, byte[][] expected) {
        this.input = input;
        this.expected = expected;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, new byte[][]{}},
                {Collections.<String>emptyList(), new byte[][]{}},
                {Collections.singletonList("value"), new byte[][]{"value".getBytes()}},
                {Arrays.asList("first", "second"), new byte[][]{"first".getBytes(), "second".getBytes()}}
        });
    }

    @Test
    public void getUtf8Bytes() {
        assertThat(StringUtils.getUTF8Bytes(input)).isEqualTo(expected);
    }
}
