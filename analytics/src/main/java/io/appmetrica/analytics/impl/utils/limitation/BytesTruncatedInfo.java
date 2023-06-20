package io.appmetrica.analytics.impl.utils.limitation;

public class BytesTruncatedInfo implements BytesTruncatedProvider {
    public final int bytesTruncated;

    public BytesTruncatedInfo(int bytesTruncated) {
        this.bytesTruncated = bytesTruncated;
    }

    @Override
    public int getBytesTruncated() {
        return bytesTruncated;
    }

    public static BytesTruncatedProvider total(BytesTruncatedProvider... results) {
        return new BytesTruncatedInfo(totalBytesTruncated(results));
    }

    public static int totalBytesTruncated(BytesTruncatedProvider... results) {
        int bytesTruncated = 0;
        for (BytesTruncatedProvider result : results) {
            if (result != null) {
                bytesTruncated += result.getBytesTruncated();
            }
        }
        return bytesTruncated;
    }

    @Override
    public String toString() {
        return "BytesTruncatedInfo{" +
                "bytesTruncated=" + bytesTruncated +
                '}';
    }
}
