package io.appmetrica.analytics.impl.utils.validation.api;

import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ApiKeyParameterizedValidatorTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "Validation result for {0} is {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {TestsData.SOME_STRING_API_KEY, false},
            {TestsData.NULL_API_KEY, false},
            {TestsData.EMPTY_API_KEY, false},
            {TestsData.INT_API_KEY, false},
            {TestsData.BIG_INT_API_KEY, false},
            {TestsData.VERY_BIG_INT_API_KEY, false},
            {TestsData.UUID_API_KEY, true}
        });
    }

    private final ApiKeyValidator mValidator = new ApiKeyValidator();

    private final String mApiKey;
    private final boolean mResult;

    public ApiKeyParameterizedValidatorTest(String apiKey, boolean result) {
        mApiKey = apiKey;
        mResult = result;
    }

    @Test
    public void test() {
        assertThat(mValidator.validate(mApiKey).isValid()).isEqualTo(mResult);
    }

}
