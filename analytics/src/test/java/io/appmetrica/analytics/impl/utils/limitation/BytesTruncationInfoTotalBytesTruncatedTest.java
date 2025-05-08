package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see BytesTruncationInfoTest
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class BytesTruncationInfoTotalBytesTruncatedTest extends CommonTest {

    private final TrimmingResult<Object, BytesTruncatedInfo>[] input;
    private final int expectedResult;

    public BytesTruncationInfoTotalBytesTruncatedTest(TrimmingResult<Object, BytesTruncatedInfo>[] input,
                                                      int expectedResult,
                                                      String description) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {new TrimmingResult[0], 0, "Empty items"},
            {
                new TrimmingResult[]{
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(0))
                },
                0,
                "Single zero bytes truncated item"
            },
            {
                new TrimmingResult[]{
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(10))
                },
                10,
                "Single non-zero bytes truncated item"
            },
            {
                new TrimmingResult[]{
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(0)),
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(0)),
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(0))
                },
                0,
                "Multiple zero bytes truncated items"
            },
            {
                new TrimmingResult[]{
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(1)),
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(10)),
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(100))
                },
                111,
                "Multiple non-zero bytes truncated items"
            },
            {
                new TrimmingResult[]{
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(0)),
                    null,
                    new TrimmingResult(new Object(), new BytesTruncatedInfo(1))
                },
                1,
                "Multiple items with single null item"
            },
            {
                new TrimmingResult[]{
                    null
                },
                0,
                "Single null item"
            },
            {
                new TrimmingResult[]{
                    null,
                    null,
                    null
                },
                0,
                "Multiple null items"
            }
        });
    }

    @Test
    public void totalBytesTruncated() {
        assertThat(BytesTruncatedInfo.totalBytesTruncated(input)).isEqualTo(expectedResult);
    }

}
