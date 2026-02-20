package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(Parameterized.class)
public class HierarchicalListTrimmerWithoutItemTrimmerTest extends CommonTest {

    private static class Item {
        final int size;

        public Item(int size) {
            this.size = size;
        }
    }

    private final List<Item> inputValue;
    private final List<Item> expectedValue;
    private final int expectedDroppedItems;
    private final int expectedBytesTruncated;

    public HierarchicalListTrimmerWithoutItemTrimmerTest(List<Item> inputValue,
                                                         List<Item> expectedValue,
                                                         int expectedDroppedItems,
                                                         int expectedBytesTruncated,
                                                         String description) {
        this.inputValue = inputValue;
        this.expectedValue = expectedValue;
        this.expectedDroppedItems = expectedDroppedItems;
        this.expectedBytesTruncated = expectedBytesTruncated;
    }

    private static final Item UP_TO_LIST_SIZE_LIMIT_ITEM_1 = new Item(7);
    private static final Item UP_TO_LIST_SIZE_LIMIT_ITEM_2 = new Item(3);
    private static final Item UP_TO_LIST_SIZE_LIMIT_ITEM_3 = new Item(4);
    private static final Item UP_TO_LIST_SIZE_LIMIT_ITEM_4 = new Item(7);
    private static final Item UP_TO_LIST_SIZE_LIMIT_ITEM_5 = new Item(11);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_ITEM_1 = new Item(15);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_ITEM_2 = new Item(0);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_ITEM_3 = new Item(1);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_ITEM_4 = new Item(6);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_ITEM_5 = new Item(8);

    @Parameters(name = "#{index} - {4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {null, null, 0, 0, "null"},
            {Collections.emptyList(), Collections.emptyList(), 0, 0, "empty list"},
            {
                Collections.singletonList(UP_TO_LIST_SIZE_LIMIT_ITEM_1),
                Collections.singletonList(UP_TO_LIST_SIZE_LIMIT_ITEM_1), 0, 0,
                "list with single item out of length limit"
            },
            {
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_3, UP_TO_LIST_SIZE_LIMIT_ITEM_4,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_5
                ),
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_3, UP_TO_LIST_SIZE_LIMIT_ITEM_4,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_5
                ),
                0,
                0,
                "list with items up to items count limit"
            },
            {
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_3, UP_TO_LIST_SIZE_LIMIT_ITEM_4,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_5, OUT_OF_LIST_SIZE_LIMIT_ITEM_1
                ),
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_3, UP_TO_LIST_SIZE_LIMIT_ITEM_4,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_5
                ),
                1,
                sizeOf(OUT_OF_LIST_SIZE_LIMIT_ITEM_1),
                "list with single item out of items count limit"
            },
            {
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_3, UP_TO_LIST_SIZE_LIMIT_ITEM_4,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_5, OUT_OF_LIST_SIZE_LIMIT_ITEM_1,
                    OUT_OF_LIST_SIZE_LIMIT_ITEM_2, OUT_OF_LIST_SIZE_LIMIT_ITEM_3,
                    OUT_OF_LIST_SIZE_LIMIT_ITEM_4, OUT_OF_LIST_SIZE_LIMIT_ITEM_5
                ),
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_3, UP_TO_LIST_SIZE_LIMIT_ITEM_4,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_5
                ),
                5,
                sizeOf(
                    OUT_OF_LIST_SIZE_LIMIT_ITEM_1, OUT_OF_LIST_SIZE_LIMIT_ITEM_2,
                    OUT_OF_LIST_SIZE_LIMIT_ITEM_3, OUT_OF_LIST_SIZE_LIMIT_ITEM_4,
                    OUT_OF_LIST_SIZE_LIMIT_ITEM_5
                ),
                "list with 5 items out of items count limit"
            },
        });
    }

    private static List<Item> listOf(Item... items) {
        return Arrays.asList(items);
    }

    private static int sizeOf(Item... items) {
        int result = 0;
        for (Item item : items) {
            result += item.size;
        }
        return result;
    }

    private final int limit = 5;

    @Test
    public void trim() throws Exception {
        HierarchicalListTrimmer<Item> trimmer = new HierarchicalListTrimmer<Item>(limit);
        assertTrimmedResultMathExpectedValues(trimmer, 0);
    }

    @Test
    public void trimWithSizeOfOverriding() throws Exception {
        HierarchicalListTrimmer<Item> trimmer = new HierarchicalListTrimmer<Item>(limit) {
            @Override
            protected int byteSizeOf(Item entity) {
                return entity.size;
            }
        };
        assertTrimmedResultMathExpectedValues(trimmer, expectedBytesTruncated);
    }

    private void assertTrimmedResultMathExpectedValues(HierarchicalListTrimmer<Item> trimmer,
                                                       int bytesTruncated) throws Exception {

        TrimmingResult<List<Item>, CollectionTrimInfo> trimmingResult =
            trimmer.trim(inputValue);

        ObjectPropertyAssertions<TrimmingResult<List<Item>, CollectionTrimInfo>> assertions =
            ObjectPropertyAssertions(
                trimmingResult
            );

        assertions.checkField("value", expectedValue, true);
        assertions.checkFieldComparingFieldByField(
            "metaInfo",
            new CollectionTrimInfo(expectedDroppedItems, bytesTruncated)
        );

        assertions.checkAll();
    }
}
