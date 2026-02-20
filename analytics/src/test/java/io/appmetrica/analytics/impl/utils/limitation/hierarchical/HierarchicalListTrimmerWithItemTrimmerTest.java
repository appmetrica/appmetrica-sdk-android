package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.testutils.CollectionTrimInfoConsumer;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class HierarchicalListTrimmerWithItemTrimmerTest extends CommonTest {

    private static class Item {
        final int size;
        final int truncatedSize;
        final Item truncatedItem;

        public Item(int size, int truncatedSize) {
            this.size = size;
            this.truncatedSize = truncatedSize;
            truncatedItem = truncatedSize == 0 ? this : new Item(this);
        }

        private Item(Item item) {
            this.size = item.size;
            this.truncatedSize = item.truncatedSize;
            this.truncatedItem = null;
        }
    }

    private final List<Item> inputList;
    private final List<Item> expectedList;
    private final int expectedDroppedItems;
    private final int expectedBytesTruncatedWithoutSizeOverriding;
    private final int expectedBytesTruncatedWithSizeOverriding;

    public HierarchicalListTrimmerWithItemTrimmerTest(List<Item> inputList,
                                                      List<Item> expectedList,
                                                      int expectedDroppedItems,
                                                      int expectedBytesTruncatedWithoutSizeOverriding,
                                                      int expectedBytesTruncatedWithSizeOverriding,
                                                      String description) {
        this.inputList = inputList;
        this.expectedList = expectedList;
        this.expectedDroppedItems = expectedDroppedItems;
        this.expectedBytesTruncatedWithoutSizeOverriding = expectedBytesTruncatedWithoutSizeOverriding;
        this.expectedBytesTruncatedWithSizeOverriding = expectedBytesTruncatedWithSizeOverriding;
    }

    private static final Item UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1 = new Item(20, 10);
    private static final Item UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2 = new Item(15, 3);
    private static final Item UP_TO_LIST_SIZE_LIMIT_ITEM_1 = new Item(44, 0);
    private static final Item UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3 = new Item(18, 12);
    private static final Item UP_TO_LIST_SIZE_LIMIT_ITEM_2 = new Item(26, 0);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1 = new Item(22, 19);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_ITEM_1 = new Item(23, 0);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2 = new Item(11, 4);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3 = new Item(84, 56);
    private static final Item OUT_OF_LIST_SIZE_LIMIT_ITEM_2 = new Item(31, 0);

    static final int LIST_SIZE_LIMIT = 5;

    @Parameters(name = "#{index} - {5}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {null, null, 0, 0, 0, "null"},
            {Collections.emptyList(), Collections.emptyList(), 0, 0, 0, "empty list"},
            {
                Collections.singletonList(UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1),
                Collections.singletonList(UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1.truncatedItem),
                0,
                UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1.truncatedSize,
                UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1.truncatedSize,
                "list with single item out of length limit"
            },
            {
                Collections.singletonList(UP_TO_LIST_SIZE_LIMIT_ITEM_1),
                Collections.singletonList(UP_TO_LIST_SIZE_LIMIT_ITEM_1.truncatedItem),
                0,
                0,
                UP_TO_LIST_SIZE_LIMIT_ITEM_1.truncatedSize,
                "list with single item within item length limit"
            },
            {
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2
                ),
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2.truncatedItem
                ),
                0,
                truncatedSizeOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2
                ),
                truncatedSizeOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2
                ),
                "list with items within item length limit"
            },
            {
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2, OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1
                ),
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2.truncatedItem
                ),
                1,
                truncatedSizeOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2
                ),
                truncatedSizeOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2
                ) + sizeOf(OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1),
                "list with single item out of items count limit"
            },
            {
                listOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2, OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1,
                    OUT_OF_LIST_SIZE_LIMIT_ITEM_1, OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3, OUT_OF_LIST_SIZE_LIMIT_ITEM_2
                ),
                listOf(UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3.truncatedItem,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2.truncatedItem
                ),
                5,
                truncatedSizeOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2
                ),
                truncatedSizeOf(
                    UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_1, UP_TO_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    UP_TO_LIST_SIZE_LIMIT_ITEM_2
                ) + sizeOf(
                    OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_1, OUT_OF_LIST_SIZE_LIMIT_ITEM_1,
                    OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_2, OUT_OF_LIST_SIZE_LIMIT_TRUNCATED_ITEM_3,
                    OUT_OF_LIST_SIZE_LIMIT_ITEM_2
                ),
                "list with 5 items out of items count limit"
            }
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

    private static int truncatedSizeOf(Item... items) {
        int result = 0;
        for (Item item : items) {
            result += item.truncatedSize;
        }
        return result;
    }

    @Mock
    private HierarchicalTrimmer<Item, BytesTruncatedProvider> itemTrimmer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(itemTrimmer.trim(any(Item.class)))
            .then(new Answer<TrimmingResult<Item, BytesTruncatedProvider>>() {
                @Override
                public TrimmingResult<Item, BytesTruncatedProvider> answer(InvocationOnMock invocation) throws Throwable {
                    Item item = invocation.getArgument(0);
                    return new TrimmingResult<Item, BytesTruncatedProvider>(
                        item.truncatedItem,
                        new BytesTruncatedInfo(item.truncatedSize)
                    );
                }
            });
    }

    @Test
    public void trimWithoutSizeOfOverriding() throws Exception {
        HierarchicalListTrimmer<Item> trimmer = new HierarchicalListTrimmer<Item>(LIST_SIZE_LIMIT, itemTrimmer);
        assertThatTrimmedResultMatchExpectedValue(trimmer, expectedBytesTruncatedWithoutSizeOverriding);
    }

    @Test
    public void trimWithSizeOfOverriding() throws Exception {
        HierarchicalListTrimmer<Item> trimmer = new HierarchicalListTrimmer<Item>(LIST_SIZE_LIMIT, itemTrimmer) {
            @Override
            protected int byteSizeOf(Item entity) {
                return entity.size;
            }
        };
        assertThatTrimmedResultMatchExpectedValue(trimmer, expectedBytesTruncatedWithSizeOverriding);
    }

    private void assertThatTrimmedResultMatchExpectedValue(HierarchicalListTrimmer<Item> trimmer,
                                                           int bytesTruncated) throws Exception {

        TrimmingResult<List<Item>, CollectionTrimInfo> trimmingResult =
            trimmer.trim(inputList);

        ObjectPropertyAssertions(trimmingResult)
            .checkField("value", expectedList, true)
            .checkFieldRecursively(
                "metaInfo",
                new CollectionTrimInfoConsumer(bytesTruncated, expectedDroppedItems)
            )
            .checkAll();
    }
}
