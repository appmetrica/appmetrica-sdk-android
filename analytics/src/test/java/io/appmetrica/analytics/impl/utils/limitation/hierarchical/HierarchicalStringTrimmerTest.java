package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(Parameterized.class)
public class HierarchicalStringTrimmerTest extends CommonTest {

    private final String input;
    private final String expected;
    private final int expectedTruncatedBytes;

    public HierarchicalStringTrimmerTest(String input, String expected, int expectedTruncatedBytes) {
        this.input = input;
        this.expected = expected;
        this.expectedTruncatedBytes = expectedTruncatedBytes;
    }

    @Parameters(name = "\"{0}\" -> \"{1}\": {2} bytes")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {null, null, 0},
            {"", "", 0},
            {"q", "q", 0},
            {"#`&*%", "#`&*%", 0},
            {"ыфвы", "ыфвы", 0},
            {"qwertyuioo", "qwertyuioo", 0},
            {"qwertyuiooa", "qwertyuioo", 1},
            {"qwertyuiooaф", "qwertyuioo", 3},
            {"фыаперпвмп", "фыаперпвмп", 0},
            {"выдылвдвлвцдвлв", "выдылвдвлв", 10},
            {"!@#$%^&*()_+:>", "!@#$%^&*()", 4},
            {"вqдakвkвлвцдlлf", "вqдakвkвлв", 8},
        });
    }

    private HierarchicalStringTrimmer hierarchicalStringTrimmer;

    @Before
    public void setUp() throws Exception {
        hierarchicalStringTrimmer = new HierarchicalStringTrimmer(10);
    }

    @Test
    public void trim() throws Exception {
        TrimmingResult<String, BytesTruncatedProvider> trimmingResult = hierarchicalStringTrimmer.trim(input);

        ObjectPropertyAssertions(trimmingResult)
            .checkField("value", expected)
            .checkFieldRecursively(
                "metaInfo",
                new TruncationInfoConsumer(expectedTruncatedBytes)
            )
            .checkAll();
    }
}
