package io.appmetrica.analytics.impl.utils.validation.revenue;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class QuantityValidatorTest extends CommonTest {

    private final QuantityValidator mValidator = new QuantityValidator();

    @Test
    public void testValidQuantity() {
        assertThat(mValidator.validate(1).isValid()).isTrue();
    }

    @Test
    public void testZeroQuantity() {
        assertThat(mValidator.validate(0).isValid()).isFalse();
    }

    @Test
    public void testNegativeQuantity() {
        assertThat(mValidator.validate(-1).isValid()).isFalse();
    }

    @Test
    public void testNull() {
        assertThat(mValidator.validate(null).isValid()).isTrue();
    }

}
