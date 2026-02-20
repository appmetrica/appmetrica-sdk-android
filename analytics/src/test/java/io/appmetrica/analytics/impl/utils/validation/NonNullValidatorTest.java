package io.appmetrica.analytics.impl.utils.validation;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class NonNullValidatorTest extends CommonTest {

    private final Validator mValidator = new NonNullValidator("test non null");

    @Test
    public void testValid() {
        assertThat(mValidator.validate(mock(Object.class)).isValid()).isTrue();
    }

    @Test
    public void testNonValid() {
        assertThat(mValidator.validate(null).isValid()).isFalse();
    }

    @Test
    public void testNonValidDescription() {
        assertThat(mValidator.validate(null).getDescription()).startsWith("test non null");
    }

}
