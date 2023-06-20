package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ProtoMessageSizeCalculatorForNestedOrderCartItem extends CommonTest {

    private Ecommerce.ECommerceEvent.OrderCartItem input;

    public ProtoMessageSizeCalculatorForNestedOrderCartItem(Ecommerce.ECommerceEvent.OrderCartItem input) {
        this.input = input;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new Ecommerce.ECommerceEvent.OrderCartItem()},
                {filledOrderCartItem()}
        });
    }

    private static Ecommerce.ECommerceEvent.OrderCartItem filledOrderCartItem() {
        Ecommerce.ECommerceEvent.OrderCartItem cartItem = new Ecommerce.ECommerceEvent.OrderCartItem();
        cartItem.item = RandomGeneratorUtils.generateCartItem();
        return cartItem;
    }

    private ProtoMessageSizeCalculator protoMessageSizeCalculator;

    @Before
    public void setUp() throws Exception {
        protoMessageSizeCalculator = new ProtoMessageSizeCalculator();
    }

    @Test
    public void computeSizeNested() {
        int actual = protoMessageSizeCalculator.computeAdditionalNestedSize(input);

        Ecommerce.ECommerceEvent eventWithoutItem = new Ecommerce.ECommerceEvent();
        eventWithoutItem.orderInfo = new Ecommerce.ECommerceEvent.OrderInfo();
        eventWithoutItem.orderInfo.order = new Ecommerce.ECommerceEvent.Order();
        eventWithoutItem.orderInfo.order.items = new Ecommerce.ECommerceEvent.OrderCartItem[1];
        eventWithoutItem.orderInfo.order.items[0] = new Ecommerce.ECommerceEvent.OrderCartItem();
        int sizeWithoutItem = MessageNano.toByteArray(eventWithoutItem).length;

        Ecommerce.ECommerceEvent eventWithItem = new Ecommerce.ECommerceEvent();
        eventWithItem.orderInfo = new Ecommerce.ECommerceEvent.OrderInfo();
        eventWithItem.orderInfo.order = new Ecommerce.ECommerceEvent.Order();
        eventWithItem.orderInfo.order.items = new Ecommerce.ECommerceEvent.OrderCartItem[2];
        eventWithItem.orderInfo.order.items[0] = new Ecommerce.ECommerceEvent.OrderCartItem();
        eventWithItem.orderInfo.order.items[1] = input;
        int sizeWithItem = MessageNano.toByteArray(eventWithItem).length;

        int expected = sizeWithItem - sizeWithoutItem;

        assertThat(actual).isEqualTo(expected);
    }
}
