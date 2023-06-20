package io.appmetrica.analytics.impl.referrer.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.protobuf.client.ReferrerInfoClient;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.protobuf.nano.MessageNano;

public final class ReferrerInfo {

    public enum Source {
        UNKNOWN("unknown"),
        GP(Constants.INSTALL_REFERRER_SOURCE_GPL),
        HMS(Constants.INSTALL_REFERRER_SOURCE_HMS);

        public final String value;

        Source(String value) {
            this.value = value;
        }
    }

    @NonNull
    public final String installReferrer;
    public final long referrerClickTimestampSeconds;
    public final long installBeginTimestampSeconds;
    @NonNull
    public final Source source;

    @Nullable
    public static ReferrerInfo parseFrom(@NonNull byte[] proto) throws InvalidProtocolBufferNanoException {
        if (Utils.isNullOrEmpty(proto)) {
            return null;
        } else {
            return new ReferrerInfo(proto);
        }
    }

    private ReferrerInfo(@NonNull byte[] proto) throws InvalidProtocolBufferNanoException {
        ReferrerInfoClient client = ReferrerInfoClient.parseFrom(proto);
        installReferrer = client.value;
        referrerClickTimestampSeconds = client.clickTimeSeconds;
        installBeginTimestampSeconds = client.installBeginTimeSeconds;
        source = sourceToModel(client.source);
    }

    public ReferrerInfo(@NonNull String installReferrer,
                        long referrerClickTimestampSeconds,
                        long installBeginTimestampSeconds,
                        @NonNull Source source) {
        this.installReferrer = installReferrer;
        this.referrerClickTimestampSeconds = referrerClickTimestampSeconds;
        this.installBeginTimestampSeconds = installBeginTimestampSeconds;
        this.source = source;
    }

    public byte[] toProto() {
        ReferrerInfoClient client = new ReferrerInfoClient();
        client.value = installReferrer;
        client.clickTimeSeconds = referrerClickTimestampSeconds;
        client.installBeginTimeSeconds = installBeginTimestampSeconds;
        client.source = sourceToProto(source);
        return MessageNano.toByteArray(client);
    }

    @NonNull
    private Source sourceToModel(int proto) {
        switch (proto) {
            case ReferrerInfoClient.GP:
                return Source.GP;
            case ReferrerInfoClient.HMS:
                return Source.HMS;
            default:
                return Source.UNKNOWN;
        }
    }

    private int sourceToProto(@NonNull Source source) {
        switch (source) {
            case GP:
                return ReferrerInfoClient.GP;
            case HMS:
                return ReferrerInfoClient.HMS;
            default:
                return ReferrerInfoClient.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return "ReferrerInfo{" +
                "installReferrer='" + installReferrer + '\'' +
                ", referrerClickTimestampSeconds=" + referrerClickTimestampSeconds +
                ", installBeginTimestampSeconds=" + installBeginTimestampSeconds +
                ", source=" + source +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferrerInfo that = (ReferrerInfo) o;

        if (referrerClickTimestampSeconds != that.referrerClickTimestampSeconds) return false;
        if (installBeginTimestampSeconds != that.installBeginTimestampSeconds) return false;
        if (!installReferrer.equals(that.installReferrer)) return false;
        return source == that.source;
    }

    @Override
    public int hashCode() {
        int result = installReferrer.hashCode();
        result = 31 * result + (int) (referrerClickTimestampSeconds ^ (referrerClickTimestampSeconds >>> 32));
        result = 31 * result + (int) (installBeginTimestampSeconds ^ (installBeginTimestampSeconds >>> 32));
        result = 31 * result + source.hashCode();
        return result;
    }
}
