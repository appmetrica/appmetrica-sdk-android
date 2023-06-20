package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SimpleSaverTest extends CommonTest {

    @Test
    public void testAttribute() {
        UserProfileStorage storage = mock(UserProfileStorage.class);
        Userprofile.Profile.Attribute attribute = new Userprofile.Profile.Attribute();
        new SimpleSaver().save(storage, attribute);
        verify(storage, times(1)).put(attribute);
    }

}
