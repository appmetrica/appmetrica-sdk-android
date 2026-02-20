package io.appmetrica.analytics.impl.utils.validation;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyValidatorTest extends CommonTest {

    private final Validator<Object> mValidator = new DummyValidator<Object>();

    @Test
    public void testNull() {
        assertThat(mValidator.validate(null).isValid()).isTrue();
    }

    @Test
    public void testNonNull() {
        assertThat(mValidator.validate(new Object()).isValid()).isTrue();
    }

}
