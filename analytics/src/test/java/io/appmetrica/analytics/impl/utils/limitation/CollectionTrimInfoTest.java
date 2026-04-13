package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import org.junit.Test;

public class CollectionTrimInfoTest extends CommonTest {

    @Test
    public void constructor() throws Exception {
        int itemsDropped = 5;
        int bytesTruncated = 300;

        Assertions.INSTANCE.ObjectPropertyAssertions(new CollectionTrimInfo(itemsDropped, bytesTruncated))
            .checkField("itemsDropped", itemsDropped)
            .checkField("bytesTruncated", bytesTruncated)
            .checkAll();
    }

}
