package io.appmetrica.analytics.impl.startup;

import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;

import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;

public class CacheControlTest extends CommonTest {

    @Test
    public void testCacheControl() throws Exception {
        long lastKnownLocationTtl = 6443L;

        CacheControl cacheControl =
            new CacheControl(lastKnownLocationTtl);

        ObjectPropertyAssertions<CacheControl> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(cacheControl);
        assertions.checkField("lastKnownLocationTtl", lastKnownLocationTtl);
        assertions.checkAll();
    }

}
