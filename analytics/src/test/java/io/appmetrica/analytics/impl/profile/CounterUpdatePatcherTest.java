package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CounterUpdatePatcherTest extends CommonTest {

    private static final String KEY = "counterKey";
    private final Userprofile.Profile.Attribute mAttribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();

    @Test
    public void testValue() {
        CounterUpdatePatcher update = new CounterUpdatePatcher(KEY, 100.);
        mAttribute.value.counterModification = 100;
        update.setValue(mAttribute);
        assertThat(mAttribute.value.counterModification).isEqualTo(200);
    }

    @Test
    public void testKey() {
        CounterUpdatePatcher patcher = new CounterUpdatePatcher(KEY, 0.);
        assertThat(patcher.getType()).isEqualTo(Userprofile.Profile.Attribute.COUNTER);
    }

}
