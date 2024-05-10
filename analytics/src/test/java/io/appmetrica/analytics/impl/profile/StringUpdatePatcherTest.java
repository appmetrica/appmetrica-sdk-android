package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.DummyTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.Trimmer;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class StringUpdatePatcherTest extends CommonTest {

    private static final String KEY = "stringKey";
    private static final String VALUE = "value";

    private Userprofile.Profile.Attribute mAttribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();

    @Test
    public void testValue() {
        StringUpdatePatcher update = new StringUpdatePatcher(KEY, VALUE, new DummyTrimmer<String>(), new DummyValidator<String>(), mock(BaseSavingStrategy.class));
        update.setValue(mAttribute);
        assertThat(mAttribute.value.stringValue).isEqualTo(VALUE.getBytes());
    }

    @Test
    public void testType() {
        StringUpdatePatcher update = new StringUpdatePatcher(KEY, VALUE, new DummyTrimmer<String>(), new DummyValidator<String>(), mock(BaseSavingStrategy.class));
        assertThat(update.getType()).isEqualTo(Userprofile.Profile.Attribute.STRING);
    }

    @Test
    public void testTrimming() {
        Trimmer<String> trimmer = mock(Trimmer.class);
        doReturn("").when(trimmer).trim(anyString());
        new StringUpdatePatcher(
                KEY,
                VALUE,
                trimmer,
                new DummyValidator<String>(),
                new CommonSavingStrategy(new SimpleSaver())
        ).apply(new UserProfileStorage());
        verify(trimmer, times(1)).trim(VALUE);
    }
}
