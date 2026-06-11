package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.coreutils.internal.validation.DummyValidator;
import io.appmetrica.analytics.coreutils.internal.validation.Validator;
import io.appmetrica.analytics.impl.profile.StringUpdatePatcher;
import io.appmetrica.analytics.coreutils.internal.limitation.StringTrimmer;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
