package io.appmetrica.analytics.impl.ecommerce.client.converter;

final class Limits {

    static final int CURRENCY_LENGTH = 20;
    static final int CATEGORY_PATH_ITEMS_COUNT = 20;
    static final int CATEGORY_PATH_ITEM_LENGTH = 100;
    static final int ORDER_ID_LENGTH = 100;
    static final int TOTAL_ECOMMERCE_PROTO = 200 * 1024;
    static final int TOTAL_PAYLOAD_BYTES = 20 * 1024;
    static final int PAYLOAD_KEY_LENGTH = 100;
    static final int PAYLOAD_VALUE_LENGTH = 1000;
    static final int PRICE_INTERNAL_COMPONENTS_COUNT = 30;
    static final int PRODUCT_SKU_LENGTH = 100;
    static final int PRODUCT_NAME_LENGTH = 1000;
    static final int PROMOCODES_ITEMS_COUNT = 20;
    static final int PROMOCODES_ITEM_LENGTH = 100;
    static final int REFERRER_TYPE_LENGTH = 100;
    static final int REFERRER_ID_LENGTH = 2 * 1024;
    static final int SCREEN_NAME_LENGTH = 100;
    static final int SCREEN_SEARCH_QUERY_LENGTH = 1000;

    private Limits() {
    }
}
