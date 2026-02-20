package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

public class ECommercePriceTest extends CommonTest {

    @Mock
    private ECommerceAmount fiat;
    @Mock
    private List<ECommerceAmount> internalComponents;
    @Mock
    private List<ECommerceAmount> secondInternalComponents;

    private ECommercePrice price;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        price = new ECommercePrice(fiat);
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions<ECommercePrice> assertions = ObjectPropertyAssertions(price)
                .withDeclaredAccessibleFields(true)
                .withFinalFieldOnly(false);

        assertions.checkField("fiat", "getFiat", fiat);
        assertions.checkField("internalComponents", "getInternalComponents", null);

        assertions.checkAll();
    }

    @Test
    public void setInternalComponents() {
        price.setInternalComponents(internalComponents);
        assertThat(price.getInternalComponents()).isEqualTo(internalComponents);
    }

    @Test
    public void setInternalComponentsTwice() {
        price.setInternalComponents(internalComponents);
        price.setInternalComponents(secondInternalComponents);
        assertThat(price.getInternalComponents()).isEqualTo(secondInternalComponents);
    }

    @Test
    public void setInternalComponentsForNull() {
        price.setInternalComponents(null);
        assertThat(price.getInternalComponents()).isNull();
    }

    @Test
    public void setNullInternalComponentsAfterNonNull() {
        price.setInternalComponents(internalComponents);
        price.setInternalComponents(null);
        assertThat(price.getInternalComponents()).isNull();
    }
}
