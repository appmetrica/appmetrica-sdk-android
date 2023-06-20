package io.appmetrica.analytics.impl.utils.limitation;

public class CollectionTrimInfo extends BytesTruncatedInfo {
    public final int itemsDropped;

    public CollectionTrimInfo(int itemsDropped, int bytesTruncated) {
        super(bytesTruncated);
        this.itemsDropped = itemsDropped;
    }

    @Override
    public String toString() {
        return "CollectionTrimInfo{" +
                "itemsDropped=" + itemsDropped +
                ", bytesTruncated=" + bytesTruncated +
                '}';
    }
}
