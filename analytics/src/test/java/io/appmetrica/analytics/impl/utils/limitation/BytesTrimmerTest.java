package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Random;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BytesTrimmerTest extends CommonTest {

    @Mock
    private PublicLogger mLogger;

    private BytesTrimmer mBytesTrimmer;
    private Random mRandom;

    private byte[] mShortBytes;
    private byte[] mLongBytes;

    private static final int LIMIT = 1000;
    private static final String LOG_NAME = "TestLog";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mLogger.isEnabled()).thenReturn(true);

        mBytesTrimmer = new BytesTrimmer(LIMIT, LOG_NAME, mLogger);
        mRandom = new Random();
        mShortBytes = new byte[LIMIT - 10];
        mLongBytes = new byte[LIMIT + 10];
        mRandom.nextBytes(mShortBytes);
        mRandom.nextBytes(mLongBytes);
    }

    @Test
    public void testDefaultConstructor() {
        mBytesTrimmer = new BytesTrimmer(LIMIT, LOG_NAME, mLogger);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mBytesTrimmer.getMaxSize()).isEqualTo(LIMIT);
        softAssertions.assertThat(mBytesTrimmer.getLogName()).isEqualTo(LOG_NAME);
        softAssertions.assertAll();
    }

    @Test
    public void testNoTruncateShortBytes() {
        assertThat(mBytesTrimmer.trim(mShortBytes)).isEqualTo(mShortBytes);
    }

    @Test
    public void testTruncateLongBytes() {
        assertThat(mBytesTrimmer.trim(mLongBytes)).isEqualTo(Arrays.copyOf(mLongBytes, LIMIT));
    }

    @Test
    public void testTruncateNullBytes() {
        assertThat(mBytesTrimmer.trim(null)).isNull();
    }

    @Test
    public void testTruncateEmptyBytes() {
        final byte[] emptyBytes = new byte[0];
        assertThat(mBytesTrimmer.trim(emptyBytes)).isSameAs(emptyBytes);
    }

    @Test
    public void testPrintLogForLongBytes() {
        mBytesTrimmer.trim(mLongBytes);
        verify(mLogger).fw(anyString(), any());
    }

    @Test
    public void testDoesNotPrintLogForShortBytes() {
        mBytesTrimmer.trim(mShortBytes);
        verify(mLogger, never()).fw(anyString(), any());
    }

    @Test
    public void testDoesNotPrintLogIfLoggerDisabled() {
        when(mLogger.isEnabled()).thenReturn(false);
        mBytesTrimmer.trim(mLongBytes);
        verify(mLogger, never()).fw(anyString(), any());
    }

}
