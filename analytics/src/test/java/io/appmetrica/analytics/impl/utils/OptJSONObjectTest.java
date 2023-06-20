package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class OptJSONObjectTest extends CommonTest {

    @Test
    public void testGetLongForEmpty() {
        assertThat(new JsonHelper.OptJSONObject().getLongSilently("someKey")).isNull();
    }

    @Test
    public void testGetBooleanForEmpty() {
        assertThat(new JsonHelper.OptJSONObject().getBooleanSilently("someKey")).isNull();
    }

}
