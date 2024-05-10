package io.appmetrica.analytics.impl.profile;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class ResetUpdatePatcherTest extends CommonTest {

    private static final int TYPE = 100500;
    private static final String KEY = "key";

    private AttributeSaver mAttributeSaver = mock(AttributeSaver.class);
    private ResetUpdatePatcher mPatcher = new ResetUpdatePatcher(TYPE, KEY, new DummyValidator<String>(), mAttributeSaver);

    @Test
    public void testNonExistedValue() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        doReturn(null).when(storage).get(anyInt(), anyString());
        mPatcher.apply(storage);

        ArgumentCaptor<Userprofile.Profile.Attribute> captor =
                ArgumentCaptor.forClass(Userprofile.Profile.Attribute.class);
        verify(mAttributeSaver, times(1))
                .save(any(UserProfileStorage.class), captor.capture());
        assertThat(captor.getValue().metaInfo.reset).isTrue();
    }

    @Test
    public void testExistedValue() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        Userprofile.Profile.Attribute attribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();
        doReturn(attribute).when(storage).get(TYPE, KEY);
        mPatcher.apply(storage);

        ArgumentCaptor<Userprofile.Profile.Attribute> captor =
                ArgumentCaptor.forClass(Userprofile.Profile.Attribute.class);
        verify(mAttributeSaver, times(1))
                .save(any(UserProfileStorage.class), captor.capture());
        assertThat(captor.getValue()).isNotSameAs(attribute);
        assertThat(captor.getValue().metaInfo.reset).isTrue();
    }

    @Test
    public void testValidKey() {
        Validator<String> validator = mock(Validator.class);
        doReturn(ValidationResult.successful(validator)).when(validator).validate(anyString());

        ArgumentCaptor<Userprofile.Profile.Attribute> captor =
                ArgumentCaptor.forClass(Userprofile.Profile.Attribute.class);
        new ResetUpdatePatcher(100, "key", validator, mAttributeSaver).apply(mock(UserProfileStorage.class));
        Userprofile.Profile.Attribute attribute = verify(mAttributeSaver, times(1))
                .save(any(UserProfileStorage.class), captor.capture());
        assertThat(captor.getValue().name).isEqualTo("key".getBytes());
        assertThat(captor.getValue().type).isEqualTo(100);
    }

    @Test
    public void testInvalidKey() {
        Validator<String> validator = mock(Validator.class);
        doReturn(ValidationResult.failed(validator, "some error")).when(validator).validate(anyString());
        new ResetUpdatePatcher(100, "key", validator, mAttributeSaver).apply(mock(UserProfileStorage.class));
        verifyNoMoreInteractions(mAttributeSaver);
    }
}
