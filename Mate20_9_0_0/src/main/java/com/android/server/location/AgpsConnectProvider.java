package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.hidata.wavemapping.cons.Constant;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class AgpsConnectProvider implements IAgpsConnectProvider {
    private static final String ACTION = "action";
    private static final String ADDRESS = "address";
    private static final int CLOSE_SOCKET = 0;
    private static final int CREATE_SOCKET = 1;
    private static final String DATA = "data";
    private static final String DATASTATUS = "dataStatus";
    private static final boolean DEBUG = HWFLOW;
    private static final boolean DEBUGE_OVER = false;
    private static final int DEFAULT_DATASTATUS = 1;
    private static final int FINISH_DATA_CONNECT = 0;
    private static final int GPSRPC_SERVER_MPC = 1;
    private static final int GPSRPC_SERVER_NULL = 0;
    private static final int GPSRPC_SERVER_PDE = 2;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    protected static final boolean HWFLOW;
    private static final boolean IS_CDMA_GSM = SystemProperties.get("ro.config.dsds_mode", "").equals("cdma_gsm");
    private static final boolean IS_HISI_CDMA_SUPPORTED = SystemProperties.getBoolean("ro.config.hisi_cdma_supported", false);
    private static final String LEN = "len";
    private static final int MPC_SERVICE_ID = 1;
    private static final String PORT = "port";
    private static final String SERVER = "server";
    private static final int STATUS_CLOSE = 0;
    private static final int STATUS_CONNECTED = 1;
    private static final String TAG = "AgpsConnectProvider";
    private static final String TRIGGERMODE = "triggerMode";
    private static final int TRIGGERMODE_NI = 1;
    private static final long delaytime = 5000;
    private Context mContext;
    private AgpsHandler mHandler;
    private SocketThread mSocketThread;
    private HandlerThread mThread;

    private class AgpsHandler extends Handler {
        private static final long CTWAP_INTERVAL = 7200000;
        private static final String FORBIDEN_AGPS_IN_2_HOURS_SWITCH = "forbiden_agps_in_2_hours";
        private static final int FORBIDEN_OPEN = 1;
        private static final int FORCE_START_DOING = 1;
        private static final int FORCE_START_ERROR = 2;
        private static final int FORCE_START_READY = 0;
        private static final long FORE_OPEN = 0;
        private static final long FORE_OPEN_DATA_TIMEOUT = 180000;
        private static final int MSG_CHECK_CONNECT = 100;
        private static final int MSG_CONTROL_SOCKET_CONNECT = 101;
        private static final int MSG_FORE_OPEN_DATA_SUCCESS = 105;
        private static final int MSG_FORE_OPEN_DATA_TIMEOUT = 104;
        private static final int MSG_HANDLE_SOCKET = 102;
        private static final int MSG_INIT = 103;
        private static final int RESULT_CONNECTED = 1;
        private static final int RESULT_IDLE = 0;
        private static final int SOCKET_MODE = 1;
        private long mLastMsgTime = System.currentTimeMillis();
        private long mLastSwitchTime = 0;
        private ConnectivityBroadcastReceiver mReceiver;
        private Socket mSocketMPC;
        private Socket mSocketPDE;

        private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
            private ConnectivityBroadcastReceiver() {
            }

            /* synthetic */ ConnectivityBroadcastReceiver(AgpsHandler x0, AnonymousClass1 x1) {
                this();
            }

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (AgpsConnectProvider.DEBUG) {
                    String str = AgpsConnectProvider.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("ConnectivityBroadcastReceiver.onReceive() action: ");
                    stringBuilder.append(action);
                    Log.w(str, stringBuilder.toString());
                }
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action) || "android.net.conn.CONNECTIVITY_CHANGE_SUPL".equals(action)) {
                    ConnectivityManager cm = (ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity");
                    if (cm != null) {
                        NetworkInfo suplNetworkInfo = cm.getNetworkInfo(3);
                        if (suplNetworkInfo == null) {
                            if (AgpsConnectProvider.DEBUG) {
                                Log.v(AgpsConnectProvider.TAG, "suplNetworkInfo is null");
                            }
                            return;
                        }
                        if (AgpsConnectProvider.DEBUG) {
                            String str2 = AgpsConnectProvider.TAG;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(" ConnectivityBroadcastReceiver.onReceive()");
                            stringBuilder2.append(suplNetworkInfo);
                            Log.v(str2, stringBuilder2.toString());
                        }
                        if (!suplNetworkInfo.isAvailable()) {
                            Log.d(AgpsConnectProvider.TAG, "net not available");
                            return;
                        } else if (suplNetworkInfo.getType() != 3) {
                            if (AgpsConnectProvider.DEBUG) {
                                Log.v(AgpsConnectProvider.TAG, "   type is not TYPE_MOBILE_SUPL");
                            }
                            if ("2GVoiceCallEnded".equals(suplNetworkInfo.getReason())) {
                                if (AgpsConnectProvider.DEBUG) {
                                    Log.v(AgpsConnectProvider.TAG, "   reason is 2GVoiceCallEnded, retrying SUPL connectivity");
                                }
                                AgpsHandler.this.renewSuplConnectivity();
                            }
                            return;
                        } else if (suplNetworkInfo.isConnected()) {
                            Message msg = new Message();
                            msg.what = 105;
                            AgpsConnectProvider.this.mHandler.sendMessage(msg);
                            return;
                        } else {
                            if (AgpsConnectProvider.DEBUG) {
                                Log.v(AgpsConnectProvider.TAG, "   TYPE_MOBILE_SUPL not connected");
                            }
                            if (suplNetworkInfo.isAvailable()) {
                                if (AgpsConnectProvider.DEBUG) {
                                    Log.v(AgpsConnectProvider.TAG, "   retrying  connectivity for it's available");
                                }
                                AgpsHandler.this.renewSuplConnectivity();
                            }
                            return;
                        }
                    }
                    if (AgpsConnectProvider.DEBUG) {
                        Log.v(AgpsConnectProvider.TAG, "ConnectivityManager is null");
                    }
                    return;
                }
                Log.w(AgpsConnectProvider.TAG, "ConnectivityBroadcastReceiver.onReceive() NULL action ");
            }
        }

        AgpsHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (AgpsConnectProvider.DEBUG) {
                String str = AgpsConnectProvider.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("handleMessage msg is : ");
                stringBuilder.append(msg.what);
                Log.d(str, stringBuilder.toString());
            }
            if (AgpsConnectProvider.IS_HISI_CDMA_SUPPORTED) {
                long intervalTime = System.currentTimeMillis() - this.mLastMsgTime;
                if (AgpsConnectProvider.DEBUG) {
                    String str2 = AgpsConnectProvider.TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("interval between msg is ");
                    stringBuilder2.append(intervalTime);
                    Log.d(str2, stringBuilder2.toString());
                }
                if (intervalTime < 50) {
                    long delayedTime = 50 - intervalTime;
                    if (delayedTime > 50) {
                        delayedTime = 50;
                    }
                    try {
                        Thread.sleep(delayedTime);
                    } catch (Exception e) {
                    }
                }
                this.mLastMsgTime = System.currentTimeMillis();
            }
            switch (msg.what) {
                case 100:
                    handleCheckConnect(msg.getData());
                    return;
                case 101:
                    Log.d(AgpsConnectProvider.TAG, "enter case MSG_CONTROL_SOCKET_CONNECT:");
                    handleControlSocketConnet(msg.getData());
                    return;
                case 102:
                    handleSocketCommunication(msg.getData());
                    return;
                case 103:
                    new Thread(new Runnable() {
                        public void run() {
                            AgpsHandler.this.handleInit();
                        }
                    }).start();
                    return;
                case 104:
                    handleForeOpenDataTimeout();
                    return;
                case 105:
                    handleForeOpenDataSuccess();
                    return;
                default:
                    return;
            }
        }

        private void handleInit() {
            if (AgpsConnectProvider.DEBUG) {
                Log.d(AgpsConnectProvider.TAG, "handle init obj");
            }
            AgpsConnectProvider.start_socket();
        }

        private void handleSocketCommunication(Bundle data) {
            int server = data.getInt(AgpsConnectProvider.SERVER);
            if (server == 1) {
                if (this.mSocketMPC == null) {
                    Log.w(AgpsConnectProvider.TAG, "mSocketMPC == null");
                    AgpsConnectProvider.this.native_agps_sock_status(server, 0);
                    return;
                }
                Log.d(AgpsConnectProvider.TAG, "Create MPC_SERVICE SocketThread");
                AgpsConnectProvider.this.mSocketThread = new SocketThread(this.mSocketMPC, data);
            } else if (this.mSocketPDE == null) {
                Log.w(AgpsConnectProvider.TAG, "mSocketPDE == null");
                AgpsConnectProvider.this.native_agps_sock_status(server, 0);
                return;
            } else {
                Log.d(AgpsConnectProvider.TAG, "Create PDE_SERVICE SocketThread");
                AgpsConnectProvider.this.mSocketThread = new SocketThread(this.mSocketPDE, data);
            }
            AgpsConnectProvider.this.mSocketThread.start();
        }

        private void handleControlSocketConnet(Bundle data) {
            Log.d(AgpsConnectProvider.TAG, "enter handleControlSocketConnet");
            int server = data.getInt(AgpsConnectProvider.SERVER);
            int action = data.getInt("action");
            String address = data.getString(AgpsConnectProvider.ADDRESS);
            int port = data.getInt(AgpsConnectProvider.PORT);
            String str;
            StringBuilder stringBuilder;
            StringBuilder stringBuilder2;
            if (action == 1) {
                if (startConnection(address, port, server)) {
                    if (AgpsConnectProvider.DEBUG) {
                        str = AgpsConnectProvider.TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Connect success, address :");
                        stringBuilder.append(address);
                        stringBuilder.append(" ,port: ");
                        stringBuilder.append(port);
                        Log.d(str, stringBuilder.toString());
                    }
                    AgpsConnectProvider.this.native_agps_sock_status(server, 1);
                    return;
                }
                AgpsConnectProvider.this.native_agps_sock_status(server, 0);
                if (AgpsConnectProvider.DEBUG) {
                    str = AgpsConnectProvider.TAG;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Connect fail, address :");
                    stringBuilder2.append(address);
                    stringBuilder2.append(" ,port: ");
                    stringBuilder2.append(port);
                    Log.d(str, stringBuilder2.toString());
                }
            } else if (action == 0) {
                if (server == 1) {
                    try {
                        Log.d(AgpsConnectProvider.TAG, "Close MPC_SERVICE");
                        if (this.mSocketMPC != null) {
                            this.mSocketMPC.close();
                            this.mSocketMPC = null;
                        }
                    } catch (IOException e) {
                        Log.e(AgpsConnectProvider.TAG, "close socket IOException");
                    }
                } else {
                    Log.d(AgpsConnectProvider.TAG, "Close PDE_SERVICE");
                    if (this.mSocketPDE != null) {
                        this.mSocketPDE.close();
                        this.mSocketPDE = null;
                    }
                }
                if (AgpsConnectProvider.DEBUG) {
                    String str2 = AgpsConnectProvider.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("native_agps_sock_status, server ");
                    stringBuilder.append(server);
                    stringBuilder.append(" ,status  ");
                    stringBuilder.append(0);
                    Log.d(str2, stringBuilder.toString());
                }
                AgpsConnectProvider.this.native_agps_sock_status(server, 0);
            } else if (AgpsConnectProvider.DEBUG) {
                str = AgpsConnectProvider.TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("error action:");
                stringBuilder2.append(action);
                Log.e(str, stringBuilder2.toString());
            }
        }

        private boolean startConnection(String address, int port, int server) {
            Socket socket = null;
            int retryNum = 1;
            addRoute(address);
            while (socket == null) {
                if (retryNum > 2) {
                    Log.d(AgpsConnectProvider.TAG, "socket is null!");
                    return false;
                }
                try {
                    Log.d(AgpsConnectProvider.TAG, "create new Socket");
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(address, port), Constant.SEND_STALL_MSG_DELAY);
                } catch (UnknownHostException e) {
                    Log.e(AgpsConnectProvider.TAG, "UnknownHostException");
                } catch (IOException e2) {
                    e2.printStackTrace();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
                if (!(socket == null || socket.isConnected())) {
                    try {
                        socket.close();
                    } catch (IOException e4) {
                        Log.e(AgpsConnectProvider.TAG, "close socket IOException");
                    }
                    socket = null;
                    if (AgpsConnectProvider.DEBUG) {
                        String str = AgpsConnectProvider.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Connect fail ");
                        stringBuilder.append(retryNum);
                        Log.d(str, stringBuilder.toString());
                    }
                }
                retryNum++;
            }
            if (server == 1) {
                Log.d(AgpsConnectProvider.TAG, "Create MPC_SERVICE");
                this.mSocketMPC = socket;
            } else {
                Log.d(AgpsConnectProvider.TAG, "Create PDE_SERVICE");
                this.mSocketPDE = socket;
            }
            return true;
        }

        private void addRoute(String address) {
            try {
                if (!((ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity")).requestRouteToHostAddress(3, InetAddress.getByName(address))) {
                    Log.d(AgpsConnectProvider.TAG, "add roater, fail");
                }
            } catch (Exception e) {
                Log.e(AgpsConnectProvider.TAG, "add Route Exception");
            }
        }

        private void handleForeOpenDataTimeout() {
            AgpsConnectProvider.this.native_agps_data_conn_status(0);
            if (this.mReceiver != null) {
                AgpsConnectProvider.this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
        }

        private void handleForeOpenDataSuccess() {
            if (this.mReceiver != null) {
                AgpsConnectProvider.this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
            AgpsConnectProvider.this.native_agps_data_conn_status(1);
            AgpsConnectProvider.this.mHandler.removeMessages(104);
        }

        private int forceOpenData(ConnectivityManager cm) {
            int result = cm.startUsingNetworkFeature(0, "enableSUPL");
            if (result == 0) {
                if (AgpsConnectProvider.DEBUG) {
                    Log.d(AgpsConnectProvider.TAG, "PhoneConstants.APN_ALREADY_ACTIVE");
                }
                return 0;
            } else if (result == 1) {
                if (AgpsConnectProvider.DEBUG) {
                    Log.d(AgpsConnectProvider.TAG, "PhoneConstants.APN_REQUEST_STARTED");
                }
                return 1;
            } else {
                if (AgpsConnectProvider.DEBUG) {
                    String str = AgpsConnectProvider.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("startUsingNetworkFeature failed, value is ");
                    stringBuilder.append(result);
                    Log.d(str, stringBuilder.toString());
                }
                return 2;
            }
        }

        private void handleCheckConnect(Bundle data) {
            int triggerMode = data.getInt(AgpsConnectProvider.TRIGGERMODE);
            int dataStatus = data.getInt(AgpsConnectProvider.DATASTATUS);
            ConnectivityManager cm = (ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity");
            if (cm != null) {
                if (dataStatus == 0) {
                    cm.stopUsingNetworkFeature(0, "enableSUPL");
                    if (1 == System.getInt(AgpsConnectProvider.this.mContext.getContentResolver(), FORBIDEN_AGPS_IN_2_HOURS_SWITCH, 1)) {
                        this.mLastSwitchTime = System.currentTimeMillis();
                    }
                    Log.d(AgpsConnectProvider.TAG, "Close data call ,call stopUsingNetworkFeature");
                    return;
                }
                boolean isConnect = false;
                NetworkInfo networkInfo = cm.getNetworkInfo(0);
                if (networkInfo != null) {
                    isConnect = networkInfo.isConnected();
                }
                if (AgpsConnectProvider.DEBUG) {
                    String str = AgpsConnectProvider.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("isConnect = ");
                    stringBuilder.append(isConnect);
                    Log.d(str, stringBuilder.toString());
                }
                if (isConnect || triggerMode == 1) {
                    boolean isCdmaType = false;
                    try {
                        isCdmaType = 2 == TelephonyManager.getDefault().getCurrentPhoneType(Global.getInt(AgpsConnectProvider.this.mContext.getContentResolver(), "multi_sim_data_call", 0));
                        if (AgpsConnectProvider.DEBUG) {
                            String str2 = AgpsConnectProvider.TAG;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("isCdmaType = ");
                            stringBuilder2.append(isCdmaType);
                            Log.d(str2, stringBuilder2.toString());
                        }
                    } catch (Exception e) {
                        Log.e(AgpsConnectProvider.TAG, "get cdmatype exception");
                    }
                    if (triggerMode != 1 && isConnect && !isCdmaType) {
                        AgpsConnectProvider.this.native_agps_data_conn_status(0);
                        return;
                    } else if (1 != System.getInt(AgpsConnectProvider.this.mContext.getContentResolver(), FORBIDEN_AGPS_IN_2_HOURS_SWITCH, 1) || triggerMode == 1 || this.mLastSwitchTime == 0 || System.currentTimeMillis() - this.mLastSwitchTime >= CTWAP_INTERVAL) {
                        int feature = forceOpenData((ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity"));
                        if (feature == 0) {
                            AgpsConnectProvider.this.native_agps_data_conn_status(1);
                        } else if (feature == 1) {
                            startReceiver();
                            Message msg = new Message();
                            msg.what = 104;
                            AgpsConnectProvider.this.mHandler.sendMessageDelayed(msg, FORE_OPEN_DATA_TIMEOUT);
                        } else {
                            AgpsConnectProvider.this.native_agps_data_conn_status(0);
                        }
                        return;
                    } else {
                        Log.d(AgpsConnectProvider.TAG, "forbidden");
                        AgpsConnectProvider.this.native_agps_data_conn_status(0);
                        return;
                    }
                }
                AgpsConnectProvider.this.native_agps_data_conn_status(0);
            }
        }

        private void startReceiver() {
            if (this.mReceiver != null) {
                AgpsConnectProvider.this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
            this.mReceiver = new ConnectivityBroadcastReceiver(this, null);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE_SUPL");
            AgpsConnectProvider.this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }

        private void renewSuplConnectivity() {
            if (AgpsConnectProvider.DEBUG) {
                Log.v(AgpsConnectProvider.TAG, "renewSuplConnectivity");
            }
            int feature = forceOpenData((ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity"));
            if (feature == 0) {
                AgpsConnectProvider.this.native_agps_data_conn_status(1);
            } else if (feature == 1) {
                if (AgpsConnectProvider.DEBUG) {
                    Log.v(AgpsConnectProvider.TAG, "   FORCE_START_DOING");
                }
                return;
            } else {
                AgpsConnectProvider.this.native_agps_data_conn_status(0);
            }
            AgpsConnectProvider.this.mHandler.removeMessages(104);
            if (this.mReceiver != null) {
                AgpsConnectProvider.this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
        }
    }

    private class SocketThread extends Thread {
        byte[] mBytes;
        int mServer;
        private Socket mSocket;

        public SocketThread(Socket socket, Bundle data) {
            this.mSocket = socket;
            this.mServer = data.getInt(AgpsConnectProvider.SERVER);
            this.mBytes = data.getByteArray(AgpsConnectProvider.DATA);
        }

        public void run() {
            try {
                this.mSocket.setSoTimeout(10000);
                OutputStream ops = this.mSocket.getOutputStream();
                ops.write(this.mBytes);
                ops.flush();
                InputStream ips = this.mSocket.getInputStream();
                byte[] oBytes = new byte[1460];
                int readlen = 1;
                while (readlen > 0) {
                    readlen = ips.read(oBytes);
                    if (AgpsConnectProvider.DEBUG) {
                        String str = AgpsConnectProvider.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("readlen is = ");
                        stringBuilder.append(readlen);
                        stringBuilder.append(" ,oBytes.length is ");
                        stringBuilder.append(oBytes.length);
                        Log.d(str, stringBuilder.toString());
                    }
                    if (readlen > 0) {
                        AgpsConnectProvider.this.native_agps_sock_data(this.mServer, oBytes, readlen);
                    }
                }
            } catch (IOException e) {
                Log.d(AgpsConnectProvider.TAG, "read/write time out,force to close socket,not exception");
            }
        }
    }

    private static native void class_init_native();

    private native void init_native_gpsrpc_object();

    private native int native_agps_data_conn_status(int i);

    private native int native_agps_sock_data(int i, byte[] bArr, int i2);

    private native int native_agps_sock_status(int i, int i2);

    private static native void start_socket();

    static {
        boolean z = Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4));
        HWFLOW = z;
        Log.d(TAG, "load library ");
        class_init_native();
    }

    public AgpsConnectProvider(Context context) {
        if (DEBUG) {
            Log.d(TAG, "AgpsConnectProvider init");
        }
        this.mContext = context;
        init_native_gpsrpc_object();
        this.mThread = new HandlerThread(TAG);
        this.mThread.start();
        this.mHandler = new AgpsHandler(this.mThread.getLooper());
        Message msg = new Message();
        msg.what = 103;
        this.mHandler.sendMessage(msg);
        if (DEBUG) {
            Log.d(TAG, "send msg to handle init");
        }
        checkGpsEnable();
    }

    private void checkDataConnect(int triggerMode, int dataStatus) {
        Message msg = new Message();
        msg.what = 100;
        Bundle bundle = new Bundle();
        bundle.putInt(TRIGGERMODE, triggerMode);
        bundle.putInt(DATASTATUS, dataStatus);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("enter checkDataConnect,triggerMode = ");
            stringBuilder.append(triggerMode);
            stringBuilder.append(" ,dataStatus = ");
            stringBuilder.append(dataStatus);
            Log.d(str, stringBuilder.toString());
        }
    }

    private void jnitest() {
        Log.e(TAG, "jni test callback success!");
    }

    private void controlSocketConnect(int server, int action, String address, int port) {
        Message msg = new Message();
        msg.what = 101;
        Bundle bundle = new Bundle();
        bundle.putInt(SERVER, server);
        bundle.putInt("action", action);
        bundle.putString(ADDRESS, address);
        bundle.putInt(PORT, port);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("controlSocketConnect,server = ");
            stringBuilder.append(server);
            stringBuilder.append(" ,action = ");
            stringBuilder.append(action);
            stringBuilder.append(" ,address = ");
            stringBuilder.append(address);
            stringBuilder.append(" ,port = ");
            stringBuilder.append(port);
            Log.d(str, stringBuilder.toString());
        }
    }

    private void handleSocket(int server, byte[] data, int len) {
        Message msg = new Message();
        msg.what = 102;
        Bundle bundle = new Bundle();
        bundle.putInt(SERVER, server);
        bundle.putByteArray(DATA, data);
        bundle.putInt(LEN, len);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("handleSocket,server = ");
            stringBuilder.append(server);
            stringBuilder.append(" ,len = ");
            stringBuilder.append(len);
            Log.d(str, stringBuilder.toString());
        }
    }

    public static AgpsConnectProvider createAgpsConnectProvider(Context context) {
        if (IS_CDMA_GSM || IS_HISI_CDMA_SUPPORTED) {
            return new AgpsConnectProvider(context);
        }
        return null;
    }

    private void checkGpsEnable() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("location_providers_allowed"), true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                if (Secure.isLocationProviderEnabled(AgpsConnectProvider.this.mContext.getContentResolver(), "gps")) {
                    SystemProperties.set("sys.gps_provider_enabled", "true");
                } else {
                    SystemProperties.set("sys.gps_provider_enabled", "false");
                }
            }
        }, -1);
        if (Secure.isLocationProviderEnabled(this.mContext.getContentResolver(), "gps")) {
            SystemProperties.set("sys.gps_provider_enabled", "true");
        } else {
            SystemProperties.set("sys.gps_provider_enabled", "false");
        }
    }

    public int setPostionMode(int oldPositionMode, Context context) {
        if (!IS_CDMA_GSM || Secure.isLocationProviderEnabled(context.getContentResolver(), "network")) {
            return oldPositionMode;
        }
        Log.d(TAG, "set GPS_POSITION_MODE_STANDALONE");
        return 0;
    }
}
