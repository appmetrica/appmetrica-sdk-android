package io.appmetrica.analytics.impl.utils.validation.api;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiKeyValidatorTest extends CommonTest {

    private final ApiKeyValidator mValidator = new ApiKeyValidator();

    @Test
    public void testMessageContainsUrlForEmptyApiKey() {
        String description = mValidator.validate("").getDescription();
        assertThat(description).contains("https://appmetrica.io/docs/mobile-sdk-dg/android/about/android-initialize.html");
    }

    @Test
    public void testMessageContainsUrl() {
        String description = mValidator.validate("1").getDescription();
        assertThat(description).contains("https://appmetrica.io/docs/mobile-sdk-dg/android/about/android-initialize.html");
    }
}
