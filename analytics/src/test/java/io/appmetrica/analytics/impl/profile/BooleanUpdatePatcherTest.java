package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class BooleanUpdatePatcherTest extends CommonTest {

    private static final String KEY = "boolKey";

    private Userprofile.Profile.Attribute mAttribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();

    @Test
    public void testValue() {
        BooleanUpdatePatcher update = new BooleanUpdatePatcher(KEY, true, new DummyValidator<String>(), mock(BaseSavingStrategy.class));
        update.setValue(mAttribute);
        assertThat(mAttribute.value.boolValue).isTrue();
    }

    @Test
    public void testType() {
        BooleanUpdatePatcher update = new BooleanUpdatePatcher(KEY, true, new DummyValidator<String>(), mock(BaseSavingStrategy.class));
        assertThat(update.getType()).isEqualTo(Userprofile.Profile.Attribute.BOOL);
    }

}
