package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ECommerceProductTest extends CommonTest {

    private String sku = "sku";
    private String name = "name";
    private String secondName = "secondName";
    @Mock
    private List<String> categoriesPath;
    @Mock
    private List<String> secondCategoriesPath;
    @Mock
    private Map<String, String> payload;
    @Mock
    private Map<String, String> secondPayload;
    @Mock
    private ECommercePrice actualPrice;
    @Mock
    private ECommercePrice secondActualPrice;
    @Mock
    private ECommercePrice originalPrice;
    @Mock
    private ECommercePrice secondOriginalPrice;
    @Mock
    private List<String> promocodes;
    @Mock
    private List<String> secondPromocodes;

    private ECommerceProduct product;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        product = new ECommerceProduct(sku);
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions<ECommerceProduct> assertions = ObjectPropertyAssertions(product)
                .withDeclaredAccessibleFields(true)
                .withFinalFieldOnly(false);

        assertions.checkField("sku", "getSku", sku);
        assertions.checkField("name", "getName", null);
        assertions.checkField("categoriesPath", "getCategoriesPath", null);
        assertions.checkField("payload", "getPayload", null);
        assertions.checkField("actualPrice", "getActualPrice", null);
        assertions.checkField("originalPrice", "getOriginalPrice", null);
        assertions.checkField("promocodes", "getPromocodes", null);

        assertions.checkAll();
    }

    @Test
    public void setName() {
        product.setName(name);
        assertThat(product.getName()).isEqualTo(name);
    }

    @Test
    public void setNameTwice() {
        product.setName(name);
        product.setName(secondName);
        assertThat(product.getName()).isEqualTo(secondName);
    }

    @Test
    public void setNameForNull() {
        product.setName(null);
        assertThat(product.getName()).isNull();
    }

    @Test
    public void setNullNameAfterNonNull() {
        product.setName(name);
        product.setName(null);
        assertThat(product.getName()).isNull();
    }

    @Test
    public void setCategoriesPath() {
        product.setCategoriesPath(categoriesPath);
        assertThat(product.getCategoriesPath()).isEqualTo(categoriesPath);
    }

    @Test
    public void setCategoriesPathTwice() {
        product.setCategoriesPath(categoriesPath);
        product.setCategoriesPath(secondCategoriesPath);
        assertThat(product.getCategoriesPath()).isEqualTo(secondCategoriesPath);
    }

    @Test
    public void setCategoriesPathForNull() {
        product.setCategoriesPath(null);
        assertThat(product.getCategoriesPath()).isNull();
    }

    @Test
    public void setNullCategoriesPathAfterNonNull() {
        product.setCategoriesPath(categoriesPath);
        product.setCategoriesPath(null);
        assertThat(product.getCategoriesPath()).isNull();
    }

    @Test
    public void setPayload() {
        product.setPayload(payload);
        assertThat(product.getPayload()).isEqualTo(payload);
    }

    @Test
    public void setPayloadTwice() {
        product.setPayload(payload);
        product.setPayload(secondPayload);
        assertThat(product.getPayload()).isEqualTo(secondPayload);
    }

    @Test
    public void setPayloadForNull() {
        product.setPayload(null);
        assertThat(product.getPayload()).isNull();
    }

    @Test
    public void setNullPayloadAfterNonNull() {
        product.setPayload(payload);
        product.setPayload(null);
        assertThat(product.getPayload()).isNull();
    }

    @Test
    public void setActualPrice() {
        product.setActualPrice(actualPrice);
        assertThat(product.getActualPrice()).isEqualTo(actualPrice);
    }

    @Test
    public void setActualPriceTwice() {
        product.setActualPrice(actualPrice);
        product.setActualPrice(secondActualPrice);
        assertThat(product.getActualPrice()).isEqualTo(secondActualPrice);
    }

    @Test
    public void setActualPriceForNull() {
        product.setActualPrice(null);
        assertThat(product.getActualPrice()).isNull();
    }

    @Test
    public void setNullActualPriceAfterNonNull() {
        product.setActualPrice(actualPrice);
        product.setActualPrice(null);
        assertThat(product.getActualPrice()).isNull();
    }

    @Test
    public void setOriginalPrice() {
        product.setOriginalPrice(originalPrice);
        assertThat(product.getOriginalPrice()).isEqualTo(originalPrice);
    }

    @Test
    public void setOriginalPriceTwice() {
        product.setOriginalPrice(originalPrice);
        product.setOriginalPrice(secondOriginalPrice);
        assertThat(product.getOriginalPrice()).isEqualTo(secondOriginalPrice);
    }

    @Test
    public void setOriginalPriceForNull() {
        product.setOriginalPrice(null);
        assertThat(product.getOriginalPrice()).isNull();
    }

    @Test
    public void setNullOriginalPriceAfterNonNull() {
        product.setOriginalPrice(originalPrice);
        product.setOriginalPrice(null);
        assertThat(product.getOriginalPrice()).isNull();
    }

    @Test
    public void setPromocodes() {
        product.setPromocodes(promocodes);
        assertThat(product.getPromocodes()).isEqualTo(promocodes);
    }

    @Test
    public void setPromocodesTwice() {
        product.setPromocodes(promocodes);
        product.setPromocodes(secondPromocodes);
        assertThat(product.getPromocodes()).isEqualTo(secondPromocodes);
    }

    @Test
    public void setPromocodesForNull() {
        product.setPromocodes(null);
        assertThat(product.getPromocodes()).isNull();
    }

    @Test
    public void setNullPromocodesAfterNonNull() {
        product.setPromocodes(promocodes);
        product.setPromocodes(null);
        assertThat(product.getPromocodes()).isNull();
    }
}
