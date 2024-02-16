package io.appmetrica.analytics.impl.proxy;

import io.appmetrica.analytics.impl.VerificationConstants;
import io.appmetrica.analytics.impl.proxy.validation.SilentActivationValidator;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SilentActivationValidatorTest extends CommonTest {

    @Mock
    private AppMetricaFacadeProvider provider;
    private SilentActivationValidator validator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new SilentActivationValidator(provider);
    }

    @Test
    public void validateActivated() {
        when(provider.isActivated()).thenReturn(true);
        assertThat(validator.validate().isValid()).isTrue();
    }

    @Test
    public void validateNotActivated() {
        when(provider.isActivated()).thenReturn(false);
        ValidationResult result = validator.validate();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.isValid()).isFalse();
        softly.assertThat(result.getDescription()).isEqualTo(VerificationConstants.SDK_UNINITIALIZED_ERROR);
        softly.assertThat(result.getValidatorClass()).isEqualTo(SilentActivationValidator.class);
        softly.assertAll();
    }
}
