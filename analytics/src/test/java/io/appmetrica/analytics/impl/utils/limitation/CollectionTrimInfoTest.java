package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

public class CollectionTrimInfoTest extends CommonTest {

    @Test
    public void constructor() throws Exception {
        int itemsDropped = 5;
        int bytesTruncated = 300;

        ObjectPropertyAssertions(new CollectionTrimInfo(itemsDropped, bytesTruncated))
            .checkField("itemsDropped", itemsDropped)
            .checkField("bytesTruncated", bytesTruncated)
            .checkAll();
    }

}
