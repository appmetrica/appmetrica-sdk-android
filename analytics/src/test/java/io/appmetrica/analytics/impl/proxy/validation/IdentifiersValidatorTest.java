package io.appmetrica.analytics.impl.proxy.validation;

import io.appmetrica.analytics.impl.startup.Constants;
import io.appmetrica.analytics.impl.utils.validation.IdentifiersValidator;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class IdentifiersValidatorTest extends CommonTest {

    private final HashSet<String> mValidIdentifiers = new HashSet<String>();
    private IdentifiersValidator mIdentifiersValidator;

    @Before
    public void setUp() {
        mValidIdentifiers.add(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH);
        mValidIdentifiers.add(Constants.StartupParamsCallbackKeys.UUID);
        mValidIdentifiers.add(Constants.StartupParamsCallbackKeys.DEVICE_ID);
        mValidIdentifiers.add(Constants.StartupParamsCallbackKeys.REPORT_AD_URL);
        mValidIdentifiers.add(Constants.StartupParamsCallbackKeys.GET_AD_URL);
        mValidIdentifiers.add(Constants.StartupParamsCallbackKeys.CLIDS);
        mIdentifiersValidator = new IdentifiersValidator("identifiers", mValidIdentifiers);
    }

    @Test
    public void testValidateEmpty() {
        assertThat(mIdentifiersValidator.validate(Collections.EMPTY_LIST).isValid()).isTrue();
    }

    @Test
    public void testValidateInvalidIdentifier() {
        ValidationResult result = mIdentifiersValidator.validate(Collections.singletonList("not_identifier"));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDescription()).contains("invalid identifier");
    }

    @Test
    public void testValidateValidAndInvalidIdentifiers() {
        ValidationResult result = mIdentifiersValidator.validate(Arrays.asList(
            Constants.StartupParamsCallbackKeys.DEVICE_ID,
            "not_identifier"
        ));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDescription()).contains("invalid identifier");
    }

    @Test
    public void testValidateValidIdentifiers() {
        assertThat(mIdentifiersValidator.validate(Arrays.asList(
            Constants.StartupParamsCallbackKeys.UUID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
            Constants.StartupParamsCallbackKeys.GET_AD_URL,
            Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
            Constants.StartupParamsCallbackKeys.CLIDS
        )).isValid()).isTrue();
    }
}
