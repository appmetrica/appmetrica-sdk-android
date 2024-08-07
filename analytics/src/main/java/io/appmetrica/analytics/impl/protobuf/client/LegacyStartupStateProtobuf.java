// Generated by the protocol buffer compiler.  DO NOT EDIT!

package io.appmetrica.analytics.impl.protobuf.client;

@SuppressWarnings("hiding")
public interface LegacyStartupStateProtobuf {

  public static final class LegacyStartupState extends
      io.appmetrica.analytics.protobuf.nano.MessageNano {

    private static volatile LegacyStartupState[] _emptyArray;
    public static LegacyStartupState[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            io.appmetrica.analytics.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new LegacyStartupState[0];
          }
        }
      }
      return _emptyArray;
    }

    // optional string uuid = 1 [deprecated = true];
    public java.lang.String uuid;

    // optional string countryInit = 19;
    public java.lang.String countryInit;

    // optional bool hadFirstStartup = 22 [default = false];
    public boolean hadFirstStartup;

    // optional string deviceId = 25 [deprecated = true];
    public java.lang.String deviceId;

    // optional string deviceIdHash = 26 [deprecated = true];
    public java.lang.String deviceIdHash;

    public LegacyStartupState() {
      clear();
    }

    public LegacyStartupState clear() {
      uuid = "";
      countryInit = "";
      hadFirstStartup = false;
      deviceId = "";
      deviceIdHash = "";
      cachedSize = -1;
      return this;
    }

    @Override
    public void writeTo(io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (!this.uuid.equals("")) {
        output.writeString(1, this.uuid);
      }
      if (!this.countryInit.equals("")) {
        output.writeString(19, this.countryInit);
      }
      if (this.hadFirstStartup != false) {
        output.writeBool(22, this.hadFirstStartup);
      }
      if (!this.deviceId.equals("")) {
        output.writeString(25, this.deviceId);
      }
      if (!this.deviceIdHash.equals("")) {
        output.writeString(26, this.deviceIdHash);
      }
      super.writeTo(output);
    }

    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (!this.uuid.equals("")) {
        size += io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(1, this.uuid);
      }
      if (!this.countryInit.equals("")) {
        size += io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(19, this.countryInit);
      }
      if (this.hadFirstStartup != false) {
        size += io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano
            .computeBoolSize(22, this.hadFirstStartup);
      }
      if (!this.deviceId.equals("")) {
        size += io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(25, this.deviceId);
      }
      if (!this.deviceIdHash.equals("")) {
        size += io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(26, this.deviceIdHash);
      }
      return size;
    }

    @Override
    public LegacyStartupState mergeFrom(
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
          case 10: {
            this.uuid = input.readString();
            break;
          }
          case 154: {
            this.countryInit = input.readString();
            break;
          }
          case 176: {
            this.hadFirstStartup = input.readBool();
            break;
          }
          case 202: {
            this.deviceId = input.readString();
            break;
          }
          case 210: {
            this.deviceIdHash = input.readString();
            break;
          }
        }
      }
    }

    public static LegacyStartupState parseFrom(byte[] data)
        throws io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException {
      return io.appmetrica.analytics.protobuf.nano.MessageNano.mergeFrom(new LegacyStartupState(), data);
    }

    public static LegacyStartupState parseFrom(
            io.appmetrica.analytics.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new LegacyStartupState().mergeFrom(input);
    }
  }
}
