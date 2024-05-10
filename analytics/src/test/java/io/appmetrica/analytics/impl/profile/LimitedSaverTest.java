package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.CollectionLimitation;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class LimitedSaverTest extends CommonTest {

    @Test
    public void notExistedAndHasSpace() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        LimitedSaver saver = new LimitedSaver(new CollectionLimitation(10));
        doReturn(9).when(storage).getLimitedAttributeCount();
        Userprofile.Profile.Attribute attribute = new Userprofile.Profile.Attribute();

        saver.save(storage, attribute);

        verify(storage, times(1)).put(attribute);
    }

    @Test
    public void testExistedAndLimitReached() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        LimitedSaver saver = new LimitedSaver(new CollectionLimitation(10));
        doReturn(10).when(storage).getLimitedAttributeCount();
        Userprofile.Profile.Attribute attribute = new Userprofile.Profile.Attribute();

        doReturn(attribute).when(storage).get(anyInt(), anyString());

        saver.save(storage, attribute);

        verify(storage, times(1)).put(attribute);
    }

    @Test
    public void testNotExistedAndLimitReached() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        LimitedSaver saver = new LimitedSaver(new CollectionLimitation(10));
        doReturn(10).when(storage).getLimitedAttributeCount();
        Userprofile.Profile.Attribute attribute = new Userprofile.Profile.Attribute();

        saver.save(storage, attribute);

        verify(storage, never()).put(any(Userprofile.Profile.Attribute.class));
    }

    @Test
    public void testNotExistedAndHasNoSpace() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        LimitedSaver saver = new LimitedSaver(new CollectionLimitation(10));
        doReturn(11).when(storage).getLimitedAttributeCount();
        Userprofile.Profile.Attribute attribute = new Userprofile.Profile.Attribute();

        saver.save(storage, attribute);

        verify(storage, never()).put(any(Userprofile.Profile.Attribute.class));
    }

}
