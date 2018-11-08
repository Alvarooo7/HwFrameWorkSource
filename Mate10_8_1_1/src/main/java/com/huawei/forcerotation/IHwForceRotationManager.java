package com.huawei.forcerotation;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwForceRotationManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwForceRotationManager {
        private static final String DESCRIPTOR = "com.huawei.forcerotation.IHwForceRotationManager";
        static final int TRANSACTION_applyForceRotationLayout = 6;
        static final int TRANSACTION_isAppForceLandRotatable = 3;
        static final int TRANSACTION_isAppInForceRotationWhiteList = 2;
        static final int TRANSACTION_isForceRotationSwitchOpen = 1;
        static final int TRANSACTION_recalculateWidthForForceRotation = 7;
        static final int TRANSACTION_saveOrUpdateForceRotationAppInfo = 4;
        static final int TRANSACTION_showToastIfNeeded = 5;

        private static class Proxy implements IHwForceRotationManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public boolean isForceRotationSwitchOpen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppInForceRotationWhiteList(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppForceLandRotatable(String packageName, IBinder aToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(aToken);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean saveOrUpdateForceRotationAppInfo(String packageName, String componentName, IBinder aToken, int reqOrientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(componentName);
                    _data.writeStrongBinder(aToken);
                    _data.writeInt(reqOrientation);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showToastIfNeeded(String packageName, int pid, String processName, IBinder aToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeString(processName);
                    _data.writeStrongBinder(aToken);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void applyForceRotationLayout(IBinder aToken, Rect vf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(aToken);
                    if (vf != null) {
                        _data.writeInt(1);
                        vf.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        vf.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int recalculateWidthForForceRotation(int width, int height, int logicalHeight, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(logicalHeight);
                    _data.writeString(packageName);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwForceRotationManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwForceRotationManager)) {
                return new Proxy(obj);
            }
            return (IHwForceRotationManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isForceRotationSwitchOpen();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isAppInForceRotationWhiteList(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isAppForceLandRotatable(data.readString(), data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = saveOrUpdateForceRotationAppInfo(data.readString(), data.readString(), data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    showToastIfNeeded(data.readString(), data.readInt(), data.readString(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 6:
                    Rect rect;
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    applyForceRotationLayout(_arg0, rect);
                    reply.writeNoException();
                    if (rect != null) {
                        reply.writeInt(1);
                        rect.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = recalculateWidthForForceRotation(data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void applyForceRotationLayout(IBinder iBinder, Rect rect) throws RemoteException;

    boolean isAppForceLandRotatable(String str, IBinder iBinder) throws RemoteException;

    boolean isAppInForceRotationWhiteList(String str) throws RemoteException;

    boolean isForceRotationSwitchOpen() throws RemoteException;

    int recalculateWidthForForceRotation(int i, int i2, int i3, String str) throws RemoteException;

    boolean saveOrUpdateForceRotationAppInfo(String str, String str2, IBinder iBinder, int i) throws RemoteException;

    void showToastIfNeeded(String str, int i, String str2, IBinder iBinder) throws RemoteException;
}
