package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

/**
 * @see BytesTruncationInfoTotalBytesTruncatedTest
 */
public class BytesTruncationInfoTest extends CommonTest {

    @Test
    public void constructor() throws Exception {
        int bytesTruncated = 4;
        ObjectPropertyAssertions(new BytesTruncatedInfo(bytesTruncated))
            .checkField("bytesTruncated", bytesTruncated)
            .checkAll();
    }

}
