package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.profile.StringUpdatePatcher;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.DummyTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.Trimmer;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class GenderAttributeTest extends CommonTest {

    @Test
    public void testStringValue() {
        StringUpdatePatcher patcher = (StringUpdatePatcher) new GenderAttribute()
                .withValue(GenderAttribute.Gender.MALE).getUserProfileUpdatePatcher();
        assertThat(patcher.getAttributeSavingStrategy()).isNotInstanceOf(SetIfUndefinedSavingStrategy.class);
        assertThat(patcher.getValue()).isEqualTo("M");
        assertThat(patcher.getKeyValidator()).isExactlyInstanceOf(DummyValidator.class);
    }

    @Test
    public void testStringPermanentValue() {
        StringUpdatePatcher patcher = (StringUpdatePatcher) new GenderAttribute()
                .withValueIfUndefined(GenderAttribute.Gender.FEMALE).getUserProfileUpdatePatcher();
        assertThat(patcher.getAttributeSavingStrategy()).isInstanceOf(SetIfUndefinedSavingStrategy.class);
        assertThat(patcher.getValue()).isEqualTo("F");
        assertThat(patcher.getKeyValidator()).isExactlyInstanceOf(DummyValidator.class);
    }

    @Test
    public void testResetStringAttribute() {
        ResetUpdatePatcher patcher = (ResetUpdatePatcher) Attribute
                .gender().withValueReset().getUserProfileUpdatePatcher();
        assertThat(patcher.getType()).isEqualTo(Userprofile.Profile.Attribute.STRING);
        assertThat(patcher.getKeyValidator()).isExactlyInstanceOf(DummyValidator.class);
    }

    @Test
    public void testTrimmer() {
        Trimmer<String> trimmer = ((StringUpdatePatcher)
                new GenderAttribute().withValue(GenderAttribute.Gender.MALE).getUserProfileUpdatePatcher()).getValueTrimmer();
        assertThat(trimmer).isExactlyInstanceOf(DummyTrimmer.class);
    }

    @RunWith(Parameterized.class)
    public static class EnumToStringMappingTest {

        @Parameters(name = "for {0} should be {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {GenderAttribute.Gender.MALE, "M"},
                    {GenderAttribute.Gender.FEMALE, "F"},
                    {GenderAttribute.Gender.OTHER, "O"},
            });
        }

        private final GenderAttribute.Gender mGender;
        private final String mTypeName;

        public EnumToStringMappingTest(GenderAttribute.Gender gender, @NonNull String typeName) {
            mGender = gender;
            mTypeName = typeName;
        }

        @Test
        public void testMapping() {
            assertThat(mGender.getStringValue()).isEqualTo(mTypeName);
        }

    }

}
