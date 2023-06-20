package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SetIfUndefinedSaverTest extends CommonTest {

    private UserProfileStorage mStorage = mock(UserProfileStorage.class);
    private AttributeSaver mInternalSaver = mock(AttributeSaver.class);
    private SetIfUndefinedSavingStrategy mIfUndefinedSaver = new SetIfUndefinedSavingStrategy(mInternalSaver);

    @Test
    public void testNonExisted() {
        AttributeFactory factory = mock(AttributeFactory.class);
        Userprofile.Profile.Attribute attributeFromFactory = UserProfilesTestUtils.createEmpty();
        doReturn(attributeFromFactory).when(factory).createAttribute();
        mIfUndefinedSaver.save(mStorage, null, factory);

        ArgumentCaptor<Userprofile.Profile.Attribute> captor =
                ArgumentCaptor.forClass(Userprofile.Profile.Attribute.class);
        verify(mInternalSaver, times(1)).save(
                same(mStorage),
                captor.capture()
        );
        assertThat(captor.getValue().metaInfo.setIfUndefined).isTrue();
    }

    @Test
    public void testExisted() {
        mIfUndefinedSaver.save(mStorage, UserProfilesTestUtils.createEmpty(), mock(AttributeFactory.class));
        verify(mInternalSaver, never()).save(
                any(UserProfileStorage.class),
                any(Userprofile.Profile.Attribute.class)
        );
    }

}
