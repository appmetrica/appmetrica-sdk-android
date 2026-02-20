package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.ScreenWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringTrimmer;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

public class ScreenConverterTest extends CommonTest {

    @Mock
    private PayloadConverter payloadConverter;
    @Mock
    private CategoryConverter categoryConverter;
    @Mock
    private Map<String, String> payloadWrapper;
    @Mock
    private Ecommerce.ECommerceEvent.Payload payloadProto;
    @Mock
    private Ecommerce.ECommerceEvent.Category categoryProto;
    @Mock
    private HierarchicalStringTrimmer nameTrimmer;
    @Mock
    private HierarchicalStringTrimmer searchQueryTrimmer;

    private ScreenConverter screenConverter;

    private final List<String> categories = Collections.singletonList("category");

    private final int nameBytesTruncated = 1;
    private final int searchQueryBytesTruncated = 10;
    private final int payloadBytesTruncated = 100;
    private final int categoryBytesTruncated = 1000;

    private final int totalBytesTruncated = nameBytesTruncated + searchQueryBytesTruncated + payloadBytesTruncated +
        categoryBytesTruncated;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(payloadConverter.fromModel(payloadWrapper))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider>(
                payloadProto,
                new BytesTruncatedInfo(payloadBytesTruncated)
            ));
        when(categoryConverter.fromModel(categories))
            .thenReturn(
                new Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider>(
                    categoryProto,
                    new BytesTruncatedInfo(categoryBytesTruncated)
                )
            );

        screenConverter = new ScreenConverter(payloadConverter, categoryConverter, nameTrimmer, searchQueryTrimmer);
    }

    @Test
    public void constructor() throws Exception {
        screenConverter = new ScreenConverter();

        ObjectPropertyAssertions<ScreenConverter> assertions =
            ObjectPropertyAssertions(screenConverter)
                .withPrivateFields(true);

        assertions.checkFieldNonNull("payloadConverter");
        assertions.checkFieldNonNull("categoryConverter");
        assertions.checkFieldComparingFieldByField("nameTrimmer", new HierarchicalStringTrimmer(100));
        assertions.checkFieldComparingFieldByField("searchQueryTrimmer", new HierarchicalStringTrimmer(1000));

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        String inputName = "input name";
        String truncatedName = "truncated name";
        when(nameTrimmer.trim(inputName))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                truncatedName,
                new BytesTruncatedInfo(nameBytesTruncated)
            ));

        String inputSearchQuery = "input search query";
        String truncatedSearchQuery = "search query";
        when(searchQueryTrimmer.trim(inputSearchQuery))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                truncatedSearchQuery,
                new BytesTruncatedInfo(searchQueryBytesTruncated)
            ));

        ScreenWrapper screenWrapper = new ScreenWrapper(inputName, categories, inputSearchQuery, payloadWrapper);
        Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider> screenResult =
            screenConverter.fromModel(screenWrapper);

        ObjectPropertyAssertions(screenResult)
            .checkFieldRecursively(
                "metaInfo",
                new TruncationInfoConsumer(totalBytesTruncated)
            )
            .checkFieldRecursively(
                "result",
                screenAssertionsConsumer(truncatedName, categoryProto, truncatedSearchQuery, payloadProto)
            )
            .checkAll();
    }

    @Test
    public void toProtoForNull() throws Exception {
        ScreenWrapper screenWrapper = new ScreenWrapper(null, null, null, null);

        when(nameTrimmer.trim(nullable(String.class)))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                null,
                new BytesTruncatedInfo(0)
            ));
        when(searchQueryTrimmer.trim(nullable(String.class)))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                null,
                new BytesTruncatedInfo(0)
            ));

        Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider> result = screenConverter.fromModel(screenWrapper);

        ObjectPropertyAssertions(result)
            .checkFieldRecursively(
                "metaInfo",
                new TruncationInfoConsumer(0)
            )
            .checkFieldRecursively("result", screenAssertionsConsumer("", null, "", null))
            .checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        screenConverter.toModel(
            new Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider>(
                new Ecommerce.ECommerceEvent.Screen(),
                new BytesTruncatedInfo(0)
            )
        );
    }

    private Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Screen>> screenAssertionsConsumer(
        final String name,
        final Ecommerce.ECommerceEvent.Category category,
        final String searchQuery,
        final Ecommerce.ECommerceEvent.Payload payload
    ) {
        return new Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Screen>>() {
            @Override
            public void accept(ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Screen> assertions) {
                try {
                    assertions.withFinalFieldOnly(false)
                        .checkFieldComparingFieldByField("name", name.getBytes())
                        .checkFieldComparingFieldByField("category", category)
                        .checkFieldComparingFieldByField("searchQuery", searchQuery.getBytes())
                        .checkFieldComparingFieldByField("payload", payload);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
