package io.appmetrica.analytics.impl.utils.validation.revenue;

import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RevenueValidatorTest extends CommonTest {

    private final Validator<List<ValidationResult>> mValidator = mock(Validator.class);
    private final RevenueValidator mRevenueValidator = new RevenueValidator(mValidator);

    @Test
    public void testValid() {
        doReturn(ValidationResult.successful(mRevenueValidator)).when(mValidator).validate(any(List.class));
        ValidationResult result = mRevenueValidator.validate(mock(Revenue.class));
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testInvalid() {
        doReturn(ValidationResult.failed(mRevenueValidator, "test fail")).when(mValidator).validate(any(List.class));
        ValidationResult result = mRevenueValidator.validate(mock(Revenue.class));
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testProperValidationResultList() {
        doReturn(ValidationResult.successful(mRevenueValidator)).when(mValidator).validate(any(List.class));

        mRevenueValidator.validate(mock(Revenue.class));

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mValidator, times(1)).validate(captor.capture());
        assertThat(captor.getValue()).extracting("validatorClass").containsExactly(QuantityValidator.class);
    }

}
