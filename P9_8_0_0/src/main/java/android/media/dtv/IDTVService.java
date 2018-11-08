package android.media.dtv;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IDTVService extends IInterface {

    public static abstract class Stub extends Binder implements IDTVService {
        private static final String DESCRIPTOR = "android.media.dtv.IDTVService";
        static final int TRANSACTION_ecmProcess = 3;
        static final int TRANSACTION_emmProcess = 2;
        static final int TRANSACTION_getInitialCbc = 6;
        static final int TRANSACTION_getScrambleKey = 4;
        static final int TRANSACTION_getSystemKey = 5;
        static final int TRANSACTION_tsidProcess = 1;
        static final int TRANSACTION_tunerxCsAddch = 20;
        static final int TRANSACTION_tunerxCsDelch = 21;
        static final int TRANSACTION_tunerxCsTuning = 9;
        static final int TRANSACTION_tunerxEnd = 23;
        static final int TRANSACTION_tunerxGetAc = 12;
        static final int TRANSACTION_tunerxGetBper = 16;
        static final int TRANSACTION_tunerxGetCnInfo = 15;
        static final int TRANSACTION_tunerxGetMonInfo = 10;
        static final int TRANSACTION_tunerxGetRssiInfo = 13;
        static final int TRANSACTION_tunerxGetSyncInfo = 14;
        static final int TRANSACTION_tunerxGetTmccInfo = 11;
        static final int TRANSACTION_tunerxSearchCh = 22;
        static final int TRANSACTION_tunerxStart = 7;
        static final int TRANSACTION_tunerxTsRead = 18;
        static final int TRANSACTION_tunerxTsStart = 17;
        static final int TRANSACTION_tunerxTsStop = 19;
        static final int TRANSACTION_tunerxTuning = 8;

        private static class Proxy implements IDTVService {
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

            public int tsidProcess(int ts_id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ts_id);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int emmProcess(byte[] data, int size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    _data.writeInt(size);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(data);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int ecmProcess(byte[] data, int size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    _data.writeInt(size);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(data);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getScrambleKey(int uKeyLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uKeyLen);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getSystemKey(int uLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uLen);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getInitialCbc(int uLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uLen);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxStart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxTuning(int tunerBand, int ch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tunerBand);
                    _data.writeInt(ch);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxCsTuning(int tunerBand, int ch, int[] lockFlg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tunerBand);
                    _data.writeInt(ch);
                    _data.writeIntArray(lockFlg);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readIntArray(lockFlg);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxGetMonInfo(TunerMonitoringInfo tunerMonitoringInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tunerMonitoringInfo != null) {
                        _data.writeInt(1);
                        tunerMonitoringInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        tunerMonitoringInfo.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxGetTmccInfo(TunerTMCCInfo tunerTMCCInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tunerTMCCInfo != null) {
                        _data.writeInt(1);
                        tunerTMCCInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        tunerTMCCInfo.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxGetAc(TunerACInfo tunerACInfo, TunerSyncInfo tunerSyncInfo, long toms) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tunerACInfo != null) {
                        _data.writeInt(1);
                        tunerACInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tunerSyncInfo != null) {
                        _data.writeInt(1);
                        tunerSyncInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(toms);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        tunerACInfo.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        tunerSyncInfo.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxGetRssiInfo(TunerRSSIInfo tunerRSSIInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tunerRSSIInfo != null) {
                        _data.writeInt(1);
                        tunerRSSIInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        tunerRSSIInfo.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxGetSyncInfo(TunerSyncInfo tunerSyncInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tunerSyncInfo != null) {
                        _data.writeInt(1);
                        tunerSyncInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        tunerSyncInfo.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxGetCnInfo(TunerCNInfo tunerACInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tunerACInfo != null) {
                        _data.writeInt(1);
                        tunerACInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        tunerACInfo.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxGetBper(TunerBperInfo tunerBperInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tunerBperInfo != null) {
                        _data.writeInt(1);
                        tunerBperInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        tunerBperInfo.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxTsStart(TunerDataTsif tunerDataTsif) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tunerDataTsif != null) {
                        _data.writeInt(1);
                        tunerDataTsif.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxTsRead(TunerChData tunerChData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tunerChData != null) {
                        _data.writeInt(1);
                        tunerChData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        tunerChData.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxTsStop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TuningTargetChannel[] tunerxCsAddch(int tunerBand, int ch, TuningTargetChannel[] tuningTargetChannels) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tunerBand);
                    _data.writeInt(ch);
                    _data.writeTypedArray(tuningTargetChannels, 0);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    TuningTargetChannel[] _result = (TuningTargetChannel[]) _reply.createTypedArray(TuningTargetChannel.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TuningTargetChannel[] tunerxCsDelch(int tunerBand, int ch, TuningTargetChannel[] tuningTargetChannels) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tunerBand);
                    _data.writeInt(ch);
                    _data.writeTypedArray(tuningTargetChannels, 0);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    TuningTargetChannel[] _result = (TuningTargetChannel[]) _reply.createTypedArray(TuningTargetChannel.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tunerxSearchCh(List<TuningTargetChannel> tuningTargetChannels) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(tuningTargetChannels);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(tuningTargetChannels, TuningTargetChannel.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void tunerxEnd() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDTVService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDTVService)) {
                return new Proxy(obj);
            }
            return (IDTVService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            byte[] _arg0;
            byte[] _result2;
            TuningTargetChannel[] _result3;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = tsidProcess(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.createByteArray();
                    _result = emmProcess(_arg0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeByteArray(_arg0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.createByteArray();
                    _result = ecmProcess(_arg0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeByteArray(_arg0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getScrambleKey(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSystemKey(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInitialCbc(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result = tunerxStart();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = tunerxTuning(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg02 = data.readInt();
                    int _arg1 = data.readInt();
                    int[] _arg2 = data.createIntArray();
                    _result = tunerxCsTuning(_arg02, _arg1, _arg2);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeIntArray(_arg2);
                    return true;
                case 10:
                    TunerMonitoringInfo tunerMonitoringInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tunerMonitoringInfo = (TunerMonitoringInfo) TunerMonitoringInfo.CREATOR.createFromParcel(data);
                    } else {
                        tunerMonitoringInfo = null;
                    }
                    _result = tunerxGetMonInfo(tunerMonitoringInfo);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (tunerMonitoringInfo != null) {
                        reply.writeInt(1);
                        tunerMonitoringInfo.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 11:
                    TunerTMCCInfo tunerTMCCInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tunerTMCCInfo = (TunerTMCCInfo) TunerTMCCInfo.CREATOR.createFromParcel(data);
                    } else {
                        tunerTMCCInfo = null;
                    }
                    _result = tunerxGetTmccInfo(tunerTMCCInfo);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (tunerTMCCInfo != null) {
                        reply.writeInt(1);
                        tunerTMCCInfo.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 12:
                    TunerACInfo tunerACInfo;
                    TunerSyncInfo tunerSyncInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tunerACInfo = (TunerACInfo) TunerACInfo.CREATOR.createFromParcel(data);
                    } else {
                        tunerACInfo = null;
                    }
                    if (data.readInt() != 0) {
                        tunerSyncInfo = (TunerSyncInfo) TunerSyncInfo.CREATOR.createFromParcel(data);
                    } else {
                        tunerSyncInfo = null;
                    }
                    _result = tunerxGetAc(tunerACInfo, tunerSyncInfo, data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (tunerACInfo != null) {
                        reply.writeInt(1);
                        tunerACInfo.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (tunerSyncInfo != null) {
                        reply.writeInt(1);
                        tunerSyncInfo.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 13:
                    TunerRSSIInfo tunerRSSIInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tunerRSSIInfo = (TunerRSSIInfo) TunerRSSIInfo.CREATOR.createFromParcel(data);
                    } else {
                        tunerRSSIInfo = null;
                    }
                    _result = tunerxGetRssiInfo(tunerRSSIInfo);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (tunerRSSIInfo != null) {
                        reply.writeInt(1);
                        tunerRSSIInfo.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 14:
                    TunerSyncInfo tunerSyncInfo2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tunerSyncInfo2 = (TunerSyncInfo) TunerSyncInfo.CREATOR.createFromParcel(data);
                    } else {
                        tunerSyncInfo2 = null;
                    }
                    _result = tunerxGetSyncInfo(tunerSyncInfo2);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (tunerSyncInfo2 != null) {
                        reply.writeInt(1);
                        tunerSyncInfo2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 15:
                    TunerCNInfo tunerCNInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tunerCNInfo = (TunerCNInfo) TunerCNInfo.CREATOR.createFromParcel(data);
                    } else {
                        tunerCNInfo = null;
                    }
                    _result = tunerxGetCnInfo(tunerCNInfo);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (tunerCNInfo != null) {
                        reply.writeInt(1);
                        tunerCNInfo.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 16:
                    TunerBperInfo tunerBperInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tunerBperInfo = (TunerBperInfo) TunerBperInfo.CREATOR.createFromParcel(data);
                    } else {
                        tunerBperInfo = null;
                    }
                    _result = tunerxGetBper(tunerBperInfo);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (tunerBperInfo != null) {
                        reply.writeInt(1);
                        tunerBperInfo.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 17:
                    TunerDataTsif tunerDataTsif;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tunerDataTsif = (TunerDataTsif) TunerDataTsif.CREATOR.createFromParcel(data);
                    } else {
                        tunerDataTsif = null;
                    }
                    _result = tunerxTsStart(tunerDataTsif);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 18:
                    TunerChData tunerChData;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tunerChData = (TunerChData) TunerChData.CREATOR.createFromParcel(data);
                    } else {
                        tunerChData = null;
                    }
                    _result = tunerxTsRead(tunerChData);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (tunerChData != null) {
                        reply.writeInt(1);
                        tunerChData.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result = tunerxTsStop();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = tunerxCsAddch(data.readInt(), data.readInt(), (TuningTargetChannel[]) data.createTypedArray(TuningTargetChannel.CREATOR));
                    reply.writeNoException();
                    reply.writeTypedArray(_result3, 1);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = tunerxCsDelch(data.readInt(), data.readInt(), (TuningTargetChannel[]) data.createTypedArray(TuningTargetChannel.CREATOR));
                    reply.writeNoException();
                    reply.writeTypedArray(_result3, 1);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    List<TuningTargetChannel> _arg03 = data.createTypedArrayList(TuningTargetChannel.CREATOR);
                    _result = tunerxSearchCh(_arg03);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeTypedList(_arg03);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    tunerxEnd();
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int ecmProcess(byte[] bArr, int i) throws RemoteException;

    int emmProcess(byte[] bArr, int i) throws RemoteException;

    byte[] getInitialCbc(int i) throws RemoteException;

    byte[] getScrambleKey(int i) throws RemoteException;

    byte[] getSystemKey(int i) throws RemoteException;

    int tsidProcess(int i) throws RemoteException;

    TuningTargetChannel[] tunerxCsAddch(int i, int i2, TuningTargetChannel[] tuningTargetChannelArr) throws RemoteException;

    TuningTargetChannel[] tunerxCsDelch(int i, int i2, TuningTargetChannel[] tuningTargetChannelArr) throws RemoteException;

    int tunerxCsTuning(int i, int i2, int[] iArr) throws RemoteException;

    void tunerxEnd() throws RemoteException;

    int tunerxGetAc(TunerACInfo tunerACInfo, TunerSyncInfo tunerSyncInfo, long j) throws RemoteException;

    int tunerxGetBper(TunerBperInfo tunerBperInfo) throws RemoteException;

    int tunerxGetCnInfo(TunerCNInfo tunerCNInfo) throws RemoteException;

    int tunerxGetMonInfo(TunerMonitoringInfo tunerMonitoringInfo) throws RemoteException;

    int tunerxGetRssiInfo(TunerRSSIInfo tunerRSSIInfo) throws RemoteException;

    int tunerxGetSyncInfo(TunerSyncInfo tunerSyncInfo) throws RemoteException;

    int tunerxGetTmccInfo(TunerTMCCInfo tunerTMCCInfo) throws RemoteException;

    int tunerxSearchCh(List<TuningTargetChannel> list) throws RemoteException;

    int tunerxStart() throws RemoteException;

    int tunerxTsRead(TunerChData tunerChData) throws RemoteException;

    int tunerxTsStart(TunerDataTsif tunerDataTsif) throws RemoteException;

    int tunerxTsStop() throws RemoteException;

    int tunerxTuning(int i, int i2) throws RemoteException;
}
