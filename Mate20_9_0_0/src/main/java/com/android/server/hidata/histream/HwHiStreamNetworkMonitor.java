package com.android.server.hidata.histream;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings.Global;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.huawei.lcagent.client.LogCollectManager;

class HwHiStreamNetworkMonitor {
    public static final int EVENT_CONNECTIVITY_CHANGE = 3;
    public static final int EVENT_MOBILE_DATA_DISABLED = 2;
    public static final int EVENT_WIFI_DISABLED = 1;
    private static HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private String mCurrBSSID = null;
    private Handler mHandler;
    private IntentFilter mIntentFilter;
    public long mLastCellDisableTime = 0;
    public long mLastHandoverTime = 0;
    public long mLastWifiDisabledTime = 0;
    public long mLastWifiEnabledTime = 0;
    private LogCollectManager mLogCollectManager;
    private NetworkInfo mNetworkInfo;
    private ContentResolver mResolver;
    private TelephonyManager mTelephonyManager;
    private UserDataEnableObserver mUserDataEnableObserver;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;

    private class UserDataEnableObserver extends ContentObserver {
        public UserDataEnableObserver(Handler handler) {
            super(handler);
            HwHiStreamNetworkMonitor.this.mResolver = HwHiStreamNetworkMonitor.this.mContext.getContentResolver();
        }

        public void register() {
            HwHiStreamNetworkMonitor.this.mResolver.registerContentObserver(Global.getUriFor("mobile_data"), false, this);
        }

        public void unregister() {
            HwHiStreamNetworkMonitor.this.mResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            if (!HwHiStreamNetworkMonitor.this.isUserDataEnabled()) {
                HwHiStreamNetworkMonitor.this.mLastCellDisableTime = System.currentTimeMillis();
                Bundle bundle = new Bundle();
                bundle.putInt("event", 2);
                HwHiStreamNetworkMonitor.this.mHandler.sendMessage(HwHiStreamNetworkMonitor.this.mHandler.obtainMessage(3, bundle));
            }
        }
    }

    private HwHiStreamNetworkMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        registerBroadcastReceiver();
        this.mUserDataEnableObserver = new UserDataEnableObserver(this.mHandler);
        this.mUserDataEnableObserver.register();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mLogCollectManager = new LogCollectManager(this.mContext);
    }

    public static HwHiStreamNetworkMonitor createInstance(Context context, Handler handler) {
        if (mHwHiStreamNetworkMonitor == null) {
            mHwHiStreamNetworkMonitor = new HwHiStreamNetworkMonitor(context, handler);
        }
        return mHwHiStreamNetworkMonitor;
    }

    public static HwHiStreamNetworkMonitor getInstance() {
        return mHwHiStreamNetworkMonitor;
    }

    public int getCurrNetworkType(int uid) {
        return HwArbitrationFunction.getCurrentNetwork(this.mContext, uid);
    }

    public String getCurBSSID() {
        if (this.mCurrBSSID == null) {
            this.mWifiInfo = this.mWifiManager.getConnectionInfo();
            if (this.mWifiInfo != null) {
                this.mCurrBSSID = this.mWifiInfo.getBSSID();
            }
        }
        return this.mCurrBSSID;
    }

    private void handleWifiConnected() {
        this.mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (this.mWifiInfo != null) {
            this.mCurrBSSID = this.mWifiInfo.getBSSID();
        }
    }

    public boolean getMoblieDateSettings() {
        return getSettingsGlobalBoolean(this.mContext.getContentResolver(), "mobile_data", false);
    }

    private boolean getSettingsGlobalBoolean(ContentResolver cr, String name, boolean def) {
        return Global.getInt(cr, name, def) == 1;
    }

    private void registerBroadcastReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    HwHiStreamNetworkMonitor.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (HwHiStreamNetworkMonitor.this.mNetworkInfo != null && HwHiStreamNetworkMonitor.this.mNetworkInfo.getState() == State.CONNECTED) {
                        HwHiStreamNetworkMonitor.this.handleWifiConnected();
                    }
                } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("event", 3);
                    HwHiStreamNetworkMonitor.this.mHandler.sendMessageDelayed(HwHiStreamNetworkMonitor.this.mHandler.obtainMessage(3, bundle), 500);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    int wifistatue = intent.getIntExtra("wifi_state", 4);
                    if (1 == wifistatue) {
                        HwHiStreamNetworkMonitor.this.mLastWifiDisabledTime = System.currentTimeMillis();
                        Bundle bundle2 = new Bundle();
                        bundle2.putInt("event", 1);
                        HwHiStreamNetworkMonitor.this.mHandler.sendMessage(HwHiStreamNetworkMonitor.this.mHandler.obtainMessage(3, bundle2));
                    } else if (3 == wifistatue) {
                        HwHiStreamUtils.logD("+++++++WIFI enabled ++++++");
                        HwHiStreamNetworkMonitor.this.mLastWifiEnabledTime = System.currentTimeMillis();
                    }
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mIntentFilter.addAction("com.android.server.hidata.arbitration.HwArbitrationStateMachine");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    public String getCurrentDefaultDataImsi() {
        String mCurrentDefaultDataImsi = this.mTelephonyManager.getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (this.mLogCollectManager == null || mCurrentDefaultDataImsi == null) {
            return mCurrentDefaultDataImsi;
        }
        try {
            return this.mLogCollectManager.doEncrypt(mCurrentDefaultDataImsi);
        } catch (RemoteException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getCurrentDefaultDataImsi doEncrypt error:");
            stringBuilder.append(e);
            HwHiStreamUtils.logE(stringBuilder.toString());
            return null;
        }
    }

    public boolean isUserDataEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), "mobile_data", 0) != 0;
    }
}
