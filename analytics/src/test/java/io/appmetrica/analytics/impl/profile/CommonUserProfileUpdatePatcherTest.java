package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class CommonUserProfileUpdatePatcherTest extends CommonTest {

    private AttributeSaver mAttributeSaver = mock(AttributeSaver.class);

    @Test
    public void testType() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        doReturn(null).when(storage).get(anyInt(), anyString());
        ArgumentCaptor<Userprofile.Profile.Attribute> attribute =
                ArgumentCaptor.forClass(Userprofile.Profile.Attribute.class);
        new CommonUserProfileUpdatePatcher(999, "key", 200, new DummyValidator<String>(), new CommonSavingStrategy(mAttributeSaver)) {
            @Override
            protected void setValue(@NonNull Userprofile.Profile.Attribute attribute) {

            }
        }.apply(storage);
        verify(mAttributeSaver, times(1))
                .save(any(UserProfileStorage.class), attribute.capture());
        assertThat(attribute.getValue().type).isEqualTo(999);
    }

    @Test
    public void testExistingAttribute() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        doReturn(createEmptyAttribute()).when(storage).get(anyInt(), anyString());
        new CommonUserProfileUpdatePatcher(999, "key", 200, new DummyValidator<String>(), new CommonSavingStrategy(mAttributeSaver)) {
            @Override
            protected void setValue(@NonNull Userprofile.Profile.Attribute attribute) {

            }
        }.apply(storage);
        verify(storage, times(1)).get(999, "key");
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void testNonExistingAttribute() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        doReturn(null).when(storage).get(anyInt(), anyString());
        new CommonUserProfileUpdatePatcher(999, "key", 200, new DummyValidator<String>(), new CommonSavingStrategy(mAttributeSaver)) {
            @Override
            protected void setValue(@NonNull Userprofile.Profile.Attribute attribute) {

            }
        }.apply(storage);
        ArgumentCaptor<Userprofile.Profile.Attribute> captor = ArgumentCaptor.forClass(Userprofile.Profile.Attribute.class);
        verify(storage, times(1)).get(999, "key");
        verify(mAttributeSaver, times(1))
                .save(any(UserProfileStorage.class), captor.capture());
        assertThat(captor.getValue().type).isEqualTo(999);
        assertThat(captor.getValue().name).isEqualTo("key".getBytes());
    }

    @Test
    public void testValidKey() {
        Validator<String> validator = mock(Validator.class);
        doReturn(ValidationResult.successful(validator)).when(validator).validate(anyString());
        UserProfileStorage storage = mock(UserProfileStorage.class);
        new CommonUserProfileUpdatePatcher(100, "key", 10, validator, new CommonSavingStrategy(mAttributeSaver)) {

            @Override
            protected void setValue(@NonNull Userprofile.Profile.Attribute attribute) {

            }
        }.apply(storage);
        ArgumentCaptor<Userprofile.Profile.Attribute> captor = ArgumentCaptor.forClass(Userprofile.Profile.Attribute.class);
        verify(mAttributeSaver, times(1)).save(any(UserProfileStorage.class), captor.capture());
        assertThat(captor.getValue().name).isEqualTo("key".getBytes());
        assertThat(captor.getValue().type).isEqualTo(100);
    }

    @Test
    public void testInvalidKey() {
        Validator<String> validator = mock(Validator.class);
        doReturn(ValidationResult.failed(validator, "some error")).when(validator).validate(anyString());
        UserProfileStorage storage = mock(UserProfileStorage.class);
        new CommonUserProfileUpdatePatcher(100, "key", 10, validator, new CommonSavingStrategy(mAttributeSaver)) {

            @Override
            protected void setValue(@NonNull Userprofile.Profile.Attribute attribute) {

            }
        }.apply(storage);
        verifyNoMoreInteractions(storage);
        verifyNoMoreInteractions(mAttributeSaver);
    }

    public static Userprofile.Profile.Attribute createEmptyAttribute() {
        Userprofile.Profile.Attribute attribute = new Userprofile.Profile.Attribute();
        attribute.metaInfo = new Userprofile.Profile.AttributeMetaInfo();
        attribute.value = new Userprofile.Profile.AttributeValue();
        return attribute;
    }

}
