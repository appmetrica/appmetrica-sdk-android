package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartItemWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.OrderInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.OrderWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringTrimmer;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OrderInfoEventConverterTest extends CommonTest {

    @Mock
    private PayloadConverter payloadConverter;
    @Mock
    private CartItemConverter cartItemConverter;
    @Mock
    private HierarchicalStringTrimmer orderIdentifierTrimmer;
    @Mock
    private ProtoMessageSizeCalculator protoMessageSizeCalculator;
    @Mock
    private Map<String, String> payload;
    @Mock
    private Ecommerce.ECommerceEvent.Payload payloadProto;
    @Mock
    private CartItemWrapper firstCartItem;
    @Mock
    private CartItemWrapper secondCartItem;
    @Mock
    private CartItemWrapper thirdCartItem;
    @Mock
    private Ecommerce.ECommerceEvent.CartItem firstCartItemProto;
    @Mock
    private Ecommerce.ECommerceEvent.CartItem secondCartItemProto;
    @Mock
    private Ecommerce.ECommerceEvent.CartItem thirdCartItemProto;

    private OrderInfoEventConverter orderInfoEventConverter;
    private List<CartItemWrapper> cartItems;
    private Ecommerce.ECommerceEvent.CartItem[] cartItemsProto;

    private final String inputOrderIdentifier = "Input e-commerce order identifier";
    private final String truncatedOrderIdentifier = "Truncated e-commerce order identifier";
    private final String orderUuid = "E-commerce order uuid";

    private final int payloadBytesTruncated = 1;
    private final int orderIdentifierBytesTruncated = 10;
    private final int firstCartItemBytesTruncated = 100;
    private final int secondCartItemBytesTruncated = 1000;
    private final int thirdCartItemBytesTruncated = 10000;

    private final int emptyOrderInfoBytesTruncated = payloadBytesTruncated + orderIdentifierBytesTruncated;

    private final int totalBytesTruncated = emptyOrderInfoBytesTruncated +
        firstCartItemBytesTruncated + secondCartItemBytesTruncated + thirdCartItemBytesTruncated;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        cartItems = Arrays.asList(firstCartItem, secondCartItem, thirdCartItem);
        cartItemsProto =
            new Ecommerce.ECommerceEvent.CartItem[]{firstCartItemProto, secondCartItemProto, thirdCartItemProto};

        when(payloadConverter.fromModel(payload))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider>(
                payloadProto,
                new BytesTruncatedInfo(payloadBytesTruncated)
            ));
        when(cartItemConverter.fromModel(firstCartItem))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider>(
                firstCartItemProto,
                new BytesTruncatedInfo(firstCartItemBytesTruncated)
            ));
        when(cartItemConverter.fromModel(secondCartItem))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider>(
                secondCartItemProto,
                new BytesTruncatedInfo(secondCartItemBytesTruncated)
            ));
        when(cartItemConverter.fromModel(thirdCartItem))
            .thenReturn(new Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider>(
                thirdCartItemProto,
                new BytesTruncatedInfo(thirdCartItemBytesTruncated)
            ));
        when(orderIdentifierTrimmer.trim(inputOrderIdentifier))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                truncatedOrderIdentifier,
                new BytesTruncatedInfo(orderIdentifierBytesTruncated)
            ));

        orderInfoEventConverter = new OrderInfoEventConverter(
            payloadConverter,
            cartItemConverter,
            orderIdentifierTrimmer,
            protoMessageSizeCalculator
        );
    }

    @Test
    public void constructor() throws Exception {
        orderInfoEventConverter = new OrderInfoEventConverter();

        ObjectPropertyAssertions(orderInfoEventConverter)
            .withPrivateFields(true)
            .checkFieldNonNull("payloadConverter")
            .checkFieldNonNull("cartItemConverter")
            .checkFieldComparingFieldByField("orderIdentifierTrimmer", new HierarchicalStringTrimmer(100))
            .checkFieldNonNull("protoMessageSizeCalculator")
            .checkAll();
    }

    @Test
    public void toProtoForBeginCheckoutEvent() throws Exception {
        toProtoForEventType(OrderInfoEvent.EVENT_TYPE_BEGIN_CHECKOUT);
    }

    @Test
    public void toProtoForPurchaseEvent() throws Exception {
        toProtoForEventType(OrderInfoEvent.EVENT_TYPE_PURCHASE);
    }

    private void toProtoForEventType(int eventType) throws Exception {
        when(orderIdentifierTrimmer.trim(null))
            .thenReturn(new TrimmingResult<String, BytesTruncatedProvider>(
                null,
                new BytesTruncatedInfo(0)
            ));
        OrderWrapper orderWrapper = new OrderWrapper(
            orderUuid,
            inputOrderIdentifier,
            cartItems,
            payload
        );
        OrderInfoEvent orderInfoEvent = new OrderInfoEvent(eventType, orderWrapper, orderInfoEventConverter);
        List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> results =
            orderInfoEventConverter.fromModel(orderInfoEvent);

        assertThat(results.size()).isEqualTo(1);

        ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> assertions =
            ObjectPropertyAssertions(results.get(0));

        assertions.checkFieldRecursively(
            "metaInfo",
            new TruncationInfoConsumer(totalBytesTruncated)
        );
        assertions.checkFieldRecursively(
            "result",
            new ECommerceEventAssertionsConsumer(eventType)
                .setOrderInfoAssertionConsumer(
                    new OrderInfoAssertionsConsumer(
                        new OrderAssertionConsumer()
                            .setExpectedOrderUuid(orderUuid)
                            .setExpectedOrderId(truncatedOrderIdentifier)
                            .setExpectedPayload(payloadProto)
                            .setExpectedOrderCartItems(cartItemsProto)
                    )
                )
        );

        assertions.checkAll();
    }

    @Test
    public void toProtoForNullPayload() {
        OrderInfoEvent event = new OrderInfoEvent(
            OrderInfoEvent.EVENT_TYPE_PURCHASE,
            new OrderWrapper(
                orderUuid,
                inputOrderIdentifier,
                cartItems,
                null
            ),
            orderInfoEventConverter
        );

        List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> protos = orderInfoEventConverter.fromModel(event);

        assertThat(protos.size()).isEqualTo(1);

        assertThat(protos.get(0).result.orderInfo.order.payload).isNull();
        assertThat(protos.get(0).getBytesTruncated()).isEqualTo(totalBytesTruncated - payloadBytesTruncated);
    }

    @Test
    public void toProtoForEmptyCartItems() {
        OrderInfoEvent event = new OrderInfoEvent(
            OrderInfoEvent.EVENT_TYPE_PURCHASE,
            new OrderWrapper(
                orderUuid,
                inputOrderIdentifier,
                new ArrayList<CartItemWrapper>(),
                payload
            ),
            orderInfoEventConverter
        );
        List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> protos = orderInfoEventConverter.fromModel(event);

        assertThat(protos.size()).isEqualTo(1);

        Ecommerce.ECommerceEvent.Order order = protos.get(0).result.orderInfo.order;

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(protos.get(0).getBytesTruncated())
            .as("Total bytes truncated")
            .isEqualTo(emptyOrderInfoBytesTruncated);
        assertions.assertThat(order.totalItemsCount)
            .as("Total order cart items count")
            .isZero();
        assertions.assertThat(order.items)
            .as("Order items")
            .isEmpty();

        assertions.assertAll();
    }

    @Test
    public void toProtoForSplittableCartItems() {
        when(protoMessageSizeCalculator.computeAdditionalNestedSize(any(Ecommerce.ECommerceEvent.class)))
            .thenReturn(100 * 1024);

        int totalInputCount = 100;
        int singleCartItemSizeBytes = 10 * 1024;
        int cartItemsCountInSingleEvent = 10;
        int eventsCount = totalInputCount / cartItemsCountInSingleEvent;
        List<CartItemWrapper> inputCartItems = new ArrayList<CartItemWrapper>();
        Ecommerce.ECommerceEvent.CartItem[] protoCartItems = new Ecommerce.ECommerceEvent.CartItem[totalInputCount];
        for (int i = 0; i < totalInputCount; i++) {
            CartItemWrapper inputCartItem = mock(CartItemWrapper.class);
            Ecommerce.ECommerceEvent.CartItem cartItemProto = mock(Ecommerce.ECommerceEvent.CartItem.class);
            when(cartItemConverter.fromModel(inputCartItem))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider>(
                    cartItemProto,
                    new BytesTruncatedInfo(i) //Use index as bytes truncated
                ));
            when(protoMessageSizeCalculator.computeAdditionalNestedSize(
                any(Ecommerce.ECommerceEvent.OrderCartItem.class))
            )
                .thenReturn(singleCartItemSizeBytes);
            inputCartItems.add(inputCartItem);
            protoCartItems[i] = cartItemProto;
        }
        OrderWrapper orderWrapper = new OrderWrapper(
            orderUuid,
            inputOrderIdentifier,
            inputCartItems,
            payload
        );
        OrderInfoEvent orderInfoEvent =
            new OrderInfoEvent(OrderInfoEvent.EVENT_TYPE_PURCHASE, orderWrapper, orderInfoEventConverter);

        List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> result =
            orderInfoEventConverter.fromModel(orderInfoEvent);

        assertThat(result).hasSize(eventsCount);

        SoftAssertions softAssertions = new SoftAssertions();

        for (int i = 0; i < eventsCount; i++) {
            int expectedItemsBytesTruncated = sumOfProgression(i, i + cartItemsCountInSingleEvent - 1);
            final int expectedBytesTruncated = emptyOrderInfoBytesTruncated + expectedItemsBytesTruncated;
            Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider> item = result.get(i);
            final Ecommerce.ECommerceEvent.CartItem[] expectedCartItems = new Ecommerce.ECommerceEvent.CartItem[10];
            System.arraycopy(protoCartItems, i * cartItemsCountInSingleEvent + 0, expectedCartItems, 0, cartItemsCountInSingleEvent);
            assertThatEventMatchExpectedValues(softAssertions, i, item, expectedBytesTruncated, expectedCartItems);
        }
    }

    private void assertThatEventMatchExpectedValues(SoftAssertions softAssertions,
                                                    int orderNumber,
                                                    Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider> item,
                                                    final int expectedBytesTruncated,
                                                    final Ecommerce.ECommerceEvent.CartItem[] expectedCartItems) {
        softAssertions.assertThat(item)
            .as("Result #%d", orderNumber)
            .matches(new Predicate<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>>() {
                @Override
                public boolean test(Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider> result) {
                    ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> assertions =
                        ObjectPropertyAssertions(
                            result);

                    try {
                        assertions.checkField(
                            "metaInfo",
                            new TruncationInfoConsumer(expectedBytesTruncated)
                        );
                        assertions.checkFieldRecursively(
                            "result",
                            new ECommerceEventAssertionsConsumer(Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_PURCHASE)
                                .setOrderInfoAssertionConsumer(
                                    new OrderInfoAssertionsConsumer(
                                        new OrderAssertionConsumer()
                                            .setExpectedOrderUuid(orderUuid)
                                            .setExpectedOrderId(truncatedOrderIdentifier)
                                            .setExpectedPayload(payloadProto)
                                            .setExpectedOrderCartItems(expectedCartItems)
                                    )
                                )
                        );
                        return true;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
    }

    private int sumOfProgression(int initial, int last) {
        return (last + initial) / 2 * (last - initial + 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        orderInfoEventConverter.toModel(Collections.emptyList());
    }
}
