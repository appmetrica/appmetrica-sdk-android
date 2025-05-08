package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer;
import io.appmetrica.analytics.testutils.CollectionTrimInfoConsumer;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PayloadConverterTest extends CommonTest {

    @Mock
    private HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer payloadTrimmer;
    @Mock
    private Map<String, String> inputPayload;

    private PayloadConverter payloadConverter;

    private final String firstKey = "First key";
    private final byte[] firstKeyBytes = firstKey.getBytes();
    private final String firstValue = "First value";
    private final byte[] firstValueBytes = firstValue.getBytes();

    private final String secondKey = "Second key";
    private final byte[] secondKeyBytes = secondKey.getBytes();
    private final String secondValue = "Second value";
    private final byte[] secondValueBytes = secondValue.getBytes();

    private final String thirdKey = "Third key";
    private final byte[] thirdKeyBytes = thirdKey.getBytes();
    private final String thirdValue = "Third value";
    private final byte[] thirdValueBytes = thirdValue.getBytes();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        payloadConverter = new PayloadConverter(payloadTrimmer);
    }

    @Test
    public void constructor() throws Exception {
        payloadConverter = new PayloadConverter();

        HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer expectedTrimmer =
            new HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer(20 * 1024, 100, 1000);

        ObjectPropertyAssertions<PayloadConverter> assertions =
            ObjectPropertyAssertions(payloadConverter)
                .withPrivateFields(true);

        assertions.checkFieldComparingFieldByFieldRecursively(
            "payloadTrimmer",
            expectedTrimmer
        );

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        Map<String, String> payload = new LinkedHashMap<String, String>();
        payload.put(firstKey, firstValue);
        payload.put(secondKey, secondValue);
        payload.put(thirdKey, thirdValue);
        int pairsTruncated = 17;
        int bytesTruncated = 123;

        when(payloadTrimmer.trim(inputPayload))
            .thenReturn(new TrimmingResult<Map<String, String>, CollectionTrimInfo>(
                payload,
                new CollectionTrimInfo(pairsTruncated, bytesTruncated)
            ));

        Ecommerce.ECommerceEvent.Payload.Pair[] expectedPairs = new Ecommerce.ECommerceEvent.Payload.Pair[3];
        expectedPairs[0] = new Ecommerce.ECommerceEvent.Payload.Pair();
        expectedPairs[0].key = firstKeyBytes;
        expectedPairs[0].value = firstValueBytes;
        expectedPairs[1] = new Ecommerce.ECommerceEvent.Payload.Pair();
        expectedPairs[1].key = secondKeyBytes;
        expectedPairs[1].value = secondValueBytes;
        expectedPairs[2] = new Ecommerce.ECommerceEvent.Payload.Pair();
        expectedPairs[2].key = thirdKeyBytes;
        expectedPairs[2].value = thirdValueBytes;
        Ecommerce.ECommerceEvent.Payload expectedPayload = new Ecommerce.ECommerceEvent.Payload();
        expectedPayload.truncatedPairsCount = pairsTruncated;
        expectedPayload.pairs = expectedPairs;

        Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider> payloadResult =
            payloadConverter.fromModel(inputPayload);

        ObjectPropertyAssertions(
            payloadResult)
            .checkFieldRecursively(
                "metaInfo",
                new CollectionTrimInfoConsumer(bytesTruncated, pairsTruncated)
            )
            .checkFieldComparingFieldByFieldRecursively("result", expectedPayload)
            .checkAll();
    }

    @Test
    public void toProtoForEmpty() {
        when(payloadTrimmer.trim(inputPayload))
            .thenReturn(
                new TrimmingResult<Map<String, String>, CollectionTrimInfo>(
                    new HashMap<String, String>(),
                    new CollectionTrimInfo(0, 0)
                ));
        Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider> payloadResult =
            payloadConverter.fromModel(inputPayload);

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(payloadResult.result.pairs).isEmpty();
        assertions.assertThat(payloadResult.result.truncatedPairsCount).isZero();
        assertions.assertThat(payloadResult.getBytesTruncated()).isZero();

        assertions.assertAll();
    }

    @Test
    public void toProtoForNullValueReturnedByTrimmer() {
        when(payloadTrimmer.trim(inputPayload))
            .thenReturn(
                new TrimmingResult<Map<String, String>, CollectionTrimInfo>(
                    null,
                    new CollectionTrimInfo(0, 0)
                )
            );
        Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider> payloadResult =
            payloadConverter.fromModel(inputPayload);

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(payloadResult.result.pairs).isEmpty();
        assertions.assertThat(payloadResult.result.truncatedPairsCount).isZero();
        assertions.assertThat(payloadResult.getBytesTruncated()).isZero();

        assertions.assertAll();

    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        payloadConverter.toModel(new Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider>(
            new Ecommerce.ECommerceEvent.Payload(),
            new BytesTruncatedInfo(0)
        ));
    }

}
