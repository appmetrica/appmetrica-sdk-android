// Generated by the protocol buffer compiler.  DO NOT EDIT!

package io.appmetrica.analytics.impl.protobuf.backend;

@SuppressWarnings("hiding")
public interface ExternalAttribution {

  public static final class ClientExternalAttribution extends
      io.appmetrica.analytics.protobuf.nano.MessageNano {

    // enum AttributionType
    public static final int UNKNOWN = 0;
    public static final int APPSFLYER = 1;
    public static final int ADJUST = 2;
    public static final int KOCHAVA = 3;
    public static final int TENJIN = 4;
    public static final int AIRBRIDGE = 5;

    private static volatile ClientExternalAttribution[] _emptyArray;
    public static ClientExternalAttribution[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            io.appmetrica.analytics.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new ClientExternalAttribution[0];
          }
        }
      }
      return _emptyArray;
    }

    // optional .ClientExternalAttribution.AttributionType attribution_type = 1 [default = UNKNOWN];
    public int attributionType;

    // optional bytes value = 2;
    public byte[] value;

    public ClientExternalAttribution() {
      clear();
    }

    public ClientExternalAttribution clear() {
      attributionType = ExternalAttribution.ClientExternalAttribution.UNKNOWN;
      value = io.appmetrica.analytics.protobuf.nano.WireFormatNano.EMPTY_BYTES;
      cachedSize = -1;
      return this;
    }

    @Override
    public void writeTo(io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.attributionType != ExternalAttribution.ClientExternalAttribution.UNKNOWN) {
        output.writeInt32(1, this.attributionType);
      }
      if (!java.util.Arrays.equals(this.value, io.appmetrica.analytics.protobuf.nano.WireFormatNano.EMPTY_BYTES)) {
        output.writeBytes(2, this.value);
      }
      super.writeTo(output);
    }

    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.attributionType != ExternalAttribution.ClientExternalAttribution.UNKNOWN) {
        size += io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano
          .computeInt32Size(1, this.attributionType);
      }
      if (!java.util.Arrays.equals(this.value, io.appmetrica.analytics.protobuf.nano.WireFormatNano.EMPTY_BYTES)) {
        size += io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano
            .computeBytesSize(2, this.value);
      }
      return size;
    }

    @Override
    public ClientExternalAttribution mergeFrom(
            io.appmetrica.analytics.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            return this;
          default: {
            if (!io.appmetrica.analytics.protobuf.nano.WireFormatNano.parseUnknownField(input, tag)) {
              return this;
            }
            break;
          }
          case 8: {
            int value = input.readInt32();
            switch (value) {
              case ExternalAttribution.ClientExternalAttribution.UNKNOWN:
              case ExternalAttribution.ClientExternalAttribution.APPSFLYER:
              case ExternalAttribution.ClientExternalAttribution.ADJUST:
              case ExternalAttribution.ClientExternalAttribution.KOCHAVA:
              case ExternalAttribution.ClientExternalAttribution.TENJIN:
              case ExternalAttribution.ClientExternalAttribution.AIRBRIDGE:
                this.attributionType = value;
                break;
            }
            break;
          }
          case 18: {
            this.value = input.readBytes();
            break;
          }
        }
      }
    }

    public static ClientExternalAttribution parseFrom(byte[] data)
        throws io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException {
      return io.appmetrica.analytics.protobuf.nano.MessageNano.mergeFrom(new ClientExternalAttribution(), data);
    }

    public static ClientExternalAttribution parseFrom(
            io.appmetrica.analytics.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new ClientExternalAttribution().mergeFrom(input);
    }
  }
}