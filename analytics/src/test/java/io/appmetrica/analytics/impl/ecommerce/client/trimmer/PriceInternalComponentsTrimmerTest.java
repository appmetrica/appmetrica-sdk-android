package io.appmetrica.analytics.impl.ecommerce.client.trimmer;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.AmountWrapper;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class PriceInternalComponentsTrimmerTest extends CommonTest {

    private final List<AmountWrapper> inputInternalComponents;
    private final List<AmountWrapper> expectedInternalComponents;
    private final int expectedItemsDropped;
    private final int expectedBytesTruncated;

    public PriceInternalComponentsTrimmerTest(List<AmountWrapper> inputInternalComponents,
                                              List<AmountWrapper> expectedInternalComponents,
                                              int expectedItemsDropped,
                                              int expectedBytesTruncated,
                                              String description) {
        this.inputInternalComponents = inputInternalComponents;
        this.expectedInternalComponents = expectedInternalComponents;
        this.expectedItemsDropped = expectedItemsDropped;
        this.expectedBytesTruncated = expectedBytesTruncated;
    }

    private static final AmountWrapper AMOUNT_IN_LIMIT_1 = amountOf("1243", "USD");
    private static final AmountWrapper AMOUNT_IN_LIMIT_2 = amountOf("12334", "USD");
    private static final AmountWrapper AMOUNT_IN_LIMIT_3 = amountOf("3442", "BTC");
    private static final AmountWrapper AMOUNT_IN_LIMIT_4 = amountOf("6.3242", "BYN");
    private static final AmountWrapper AMOUNT_IN_LIMIT_5 = amountOf("5.131", "RUB");
    private static final AmountWrapper AMOUNT_OUT_OF_LIMIT_1 = amountOf("6.3134341232", "EUR");
    private static final AmountWrapper AMOUNT_OUT_OF_LIMIT_2 = amountOf("61090", "BTC");
    private static final AmountWrapper AMOUNT_OUT_OF_LIMIT_3 = amountOf("100", "USD");
    private static final AmountWrapper AMOUNT_OUT_OF_LIMIT_4 = amountOf("100500", "EUR");
    private static final AmountWrapper ANOUNT_OUT_OF_LIMIT_5 = amountOf("200500", "USD");

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {null, null, 0, 0, "with null internal components"},
            //#2
            {Collections.emptyList(), Collections.emptyList(), 0, 0, "with empty internal components"},
            //#3
            {
                Collections.singletonList(AMOUNT_IN_LIMIT_1),
                Collections.singletonList(AMOUNT_IN_LIMIT_1),
                0, 0,
                "with single in-limit internal component"
            },
            //#4
            {
                listOf(AMOUNT_IN_LIMIT_1, AMOUNT_IN_LIMIT_2, AMOUNT_IN_LIMIT_3, AMOUNT_IN_LIMIT_4,
                    AMOUNT_IN_LIMIT_5),
                listOf(AMOUNT_IN_LIMIT_1, AMOUNT_IN_LIMIT_2, AMOUNT_IN_LIMIT_3, AMOUNT_IN_LIMIT_4,
                    AMOUNT_IN_LIMIT_5),
                0, 0,
                "with full (5) in-limit internal components"
            },
            //#5
            {
                listOf(AMOUNT_IN_LIMIT_1, AMOUNT_IN_LIMIT_2, AMOUNT_IN_LIMIT_3, AMOUNT_IN_LIMIT_4,
                    AMOUNT_IN_LIMIT_5, AMOUNT_OUT_OF_LIMIT_1),
                listOf(AMOUNT_IN_LIMIT_1, AMOUNT_IN_LIMIT_2, AMOUNT_IN_LIMIT_3, AMOUNT_IN_LIMIT_4,
                    AMOUNT_IN_LIMIT_5),
                1, 15,
                "with single out-of-limit internal component"
            },
            //#6
            {
                listOf(AMOUNT_IN_LIMIT_1, AMOUNT_IN_LIMIT_2, AMOUNT_IN_LIMIT_3, AMOUNT_IN_LIMIT_4,
                    AMOUNT_IN_LIMIT_5, AMOUNT_OUT_OF_LIMIT_1, AMOUNT_OUT_OF_LIMIT_2, AMOUNT_OUT_OF_LIMIT_3,
                    AMOUNT_OUT_OF_LIMIT_4, ANOUNT_OUT_OF_LIMIT_5),
                listOf(AMOUNT_IN_LIMIT_1, AMOUNT_IN_LIMIT_2, AMOUNT_IN_LIMIT_3, AMOUNT_IN_LIMIT_4,
                    AMOUNT_IN_LIMIT_5),
                5,
                sizeOf(AMOUNT_OUT_OF_LIMIT_1, AMOUNT_OUT_OF_LIMIT_2, AMOUNT_OUT_OF_LIMIT_3,
                    AMOUNT_OUT_OF_LIMIT_4, ANOUNT_OUT_OF_LIMIT_5),
                "with 5 out-of-limit internal components"
            },
            //7
            {
                listOf(AMOUNT_IN_LIMIT_1, AMOUNT_IN_LIMIT_2, AMOUNT_IN_LIMIT_3, AMOUNT_IN_LIMIT_4,
                    AMOUNT_IN_LIMIT_5, null, null),
                listOf(AMOUNT_IN_LIMIT_1, AMOUNT_IN_LIMIT_2, AMOUNT_IN_LIMIT_3, AMOUNT_IN_LIMIT_4,
                    AMOUNT_IN_LIMIT_5),
                2,
                0,
                "with 2 out-of-limit null internal components"
            }
        });
    }

    private static List<AmountWrapper> listOf(AmountWrapper... amounts) {
        List<AmountWrapper> amountList = new ArrayList<AmountWrapper>();
        Collections.addAll(amountList, amounts);
        return amountList;
    }

    private static AmountWrapper amountOf(String amount, String unit) {
        return new AmountWrapper(new BigDecimal(amount), unit);
    }

    private static int sizeOf(AmountWrapper... amounts) {
        int size = 0;
        for (AmountWrapper amount : amounts) {
            size += 12 + amount.unit.getBytes().length;
        }
        return size;
    }

    public final int internalComponentsLimit = 5;

    @Test
    public void trim() throws Exception {
        PriceHierarchicalComponentsTrimmer trimmer = new PriceHierarchicalComponentsTrimmer(internalComponentsLimit);
        TrimmingResult<List<AmountWrapper>, CollectionTrimInfo> trimmingResult =
            trimmer.trim(inputInternalComponents);

        ObjectPropertyAssertions<TrimmingResult<List<AmountWrapper>, CollectionTrimInfo>> assertions =
            ObjectPropertyAssertions(trimmingResult);

        assertions.checkField("value", expectedInternalComponents, true);
        assertions.checkFieldComparingFieldByField(
            "metaInfo",
            new CollectionTrimInfo(expectedItemsDropped, expectedBytesTruncated)
        );

        assertions.checkAll();
    }
}
