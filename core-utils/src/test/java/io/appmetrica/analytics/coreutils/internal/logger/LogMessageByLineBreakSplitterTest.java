package io.appmetrica.analytics.coreutils.internal.logger;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class LogMessageByLineBreakSplitterTest {

    private final String input;
    private final String[] expected;

    public LogMessageByLineBreakSplitterTest(String input, String[] expected) {
        this.input = input;
        this.expected = expected;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"", new String[]{""}},
                {"Some text", new String[]{"Some text"}},
                {"First\nSecond", new String[]{"First", "Second"}},
                {"First\nSecond\nThird", new String[] {"First", "Second", "Third"}},
        });
    }

    @Test
    public void split() {
        LogMessageByLineBreakSplitter splitter = new LogMessageByLineBreakSplitter();
        assertThat(splitter.split(input)).containsExactly(expected);
    }
}
