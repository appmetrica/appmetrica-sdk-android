package io.appmetrica.analytics.testutils;

import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;

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
