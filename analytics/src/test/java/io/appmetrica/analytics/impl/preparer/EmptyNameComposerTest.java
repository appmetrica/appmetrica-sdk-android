package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class EmptyNameComposerTest extends CommonTest {

    private final EmptyNameComposer mEmptyNameComposer = new EmptyNameComposer();

    @Test
    public void testGetName() {
        assertThat(mEmptyNameComposer.getName("some name")).isNotNull().isEmpty();
    }

    @Test
    public void testGetNameNull() {
        assertThat(mEmptyNameComposer.getName(null)).isNotNull().isEmpty();
    }
}
