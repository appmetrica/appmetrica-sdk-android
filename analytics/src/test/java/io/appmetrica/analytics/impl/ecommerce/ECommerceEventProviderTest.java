package io.appmetrica.analytics.impl.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.ecommerce.ECommerceAmount;
import io.appmetrica.analytics.ecommerce.ECommerceCartItem;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.ecommerce.ECommerceOrder;
import io.appmetrica.analytics.ecommerce.ECommercePrice;
import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import io.appmetrica.analytics.ecommerce.ECommerceReferrer;
import io.appmetrica.analytics.ecommerce.ECommerceScreen;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartActionInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.OrderInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductCardInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductDetailInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownScreenInfoEvent;
import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ECommerceEventProviderTest extends CommonTest {

    private ECommerceEventProvider eventProvider;

    private ECommerceScreen eCommerceScreen;
    private ECommerceProduct eCommerceProduct;
    private ECommerceReferrer eCommerceReferrer;
    private ECommerceCartItem eCommerceCartItem;
    private ECommerceOrder eCommerceOrder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        eCommerceScreen = new ECommerceScreen()
                .setName("E-commerce screen name")
                .setSearchQuery("E-commerce screen search query")
                .setCategoriesPath(Arrays.asList(
                        "E-commerce screen path #1",
                        "E-commerce screen path #2",
                        "E-commerce screen path #3"
                ))
                .setPayload(createScreenPayload());

        eCommerceProduct = new ECommerceProduct("E-commerce product sku")
                .setName("E-commerce product name")
                .setActualPrice(
                        new ECommercePrice(new ECommerceAmount(BigDecimal.TEN, "USD"))
                                .setInternalComponents(Arrays.asList(
                                        new ECommerceAmount(new BigDecimal("12313.432131"), "RUB"),
                                        new ECommerceAmount(new BigDecimal("2313"), "BYN"),
                                        new ECommerceAmount(new BigDecimal("42312.21"), "EUR")
                                ))
                )
                .setOriginalPrice(
                        new ECommercePrice(new ECommerceAmount(BigDecimal.ONE, "USD"))
                                .setInternalComponents(Arrays.asList(
                                        new ECommerceAmount(new BigDecimal("0.23123"), "RUB"),
                                        new ECommerceAmount(new BigDecimal("1.23"), "BYN"),
                                        new ECommerceAmount(new BigDecimal("7.4"), "EUR")
                                ))
                )
                .setCategoriesPath(Arrays.asList("E-commerce product path #1", "E-commerce product path #2"))
                .setPayload(createProductPayload())
                .setPromocodes(Arrays.asList("E-commerce product promocode #1", "E-commerce product promocode #2"));

        eCommerceReferrer = new ECommerceReferrer()
                .setType("E-commerce referrer type")
                .setIdentifier("E-commerce referrer identifier")
                .setScreen(eCommerceScreen);

        eCommerceCartItem = new ECommerceCartItem(
                eCommerceProduct,
                new ECommercePrice(
                        new ECommerceAmount(BigDecimal.ZERO, "USD")
                )
                        .setInternalComponents(Arrays.asList(
                                new ECommerceAmount(new BigDecimal("9.231"), "BYN"),
                                new ECommerceAmount(new BigDecimal("199.99"), "EUR")
                        )),
                new BigDecimal("99.109")
        )
                .setReferrer(eCommerceReferrer);

        eCommerceOrder = new ECommerceOrder(
                "E-commerce order identifier",
                Collections.singletonList(eCommerceCartItem)
        )
                .setPayload(createOrderPayload());

        eventProvider = new ECommerceEventProvider();
    }

    private Map<String, String> createScreenPayload() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("E-commerce screen payload key #1", "E-commerce screen payload value #1");
        result.put("E-commerce screen payload key #2", "E-commerce screen payload value #2");
        result.put("E-commerce screen payload key #3", "E-commerce screen payload value #3");
        return result;
    }

    private Map<String, String> createProductPayload() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("E-commerce product payload key #1", "E-commerce product payload value #1");
        result.put("E-commerce product payload key #2", "E-commerce product payload value #2");
        result.put("E-commerce product payload key #3", "E-commerce product payload value #3");
        return result;
    }

    private Map<String, String> createOrderPayload() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("E-commerce order payload key #1", "E-commerce order payload value #1");
        result.put("E-commerce order payload key #2", "E-commerce order payload value #2");
        result.put("E-commerce order payload key #3", "E-commerce order payload value #3");
        return result;
    }

    @Test
    public void showScreenEvent() throws Exception {
        ECommerceEvent event = eventProvider.showScreenEvent(eCommerceScreen);

        assertThat(event).isInstanceOf(ShownScreenInfoEvent.class);
        ShownScreenInfoEvent shownScreenInfoEvent = (ShownScreenInfoEvent) event;

        ObjectPropertyAssertions<ShownScreenInfoEvent> assertions =
                ObjectPropertyAssertions(shownScreenInfoEvent);

        assertions.checkFieldRecursively(
                "screen",
                new ScreenWrapperAssertionsConsumer()
                        .setExpectedName(eCommerceScreen.getName())
                        .setExpecredSearchQuery(eCommerceScreen.getSearchQuery())
                        .setExpectedCategoriesPath(eCommerceScreen.getCategoriesPath())
                        .setExpectedPayload(eCommerceScreen.getPayload())
        );

        assertions.checkAll();
    }

    @Test
    public void showProductCardEvent() throws Exception {
        ECommerceEvent event = eventProvider.showProductCardEvent(eCommerceProduct, eCommerceScreen);

        assertThat(event).isInstanceOf(ShownProductCardInfoEvent.class);

        ShownProductCardInfoEvent shownProductCardInfoEvent = ((ShownProductCardInfoEvent) event);

        ObjectPropertyAssertions<ShownProductCardInfoEvent> assertions =
                ObjectPropertyAssertions(shownProductCardInfoEvent);

        assertions.checkFieldRecursively(
                "screen",
                new ScreenWrapperAssertionsConsumer()
                        .setExpectedName(eCommerceScreen.getName())
                        .setExpecredSearchQuery(eCommerceScreen.getSearchQuery())
                        .setExpectedCategoriesPath(eCommerceScreen.getCategoriesPath())
                        .setExpectedPayload(eCommerceScreen.getPayload())
        );

        assertions.checkFieldRecursively(
                "product",
                new ProductWrapperAssertionsConsumer().setExpectedProduct(eCommerceProduct)
        );

        assertions.checkAll();

    }

    @Test
    public void showProductDetailsEvent() throws Exception {
        ECommerceEvent event = eventProvider.showProductDetailsEvent(eCommerceProduct, eCommerceReferrer);
        assertProductDetailsEvent(event, eCommerceReferrer);
    }

    @Test
    public void showProductDetailsEventWithNullReferrer() throws Exception {
        ECommerceEvent event = eventProvider.showProductDetailsEvent(eCommerceProduct, null);
        assertProductDetailsEvent(event, null);
    }

    private void assertProductDetailsEvent(ECommerceEvent event, ECommerceReferrer expectedReferrer)
            throws Exception {

        assertThat(event).isInstanceOf(ShownProductDetailInfoEvent.class);
        ShownProductDetailInfoEvent shownProductDetailInfoEvent = ((ShownProductDetailInfoEvent) event);

        ObjectPropertyAssertions<ShownProductDetailInfoEvent> assertions =
                ObjectPropertyAssertions(shownProductDetailInfoEvent);

        assertions.checkFieldRecursively(
                "product",
                new ProductWrapperAssertionsConsumer().setExpectedProduct(eCommerceProduct)
        );

        assertions.checkFieldRecursively(
                "referrer",
                expectedReferrer == null ? null :
                new ReferrerWrapperAssertionsConsumer()
                        .setExpectedType(expectedReferrer.getType())
                        .setExpectedId(expectedReferrer.getIdentifier())
                        .setExpectedScreen(
                                new ScreenWrapperAssertionsConsumer()
                                        .setExpectedName(expectedReferrer.getScreen().getName())
                                        .setExpecredSearchQuery(expectedReferrer.getScreen().getSearchQuery())
                                        .setExpectedCategoriesPath(expectedReferrer.getScreen().getCategoriesPath())
                                        .setExpectedPayload(expectedReferrer.getScreen().getPayload())
                        )
        );

        assertions.checkAll();
    }

    @Test
    public void addCartItemEvent() throws Exception {
        assertCartItemEvent(
                eventProvider.addCartItemEvent(eCommerceCartItem),
                CartActionInfoEvent.EVENT_TYPE_ADD_TO_CART
        );
    }

    @Test
    public void removeCartItemEvent() throws Exception {
        assertCartItemEvent(
                eventProvider.removeCartItemEvent(eCommerceCartItem),
                CartActionInfoEvent.EVENT_TYPE_REMOVE_FROM_CART
        );
    }

    private void assertCartItemEvent(ECommerceEvent event, int expectedEventType) throws Exception {
        assertThat(event).isInstanceOf(CartActionInfoEvent.class);
        CartActionInfoEvent cartActionInfoEvent = ((CartActionInfoEvent) event);

        ObjectPropertyAssertions<CartActionInfoEvent> assertions =
                ObjectPropertyAssertions(cartActionInfoEvent);

        assertions.checkField("eventType", expectedEventType);

        assertions.checkFieldRecursively(
                "cartItem",
                toAssertionsConsumer(eCommerceCartItem)
        );

        assertions.checkAll();
    }

    @Test
    public void beginCheckoutEvent() throws Exception {
        assertOrderInfoEvent(
                eventProvider.beginCheckoutEvent(eCommerceOrder),
                OrderInfoEvent.EVENT_TYPE_BEGIN_CHECKOUT
        );
    }

    @Test
    public void purchaseEvent() throws Exception {
        assertOrderInfoEvent(eventProvider.purchaseEvent(eCommerceOrder), OrderInfoEvent.EVENT_TYPE_PURCHASE);
    }

    private void assertOrderInfoEvent(ECommerceEvent event, int expectedEventType) throws Exception {
        assertThat(event).isInstanceOf(OrderInfoEvent.class);
        OrderInfoEvent orderInfoEvent = ((OrderInfoEvent) event);

        ObjectPropertyAssertions<OrderInfoEvent> assertions =
                ObjectPropertyAssertions(orderInfoEvent);

        assertions.checkField("eventType", expectedEventType);
        assertions.checkFieldRecursively(
                "order",
                new OrderWrapperAssertionsConsumer()
                        .setExpectedIdentifier(eCommerceOrder.getIdentifier())
                        .setExpectedPayload(eCommerceOrder.getPayload())
                        .setExpectedCartItems(Collections.singletonList(toAssertionsConsumer(eCommerceCartItem)))
        );

        assertions.checkAll();
    }

    private CartItemWrapperAssertionsConsumer toAssertionsConsumer(ECommerceCartItem cartItem) {
        return new CartItemWrapperAssertionsConsumer()
                .setExpectedProduct(
                        new ProductWrapperAssertionsConsumer()
                                .setExpectedProduct(cartItem.getProduct())
                )
                .setExpectedQuantity(cartItem.getQuantity())
                .setExpectedReferrer(
                        new ReferrerWrapperAssertionsConsumer()
                                .setExpectedType(cartItem.getReferrer().getType())
                                .setExpectedId(cartItem.getReferrer().getIdentifier())
                                .setExpectedScreen(
                                        new ScreenWrapperAssertionsConsumer()
                                                .setExpectedName(cartItem.getReferrer().getScreen().getName())
                                                .setExpecredSearchQuery(
                                                        cartItem.getReferrer().getScreen().getSearchQuery()
                                                )
                                                .setExpectedCategoriesPath(
                                                        cartItem.getReferrer().getScreen().getCategoriesPath()
                                                )
                                                .setExpectedPayload(cartItem.getReferrer().getScreen().getPayload())
                                )
                )
                .setExpectedRevenue(
                        new PriceWrapperAssertionConsumer()
                                .setExpectedFiat(cartItem.getRevenue().getFiat())
                                .setExpectedInternalComponents(cartItem.getRevenue().getInternalComponents())
                );
    }
}
