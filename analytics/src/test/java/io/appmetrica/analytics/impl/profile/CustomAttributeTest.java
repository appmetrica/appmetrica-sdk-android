package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CustomAttributeTest extends CommonTest {

    @Test
    public void testName() {
        assertThat(new CustomAttribute("blablakey", new DummyValidator<String>(), new SimpleSaver()).getKey()).isEqualTo("blablakey");
    }

}
