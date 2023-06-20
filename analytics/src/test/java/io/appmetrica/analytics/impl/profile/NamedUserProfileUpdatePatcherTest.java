package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class NamedUserProfileUpdatePatcherTest extends CommonTest {

    @Test
    public void testType() {
        assertThat(new NamedUserProfileUpdatePatcher(100, "key", new DummyValidator<String>(), mock(BaseSavingStrategy.class)) {
            @Override
            public void apply(@NonNull UserProfileStorage userProfileStorage) {

            }
        }.getType()).isEqualTo(100);
    }

    @Test
    public void testKey() {
        assertThat(new NamedUserProfileUpdatePatcher(100, "key", new DummyValidator<String>(), mock(BaseSavingStrategy.class)) {
            @Override
            public void apply(@NonNull UserProfileStorage userProfileStorage) {

            }
        }.getKey()).isEqualTo("key");
    }

    @Test
    public void testCreateAttribute() {
        Userprofile.Profile.Attribute attribute = new NamedUserProfileUpdatePatcher(200, "key", new DummyValidator<String>(), mock(BaseSavingStrategy.class)) {

            @Override
            public void apply(@NonNull UserProfileStorage userProfileStorage) {

            }
        }.createAttribute();
        assertThat(attribute.name).isEqualTo("key".getBytes());
        assertThat(attribute.type).isEqualTo(200);
    }

    @Test
    public void testValidKey() {
        Validator<String> validator = mock(Validator.class);
        doReturn(ValidationResult.successful(validator)).when(validator).validate(anyString());
        assertThat(new NamedUserProfileUpdatePatcher(100, "key", validator, mock(BaseSavingStrategy.class)) {
            @Override
            public void apply(@NonNull UserProfileStorage userProfileStorage) {

            }
        }.validateKey()).isTrue();
    }

    @Test
    public void testInvalidKey() {
        Validator<String> validator = mock(Validator.class);
        doReturn(ValidationResult.failed(validator, "some error")).when(validator).validate(anyString());
        assertThat(new NamedUserProfileUpdatePatcher(100, "key", validator, mock(BaseSavingStrategy.class)) {
            @Override
            public void apply(@NonNull UserProfileStorage userProfileStorage) {

            }
        }.validateKey()).isFalse();
    }

}
