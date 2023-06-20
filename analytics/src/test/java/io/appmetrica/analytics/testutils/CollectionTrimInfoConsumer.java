package io.appmetrica.analytics.testutils;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;

public class CollectionTrimInfoConsumer extends TruncationInfoConsumer {

    private int expectedItemsDropped;

    public CollectionTrimInfoConsumer(int expectedBytesTruncated, int expectedItemsDropped) {
        super(expectedBytesTruncated);
        this.expectedItemsDropped = expectedItemsDropped;
    }

    @Override
    public void accept(ObjectPropertyAssertions<BytesTruncatedInfo> assertions) {
        super.accept(assertions);
        try {
            assertions.checkField("itemsDropped", expectedItemsDropped);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
