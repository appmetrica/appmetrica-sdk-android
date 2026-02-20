package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringListTrimmer;
import io.appmetrica.analytics.testutils.CollectionTrimInfoConsumer;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.when;

public class PromocodesConverterTest extends CommonTest {

    @Mock
    private HierarchicalStringListTrimmer promocodesTrimmer;
    @Mock
    private List<String> inputPromocodes;

    private PromocodesConverter promocodesConverter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        promocodesConverter = new PromocodesConverter(promocodesTrimmer);
    }

    @Test
    public void constructor() throws Exception {
        promocodesConverter = new PromocodesConverter();

        ObjectPropertyAssertions<PromocodesConverter> assertions =
            ObjectPropertyAssertions(promocodesConverter)
                .withPrivateFields(true);

        assertions.checkFieldComparingFieldByFieldRecursively(
            "promocodesTrimmer",
            new HierarchicalStringListTrimmer(20, 100)
        );

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        String first = "first";
        String second = "second";
        String third = "third";
        List<String> promocodes = Arrays.asList(first, second, third);
        int itemsDropped = 23;
        int bytesTruncated = 240;

        when(promocodesTrimmer.trim(inputPromocodes))
            .thenReturn(new TrimmingResult<List<String>, CollectionTrimInfo>(
                promocodes,
                new CollectionTrimInfo(itemsDropped, bytesTruncated)
            ));

        Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider> promocodesResult =
            promocodesConverter.fromModel(inputPromocodes);

        assertThatPromocodesResultViaObjectPropertyAssertion(
            promocodesResult,
            new Ecommerce.ECommerceEvent.PromoCode[]{
                promoCodeWithValue(first), promoCodeWithValue(second), promoCodeWithValue(third)
            },
            bytesTruncated,
            itemsDropped
        );

        ObjectPropertyAssertions(
            promocodesResult)
            .checkFieldComparingFieldByFieldRecursively(
                "result",
                new Ecommerce.ECommerceEvent.PromoCode[]{
                    promoCodeWithValue(first), promoCodeWithValue(second), promoCodeWithValue(third)
                }
            )
            .checkFieldRecursively(
                "metaInfo",
                new CollectionTrimInfoConsumer(bytesTruncated, itemsDropped)
            )
            .checkAll();
    }

    private Ecommerce.ECommerceEvent.PromoCode promoCodeWithValue(String value) {
        Ecommerce.ECommerceEvent.PromoCode promoCode = new Ecommerce.ECommerceEvent.PromoCode();
        promoCode.code = value.getBytes();
        return promoCode;
    }

    @Test
    public void toProtoForNullPromocodes() throws Exception {
        when(promocodesTrimmer.trim(inputPromocodes))
            .thenReturn(new TrimmingResult<List<String>, CollectionTrimInfo>(
                null,
                new CollectionTrimInfo(0, 0)
            ));

        assertThatPromocodesResultViaObjectPropertyAssertion(
            promocodesConverter.fromModel(inputPromocodes),
            new Ecommerce.ECommerceEvent.PromoCode[0],
            0,
            0
        );
    }

    @Test
    public void toProtoForEmptyPromocodes() throws Exception {
        when(promocodesTrimmer.trim(inputPromocodes))
            .thenReturn(new TrimmingResult<List<String>, CollectionTrimInfo>(
                new ArrayList<String>(),
                new CollectionTrimInfo(0, 0)
            ));

        assertThatPromocodesResultViaObjectPropertyAssertion(
            promocodesConverter.fromModel(inputPromocodes),
            new Ecommerce.ECommerceEvent.PromoCode[0],
            0,
            0
        );
    }

    private void assertThatPromocodesResultViaObjectPropertyAssertion(
        Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider> result,
        Ecommerce.ECommerceEvent.PromoCode[] expectedPromocodes,
        int expectedBytesTruncated,
        int expectedItemsDropped
    ) throws Exception {
        ObjectPropertyAssertions(
            result)
            .checkField("result", expectedPromocodes, true)
            .checkFieldRecursively(
                "metaInfo",
                new CollectionTrimInfoConsumer(expectedBytesTruncated, expectedItemsDropped)
            )
            .checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        promocodesConverter.toModel(
            new Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider>(
                new Ecommerce.ECommerceEvent.PromoCode[0],
                new BytesTruncatedInfo(0)
            )
        );
    }
}
