package io.appmetrica.analytics.impl.utils.validation;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class NonEmptyStringValidatorTest extends CommonTest {

    private final Validator<String> mValidator = new NonEmptyStringValidator("someTestObject");

    @Test
    public void testNull() {
        assertThat(mValidator.validate(null).isValid()).isFalse();
    }

    @Test
    public void testEmpty() {
        assertThat(mValidator.validate("").isValid()).isFalse();
    }

    @Test
    public void testValid() {
        assertThat(mValidator.validate("string").isValid()).isTrue();
    }

}
