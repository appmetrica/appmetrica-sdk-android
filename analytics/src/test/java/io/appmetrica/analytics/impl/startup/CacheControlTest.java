package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class CacheControlTest extends CommonTest {

    @Test
    public void testCacheControl() throws Exception {
        long lastKnownLocationTtl = 6443L;

        CacheControl cacheControl =
                new CacheControl(lastKnownLocationTtl);

        ObjectPropertyAssertions<CacheControl> assertions = ObjectPropertyAssertions(cacheControl);
        assertions.checkField("lastKnownLocationTtl", lastKnownLocationTtl);
        assertions.checkAll();
    }

}
