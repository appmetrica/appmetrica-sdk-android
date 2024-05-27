package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.util.Arrays;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class EventLimitationsProcessorTest extends CommonTest {

    private static final int REPORT_NAME_MAX_LENGTH = 1000;
    public static final String ARG_TAG = "Test";

    @Mock
    private PublicLogger mPublicLogger;

    private Trimmer<String> mEventNameTrimmer;
    private RandomStringGenerator mRandomStringGenerator;

    private static final Random RANDOM = new Random();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mEventNameTrimmer = new StringTrimmer(REPORT_NAME_MAX_LENGTH, ARG_TAG, mPublicLogger);
        mRandomStringGenerator = new RandomStringGenerator(REPORT_NAME_MAX_LENGTH);
    }

    @Test
    public void testTrimToSizeShouldReturnNullIfSourceStringIsNull() {
        assertThat(mEventNameTrimmer.trim(null)).isNull();
    }

    @Test
    public void testTrimToSizeShouldReturnEmptyStringIfSourceStringIsEmpty() {
        assertThat(mEventNameTrimmer.trim(StringUtils.EMPTY)).isEmpty();
    }

    @Test
    public void testTrimToSizeShouldReturnUnchangedStringIfSourceStringLessThanLimit() {
        String randomString = mRandomStringGenerator.nextString();
        assertThat(new StringTrimmer(randomString.length() + 1, ARG_TAG, mPublicLogger).trim(randomString))
                .isEqualTo(randomString);
    }

    @Test
    public void testTrimToSizeShouldReturnUnchangedStringIfSourceStringHasSameLengthAsLimit() {
        String randomString = mRandomStringGenerator.nextString();
        assertThat(new StringTrimmer(randomString.length(), ARG_TAG, mPublicLogger).trim(randomString)).isEqualTo(randomString);
    }

    @Test
    public void testTrimToSizeShouldReturnSubstringWithLimitLengthIfSourceStringBiggerThanLimit() {
        String randomString = mRandomStringGenerator.nextString();
        String trimmedString = new StringTrimmer(randomString.length() - 1, ARG_TAG, mPublicLogger).trim(randomString);
        assertThat(randomString).contains(trimmedString);
        assertThat(trimmedString.length()).isEqualTo(randomString.length() - 1);
        verify(mPublicLogger).warning(anyString(), any(Object[].class));
    }

    @Test
    public void testByteArrayNotTrimmed() {
        final int MAX_SIZE = 1000;
        byte[] src = new byte[MAX_SIZE / 2];
        RANDOM.nextBytes(src);

        assertThat(Arrays.equals(src, new BytesTrimmer(MAX_SIZE, ARG_TAG, mPublicLogger).trim(src))).isTrue();
    }

    @Test
    public void testByteArrayTrimmedNotTrimmed() {
        final int MAX_SIZE = 1000;
        byte[] src = new byte[MAX_SIZE * 2];
        RANDOM.nextBytes(src);

        byte[] trimmed = new BytesTrimmer(MAX_SIZE, ARG_TAG, mPublicLogger).trim(src);
        assertThat(Arrays.equals(src, trimmed)).isFalse();
        assertThat(trimmed.length).isEqualTo(MAX_SIZE);
        verify(mPublicLogger).warning(anyString(), any(Object[].class));

        byte[] srcCopy = new byte[trimmed.length];
        System.arraycopy(trimmed, 0, srcCopy, 0, trimmed.length);
        assertThat(Arrays.equals(trimmed, srcCopy)).isTrue();
    }
}
