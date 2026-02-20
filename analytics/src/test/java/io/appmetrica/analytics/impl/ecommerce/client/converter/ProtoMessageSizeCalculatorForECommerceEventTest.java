package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ProtoMessageSizeCalculatorForECommerceEventTest extends CommonTest {

    private static final RandomStringGenerator RANDOM_STRING_GENERATOR = new RandomStringGenerator(20);

    private final Ecommerce.ECommerceEvent input;

    public ProtoMessageSizeCalculatorForECommerceEventTest(Ecommerce.ECommerceEvent input, String descriptions) {
        this.input = input;
    }

    @Parameters(name = "#{index} - {1}")
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {new Ecommerce.ECommerceEvent(), "empty event"},
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.emptyOrderEvent(),
                "empty order event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.partiallyFilledOrderEvent(),
                "partially filled order event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.filledOrderEvent(),
                "filled order event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.emptyShowScreenEvent(),
                "empty show screen event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.filledShowScreenEvent(),
                "show screen event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.emptyShowProductCardEvent(),
                "empty show product card event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.filledShowProductCardEvent(),
                "filled show product card event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.emptyShowProductDetailsEvent(),
                "empty show product details event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.filledShowProductDetailsEvent(),
                "filled show product details event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.emptyAddCartActionEvent(),
                "empty add cart action event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.filledAddCartActionEvent(),
                "filled add cart action event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.emptyRemoveCartActionEvent(),
                "empty remove cart action event"
            },
            {
                ProtoMessageSizeCalculatorForECommerceEventTest.filledRemoveCartActionEvent(),
                "filled remove cart action event"
            }
        });
    }

    private static Ecommerce.ECommerceEvent emptyOrderEvent() {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.orderInfo = new Ecommerce.ECommerceEvent.OrderInfo();
        return event;
    }

    private static Ecommerce.ECommerceEvent partiallyFilledOrderEvent() {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_PURCHASE;
        event.orderInfo = new Ecommerce.ECommerceEvent.OrderInfo();
        event.orderInfo.order = new Ecommerce.ECommerceEvent.Order();
        event.orderInfo.order.orderId = RANDOM_STRING_GENERATOR.nextString().getBytes();
        event.orderInfo.order.uuid = RANDOM_STRING_GENERATOR.nextString().getBytes();
        event.orderInfo.order.payload = RandomGeneratorUtils.generatePayload();

        return event;
    }

    private static Ecommerce.ECommerceEvent filledOrderEvent() {
        Ecommerce.ECommerceEvent event = partiallyFilledOrderEvent();
        int itemsCount = 10;
        event.orderInfo.order.totalItemsCount = itemsCount;
        event.orderInfo.order.items = new Ecommerce.ECommerceEvent.OrderCartItem[itemsCount];
        for (int i = 0; i < itemsCount; i++) {
            Ecommerce.ECommerceEvent.OrderCartItem orderItem = new Ecommerce.ECommerceEvent.OrderCartItem();
            event.orderInfo.order.items[i] = orderItem;
            orderItem.numberInCart = i;
            orderItem.item = RandomGeneratorUtils.generateCartItem();
        }

        return event;
    }

    private static Ecommerce.ECommerceEvent emptyShowScreenEvent() {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_SCREEN;
        event.shownScreenInfo = new Ecommerce.ECommerceEvent.ShownScreenInfo();
        return event;
    }

    private static Ecommerce.ECommerceEvent filledShowScreenEvent() {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_SCREEN;
        event.shownScreenInfo = new Ecommerce.ECommerceEvent.ShownScreenInfo();
        event.shownScreenInfo.screen = RandomGeneratorUtils.generateScreen();
        return event;
    }

    private static Ecommerce.ECommerceEvent emptyShowProductCardEvent() {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_PRODUCT_CARD;
        event.shownProductCardInfo = new Ecommerce.ECommerceEvent.ShownProductCardInfo();
        return event;
    }

    private static Ecommerce.ECommerceEvent filledShowProductCardEvent() {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_PRODUCT_CARD;
        event.shownProductCardInfo = new Ecommerce.ECommerceEvent.ShownProductCardInfo();
        event.shownProductCardInfo.screen = RandomGeneratorUtils.generateScreen();
        event.shownProductCardInfo.product = RandomGeneratorUtils.generateProduct();
        return event;
    }

    private static Ecommerce.ECommerceEvent emptyShowProductDetailsEvent() {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_PRODUCT_DETAILS;
        event.shownProductDetailsInfo = new Ecommerce.ECommerceEvent.ShownProductDetailsInfo();
        return event;
    }

    private static Ecommerce.ECommerceEvent filledShowProductDetailsEvent() {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_PRODUCT_DETAILS;
        event.shownProductDetailsInfo = new Ecommerce.ECommerceEvent.ShownProductDetailsInfo();
        event.shownProductDetailsInfo.product = RandomGeneratorUtils.generateProduct();
        event.shownProductDetailsInfo.referrer = RandomGeneratorUtils.generateReferrer();
        return event;
    }

    private static Ecommerce.ECommerceEvent emptyAddCartActionEvent() {
        return emptyCartActionEvent(Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_ADD_TO_CART);
    }

    private static Ecommerce.ECommerceEvent emptyRemoveCartActionEvent() {
        return emptyCartActionEvent(Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_REMOVE_FROM_CART);
    }

    private static Ecommerce.ECommerceEvent filledAddCartActionEvent() {
        return filledCartActionEvent(Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_ADD_TO_CART);
    }

    private static Ecommerce.ECommerceEvent filledRemoveCartActionEvent() {
        return filledCartActionEvent(Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_REMOVE_FROM_CART);
    }

    private static Ecommerce.ECommerceEvent emptyCartActionEvent(int type) {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.type = type;
        return event;
    }

    private static Ecommerce.ECommerceEvent filledCartActionEvent(int type) {
        Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
        event.type = type;
        event.cartActionInfo = new Ecommerce.ECommerceEvent.CartActionInfo();
        event.cartActionInfo.item = RandomGeneratorUtils.generateCartItem();
        return event;
    }

    private ProtoMessageSizeCalculator protoMessageSizeCalculator;

    @Before
    public void setUp() throws Exception {
        protoMessageSizeCalculator = new ProtoMessageSizeCalculator();
    }

    @Test
    public void computeSize() {
        int expectedSize = MessageNano.toByteArray(input).length;
        int actualSize = protoMessageSizeCalculator.computeAdditionalNestedSize(input);

        assertThat(actualSize).isEqualTo(expectedSize);
    }
}
