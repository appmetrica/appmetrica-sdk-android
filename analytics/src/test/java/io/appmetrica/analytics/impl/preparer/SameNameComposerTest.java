package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SameNameComposerTest extends CommonTest {

    private SameNameComposer mSameNameComposer = new SameNameComposer();

    @Test
    public void testGetName() {
        final String name = "some name";
        assertThat(mSameNameComposer.getName(name)).isEqualTo(name);
    }

    @Test
    public void testGetNameNull() {
        assertThat(mSameNameComposer.getName(null)).isNull();
    }
}
