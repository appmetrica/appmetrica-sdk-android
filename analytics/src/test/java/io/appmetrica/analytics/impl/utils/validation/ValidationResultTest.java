package io.appmetrica.analytics.impl.utils.validation;

import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ValidationResultTest extends CommonTest {

    @Test
    public void testSuccessful() {
        Validator validator = mock(Validator.class);
        ValidationResult result = ValidationResult.successful(validator);
        SoftAssertions softAssertion = new SoftAssertions();
        softAssertion.assertThat(result.isValid()).as("result").isTrue();
        softAssertion.assertThat(result.getDescription()).as("description").isEmpty();
        softAssertion.assertThat(result.getValidatorClass()).as("validatorClass")
            .isSameAs(validator.getClass());
        softAssertion.assertAll();
    }

    @Test
    public void testFailed() {
        Validator validator = mock(Validator.class);
        ValidationResult result = ValidationResult.failed(validator, "error");
        SoftAssertions softAssertion = new SoftAssertions();
        softAssertion.assertThat(result.isValid()).as("result").isFalse();
        softAssertion.assertThat(result.getDescription()).as("description").isEqualTo("error");
        softAssertion.assertThat(result.getValidatorClass()).as("validatorClass")
            .isSameAs(validator.getClass());
        softAssertion.assertAll();
    }

}
