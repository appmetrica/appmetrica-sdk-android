package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.AmountWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringTrimmer;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.math.BigDecimal;
import java.util.function.Consumer;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.when;

public class AmountConverterTest extends CommonTest {

    @Mock
    private DecimalConverter decimalConverter;
    @Mock
    private Ecommerce.ECommerceEvent.Decimal protoAmountValue;
    @Mock
    private BigDecimal amount;
    @Mock
    private HierarchicalStringTrimmer internalStringTrimmer;

    private AmountConverter amountConverter;

    private final String unit = "Unit";
    private final String truncatedUnit = "Truncated unit";
    private final int bytesTruncated = 20;
    private final TrimmingResult<String, BytesTruncatedProvider> unitTruncationTrimmingResult =
        new TrimmingResult<String, BytesTruncatedProvider>(
            truncatedUnit,
            new BytesTruncatedInfo(bytesTruncated)
        );

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(decimalConverter.fromModel(amount)).thenReturn(protoAmountValue);
        amountConverter = new AmountConverter(decimalConverter, internalStringTrimmer);
    }

    @Test
    public void constructor() throws Exception {
        amountConverter = new AmountConverter();

        ObjectPropertyAssertions<AmountConverter> assertions =
            ObjectPropertyAssertions(amountConverter)
                .withPrivateFields(true);

        assertions.checkFieldNonNull("decimalConverter");
        assertions.checkFieldComparingFieldByField("currencyTrimmer", new HierarchicalStringTrimmer(20));

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        when(internalStringTrimmer.trim(unit)).thenReturn(unitTruncationTrimmingResult);

        AmountWrapper amountWrapper = new AmountWrapper(amount, unit);
        Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider> result = amountConverter.fromModel(amountWrapper);

        ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider>> assertions =
            ObjectPropertyAssertions(result);

        assertions.checkFieldRecursively("metaInfo", new TruncationInfoConsumer(bytesTruncated));
        assertions.checkFieldRecursively(
            "result",
            new Consumer<ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent.Amount,
                BytesTruncatedProvider>>>() {
                @Override
                public void accept(ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent.Amount,
                    BytesTruncatedProvider>> innerAssertions) {
                    try {
                        innerAssertions.withFinalFieldOnly(false)
                            .checkField("value", protoAmountValue)
                            .checkField("unitType", truncatedUnit.getBytes());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        );

        assertions.checkAll();
    }

    @Test
    public void toProtoWithoutTruncation() {
        when(internalStringTrimmer.trim(unit))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(unit, new BytesTruncatedInfo(0)));
        AmountWrapper amountWrapper = new AmountWrapper(amount, unit);
        Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider> result = amountConverter.fromModel(amountWrapper);

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(result.result.unitType).as("unit").isEqualTo(unit.getBytes());
        assertions.assertThat(result.getBytesTruncated()).as("meta info").isZero();

        assertions.assertAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() throws Exception {
        amountConverter.toModel(new Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider>(
            new Ecommerce.ECommerceEvent.Amount(),
            new BytesTruncatedInfo(0)
        ));
    }
}
