package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmerTest extends CommonTest {

    private Map<String, String> inputMap;
    private Map<String, String> expectedMap;
    private int pairsTruncated;
    private int bytesTruncated;

    private static final String KEY_WITH_5_BYTES_1 = "Key#1";
    private static final String KEY_WITH_5_BYTES_2 = "Key#2";
    private static final String KEY_WITH_5_BYTES_3 = "Key#3";
    private static final String KEY_WITH_5_BYTES_4 = "Key#4";
    private static final String KEY_WITH_5_BYTES_5 = "Key#5";
    private static final String KEY_WITH_20_BYTES_1 = "Key#21_ _ _ _ _ _ _ ";
    private static final String KEY_WITH_20_BYTES_2 = "Key#22_ _ _ _ _ _ _ ";
    private static final String KEY_WITH_20_BYTES_3 = "Key#23_ _ _ _ _ _ _ ";
    private static final String TRUNCATED_KEY_WITH_20_BYTES_1 = "Key#21_ _ ";
    private static final String KEY_WITH_1_BYTE = "k";
    private static final String KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS = "Key_-ААААА";
    private static final String KEY_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS = "Key_-ААААА" + "_ _ _ _ _ ";
    private static final String KEY_WITH_COLLISION_1 = "Collision#1";
    private static final String KEY_WITH_COLLISION_2 = "Collision#2";
    private static final String KEY_WITH_COLLISION_3 = "Collision#3";
    private static final String TRUNCATED_KEY_WITH_COLLISION = "Collision#";

    private static final String VALUE_WITH_10_BYTES = "Value#1_ _";
    private static final String VALUE_WITH_11_BYTES = "Value#2_ _ ";
    private static final String VALUE_WITH_12_BYTES = "Value#3_ _ _";
    private static final String VALUE_WITH_13_BYTES = "Value#4_ _ _ ";
    private static final String VALUE_WITH_14_BYTES = "Value#5_ _ _ _";
    private static final String VALUE_WITH_30_BYTES = "Value#21_ _ _ _ _ _ _ _ _ _ _ ";
    private static final String VALUE_WITH_31_BYTES = "Value#22_ _ _ _ _ _ _ _ _ _ _ _";
    private static final String VALUE_WITH_32_BYTES = "Value#23_ _ _ _ _ _ _ _ _ _ _ _ ";
    private static final String TRUNCATED_VALUE_WITH_30_BYTES = "Value#21_ _ _ _ _ _ ";
    private static final String VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS = "ValueWithCyril_ААААА";
    private static final String VALUE_WITH_35_BYTES_INCLUDING_CYRILLIC_CHARS = "ValueWithCyril_АААААБББББ";
    private static final String VALUE_WITH_SINGLE_BYTE = "v";

    private static final int MAP_SIZE_LIMIT = 50;
    private static final int MAP_KEY_LIMIT = 10;
    private static final int MAP_VALUE_LIMIT = 20;

    public HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmerTest(Map<String, String> inputMap,
                                                                             Map<String, String> expectedMap,
                                                                             int pairsTruncated,
                                                                             int bytesTruncated,
                                                                             String description) {
        this.inputMap = inputMap;
        this.expectedMap = expectedMap;
        this.pairsTruncated = pairsTruncated;
        this.bytesTruncated = bytesTruncated;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, null, 0, 0, "null"},
                {Collections.emptyMap(), Collections.emptyMap(), 0, 0, "empty map"},
                {
                        TestUtils.mapOf(TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES)),
                        TestUtils.mapOf(TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES)),
                        0,
                        0,
                        "map with single short pair"
                },
                {
                        TestUtils.mapOf(
                                TestUtils.pair(
                                        KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS,
                                        VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS
                                )
                        ),
                        TestUtils.mapOf(
                                TestUtils.pair(
                                        KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS,
                                        VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS
                                )
                        ),
                        0,
                        0,
                        "map with single short pair with cyrillic chars"
                },
                {
                        TestUtils.mapOf(TestUtils.pair(KEY_WITH_20_BYTES_1, VALUE_WITH_30_BYTES)),
                        TestUtils.mapOf(TestUtils.pair(TRUNCATED_KEY_WITH_20_BYTES_1, TRUNCATED_VALUE_WITH_30_BYTES)),
                        0,
                        20,
                        "map with single large pair"
                },
                {
                        TestUtils.mapOf(
                                TestUtils.pair(
                                        KEY_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS,
                                        VALUE_WITH_35_BYTES_INCLUDING_CYRILLIC_CHARS
                                )
                        ),
                        TestUtils.mapOf(
                                TestUtils.pair(
                                        KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS,
                                        VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS
                                )
                        ),
                        0,
                        15,
                        "map with single large pair with cyrillic chars"
                },
                {
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_2, VALUE_WITH_11_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_3, VALUE_WITH_12_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_4, VALUE_WITH_13_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_5, VALUE_WITH_14_BYTES)
                        ),
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_2, VALUE_WITH_11_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_3, VALUE_WITH_12_BYTES)
                        ),
                        2,
                        37,
                        "map with 2 small items out of map size limit"
                },
                {
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_5_BYTES_4, VALUE_WITH_13_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_2, VALUE_WITH_11_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_3, VALUE_WITH_12_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_5, VALUE_WITH_14_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES)
                        ),
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_2, VALUE_WITH_11_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_3, VALUE_WITH_12_BYTES)
                        ),
                        2,
                        37,
                        "map with 2 small items out of map size limit in non sorted order"
                },
                {
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_5_BYTES_4, VALUE_WITH_13_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_2, VALUE_WITH_11_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_3, VALUE_WITH_12_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_5, VALUE_WITH_14_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(KEY_WITH_1_BYTE, null)
                        ),
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_1_BYTE, null),
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_2, VALUE_WITH_11_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_3, VALUE_WITH_12_BYTES)
                        ),
                        2,
                        37,
                        "map with null-value and some items out of map size limit"
                },
                {
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_5_BYTES_4, VALUE_WITH_13_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_2, VALUE_WITH_11_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_3, VALUE_WITH_12_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_5, VALUE_WITH_14_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(KEY_WITH_1_BYTE, "")
                        ),
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_1_BYTE, ""),
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_2, VALUE_WITH_11_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_3, VALUE_WITH_12_BYTES)
                        ),
                        2,
                        37,
                        "map with single empty value and some items out of map limit"
                },
                {
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_20_BYTES_1, VALUE_WITH_30_BYTES),
                                TestUtils.pair(KEY_WITH_20_BYTES_2, VALUE_WITH_31_BYTES),
                                TestUtils.pair(KEY_WITH_20_BYTES_3, VALUE_WITH_32_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(KEY_WITH_1_BYTE, "")
                        ),
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_1_BYTE, ""),
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(TRUNCATED_KEY_WITH_20_BYTES_1, TRUNCATED_VALUE_WITH_30_BYTES)
                        ),
                        2,
                        123,
                        "map with some pairs out of limit"
                },
                {
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_20_BYTES_1, VALUE_WITH_30_BYTES),
                                TestUtils.pair(KEY_WITH_20_BYTES_2, VALUE_WITH_31_BYTES),
                                TestUtils.pair(KEY_WITH_20_BYTES_3, VALUE_WITH_32_BYTES),
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES),
                                TestUtils.pair(KEY_WITH_1_BYTE, ""),
                                TestUtils.pair(
                                        KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS,
                                        VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS
                                ),
                                TestUtils.pair(
                                        KEY_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS,
                                        VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS
                                )
                        ),
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_1_BYTE, ""),
                                TestUtils.pair(KEY_WITH_5_BYTES_1, VALUE_WITH_10_BYTES)
                        ),
                        5,
                        243,
                        "map with some pairs out of limit including cyrillic"
                },
                {
                        TestUtils.mapOf(
                                TestUtils.pair(KEY_WITH_COLLISION_1, VALUE_WITH_SINGLE_BYTE),
                                TestUtils.pair(KEY_WITH_COLLISION_2, VALUE_WITH_SINGLE_BYTE),
                                TestUtils.pair(KEY_WITH_COLLISION_3, VALUE_WITH_SINGLE_BYTE)
                        ),
                        TestUtils.mapOf(
                                TestUtils.pair(TRUNCATED_KEY_WITH_COLLISION, VALUE_WITH_SINGLE_BYTE)
                        ),
                        0,
                        3,
                        "map with truncated key collision"
                }
        });
    }

    @Mock
    private HierarchicalStringTrimmer keyTrimmer;
    @Mock
    private HierarchicalStringTrimmer valueTrimmer;

    private HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer mMapTrimmer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(keyTrimmer.trim(nullable(String.class)))
                .then(new Answer<TrimmingResult<String, BytesTruncatedProvider>>() {
            @Override
            public TrimmingResult<String, BytesTruncatedProvider> answer(InvocationOnMock invocation) throws Throwable {
                return trimToLimit(invocation.getArgument(0).toString(), MAP_KEY_LIMIT);
            }
        });

        when(valueTrimmer.trim(nullable(String.class)))
                .then(new Answer<TrimmingResult<String, BytesTruncatedProvider>>() {
            @Override
            public TrimmingResult<String, BytesTruncatedProvider> answer(InvocationOnMock invocation) throws Throwable {
                return trimToLimit((String) invocation.getArgument(0), MAP_VALUE_LIMIT);
            }
        });

        mMapTrimmer = new HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer(
                MAP_SIZE_LIMIT,
                keyTrimmer,
                valueTrimmer
        );
    }

    private TrimmingResult<String, BytesTruncatedProvider> trimToLimit(String input, int limit) {
        String result = input;
        int truncated = 0;
        if (input != null && input.length() > limit) {
            result = input.substring(0, limit);
            truncated = input.length() - result.length();
        }
        return new TrimmingResult<String, BytesTruncatedProvider>(result, new BytesTruncatedInfo(truncated));
    }

    @Test
    public void trim() throws Exception {
        TrimmingResult<Map<String, String>, CollectionTrimInfo> trimmingResult =
                mMapTrimmer.trim(inputMap);

        ObjectPropertyAssertions<TrimmingResult<Map<String, String>, CollectionTrimInfo>> assertions =
                ObjectPropertyAssertions(trimmingResult);

        assertions.checkField("value", expectedMap);
        assertions.checkFieldComparingFieldByField(
                "metaInfo",
                new CollectionTrimInfo(pairsTruncated, bytesTruncated)
        );

        assertions.checkAll();
    }
}
