package io.appmetrica.analytics.coreutils.internal.logger;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class WordBreakFinderTest {

    private final String input;
    private final int startOffset;
    private final int endOffset;
    private final int expected;

    public WordBreakFinderTest(String input, int startOffset, int endOffset, int expected) {
        this.input = input;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.expected = expected;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"asdadasdas", 0, 10, -1},          //#0
                {"asdadasdas,", 0, 11, 10},         //#1
                {"asdadasda,s", 0, 11, 9},          //#2
                {"a,sdadasdas", 0, 11, 1},          //#3
                {",asdadasdas", 0, 11, 0},          //#4
                {"asda,dasdas", 0, 11, 4},          //#5
                {"asda,dasdas", 2, 5, 4},           //#6
                {"asda,dasdas", 1, 3, -1},          //#7
                {"asda,dasdas", 8, 10, -1},         //#8
                {"asda,dasdas", 10, 1, -1},         //#9
                {"as,da,das da,s,", 0, 13, 12},     //#10
                {"as,da,das da,s,", 0, 5, 2},       //#11
                {"as,da,das da,s,", 6, 10, 9},      //#12
                {"asdadasda s", 0, 11, 9},          //#13
                {"asdadasda;s", 0, 11, 9},          //#14
                {"asdadasda\ns", 0, 11, 9},         //#15
        });
    }

    @Test
    public void find() {
        WordBreakFinder finder = new WordBreakFinder();
        assertThat(finder.find(input, startOffset, endOffset)).isEqualTo(expected);
    }
}
