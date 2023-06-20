package io.appmetrica.analytics.impl.utils.validation;

import io.appmetrica.analytics.ValidationException;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ThrowIfFailedValidatorTest extends CommonTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final Validator mInternalValidator = mock(Validator.class);
    private final Validator mThrowValidator = new ThrowIfFailedValidator(mInternalValidator);

    @Test
    public void testSuccess() {
        doReturn(ValidationResult.successful(mInternalValidator)).when(mInternalValidator).validate(Matchers.any());
        assertThat(mThrowValidator.validate(mock(Object.class)).isValid()).isTrue();
    }

    @Test
    public void testFailed() {
        expectedException.expect(ValidationException.class);
        String description = "internal validator failed message";

        expectedException.expectMessage(description);

        doReturn(ValidationResult.failed(mInternalValidator, description)).when(mInternalValidator).validate(Matchers.any());
        mThrowValidator.validate(mock(Object.class));
    }

}
