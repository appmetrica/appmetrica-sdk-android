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
public interface InstallReferrerProvider extends android.os.IInterface
{
  /** Default implementation for InstallReferrerProvider. */
  public static class Default implements io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider
  {
    @Override public void getInstallReferrer(java.lang.String packageName, io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback installReferrerCallback) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider interface,
     * generating a proxy if needed.
     */
    public static io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider))) {
        return ((io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider)iin);
      }
      return new io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider.Stub.Proxy(obj);
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
        case TRANSACTION_getInstallReferrer:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback _arg1;
          _arg1 = io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback.Stub.asInterface(data.readStrongBinder());
          this.getInstallReferrer(_arg0, _arg1);
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
    private static class Proxy implements io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider
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
      @Override public void getInstallReferrer(java.lang.String packageName, io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback installReferrerCallback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(packageName);
          _data.writeStrongInterface(installReferrerCallback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getInstallReferrer, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_getInstallReferrer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "ru.vk.store.sdk.install.referrer.InstallReferrerProvider";
  public void getInstallReferrer(java.lang.String packageName, io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback installReferrerCallback) throws android.os.RemoteException;
}
//CHECKSTYLE:ON
