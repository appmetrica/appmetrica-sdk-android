package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringListTrimmer;
import io.appmetrica.analytics.testutils.CollectionTrimInfoConsumer;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CategoryConverterTest extends CommonTest {

    @Mock
    private HierarchicalStringListTrimmer categoryTrimmer;
    @Mock
    private List<String> inputList;
    @Mock
    private List<String> truncatedList;

    private CategoryConverter categoryConverter;

    private final int itemsDropped = 300;
    private final int bytesTruncated = 100500;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        categoryConverter = new CategoryConverter(categoryTrimmer);
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions(new CategoryConverter())
            .withPrivateFields(true)
            .checkFieldComparingFieldByFieldRecursively("categoryTrimmer", new HierarchicalStringListTrimmer(20, 100))
            .checkAll();
    }

    @Test
    public void toProto() throws Exception {
        when(categoryTrimmer.trim(inputList))
            .thenReturn(
                new TrimmingResult<List<String>, CollectionTrimInfo>(
                    truncatedList,
                    new CollectionTrimInfo(itemsDropped, bytesTruncated)
                )
            );
        when(categoryTrimmer.trim(Collections.emptyList())).
            thenReturn(
                new TrimmingResult<List<String>, CollectionTrimInfo>(
                    Collections.emptyList(),
                    new CollectionTrimInfo(0, 0)
                )
            );

        final ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider>> assertions =
            ObjectPropertyAssertions(
                categoryConverter.fromModel(inputList)
            )
                .withFinalFieldOnly(false);

        assertions.checkFieldRecursively("metaInfo", new CollectionTrimInfoConsumer(bytesTruncated, itemsDropped));
        assertions.checkFieldRecursively(
            "result",
            new Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Category>>() {
                @Override
                public void accept(ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Category> innerAssertions) {
                    try {
                        innerAssertions.withFinalFieldOnly(false)
                            .checkField("path", StringUtils.getUTF8Bytes(truncatedList));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        );

        assertions.checkAll();
    }

    @Test
    public void toProtoWithoutTruncation() throws Exception {
        when(categoryTrimmer.trim(inputList))
            .thenReturn(
                new TrimmingResult<List<String>, CollectionTrimInfo>(
                    inputList,
                    new CollectionTrimInfo(0, 0)
                )
            );

        final ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider>> assertions =
            ObjectPropertyAssertions(
                categoryConverter.fromModel(inputList)
            )
                .withFinalFieldOnly(false);

        assertions.checkFieldRecursively("metaInfo", new CollectionTrimInfoConsumer(0, 0));
        assertions.checkFieldRecursively(
            "result",
            new Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Category>>() {
                @Override
                public void accept(ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Category> innerAssertions) {
                    try {
                        innerAssertions.withFinalFieldOnly(false)
                            .checkField("path", StringUtils.getUTF8Bytes(inputList));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        );

        assertions.checkAll();
    }

    @Test
    public void toProtoForEmptyCategories() throws Exception {
        when(categoryTrimmer.trim(Collections.emptyList())).
            thenReturn(
                new TrimmingResult<List<String>, CollectionTrimInfo>(
                    Collections.emptyList(),
                    new CollectionTrimInfo(0, 0)
                )
            );

        final ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider>> assertions =
            ObjectPropertyAssertions(
                categoryConverter.fromModel(Collections.emptyList())
            )
                .withFinalFieldOnly(false);

        assertions.checkFieldRecursively("metaInfo", new CollectionTrimInfoConsumer(0, 0));

        assertions.checkFieldRecursively(
            "result",
            new Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Category>>() {
                @Override
                public void accept(ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Category> innerAssertions) {
                    try {
                        innerAssertions.withFinalFieldOnly(false)
                            .checkField("path", new byte[0][]);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        );

        assertions.checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        categoryConverter.toModel(new Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider>(
            new Ecommerce.ECommerceEvent.Category(), new BytesTruncatedInfo(0)
        ));
    }
}
