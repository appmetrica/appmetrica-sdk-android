package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class KeyValidatorTest extends CommonTest {

    KeyValidator mValidator = new KeyValidator();

    @Test
    public void testValidValue() {
        assertThat(mValidator.validate("simpleKey").isValid()).isTrue();
    }

    @Test
    public void testLongValue() {
        assertThat(mValidator.validate(
                new RandomStringGenerator(EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_KEY_MAX_LENGTH + 1).nextString()
        ).isValid()).isFalse();
    }

    @Test
    public void testAppmetricaKey() {
        assertThat(mValidator.validate("appmetricasimpleKey").isValid()).isFalse();
    }

    @Test
    public void testNull() {
        assertThat(mValidator.validate(null).isValid()).isFalse();
    }

}
