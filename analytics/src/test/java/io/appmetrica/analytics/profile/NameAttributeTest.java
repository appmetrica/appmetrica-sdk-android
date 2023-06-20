package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.impl.profile.StringUpdatePatcher;
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class NameAttributeTest extends CommonTest {

    @Test
    public void testTrimmer() {
        StringTrimmer trimmer = (StringTrimmer) ((StringUpdatePatcher)
                new NameAttribute().withValue("value").getUserProfileUpdatePatcher()).getValueTrimmer();
        assertThat(trimmer.getMaxSize()).isEqualTo(100);
    }

    @Test
    public void testKeyValidator() {
        Validator<String> validator = ((StringUpdatePatcher)
                new NameAttribute().withValue("value").getUserProfileUpdatePatcher()).getKeyValidator();
        assertThat(validator).isExactlyInstanceOf(DummyValidator.class);
    }

}
