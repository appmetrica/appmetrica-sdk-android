package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class BaseSavingStrategyTest extends CommonTest {

    @Test
    public void testNull() {
        assertThat(new BaseSavingStrategy(mock(AttributeSaver.class)) {
            @Nullable
            @Override
            public Userprofile.Profile.Attribute save(@NonNull UserProfileStorage storage, @Nullable Userprofile.Profile.Attribute existing, @NonNull AttributeFactory substitute) {
                return null;
            }
        }.isAttributeInvalid(null)).isTrue();
    }

    @Test
    public void testReset() {
        Userprofile.Profile.Attribute attribute = UserProfilesTestUtils.createEmpty();
        attribute.metaInfo.reset = true;
        assertThat(new BaseSavingStrategy(mock(AttributeSaver.class)) {
            @Nullable
            @Override
            public Userprofile.Profile.Attribute save(@NonNull UserProfileStorage storage, @Nullable Userprofile.Profile.Attribute existing, @NonNull AttributeFactory substitute) {
                return null;
            }
        }.isAttributeInvalid(attribute)).isTrue();
    }

    @Test
    public void testValid() {
        Userprofile.Profile.Attribute attribute = UserProfilesTestUtils.createEmpty();
        attribute.metaInfo.reset = false;
        assertThat(new BaseSavingStrategy(mock(AttributeSaver.class)) {
            @Nullable
            @Override
            public Userprofile.Profile.Attribute save(@NonNull UserProfileStorage storage, @Nullable Userprofile.Profile.Attribute existing, @NonNull AttributeFactory substitute) {
                return null;
            }
        }.isAttributeInvalid(attribute)).isFalse();
    }

}
