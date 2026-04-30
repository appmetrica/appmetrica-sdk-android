// @formatter:off
//CHECKSTYLE:OFF
/*
 * This file is generated from third-party AIDL sources.
 * DO NOT EDIT MANUALLY.
 *
 * Source AIDL dir: aidl/rustore-referrer
 * Original package (used as Binder DESCRIPTOR): ru.vk.store.sdk.install.referrer
 *
 * To regenerate, run:
 *   ./gradlew :analytics:generateThirdPartyAidl
 */
package io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl;
public interface GetInstallReferrerCallback extends android.os.IInterface
{
  /** Default implementation for GetInstallReferrerCallback. */
  public static class Default implements io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback
  {
    @Override public void onSuccess(java.lang.String payload) throws android.os.RemoteException
    {
    }
    @Override public void onError(int code, java.lang.String errorMessage) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback interface,
     * generating a proxy if needed.
     */
    public static io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback))) {
        return ((io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback)iin);
      }
      return new io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_onSuccess:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.onSuccess(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onError:
        {
          int _arg0;
          _arg0 = data.readInt();
          java.lang.String _arg1;
          _arg1 = data.readString();
          this.onError(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void onSuccess(java.lang.String payload) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(payload);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSuccess, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onError(int code, java.lang.String errorMessage) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(code);
          _data.writeString(errorMessage);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onError, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_onSuccess = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "ru.vk.store.sdk.install.referrer.GetInstallReferrerCallback";
  public void onSuccess(java.lang.String payload) throws android.os.RemoteException;
  public void onError(int code, java.lang.String errorMessage) throws android.os.RemoteException;
}
//CHECKSTYLE:ON
