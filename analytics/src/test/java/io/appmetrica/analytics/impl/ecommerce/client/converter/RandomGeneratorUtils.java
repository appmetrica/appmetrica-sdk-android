package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.util.Random;

class RandomGeneratorUtils {

    public static Ecommerce.ECommerceEvent.Decimal generateDecimal() {
        Ecommerce.ECommerceEvent.Decimal decimal = new Ecommerce.ECommerceEvent.Decimal();
        decimal.exponent = new Random().nextInt();
        decimal.mantissa = new Random().nextLong();
        return decimal;
    }

    public static Ecommerce.ECommerceEvent.Price generatePrice() {
        Ecommerce.ECommerceEvent.Price price = new Ecommerce.ECommerceEvent.Price();
        price.fiat = generateAmount();
        price.internalComponents = new Ecommerce.ECommerceEvent.Amount[7];
        for (int i = 0; i < 7; i++) {
            price.internalComponents[i] = generateAmount();
        }
        return price;
    }

    public static Ecommerce.ECommerceEvent.Amount generateAmount() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(10);
        Ecommerce.ECommerceEvent.Amount amount = new Ecommerce.ECommerceEvent.Amount();
        amount.value = new Ecommerce.ECommerceEvent.Decimal();
        amount.value.exponent = new Random().nextInt();
        amount.value.mantissa = new Random().nextLong();
        amount.unitType = randomStringGenerator.nextString().getBytes();

        return amount;
    }

    public static Ecommerce.ECommerceEvent.Payload generatePayload() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(24);
        Ecommerce.ECommerceEvent.Payload payload = new Ecommerce.ECommerceEvent.Payload();
        payload.truncatedPairsCount = new Random().nextInt();
        payload.pairs = new Ecommerce.ECommerceEvent.Payload.Pair[8];
        for (int i = 0; i < 8; i++) {
            payload.pairs[i] = new Ecommerce.ECommerceEvent.Payload.Pair();
            payload.pairs[i].key = randomStringGenerator.nextString().getBytes();
            payload.pairs[i].value = randomStringGenerator.nextString().getBytes();
        }
        return payload;
    }

    public static Ecommerce.ECommerceEvent.Category generateCategory() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(7);
        Ecommerce.ECommerceEvent.Category category = new Ecommerce.ECommerceEvent.Category();
        category.path = new byte[4][];
        for (int i = 0; i < 4; i++) {
            category.path[i] = randomStringGenerator.nextString().getBytes();
        }
        return category;
    }

    public static Ecommerce.ECommerceEvent.PromoCode[] generatePromocodes() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(12);
        Ecommerce.ECommerceEvent.PromoCode[] promoCodes = new Ecommerce.ECommerceEvent.PromoCode[3];
        for (int i = 0; i < 3; i++) {
            promoCodes[i] = new Ecommerce.ECommerceEvent.PromoCode();
            promoCodes[i].code = randomStringGenerator.nextString().getBytes();
        }
        return promoCodes;
    }

    public static Ecommerce.ECommerceEvent.Screen generateScreen() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(22);

        Ecommerce.ECommerceEvent.Screen screen = new Ecommerce.ECommerceEvent.Screen();
        screen.payload = generatePayload();
        screen.searchQuery = randomStringGenerator.nextString().getBytes();
        screen.name = randomStringGenerator.nextString().getBytes();
        screen.category = generateCategory();

        return screen;
    }

    public static Ecommerce.ECommerceEvent.Product generateProduct() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(11);

        Ecommerce.ECommerceEvent.Product product = new Ecommerce.ECommerceEvent.Product();
        product.actualPrice = generatePrice();
        product.originalPrice = generatePrice();
        product.name = randomStringGenerator.nextString().getBytes();
        product.category = generateCategory();
        product.promoCodes = generatePromocodes();
        product.sku = randomStringGenerator.nextString().getBytes();

        return product;
    }

    public static Ecommerce.ECommerceEvent.Referrer generateReferrer() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(13);
        Ecommerce.ECommerceEvent.Referrer referrer = new Ecommerce.ECommerceEvent.Referrer();
        referrer.screen = generateScreen();
        referrer.type = randomStringGenerator.nextString().getBytes();
        referrer.id = randomStringGenerator.nextString().getBytes();

        return referrer;
    }

    public static Ecommerce.ECommerceEvent.CartItem generateCartItem() {
        Ecommerce.ECommerceEvent.CartItem cartItem = new Ecommerce.ECommerceEvent.CartItem();
        cartItem.product = generateProduct();
        cartItem.quantity = generateDecimal();
        cartItem.revenue = generatePrice();
        cartItem.referrer = generateReferrer();

        return cartItem;
    }

}
