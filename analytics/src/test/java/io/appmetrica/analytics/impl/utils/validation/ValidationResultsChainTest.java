package io.appmetrica.analytics.impl.utils.validation;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ValidationResultsChainTest extends CommonTest {

    @Test
    public void testAllValid() {
        assertThat(new ValidationResultsChain().validate(
                Arrays.asList(
                        ValidationResult.successful(mock(Validator.class)),
                        ValidationResult.successful(mock(Validator.class)),
                        ValidationResult.successful(mock(Validator.class)),
                        ValidationResult.successful(mock(Validator.class))
                )
        ).isValid()).isTrue();
    }

    @Test
    public void testOneInvalid() {
        ValidationResult result = new ValidationResultsChain().validate(
                Arrays.asList(
                        ValidationResult.successful(mock(Validator.class)),
                        ValidationResult.successful(mock(Validator.class)),
                        ValidationResult.failed(mock(Validator.class), "error"),
                        ValidationResult.successful(mock(Validator.class))
                )
        );
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDescription()).isEqualTo("error");
    }

    @Test
    public void testAllInvalid() {
        ValidationResult result = new ValidationResultsChain().validate(
                Arrays.asList(
                        ValidationResult.failed(mock(Validator.class), "error1"),
                        ValidationResult.failed(mock(Validator.class), "error2"),
                        ValidationResult.failed(mock(Validator.class), "error3"),
                        ValidationResult.failed(mock(Validator.class), "error4")
                )
        );
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDescription()).isEqualTo("error1, error2, error3, error4");
    }
}
