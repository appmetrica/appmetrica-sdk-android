package io.appmetrica.analytics.impl.utils.validation.api;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ReporterKeyIsUsedValidatorTest extends CommonTest {

    private final Map<String, ?> mMap = mock(Map.class);
    private final ReporterKeyIsUsedValidator mValidator = new ReporterKeyIsUsedValidator(mMap);

    @Test
    public void testNonUsed() {
        doReturn(false).when(mMap).containsKey(anyString());
        assertThat(mValidator.validate("").isValid()).isTrue();
    }

    @Test
    public void testUsed() {
        String key = "key";
        doReturn(true).when(mMap).containsKey(key);
        assertThat(mValidator.validate(key).isValid()).isFalse();
    }

}
