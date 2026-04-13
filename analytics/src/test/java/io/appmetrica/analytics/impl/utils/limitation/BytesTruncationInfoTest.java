package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import org.junit.Test;

/**
 * @see BytesTruncationInfoTotalBytesTruncatedTest
 */
public class BytesTruncationInfoTest extends CommonTest {

    @Test
    public void constructor() throws Exception {
        int bytesTruncated = 4;
        Assertions.INSTANCE.ObjectPropertyAssertions(new BytesTruncatedInfo(bytesTruncated))
            .checkField("bytesTruncated", bytesTruncated)
            .checkAll();
    }

}
