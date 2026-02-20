package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.AmountWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.PriceWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.trimmer.PriceHierarchicalComponentsTrimmer;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PriceConverterTest extends CommonTest {

    @Mock
    private AmountWrapper fiatAmount;
    @Mock
    private AmountWrapper internalAmount1;
    @Mock
    private AmountWrapper internalAmount2;
    @Mock
    private AmountWrapper internalAmount3;
    @Mock
    private Ecommerce.ECommerceEvent.Amount protoFiatAmount;
    @Mock
    private Ecommerce.ECommerceEvent.Amount protoInternalAmount1;
    @Mock
    private Ecommerce.ECommerceEvent.Amount protoInternalAmount2;
    @Mock
    private Ecommerce.ECommerceEvent.Amount protoInternalAmount3;
    @Mock
    private AmountConverter amountConverter;
    @Mock
    private PriceHierarchicalComponentsTrimmer priceInternalComponentsTrimmer;

    private final List<AmountWrapper> inputInternalPriceComponents = Collections.singletonList(mock(AmountWrapper.class));

    private List<AmountWrapper> internalPriceComponents;
    private Ecommerce.ECommerceEvent.Amount[] protoInternalComponents;
    private PriceWrapper priceWrapper;

    private PriceConverter priceConverter;

    private final int fiatBytesTruncated = 0;
    private final int internalAmount1BytesTruncated = 1;
    private final int internalAmount2BytesTruncated = 53;
    private final int internalAmount3BytesTruncated = 642;
    private final int internalComponentsBytesTruncated = 1240;

    private final int internalComponentsDropped = 15;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(amountConverter.fromModel(fiatAmount))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider>(
                protoFiatAmount,
                new BytesTruncatedInfo(fiatBytesTruncated)
            ));
        when(amountConverter.fromModel(internalAmount1))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider>(
                protoInternalAmount1,
                new BytesTruncatedInfo(internalAmount1BytesTruncated)
            ));
        when(amountConverter.fromModel(internalAmount2))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider>(
                protoInternalAmount2,
                new BytesTruncatedInfo(internalAmount2BytesTruncated)
            ));
        when(amountConverter.fromModel(internalAmount3))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider>(
                protoInternalAmount3,
                new BytesTruncatedInfo(internalAmount3BytesTruncated)
            ));

        internalPriceComponents = Arrays.asList(internalAmount1, internalAmount2, internalAmount3);

        when(priceInternalComponentsTrimmer.trim(inputInternalPriceComponents))
            .thenReturn(new TrimmingResult<List<AmountWrapper>, CollectionTrimInfo>(
                internalPriceComponents,
                new CollectionTrimInfo(
                    internalComponentsDropped,
                    internalComponentsBytesTruncated
                )
            ));

        protoInternalComponents = new Ecommerce.ECommerceEvent.Amount[]{
            protoInternalAmount1,
            protoInternalAmount2,
            protoInternalAmount3
        };

        priceConverter = new PriceConverter(amountConverter, priceInternalComponentsTrimmer);
    }

    @Test
    public void constructor() throws Exception {
        priceConverter = new PriceConverter();

        ObjectPropertyAssertions<PriceConverter> assertions =
            ObjectPropertyAssertions(priceConverter)
                .withPrivateFields(true);

        assertions.checkFieldNonNull("amountConverter");
        assertions.checkFieldComparingFieldByField(
            "priceInternalComponentsTrimmer",
            new PriceHierarchicalComponentsTrimmer(30)
        );

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        priceWrapper = new PriceWrapper(fiatAmount, inputInternalPriceComponents);
        Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider> priceResult =
            priceConverter.fromModel(priceWrapper);

        Ecommerce.ECommerceEvent.Price expectedPrice = new Ecommerce.ECommerceEvent.Price();
        expectedPrice.fiat = protoFiatAmount;
        expectedPrice.internalComponents = protoInternalComponents;

        ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>> assertions =
            ObjectPropertyAssertions(
                priceResult
            ).checkFieldComparingFieldByFieldRecursively("result", expectedPrice)
                .checkFieldRecursively(
                    "metaInfo",
                    new TruncationInfoConsumer(
                        fiatBytesTruncated + internalComponentsBytesTruncated +
                            internalAmount1BytesTruncated +
                            internalAmount2BytesTruncated + internalAmount3BytesTruncated
                    )
                );

        assertions.checkAll();
    }

    @Test
    public void toProtoWithoutInternalComponents() throws Exception {
        priceWrapper = new PriceWrapper(fiatAmount, null);
        when(priceInternalComponentsTrimmer.trim(nullable(List.class)))
            .thenReturn(new TrimmingResult<List<AmountWrapper>, CollectionTrimInfo>(
                null,
                new CollectionTrimInfo(0, 0)
            ));
        assertPriceWithoutInternalComponents(priceConverter.fromModel(priceWrapper));

    }

    @Test
    public void toProtoWithEmptyInternalComponents() throws Exception {
        priceWrapper = new PriceWrapper(fiatAmount, new ArrayList<AmountWrapper>());
        when(priceInternalComponentsTrimmer.trim(any(List.class)))
            .thenReturn(new TrimmingResult<List<AmountWrapper>, CollectionTrimInfo>(
                new ArrayList<AmountWrapper>(),
                new CollectionTrimInfo(0, 0)
            ));
        assertPriceWithoutInternalComponents(priceConverter.fromModel(priceWrapper));
    }

    private void assertPriceWithoutInternalComponents(
        Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider> priceResult
    ) throws Exception {
        Ecommerce.ECommerceEvent.Price expectedPrice = new Ecommerce.ECommerceEvent.Price();
        expectedPrice.fiat = protoFiatAmount;
        expectedPrice.internalComponents = new Ecommerce.ECommerceEvent.Amount[0];

        ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>> assertions =
            ObjectPropertyAssertions(
                priceResult
            )
                .checkFieldComparingFieldByFieldRecursively("result", expectedPrice)
                .checkFieldRecursively("metaInfo", new TruncationInfoConsumer(0));

        assertions.checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() throws Exception {
        priceConverter.toModel(
            new Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>(
                new Ecommerce.ECommerceEvent.Price(),
                new BytesTruncatedInfo(0)
            )
        );
    }
}
