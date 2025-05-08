package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class CommonSaverTest extends CommonTest {

    private final AttributeSaver mInternalSaver = mock(AttributeSaver.class);

    @Test
    public void testNonExisted() {
        AttributeFactory factory = mock(AttributeFactory.class);
        new CommonSavingStrategy(mInternalSaver).save(
            mock(UserProfileStorage.class),
            null,
            factory
        );
        verify(factory, times(1)).createAttribute();
        verify(mInternalSaver, times(1)).save(
            any(UserProfileStorage.class),
            nullable(Userprofile.Profile.Attribute.class)
        );
    }

    @Test
    public void testExisted() {
        AttributeFactory factory = mock(AttributeFactory.class);
        new CommonSavingStrategy(mInternalSaver).save(
            mock(UserProfileStorage.class),
            UserProfilesTestUtils.createEmpty(),
            factory
        );
        verifyNoMoreInteractions(factory);
        verifyNoMoreInteractions(mInternalSaver);
    }

}
