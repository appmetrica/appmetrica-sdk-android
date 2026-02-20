package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SameNameComposerTest extends CommonTest {

    private final SameNameComposer mSameNameComposer = new SameNameComposer();

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
