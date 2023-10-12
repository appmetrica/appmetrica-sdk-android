package io.appmetrica.analytics.impl.utils.validation;

import io.appmetrica.analytics.ValidationException;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ThrowIfFailedValidatorTest extends CommonTest {

    private final Validator mInternalValidator = mock(Validator.class);
    private final Validator mThrowValidator = new ThrowIfFailedValidator(mInternalValidator);

    @Test
    public void testSuccess() {
        doReturn(ValidationResult.successful(mInternalValidator)).when(mInternalValidator).validate(any());
        assertThat(mThrowValidator.validate(mock(Object.class)).isValid()).isTrue();
    }

    @Test
    public void testFailed() {
        final String description = "internal validator failed message";
        assertThatThrownBy(
            new ThrowableAssert.ThrowingCallable() {
                @Override
                public void call() throws Throwable {
                    doReturn(ValidationResult.failed(mInternalValidator, description))
                        .when(mInternalValidator).validate(any());
                    mThrowValidator.validate(mock(Object.class));
                }
            }
        ).hasMessage(description).isInstanceOf(ValidationException.class);
    }
}
