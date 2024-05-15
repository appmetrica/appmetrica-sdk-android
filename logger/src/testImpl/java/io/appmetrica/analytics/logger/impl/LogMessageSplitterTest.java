package io.appmetrica.analytics.logger.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class LogMessageSplitterTest extends CommonTest {

    private final String input;
    private final int lineLimit;
    private final int[] wordBreakerStubs;
    private final String[] expected;

    public LogMessageSplitterTest(
        String input,
        int lineLimit,
        int[] wordBreakerStubs,
        String[] expected
    ) {
        this.input = input;
        this.lineLimit = lineLimit;
        this.wordBreakerStubs = wordBreakerStubs;
        this.expected = expected;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"", 100, new int[0], new String[0]},
            {"qwertyuiop", 100, new int[0], new String[] {"qwertyuiop"}},
            {"qwert\nyuiop", 100, new int[0], new String[] {"qwert", "yuiop"}},
            {"qwertyuiop", 5, new int[0], new String[] {"qwert", "yuiop"}},
            {"qwertyuiop", 4, new int[0], new String[] {"qwer", "tyui", "op"}},
            {"qwertyuiop", 20, new int[]{10}, new String[] {"qwertyuiop"}},
            {"qwe,rtyu,iop", 5, new int[]{3,8}, new String[] {"qwe,", "rtyu,", "iop"}},
            {"qwe,rtyu,iop", 8, new int[]{3,8}, new String[] {"qwe,", "rtyu,iop"}}
        });
    }

    @Mock
    private WordBreakFinder mWordBreakFinder;

    private LogMessageSplitter mSplitter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        if (wordBreakerStubs.length == 0) {
            when(mWordBreakFinder.find(eq(input), anyInt(), anyInt())).thenReturn(-1);
        } else {
            OngoingStubbing<Integer> stubbing = when(mWordBreakFinder.find(eq(input), anyInt(), anyInt()));
            for (int stub : wordBreakerStubs) {
                stubbing = stubbing.thenReturn(stub);
            }
        }

        mSplitter = new LogMessageSplitter(mWordBreakFinder, lineLimit);
    }

    @Test
    public void split() {
        assertThat(mSplitter.split(input)).containsExactly(expected);
    }
}
