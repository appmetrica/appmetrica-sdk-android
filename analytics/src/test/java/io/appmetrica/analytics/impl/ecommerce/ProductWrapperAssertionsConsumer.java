package io.appmetrica.analytics.impl.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.ecommerce.ECommercePrice;
import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProductWrapper;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class ProductWrapperAssertionsConsumer implements Consumer<ObjectPropertyAssertions<ProductWrapper>> {

    private String expectedSku;
    private String expectedName;
    private List<String> expectedCategoriesPath;
    private Map<String, String> expectedPaylaod;
    private PriceWrapperAssertionConsumer expectedActualPrice;
    private PriceWrapperAssertionConsumer expectedOriginalPrice;
    private List<String> expectedPromocodes;

    @Override
    public void accept(ObjectPropertyAssertions<ProductWrapper> assertions) {

        try {
            assertions.checkField("sku", expectedSku);
            assertions.checkField("name", expectedName);
            assertions.checkField("categoriesPath", expectedCategoriesPath, true);
            assertions.checkField("payload", expectedPaylaod);
            assertions.checkFieldRecursively("actualPrice", expectedActualPrice);
            assertions.checkFieldRecursively("originalPrice", expectedOriginalPrice);
            assertions.checkField("promocodes", expectedPromocodes, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ProductWrapperAssertionsConsumer setExpectedProduct(ECommerceProduct expectedProduct) {
        setExpectedSku(expectedProduct.getSku());
        setExpectedName(expectedProduct.getName());
        setExpectedCategoriesPath(expectedProduct.getCategoriesPath());
        setExpectedPaylaod(expectedProduct.getPayload());
        setExpectedActualPrice(toPriceConsumer(expectedProduct.getActualPrice()));
        setExpectedOriginalPrice(toPriceConsumer(expectedProduct.getOriginalPrice()));
        setExpectedPromocodes(expectedProduct.getPromocodes());
        return this;
    }

    private PriceWrapperAssertionConsumer toPriceConsumer(ECommercePrice price) {
        PriceWrapperAssertionConsumer result = null;
        if (price != null) {
            result = new PriceWrapperAssertionConsumer()
                    .setExpectedFiat(price.getFiat())
                    .setExpectedInternalComponents(price.getInternalComponents());
        }
        return result;
    }

    public void setExpectedSku(String expectedSku) {
        this.expectedSku = expectedSku;
    }

    public void setExpectedName(String expectedName) {
        this.expectedName = expectedName;
    }

    public void setExpectedCategoriesPath(List<String> expectedCategoriesPath) {
        this.expectedCategoriesPath = expectedCategoriesPath;
    }

    public void setExpectedPaylaod(Map<String, String> expectedPaylaod) {
        this.expectedPaylaod = expectedPaylaod;
    }

    public void setExpectedActualPrice(PriceWrapperAssertionConsumer expectedActualPrice) {
        this.expectedActualPrice = expectedActualPrice;
    }

    public void setExpectedOriginalPrice(PriceWrapperAssertionConsumer expectedOriginalPrice) {
        this.expectedOriginalPrice = expectedOriginalPrice;
    }

    public void setExpectedPromocodes(List<String> expectedPromocodes) {
        this.expectedPromocodes = expectedPromocodes;
    }
}
