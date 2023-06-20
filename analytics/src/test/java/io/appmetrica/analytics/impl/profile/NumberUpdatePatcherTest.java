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
public class NumberUpdatePatcherTest extends CommonTest {

    private static final String KEY = "numberKey";

    private Userprofile.Profile.Attribute mAttribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();

    @Test
    public void testSetValue() {
        NumberUpdatePatcher update = new NumberUpdatePatcher(KEY, 300., new DummyValidator<String>(), mock(BaseSavingStrategy.class));
        update.setValue(mAttribute);
        assertThat(mAttribute.value.numberValue).isEqualTo(300);
    }

    @Test
    public void testType() {
        NumberUpdatePatcher update = new NumberUpdatePatcher(KEY, 200., new DummyValidator<String>(), mock(BaseSavingStrategy.class));
        assertThat(update.getType()).isEqualTo(Userprofile.Profile.Attribute.NUMBER);
    }

}
