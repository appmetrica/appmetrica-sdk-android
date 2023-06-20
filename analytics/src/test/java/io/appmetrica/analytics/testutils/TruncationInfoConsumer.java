package io.appmetrica.analytics.testutils;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import java.util.function.Consumer;

public class TruncationInfoConsumer implements Consumer<ObjectPropertyAssertions<BytesTruncatedInfo>> {

    private int expectedBytesTruncated;

    public TruncationInfoConsumer(int expectedBytesTruncated) {
        this.expectedBytesTruncated = expectedBytesTruncated;
    }

    @Override
    public void accept(ObjectPropertyAssertions<BytesTruncatedInfo> assertions) {
        try {
            assertions.checkField("bytesTruncated", "getBytesTruncated", expectedBytesTruncated);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
