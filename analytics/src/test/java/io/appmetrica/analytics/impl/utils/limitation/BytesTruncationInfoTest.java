package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

/**
 * @see BytesTruncationInfoTotalBytesTruncatedTest
 */
@RunWith(RobolectricTestRunner.class)
public class BytesTruncationInfoTest extends CommonTest {

    @Test
    public void constructor() throws Exception {
        int bytesTruncated = 4;
        ObjectPropertyAssertions(new BytesTruncatedInfo(bytesTruncated))
                .checkField("bytesTruncated", bytesTruncated)
                .checkAll();
    }

}
