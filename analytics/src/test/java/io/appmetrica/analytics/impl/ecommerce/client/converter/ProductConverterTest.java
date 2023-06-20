package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.PriceWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProductWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringTrimmer;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ProductConverterTest extends CommonTest {

    @Mock
    private PriceConverter priceConverter;
    @Mock
    private PayloadConverter payloadConverter;
    @Mock
    private CategoryConverter categoryConverter;
    @Mock
    private PromocodesConverter promocodesConverter;
    @Mock
    private PriceWrapper originalPrice;
    @Mock
    private Ecommerce.ECommerceEvent.Price originalPriceProto;
    @Mock
    private PriceWrapper actualPrice;
    @Mock
    private Ecommerce.ECommerceEvent.Price actualPriceProto;
    @Mock
    private Map<String, String> payload;
    @Mock
    private Ecommerce.ECommerceEvent.Category categoryProto;
    @Mock
    private Ecommerce.ECommerceEvent.Payload payloadProto;
    @Mock
    private Ecommerce.ECommerceEvent.PromoCode promocodeProto1;
    @Mock
    private Ecommerce.ECommerceEvent.PromoCode promocodeProto2;
    @Mock
    private Ecommerce.ECommerceEvent.PromoCode promocodeProto3;
    @Mock
    private HierarchicalStringTrimmer skuTrimmer;
    @Mock
    private HierarchicalStringTrimmer nameTrimmer;

    private Ecommerce.ECommerceEvent.PromoCode[] promocodesProto =
            new Ecommerce.ECommerceEvent.PromoCode[]{promocodeProto1, promocodeProto2, promocodeProto3};

    private ProductConverter productConverter;
    private ProductWrapper productWrapper;
    private Ecommerce.ECommerceEvent.Product productProto;

    private List<String> categories = Arrays.asList("First", "Second", "Third");
    private List<String> promocodes = Arrays.asList("Promocode#1", "Promocode#2", "Promocode#3");

    private final int skuBytesTruncated = 14;
    private final int nameBytesTruncated = 29;
    private final int payloadBytesTruncated = 120;
    private final int categoryBytesTruncated = 80;
    private final int originalPriceBytesTruncated = 50;
    private final int actualPriceBytesTruncated = 200;
    private final int promocodesBytesTruncated = 35;

    private final int totalBytesTruncated = skuBytesTruncated + nameBytesTruncated + payloadBytesTruncated +
            categoryBytesTruncated + originalPriceBytesTruncated + actualPriceBytesTruncated + promocodesBytesTruncated;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(payloadConverter.fromModel(payload))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider>(
                        payloadProto,
                        new BytesTruncatedInfo(payloadBytesTruncated)
                ));
        when(priceConverter.fromModel(originalPrice))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>(
                        originalPriceProto,
                        new BytesTruncatedInfo(originalPriceBytesTruncated)
                ));
        when(priceConverter.fromModel(actualPrice))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>(
                        actualPriceProto,
                        new BytesTruncatedInfo(actualPriceBytesTruncated)
                ));
        when(categoryConverter.fromModel(categories))
                .thenReturn(
                        new Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider>(
                                categoryProto,
                                new BytesTruncatedInfo(categoryBytesTruncated)
                        )
                );
        when(promocodesConverter.fromModel(promocodes))
                .thenReturn(
                        new Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider>(
                                promocodesProto,
                                new BytesTruncatedInfo(promocodesBytesTruncated)
                        )
                );

        productConverter = new ProductConverter(
                payloadConverter,
                priceConverter,
                categoryConverter,
                promocodesConverter,
                skuTrimmer,
                nameTrimmer
        );
    }

    @Test
    public void constructor() throws Exception {
        productConverter = new ProductConverter();

        ObjectPropertyAssertions<ProductConverter> assertions =
                ObjectPropertyAssertions(productConverter)
                        .withPrivateFields(true);

        assertions.checkFieldNonNull("payloadConverter");
        assertions.checkFieldNonNull("priceConverter");
        assertions.checkFieldNonNull("categoryConverter");
        assertions.checkFieldNonNull("promocodesConverter");
        assertions.checkFieldComparingFieldByField("skuTrimmer", new HierarchicalStringTrimmer(100));
        assertions.checkFieldComparingFieldByField("nameTrimmer", new HierarchicalStringTrimmer(1000));

        assertions.checkAll();
    }

    @Test
    public void toProtoWithTruncation() throws Exception {
        String inputSku = "input sku";
        String truncatedSku = "truncated sku";
        String inputName = "input name";
        String truncatedName = "truncated name";

        when(skuTrimmer.trim(inputSku))
                .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                        truncatedSku,
                        new BytesTruncatedInfo(skuBytesTruncated)
                ));
        when(nameTrimmer.trim(inputName))
                .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                        truncatedName,
                        new BytesTruncatedInfo(nameBytesTruncated)
                ));

        productWrapper = new ProductWrapper(
                inputSku, inputName, categories, payload, actualPrice, originalPrice, promocodes
        );

        Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider> productResult =
                productConverter.fromModel(productWrapper);

        ObjectPropertyAssertions(
                productResult)
                .checkFieldRecursively(
                        "metaInfo",
                        new TruncationInfoConsumer(totalBytesTruncated)
                )
                .checkFieldRecursively(
                        "result",
                        productMatchAssertionsConsumer(
                                truncatedSku,
                                truncatedName,
                                categoryProto,
                                payloadProto,
                                actualPriceProto,
                                originalPriceProto,
                                promocodesProto
                        ))
                .checkAll();
    }

    @Test
    public void toProtoWithoutTruncation() throws Exception {
        String inputSku = "input sku";
        String inputName = "input name";

        when(skuTrimmer.trim(inputSku))
                .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                        inputSku,
                        new BytesTruncatedInfo(0)
                ));
        when(nameTrimmer.trim(inputName))
                .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                        inputName,
                        new BytesTruncatedInfo(0)
                ));
        when(payloadConverter.fromModel(payload))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider>(
                        payloadProto,
                        new BytesTruncatedInfo(0)
                ));
        when(priceConverter.fromModel(originalPrice))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>(
                        originalPriceProto,
                        new BytesTruncatedInfo(0)
                ));
        when(priceConverter.fromModel(actualPrice))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>(
                        actualPriceProto,
                        new BytesTruncatedInfo(0)
                ));
        when(categoryConverter.fromModel(categories))
                .thenReturn(
                        new Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider>(
                                categoryProto,
                                new BytesTruncatedInfo(0)
                        )
                );
        when(promocodesConverter.fromModel(promocodes))
                .thenReturn(
                        new Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider>(
                                promocodesProto,
                                new BytesTruncatedInfo(0)
                        )
                );

        productWrapper = new ProductWrapper(
                inputSku, inputName, categories, payload, actualPrice, originalPrice, promocodes
        );

        Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider> productResult =
                productConverter.fromModel(productWrapper);

        ObjectPropertyAssertions(
                productResult)
                .checkFieldRecursively(
                        "metaInfo",
                        new TruncationInfoConsumer(0)
                )
                .checkFieldRecursively(
                        "result",
                        productMatchAssertionsConsumer(
                                inputSku,
                                inputName,
                                categoryProto,
                                payloadProto,
                                actualPriceProto,
                                originalPriceProto,
                                promocodesProto
                        ))
                .checkAll();
    }

    @Test
    public void toProtoWithPossibleNulls() throws Exception {
        String sku = "sku";
        when(skuTrimmer.trim("sku"))
                .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(sku, new BytesTruncatedInfo(0)));
        when(nameTrimmer.trim(nullable(String.class)))
                .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(null, new BytesTruncatedInfo(0)));

        productWrapper = new ProductWrapper(sku, null, null, null, null, null, null);

        Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider> result =
                productConverter.fromModel(productWrapper);


        ObjectPropertyAssertions(result)
                .checkFieldRecursively(
                        "metaInfo",
                        new TruncationInfoConsumer(0)
                )
                .checkFieldRecursively(
                        "result", productMatchAssertionsConsumer(
                                sku,
                                "",
                                null,
                                null,
                                null,
                                null,
                                new Ecommerce.ECommerceEvent.PromoCode[0]
                        )
                )
                .checkAll();
    }

    private Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Product>> productMatchAssertionsConsumer(
            final String sku,
            final String name,
            final Ecommerce.ECommerceEvent.Category category,
            final Ecommerce.ECommerceEvent.Payload payload,
            final Ecommerce.ECommerceEvent.Price actualPrice,
            final Ecommerce.ECommerceEvent.Price originalPrice,
            final Ecommerce.ECommerceEvent.PromoCode[] promocodes
    ) {

        return new Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Product>>() {

            @Override
            public void accept(ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Product> assertions) {
                try {
                    assertions.withFinalFieldOnly(false)
                            .checkField("sku", sku.getBytes())
                            .checkField("name", name.getBytes())
                            .checkField("category", category)
                            .checkField("payload", payload)
                            .checkField("actualPrice", actualPrice)
                            .checkField("originalPrice", originalPrice)
                            .checkField("promoCodes", promocodes);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        productConverter.toModel(
                new Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider>(
                        new Ecommerce.ECommerceEvent.Product(),
                        new BytesTruncatedInfo(0)
                )
        );
    }
}
