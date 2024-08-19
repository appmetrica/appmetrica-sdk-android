package io.appmetrica.analytics.coreutils.internal;

import android.content.ContentValues;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class StringUtilsTests extends CommonTest {

    @Test
    public void testWrapFeaturesForZeroArguments() {
        assertThat(StringUtils.wrapFeatures()).isEmpty();
    }

    @Test
    public void testWrapFeaturesForOne() {
        String testFeature = new RandomStringGenerator(100).nextString();
        assertThat(StringUtils.wrapFeatures(testFeature)).isEqualTo(testFeature);
    }

    @Test
    public void testWrapFeatureForTwoArguments() {
        String firstFeature = new RandomStringGenerator(100).nextString();
        String secondFeature = new RandomStringGenerator(100).nextString();
        String expected = firstFeature + "," + secondFeature;
        assertThat(StringUtils.wrapFeatures(firstFeature, secondFeature)).isEqualTo(expected);
    }

    @Test
    public void testToHexString() {
        assertThat(StringUtils.toHexString(new byte[]{
            -125, 15, -89
        })).isEqualTo("830fa7");
    }

    @Test
    public void testFormatSha1() {
        assertThat(StringUtils.formatSha1("some string".getBytes())).isUpperCase().matches(Pattern.compile("(.{2}:)+(.{2})"));
    }

    @Test
    public void testHexToBytesForEmptyString() {
        assertThat(StringUtils.hexToBytes("")).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexToBytesForStringWithInvalidLength() {
        StringUtils.hexToBytes("1ac");
    }

    @Test
    public void testHexToBytes() {
        assertThat(StringUtils.hexToBytes("830fa7")).isEqualTo(new byte[]{-125, 15, -89});
    }

    @Test
    public void testGetUtf8Bytes() {
        final String value = "some value";
        assertThat(StringUtils.getUTF8Bytes(value)).isEqualTo(new byte[]{115, 111, 109, 101, 32, 118, 97, 108, 117, 101});
    }

    @Test
    public void testGetUtf8BytesNull() {
        assertThat(StringUtils.getUTF8Bytes((String) null)).isNotNull().isEmpty();
    }

    @Test
    public void contentValuesToStringNull() {
        assertThat(StringUtils.contentValuesToString(null)).isEqualTo("null");
    }

    @Test
    public void contentValuesToStringEmpty() {
        assertThat(StringUtils.contentValuesToString(new ContentValues())).isEmpty();
    }

    @Test
    public void contentValuesToStringSingleItem() {
        ContentValues cv = new ContentValues();
        cv.put("animal", "cat");
        assertThat(StringUtils.contentValuesToString(cv)).isEqualTo("animal=cat");
    }

    @Test
    public void contentValuesToStringFilled() {
        ContentValues cv = new ContentValues();
        cv.put("key1", "value1");
        cv.put("key2", 2);
        cv.put("key3", 3L);
        cv.put("key4", true);
        cv.put("key5", 1.2f);
        cv.put("key6", 3.4d);
        cv.put("key7", "value7".getBytes());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(StringUtils.contentValuesToString(cv))
            .contains("key1=value1")
            .contains("key2=2")
            .contains("key3=3")
            .contains("key4=true")
            .contains("key5=1.2")
            .contains("key6=3.4")
            .contains("key7=");
        softly.assertAll();
    }

    @Test
    public void contentValuesToStringWithTooLongValues() {
        RandomStringGenerator stringGenerator = new RandomStringGenerator(500);
        ContentValues cv = new ContentValues();
        String fittingPart1 = stringGenerator.nextString();
        String fittingPart2 = stringGenerator.nextString();
        cv.put("key1", fittingPart1 + "uuuuuuuuu");
        cv.put("key2", "value2");
        cv.put("key3", fittingPart2 + "ppppppppp");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(StringUtils.contentValuesToString(cv))
            .contains("key1=" + fittingPart1)
            .contains("key2=value2")
            .contains("key3=" + fittingPart2);
        softly.assertAll();
    }

    @Test
    public void throwableToString() {
        Throwable throwable = new RuntimeException("Some");
        assertThat(StringUtils.throwableToString(throwable))
            .contains(throwable.getClass().getName())
            .contains(throwable.getMessage())
            .matches(s -> {
                for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                    assertThat(s).contains(stackTraceElement.toString());
                }
                return true;
            });
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class CompareTest {

        @ParameterizedRobolectricTestRunner.Parameters(name = "for \"{0}\" and \"{1}\" will be {2}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {null, null, 0},
                {null, "", -1},
                {"", null, 1},
                {"a", "a", 0},
                {"a", "b", -1},
                {"b", "a", 1}
            });
        }

        private final String left;
        private final String right;
        private final int result;

        public CompareTest(String left, String right, int result) {
            this.left = left;
            this.right = right;
            this.result = result;
        }

        @Test
        public void testCompare() {
            assertThat(StringUtils.compare(left, right)).isEqualTo(result);
        }

    }

}
