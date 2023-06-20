package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class HierarchicalStringListTrimmerTest extends CommonTest {

    private List<String> input;
    private List<String> expectedResultList;
    private int expectedItemsDropped;
    private int expectedBytesTruncated;

    public HierarchicalStringListTrimmerTest(List<String> input,
                                             List<String> expectedResultList,
                                             int expectedItemsDropped,
                                             int expectedBytesTruncated,
                                             String description) {
        this.input = input;
        this.expectedResultList = expectedResultList;
        this.expectedItemsDropped = expectedItemsDropped;
        this.expectedBytesTruncated = expectedBytesTruncated;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {4}")
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, null, 0, 0, "null"},
                {Collections.emptyList(), Collections.emptyList(), 0, 0, "empty list"},
                {
                        Collections.singletonList("aaaaa"),
                        Collections.singletonList("aaaaa"),
                        0,
                        0,
                        "list with single non-truncated item"
                },
                {
                        Arrays.asList("aaaaa", "bbbbb", "ccccc", "ddddd", "eeeee"),
                        Arrays.asList("aaaaa", "bbbbb", "ccccc", "ddddd", "eeeee"),
                        0,
                        0,
                        "list with 5 non-truncated items within list size limitt"
                },
                {
                        Arrays.asList(
                                "aaaaaaaaaa", "aaaaaaaaaa" + "b", "aaaaaaaaaa" + "cc", "aaaaaaaaaa" + "ddd",
                                "aaaaaaaaaa" + "eeee"
                        ),
                        Arrays.asList("aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa"),
                        0,
                        10,
                        "list with 5 truncated items within list size limit"
                },
                {
                        Arrays.asList("aaaaa", "bbbbb", "ccccc", "ddddd", "eeeee", "fffff", "ggggg", "hhhhh", "iiiii"),
                        Arrays.asList("aaaaa", "bbbbb", "ccccc", "ddddd", "eeeee"),
                        4,
                        20,
                        "list with 4 non truncated items out of list size limit"
                },
                {
                        Arrays.asList(
                                "aaaaaaaaaa" + "bbbbb", "bbbbb", "cccccccccc" + "ddddd", "ddddd", "eeeee", "ffffffffff"
                        ),
                        Arrays.asList("aaaaaaaaaa", "bbbbb", "cccccccccc", "ddddd", "eeeee"),
                        1,
                        20,
                        "list with single item out of list size limit and some truncated items"
                },
                {
                        Collections.singletonList("фывапроцыыы"),
                        Collections.singletonList("фывапроцыы"),
                        0,
                        2,
                        "list with cyrillic chars string items"
                },
                {
                        Arrays.asList(
                                "aaaaaaa",
                                "bbbbbbb",
                                "ccccccc",
                                "dddddddddd",
                                "бббббббббб",
                                "аааааааааа" + "ббббб" //cyrillic "а"
                        ),
                        Arrays.asList("aaaaaaa", "bbbbbbb", "ccccccc", "dddddddddd", "бббббббббб"),
                        1,
                        30,
                        "list with single truncated item out of list size limit"
                }
        });
    }

    @Mock
    private HierarchicalStringTrimmer stringTrimmer;

    private HierarchicalStringListTrimmer stringListTrimmer;

    private final int itemCountLimit = 5;
    private final int stringLimit = 10;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(stringTrimmer.trim(anyString())).then(new Answer<TrimmingResult<String, BytesTruncatedProvider>>() {
            @Override
            public TrimmingResult<String, BytesTruncatedProvider> answer(InvocationOnMock invocation)
                    throws Throwable {
                String inputString = invocation.getArgument(0);
                String resultString = inputString;
                int bytesTruncated = 0;
                if (inputString.length() > stringLimit) {
                    resultString = inputString.substring(0, stringLimit);
                    bytesTruncated = inputString.getBytes().length - resultString.getBytes().length;
                }

                return new TrimmingResult<String, BytesTruncatedProvider>(
                        resultString,
                        new BytesTruncatedInfo(bytesTruncated)
                );
            }
        });

        stringListTrimmer = new HierarchicalStringListTrimmer(itemCountLimit, stringTrimmer);
    }

    @Test
    public void trim() throws Exception {
        TrimmingResult<List<String>, CollectionTrimInfo> trimmingResult =
                stringListTrimmer.trim(input);

        ObjectPropertyAssertions<TrimmingResult<List<String>, CollectionTrimInfo>>
                assertions = ObjectPropertyAssertions(trimmingResult);

        assertions.checkFieldComparingFieldByField(
                "metaInfo",
                new CollectionTrimInfo(expectedItemsDropped, expectedBytesTruncated)
        );
        assertions.checkField("value", expectedResultList, true);

        assertions.checkAll();
    }
}
