package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.ReferrerWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ScreenWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringTrimmer;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
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
public class ReferrerConverterTest extends CommonTest {

    @Mock
    private ScreenConverter screenConverter;
    @Mock
    private HierarchicalStringTrimmer typeTrimmer;
    @Mock
    private HierarchicalStringTrimmer identifierTrimmer;
    @Mock
    private ScreenWrapper screenWrapper;
    @Mock
    private Ecommerce.ECommerceEvent.Screen screenProto;

    private ReferrerConverter referrerConverter;

    private final String inputType = "input type";
    private final String truncatedType = "truncated type";
    private final String inputIdentifier = "input identifier";
    private final String truncatedIdentifier = "truncated identifier";

    private final int screenBytesTruncated = 1;
    private final int typeBytesTruncated = 10;
    private final int identifierBytesTruncated = 100;

    private final int totalBytesTruncated = screenBytesTruncated + typeBytesTruncated + identifierBytesTruncated;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(screenConverter.fromModel(screenWrapper))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider>(
                screenProto,
                new BytesTruncatedInfo(screenBytesTruncated)
            ));

        when(typeTrimmer.trim(inputType))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                truncatedType,
                new BytesTruncatedInfo(typeBytesTruncated)
            ));

        when(identifierTrimmer.trim(inputIdentifier))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                truncatedIdentifier,
                new BytesTruncatedInfo(identifierBytesTruncated)
            ));

        referrerConverter = new ReferrerConverter(screenConverter, typeTrimmer, identifierTrimmer);
    }

    @Test
    public void constructor() throws Exception {
        referrerConverter = new ReferrerConverter();

        ObjectPropertyAssertions<ReferrerConverter> assertions =
            ObjectPropertyAssertions(referrerConverter)
                .withPrivateFields(true);

        assertions.checkFieldNonNull("screenConverter");
        assertions.checkFieldComparingFieldByField("typeTrimmer", new HierarchicalStringTrimmer(100));
        assertions.checkFieldComparingFieldByField("idTrimmer", new HierarchicalStringTrimmer(2048));

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        ReferrerWrapper referrerWrapper = new ReferrerWrapper(inputType, inputIdentifier, screenWrapper);

        Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider> proto =
            referrerConverter.fromModel(referrerWrapper);

        Ecommerce.ECommerceEvent.Referrer expectedReferrer = new Ecommerce.ECommerceEvent.Referrer();
        expectedReferrer.screen = screenProto;
        expectedReferrer.type = truncatedType.getBytes();
        expectedReferrer.id = truncatedIdentifier.getBytes();

        ObjectPropertyAssertions(proto)
            .checkFieldRecursively(
                "metaInfo",
                new TruncationInfoConsumer(totalBytesTruncated)
            )
            .checkFieldComparingFieldByField("result", expectedReferrer)
            .checkAll();
    }

    @Test
    public void toProtoForNull() throws Exception {
        when(typeTrimmer.trim(nullable(String.class)))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                null,
                new BytesTruncatedInfo(0)
            ));
        when(identifierTrimmer.trim(nullable(String.class)))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                null,
                new BytesTruncatedInfo(0)
            ));

        ReferrerWrapper referrerWrapper = new ReferrerWrapper(null, null, null);

        Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider> result =
            referrerConverter.fromModel(referrerWrapper);

        ObjectPropertyAssertions(result)
            .checkFieldRecursively(
                "metaInfo",
                new TruncationInfoConsumer(0)
            )
            .checkFieldComparingFieldByField("result", new Ecommerce.ECommerceEvent.Referrer())
            .checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        referrerConverter.toModel(new Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider>(
            new Ecommerce.ECommerceEvent.Referrer(),
            new BytesTruncatedInfo(0)
        ));
    }
}
