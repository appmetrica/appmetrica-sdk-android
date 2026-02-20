package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.impl.profile.BooleanUpdatePatcher;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationsEnabledAttributeTest extends CommonTest {

    @Test
    public void testKeyValidator() {
        Validator<String> validator = ((BooleanUpdatePatcher)
                new NotificationsEnabledAttribute().withValue(true).getUserProfileUpdatePatcher()).getKeyValidator();
        assertThat(validator).isExactlyInstanceOf(DummyValidator.class);
    }

}
