package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.CaptivePortal;
import android.net.ConnectivityManager;
import android.net.ICaptivePortal;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Type;
import android.net.NetworkSpecifier;
import android.net.NetworkStatsHistory;
import android.net.NetworkStatsHistory.Entry;
import android.net.NetworkTemplate;
import android.net.NetworkUtils;
import android.net.StringNetworkSpecifier;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPhoneConstants;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.server.AbstractConnectivityService.DomainPreferType;
import com.android.server.ConnectivityService.NetworkFactoryInfo;
import com.android.server.ConnectivityService.NetworkRequestInfo;
import com.android.server.GcmFixer.HeartbeatReceiver;
import com.android.server.GcmFixer.NetworkStateReceiver;
import com.android.server.connectivity.DnsManager;
import com.android.server.connectivity.DnsManager.PrivateDnsConfig;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.Vpn;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.hicure.HwHiCureManager;
import com.android.server.hidata.mplink.HwMpLinkContentAware;
import com.android.server.hidata.mplink.HwMplinkManager;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.pm.auth.HwCertification;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.hisi.mapcon.IMapconService;
import com.hisi.mapcon.IMapconService.Stub;
import com.huawei.deliver.info.HwDeliverInfo;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.HwFeatureConfig;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import huawei.android.telephony.wrapper.WrapperFactory;
import huawei.cust.HwCustUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwConnectivityService extends ConnectivityService {
    private static final String ACTION_BT_CONNECTION_CHANGED = "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_MAPCON_SERVICE_FAILED = "com.hisi.mapcon.servicefailed";
    public static final String ACTION_MAPCON_SERVICE_START = "com.hisi.mapcon.serviceStartResult";
    private static final String ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY";
    private static final String ACTION_OF_ANDROID_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_SIM_RECORDS_READY = "com.huawei.intent.action.ACTION_SIM_RECORDS_READY";
    private static final int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA = 1100;
    private static final String COUNTRY_CODE_CN = "460";
    public static final int DEFAULT_PHONE_ID = 0;
    private static final String DEFAULT_PRIVATE_DNS_CONFIG = "1,4,4,6,8,8,10,60";
    private static final String DEFAULT_SERVER = "connectivitycheck.gstatic.com";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36";
    private static final int DEVICE_NOT_PROVISIONED = 0;
    private static final int DEVICE_PROVISIONED = 1;
    public static final String DISABEL_DATA_SERVICE_ACTION = "android.net.conn.DISABEL_DATA_SERVICE_ACTION";
    private static final String DISABLE_PORTAL_CHECK = "disable_portal_check";
    private static final int DNS_SUCCESS = 0;
    private static String ENABLE_NOT_REMIND_FUNCTION = "enable_not_remind_function";
    public static final String FLAG_SETUP_WIZARD = "flag_setup_wizard";
    private static final String HW_CONNECTIVITY_ACTION = "huawei.net.conn.HW_CONNECTIVITY_CHANGE";
    private static final boolean HW_SIM_ACTIVATION = SystemProperties.getBoolean("ro.config.hw_sim_activation", false);
    private static final boolean HiCureEnabled = SystemProperties.getBoolean("ro.config.hw_hicure.enabled", true);
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    public static final String MAPCON_START_INTENT = "com.hisi.mmsut.started";
    private static final String MDM_VPN_PERMISSION = "com.huawei.permission.sec.MDM_VPN";
    private static final String MODULE_POWERSAVING = "powersaving";
    private static final String MODULE_WIFI = "wifi";
    private static final int PREFER_NETWORK_TIMEOUT_INTERVAL = 10000;
    private static final int PRIVATE_DNS_DELAY_BAD = 500;
    private static final int PRIVATE_DNS_DELAY_NORMAL = 150;
    private static final int PRIVATE_DNS_DELAY_VERY_BAD = 1000;
    private static final String PROP_PRIVATE_DNS_CONFIG = "hw.wifi.private_dns_config";
    public static final int SERVICE_STATE_MMS = 1;
    public static final int SERVICE_STATE_OFF = 0;
    public static final int SERVICE_STATE_ON = 1;
    public static final int SERVICE_TYPE_MMS = 0;
    public static final int SERVICE_TYPE_OTHERS = 2;
    private static final String SYSTEM_MANAGER_PKG_NAME = "com.huawei.systemmanager";
    private static final String TAG = "HwConnectivityService";
    private static final int USER_SETUP_COMPLETE = 1;
    private static final int USER_SETUP_NOT_COMPLETE = 0;
    private static String VALUE_DISABLE_NOT_REMIND_FUNCTION = "false";
    private static String VALUE_ENABLE_NOT_REMIND_FUNCTION = "true";
    private static int VALUE_NOT_SHOW_PDP = 0;
    private static int VALUE_SHOW_PDP = 1;
    private static final String VALUE_SIM_CHANGE_ALERT_DATA_CONNECT = "0";
    private static final String VERIZON_ICCID_PREFIX = "891480";
    private static String WHETHER_SHOW_PDP_WARNING = "whether_show_pdp_warning";
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    public static final int WIFI_PULS_CSP_DISENABLED = 1;
    public static final int WIFI_PULS_CSP_ENABLED = 0;
    private static ConnectivityServiceUtils connectivityServiceUtils = ((ConnectivityServiceUtils) EasyInvokeFactory.getInvokeUtils(ConnectivityServiceUtils.class));
    private static final String descriptor = "android.net.IConnectivityManager";
    protected static final boolean isAlwaysAllowMMS = SystemProperties.getBoolean("ro.config.hw_always_allow_mms", false);
    private static final boolean isMapconOn = SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false);
    private static final boolean isVerizon;
    private static final boolean isWifiMmsUtOn = SystemProperties.getBoolean("ro.config.hw_vowifi_mmsut", false);
    private static int mLteMobileDataState = 3;
    private static INetworkStatsService mStatsService;
    private int curMmsDataSub = -1;
    private int curPrefDataSubscription = -1;
    private boolean isAlreadyPop = false;
    private boolean isConnected = false;
    private ActivityManager mActivityManager;
    private HashMap<Integer, BypassPrivateDnsInfo> mBypassPrivateDnsNetwork = new HashMap();
    private Context mContext;
    private HwCustConnectivityService mCust = ((HwCustConnectivityService) HwCustUtils.createObj(HwCustConnectivityService.class, new Object[0]));
    private AlertDialog mDataServiceToPdpDialog = null;
    private DomainPreferHandler mDomainPreferHandler = null;
    private NetworkStateReceiver mGcmFixIntentReceiver = new NetworkStateReceiver();
    private Handler mHandler;
    private HeartbeatReceiver mHeartbeatReceiver = new HeartbeatReceiver();
    private HwHiCureManager mHiCureManager = null;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwConnectivityService.log("mIntentReceiver begin");
            String action = intent.getAction();
            if (HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED.equals(action)) {
                HwConnectivityService.log("receive Intent.ACTION_BOOT_COMPLETED!");
                HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted = true;
            } else if (HwConnectivityService.ACTION_BT_CONNECTION_CHANGED.equals(action)) {
                HwConnectivityService.log("receive ACTION_BT_CONNECTION_CHANGED");
                if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == 0) {
                    HwConnectivityService.this.mIsBlueThConnected = false;
                }
            } else {
                HwConnectivityService.this.mRegisteredPushPkg.updateStatus(intent);
            }
        }
    };
    private boolean mIsBlueThConnected = false;
    private boolean mIsSimReady = false;
    private boolean mIsSimStateChanged = false;
    private boolean mIsTopAppSkytone = false;
    private boolean mIsWifiConnected = false;
    private final Object mLock = new Object();
    protected BroadcastReceiver mMapconIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent mapconIntent) {
            if (mapconIntent == null) {
                HwConnectivityService.log("intent is null");
                return;
            }
            String action = mapconIntent.getAction();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onReceive: action=");
            stringBuilder.append(action);
            HwConnectivityService.log(stringBuilder.toString());
            if (HwConnectivityService.MAPCON_START_INTENT.equals(action)) {
                ServiceConnection mConnection = new ServiceConnection() {
                    public void onServiceConnected(ComponentName className, IBinder service) {
                        HwConnectivityService.this.mMapconService = Stub.asInterface(service);
                    }

                    public void onServiceDisconnected(ComponentName className) {
                        HwConnectivityService.this.mMapconService = null;
                    }
                };
                HwConnectivityService.this.mContext.bindServiceAsUser(new Intent().setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService"), mConnection, 1, UserHandle.OWNER);
            } else if (HwConnectivityService.ACTION_MAPCON_SERVICE_FAILED.equals(action) && mapconIntent.getIntExtra("serviceType", 2) == 0) {
                int requestId = mapconIntent.getIntExtra("request-id", -1);
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Recive ACTION_MAPCON_SERVICE_FAILED, requestId = ");
                stringBuilder2.append(requestId);
                HwConnectivityService.loge(stringBuilder2.toString());
                if (requestId > 0) {
                    HwConnectivityService.this.mDomainPreferHandler.sendMessageAtFrontOfQueue(HwConnectivityService.this.mDomainPreferHandler.obtainMessage(1, requestId, 0));
                }
            }
        }
    };
    protected IMapconService mMapconService;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            HwConnectivityService.this.updateCallState(state);
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state != null && !TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect) && !HwConnectivityService.this.isAlreadyPop) {
                boolean isConnect;
                String str = HwConnectivityService.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("onServiceStateChanged:");
                stringBuilder.append(state);
                Log.d(str, stringBuilder.toString());
                switch (state.getVoiceRegState()) {
                    case 1:
                    case 2:
                        isConnect = state.getDataRegState() == 0;
                        break;
                    case 3:
                        isConnect = false;
                        break;
                    default:
                        isConnect = true;
                        break;
                }
                if (state.getRoaming()) {
                    HwTelephonyManagerInner.getDefault().setDataRoamingEnabledWithoutPromp(false);
                    HwConnectivityService.this.mShowWarningRoamingToPdp = true;
                }
                if (isConnect && HwConnectivityService.this.isSetupWizardCompleted() && HwConnectivityService.this.mIsSimReady) {
                    HwConnectivityService.this.mHandler.sendEmptyMessage(0);
                    HwConnectivityService.this.isAlreadyPop = true;
                }
            }
        }
    };
    private RegisteredPushPkg mRegisteredPushPkg = new RegisteredPushPkg();
    private boolean mRemindService = SystemProperties.getBoolean("ro.config.DataPopFirstBoot", false);
    private String mServer;
    private boolean mShowDlgEndCall = false;
    private boolean mShowDlgTurnOfDC = true;
    private boolean mShowWarningRoamingToPdp = false;
    private String mSimChangeAlertDataConnect = null;
    private BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                Log.d(HwConnectivityService.TAG, "CtrlSocket receive ACTION_SIM_STATE_CHANGED");
                HwConnectivityService.this.mIsSimStateChanged = true;
                if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                    HwConnectivityService.this.processWhenSimStateChange(intent);
                }
            }
        }
    };
    private BroadcastReceiver mTetheringReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && "android.hardware.usb.action.USB_STATE".equals(action)) {
                boolean usbConnected = intent.getBooleanExtra("connected", false);
                boolean rndisEnabled = intent.getBooleanExtra("rndis", false);
                int is_usb_tethering_on = Secure.getInt(HwConnectivityService.this.mContext.getContentResolver(), "usb_tethering_on", 0);
                String str = HwConnectivityService.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("mTetheringReceiver usbConnected = ");
                stringBuilder.append(usbConnected);
                stringBuilder.append(",rndisEnabled = ");
                stringBuilder.append(rndisEnabled);
                stringBuilder.append(", is_usb_tethering_on = ");
                stringBuilder.append(is_usb_tethering_on);
                Log.d(str, stringBuilder.toString());
                if (1 == is_usb_tethering_on && usbConnected && !rndisEnabled) {
                    new Thread() {
                        public void run() {
                            do {
                                try {
                                    if (HwConnectivityService.this.isSystemBootComplete()) {
                                        Thread.sleep(200);
                                    } else {
                                        Thread.sleep(1000);
                                    }
                                } catch (InterruptedException e) {
                                    Log.e(HwConnectivityService.TAG, "wait to boot complete error");
                                }
                            } while (!HwConnectivityService.this.isSystemBootComplete());
                            HwConnectivityService.this.setUsbTethering(true, HwConnectivityService.this.mContext.getOpPackageName());
                        }
                    }.start();
                }
            }
        }
    };
    private URL mURL;
    private BroadcastReceiver mVerizonWifiDisconnectReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo != null && netInfo.getType() == 1) {
                    if (netInfo.getState() == State.CONNECTED) {
                        HwConnectivityService.this.isConnected = true;
                    } else if (netInfo.getState() == State.DISCONNECTED && netInfo.getDetailedState() == DetailedState.DISCONNECTED && HwConnectivityService.this.isConnected) {
                        Toast.makeText(context, 33686198, 1).show();
                        HwConnectivityService.this.isConnected = false;
                    }
                }
            }
        }
    };
    private int phoneId = -1;
    private boolean sendWifiBroadcastAfterBootCompleted = false;
    private WifiDisconnectManager wifiDisconnectManager = new WifiDisconnectManager(this, null);

    /* renamed from: com.android.server.HwConnectivityService$13 */
    static /* synthetic */ class AnonymousClass13 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$State = new int[State.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkInfo$State[State.CONNECTING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[State.CONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[State.DISCONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[State.DISCONNECTED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private static class BypassPrivateDnsInfo {
        private static final int[] BACKOFF_TIME_INTERVAL = new int[]{1, 2, 2, 4, 4, 4, 4, 8};
        private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
        private static final String INTENT_WIFI_PRIVATE_DNS_STATISTICS = "com.intent.action.wifi_private_dns_statistics";
        private final int PRIVATE_DNS_OPEN = 1;
        private int backoffCnt = 0;
        private int backoffTime = Constant.MILLISEC_TO_HOURS;
        private int badCnt = 6;
        private int badTotalCnt = 8;
        private int failedCnt = 8;
        private String mAssignedServers;
        private int mBadDelayTotalCnt;
        private boolean mBypassPrivateDns;
        private Context mContext;
        private int mDelay1000Cnt;
        private int mDelay150Cnt;
        private int mDelay500Cnt;
        private int mDelayOver1000Cnt;
        private int mDnsDelayOverThresCnt = 0;
        private int mNetworkType;
        private long mPrivateDnsBackoffTime;
        private int mPrivateDnsCnt;
        private long mPrivateDnsCntResetTime;
        private int mPrivateDnsFailCount;
        private int mPrivateDnsResponseTotalTime;
        private int privateDnsOpen = 1;
        private int resetCntTime = 600000;
        private int unacceptCnt = 4;
        private int verybadCnt = 4;

        public BypassPrivateDnsInfo(Context context, int NetworkType, String assignedServers) {
            this.mContext = context;
            this.mNetworkType = NetworkType;
            this.mAssignedServers = assignedServers;
            this.mPrivateDnsCntResetTime = SystemClock.elapsedRealtime();
            this.mBypassPrivateDns = false;
            String[] privateDnsConfig = SystemProperties.get(HwConnectivityService.PROP_PRIVATE_DNS_CONFIG, HwConnectivityService.DEFAULT_PRIVATE_DNS_CONFIG).split(",");
            if (privateDnsConfig.length == 8) {
                this.privateDnsOpen = Integer.parseInt(privateDnsConfig[0]);
                this.unacceptCnt = Integer.parseInt(privateDnsConfig[1]);
                this.verybadCnt = Integer.parseInt(privateDnsConfig[2]);
                this.badCnt = Integer.parseInt(privateDnsConfig[3]);
                this.badTotalCnt = Integer.parseInt(privateDnsConfig[4]);
                this.failedCnt = Integer.parseInt(privateDnsConfig[5]);
                this.resetCntTime = (Integer.parseInt(privateDnsConfig[6]) * 60) * 1000;
                this.backoffTime = (Integer.parseInt(privateDnsConfig[7]) * 60) * 1000;
            }
        }

        public void reset() {
            HwConnectivityService.log("privateDnsCnt reset");
            this.mPrivateDnsCntResetTime = SystemClock.elapsedRealtime();
            this.mPrivateDnsCnt = 0;
            this.mDelay150Cnt = 0;
            this.mDelay500Cnt = 0;
            this.mDelay1000Cnt = 0;
            this.mDelayOver1000Cnt = 0;
            this.mPrivateDnsResponseTotalTime = 0;
            this.mPrivateDnsFailCount = 0;
            this.mBadDelayTotalCnt = 0;
        }

        public boolean isNeedUpdatePrivateDnsSettings() {
            if (this.mBypassPrivateDns) {
                if (SystemClock.elapsedRealtime() - this.mPrivateDnsBackoffTime >= ((long) (BACKOFF_TIME_INTERVAL[this.backoffCnt <= BACKOFF_TIME_INTERVAL.length - 1 ? this.backoffCnt : BACKOFF_TIME_INTERVAL.length - 1] * this.backoffTime))) {
                    HwConnectivityService.log("isNeedUpdatePrivateDnsSettings private dns backoff timeout");
                    reset();
                    this.backoffCnt++;
                    this.mPrivateDnsBackoffTime = 0;
                    this.mBypassPrivateDns = false;
                    sendIntentPrivateDnsEvent();
                    return true;
                }
            } else if ((this.mDelayOver1000Cnt >= this.unacceptCnt || this.mDelay1000Cnt >= this.verybadCnt || this.mDelay500Cnt >= this.badCnt || this.mBadDelayTotalCnt >= this.badTotalCnt || this.mPrivateDnsFailCount >= this.failedCnt) && this.mNetworkType == 1) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(" isNeedUpdatePrivateDnsSettings mDelay150Cnt : ");
                stringBuilder.append(this.mDelay150Cnt);
                stringBuilder.append(" , mDelay500Cnt : ");
                stringBuilder.append(this.mDelay500Cnt);
                stringBuilder.append(" , mDelay1000Cnt : ");
                stringBuilder.append(this.mDelay1000Cnt);
                stringBuilder.append(" , mDelayOver1000Cnt : ");
                stringBuilder.append(this.mDelayOver1000Cnt);
                stringBuilder.append(" , mBadDelayTotalCnt = ");
                stringBuilder.append(this.mBadDelayTotalCnt);
                HwConnectivityService.log(stringBuilder.toString());
                this.mPrivateDnsBackoffTime = SystemClock.elapsedRealtime();
                this.mBypassPrivateDns = true;
                this.mDnsDelayOverThresCnt++;
                sendIntentPrivateDnsEvent();
                return true;
            }
            return false;
        }

        public void updateDelayCount(int returnCode, int latencyMs) {
            if (this.mBypassPrivateDns) {
                HwConnectivityService.log("updateDelayCount mBypassPrivateDns return");
                return;
            }
            if (SystemClock.elapsedRealtime() - this.mPrivateDnsCntResetTime >= ((long) this.resetCntTime)) {
                sendIntentPrivateDnsEvent();
                reset();
            }
            this.mPrivateDnsCnt++;
            if (returnCode == 0) {
                this.mPrivateDnsResponseTotalTime += latencyMs;
                if (latencyMs > 1000) {
                    this.mDelayOver1000Cnt++;
                } else if (latencyMs <= 150) {
                    this.mDelay150Cnt++;
                } else if (latencyMs <= 500) {
                    this.mDelay500Cnt++;
                } else if (latencyMs <= 1000) {
                    this.mDelay1000Cnt++;
                }
                this.mBadDelayTotalCnt = (this.mDelay500Cnt + this.mDelay1000Cnt) + this.mDelayOver1000Cnt;
            } else {
                this.mPrivateDnsFailCount++;
            }
        }

        public void sendIntentPrivateDnsEvent() {
            if (this.mContext != null && this.mNetworkType == 1) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("sendIntentPrivateDnsEvent ReqCnt : ");
                stringBuilder.append(this.mPrivateDnsCnt);
                stringBuilder.append(" , DnsAddr : ");
                stringBuilder.append(this.mAssignedServers);
                stringBuilder.append(", DnsDelay : ");
                stringBuilder.append(this.mPrivateDnsResponseTotalTime);
                stringBuilder.append(" , ReqFailCnt : ");
                stringBuilder.append(this.mPrivateDnsFailCount);
                stringBuilder.append(", DnsDelayOverThresCnt : ");
                stringBuilder.append(this.mDnsDelayOverThresCnt);
                stringBuilder.append(", PrivDnsDisableCnt= ");
                stringBuilder.append(this.backoffCnt);
                HwConnectivityService.log(stringBuilder.toString());
                Intent intent = new Intent(INTENT_WIFI_PRIVATE_DNS_STATISTICS);
                Bundle extras = new Bundle();
                extras.putInt("ReqCnt", this.mPrivateDnsCnt);
                extras.putInt("DnsDelay", this.mPrivateDnsResponseTotalTime);
                extras.putInt("ReqFailCnt", this.mPrivateDnsFailCount);
                extras.putInt("DnsDelayOverThresCnt", this.mDnsDelayOverThresCnt);
                extras.putInt("PrivDnsDisableCnt", this.backoffCnt);
                extras.putString("DnsAddr", this.mAssignedServers);
                extras.putBoolean("PrivDns", 1 ^ this.mBypassPrivateDns);
                intent.putExtras(extras);
                this.mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
            }
        }
    }

    private static class CtrlSocketInfo {
        public int mAllowCtrlSocketLevel;
        public List<String> mPushWhiteListPkg;
        public int mRegisteredCount;
        public List<String> mRegisteredPkg;
        public List<String> mScrOffActPkg;

        CtrlSocketInfo() {
            this.mRegisteredPkg = null;
            this.mScrOffActPkg = null;
            this.mPushWhiteListPkg = null;
            this.mAllowCtrlSocketLevel = 0;
            this.mRegisteredCount = 0;
            this.mRegisteredPkg = new ArrayList();
            this.mScrOffActPkg = new ArrayList();
            this.mPushWhiteListPkg = new ArrayList();
        }
    }

    private class DomainPreferHandler extends Handler {
        private static final int MSG_PREFER_NETWORK_FAIL = 1;
        private static final int MSG_PREFER_NETWORK_SUCCESS = 0;
        private static final int MSG_PREFER_NETWORK_TIMEOUT = 2;

        public DomainPreferHandler(Looper looper) {
            super(looper);
        }

        private String getMsgName(int whatMsg) {
            switch (whatMsg) {
                case 0:
                    return "PREFER_NETWORK_SUCCESS";
                case 1:
                    return "PREFER_NETWORK_FAIL";
                case 2:
                    return "PREFER_NETWORK_TIMEOUT";
                default:
                    return Integer.toString(whatMsg);
            }
        }

        public void handleMessage(Message msg) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("DomainPreferHandler handleMessage msg.what = ");
            stringBuilder.append(getMsgName(msg.what));
            HwConnectivityService.log(stringBuilder.toString());
            switch (msg.what) {
                case 0:
                    handlePreferNetworkSuccess(msg);
                    return;
                case 1:
                    handlePreferNetworkFail(msg);
                    return;
                case 2:
                    handlePreferNetworkTimeout(msg);
                    return;
                default:
                    return;
            }
        }

        private void handlePreferNetworkSuccess(Message msg) {
            NetworkRequest req = msg.obj;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("handlePreferNetworkSuccess request = ");
            stringBuilder.append(req);
            HwConnectivityService.log(stringBuilder.toString());
            if (HwConnectivityService.this.mDomainPreferHandler.hasMessages(2, req)) {
                HwConnectivityService.this.mDomainPreferHandler.removeMessages(2, req);
            }
        }

        private void handlePreferNetworkFail(Message msg) {
            NetworkRequestInfo nri = HwConnectivityService.this.findExistingNetworkRequestInfo(msg.arg1);
            if (nri == null || nri.mPreferType == null) {
                HwConnectivityService.log("handlePreferNetworkFail, nri or preferType is null.");
                return;
            }
            NetworkRequest req = nri.request;
            int domainPrefer = nri.mPreferType.value();
            if (HwConnectivityService.this.mDomainPreferHandler.hasMessages(2, req)) {
                HwConnectivityService.this.mDomainPreferHandler.removeMessages(2, req);
                retryNetworkRequestWhenPreferException(req, DomainPreferType.fromInt(domainPrefer), "FAIL");
            }
        }

        private void handlePreferNetworkTimeout(Message msg) {
            retryNetworkRequestWhenPreferException(msg.obj, DomainPreferType.fromInt(msg.arg1), "TIMEOUT");
        }

        private void retryNetworkRequestWhenPreferException(NetworkRequest req, DomainPreferType prefer, String reason) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("retryNetworkRequestWhenPreferException req = ");
            stringBuilder.append(req);
            stringBuilder.append(" prefer = ");
            stringBuilder.append(prefer);
            stringBuilder.append(" reason = ");
            stringBuilder.append(reason);
            HwConnectivityService.log(stringBuilder.toString());
            NetworkRequestInfo nri = (NetworkRequestInfo) HwConnectivityService.this.mNetworkRequests.get(req);
            if (nri != null) {
                if ((prefer == DomainPreferType.DOMAIN_PREFER_WIFI || prefer == DomainPreferType.DOMAIN_PREFER_CELLULAR || prefer == DomainPreferType.DOMAIN_PREFER_VOLTE) && ((NetworkAgentInfo) HwConnectivityService.this.mNetworkForRequestId.get(req.requestId)) == null && prefer != null) {
                    for (NetworkFactoryInfo nfi : HwConnectivityService.this.mNetworkFactoryInfos.values()) {
                        nfi.asyncChannel.sendMessage(536577, req);
                    }
                    NetworkCapabilities networkCapabilities = req.networkCapabilities;
                    NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
                    HwConnectivityService.this.mNetworkRequests.remove(req);
                    LocalLog localLog = HwConnectivityService.this.mNetworkRequestInfoLogs;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("UPDATE-RELEASE ");
                    stringBuilder2.append(nri);
                    localLog.log(stringBuilder2.toString());
                    if (DomainPreferType.DOMAIN_PREFER_WIFI == prefer) {
                        networkCapabilities.setNetworkSpecifier(null);
                        networkCapabilities.addTransportType(0);
                        networkCapabilities.removeTransportType(1);
                        networkCapabilities.setNetworkSpecifier(networkSpecifier);
                    } else if (DomainPreferType.DOMAIN_PREFER_CELLULAR == prefer || DomainPreferType.DOMAIN_PREFER_VOLTE == prefer) {
                        networkCapabilities.setNetworkSpecifier(null);
                        networkCapabilities.addTransportType(1);
                        networkCapabilities.removeTransportType(0);
                        if (networkSpecifier == null) {
                            networkCapabilities.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(SubscriptionManager.getDefaultDataSubscriptionId())));
                        } else {
                            networkCapabilities.setNetworkSpecifier(networkSpecifier);
                        }
                    }
                    HwConnectivityService.this.mNetworkRequests.put(req, nri);
                    localLog = HwConnectivityService.this.mNetworkRequestInfoLogs;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("UPDATE-REGISTER ");
                    stringBuilder2.append(nri);
                    localLog.log(stringBuilder2.toString());
                    HwConnectivityService.this.rematchAllNetworksAndRequests(null, 0);
                    if (HwConnectivityService.this.mNetworkForRequestId.get(req.requestId) == null) {
                        HwConnectivityService.this.sendUpdatedScoreToFactories(req, 0);
                    }
                }
            }
        }
    }

    private class HwConnectivityServiceHandler extends Handler {
        private static final int EVENT_SHOW_ENABLE_PDP_DIALOG = 0;

        private HwConnectivityServiceHandler() {
        }

        /* synthetic */ HwConnectivityServiceHandler(HwConnectivityService x0, AnonymousClass1 x1) {
            this();
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwConnectivityService.this.handleShowEnablePdpDialog();
            }
        }
    }

    private class MobileEnabledSettingObserver extends ContentObserver {
        public MobileEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver().registerContentObserver(Global.getUriFor("device_provisioned"), true, this);
        }

        public void onChange(boolean selfChange) {
            if (HwConnectivityService.this.mRemindService || HwConnectivityService.this.checkDataServiceRemindMsim()) {
                super.onChange(selfChange);
                if (!HwConnectivityService.this.getMobileDataEnabled() && HwConnectivityService.this.mDataServiceToPdpDialog == null) {
                    HwConnectivityService.this.mDataServiceToPdpDialog = HwConnectivityService.this.createWarningToPdp();
                    HwConnectivityService.this.mDataServiceToPdpDialog.show();
                }
            }
        }
    }

    private class RegisteredPushPkg {
        private static final String MSG_ALL_CTRLSOCKET_ALLOWED = "android.ctrlsocket.all.allowed";
        private static final String MSG_SCROFF_CTRLSOCKET_STATS = "android.scroff.ctrlsocket.status";
        private static final String ctrl_socket_version = "v2";
        private int ALLOW_ALL_CTRL_SOCKET_LEVEL = 2;
        private int ALLOW_NO_CTRL_SOCKET_LEVEL = 0;
        private int ALLOW_PART_CTRL_SOCKET_LEVEL = 1;
        private int ALLOW_SPECIAL_CTRL_SOCKET_LEVEL = 3;
        private int MAX_REGISTERED_PKG_NUM = 10;
        private final Uri WHITELIST_URI = Secure.getUriFor("push_white_apps");
        private CtrlSocketInfo mCtrlSocketInfo = new CtrlSocketInfo();
        private boolean mEnable = SystemProperties.getBoolean("ro.config.hw_power_saving", false);

        RegisteredPushPkg() {
        }

        public void init(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED);
            filter.addAction(HwConnectivityService.ACTION_BT_CONNECTION_CHANGED);
            if (this.mEnable) {
                filter.addAction(MSG_SCROFF_CTRLSOCKET_STATS);
                filter.addAction(MSG_ALL_CTRLSOCKET_ALLOWED);
                getCtrlSocketRegisteredPkg();
                getCtrlSocketPushWhiteList();
                context.getContentResolver().registerContentObserver(this.WHITELIST_URI, false, new ContentObserver(new Handler()) {
                    public void onChange(boolean selfChange) {
                        RegisteredPushPkg.this.getCtrlSocketPushWhiteList();
                    }
                });
            }
            context.registerReceiver(HwConnectivityService.this.mIntentReceiver, filter);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            String register_pkg;
            String str;
            StringBuilder stringBuilder;
            switch (code) {
                case 1001:
                    register_pkg = data.readString();
                    str = HwConnectivityService.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("CtrlSocket registerPushSocket pkg = ");
                    stringBuilder.append(register_pkg);
                    Log.d(str, stringBuilder.toString());
                    registerPushSocket(register_pkg);
                    return true;
                case 1002:
                    register_pkg = data.readString();
                    str = HwConnectivityService.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("CtrlSocket unregisterPushSocket pkg = ");
                    stringBuilder.append(register_pkg);
                    Log.d(str, stringBuilder.toString());
                    unregisterPushSocket(register_pkg);
                    return true;
                case 1004:
                    reply.writeString(getActPkgInWhiteList());
                    return true;
                case 1005:
                    reply.writeInt(this.mCtrlSocketInfo.mAllowCtrlSocketLevel);
                    return true;
                case 1006:
                    Log.d(HwConnectivityService.TAG, "CtrlSocket getCtrlSocketVersion = v2");
                    reply.writeString(ctrl_socket_version);
                    return true;
                default:
                    return false;
            }
        }

        private void registerPushSocket(String pkgName) {
            if (pkgName != null) {
                boolean isToAdd = false;
                for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                    if (pkg.equals(pkgName)) {
                        return;
                    }
                }
                if (this.mCtrlSocketInfo.mRegisteredCount >= this.MAX_REGISTERED_PKG_NUM) {
                    for (String pkg2 : this.mCtrlSocketInfo.mPushWhiteListPkg) {
                        if (pkg2.equals(pkgName)) {
                            isToAdd = true;
                        }
                    }
                } else {
                    isToAdd = true;
                }
                if (isToAdd) {
                    CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                    ctrlSocketInfo.mRegisteredCount++;
                    this.mCtrlSocketInfo.mRegisteredPkg.add(pkgName);
                    updateRegisteredPkg();
                }
            }
        }

        private void unregisterPushSocket(String pkgName) {
            if (pkgName != null) {
                int count = 0;
                boolean isMatch = false;
                for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                    if (pkg.equals(pkgName)) {
                        isMatch = true;
                        break;
                    }
                    count++;
                }
                if (isMatch) {
                    CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                    ctrlSocketInfo.mRegisteredCount--;
                    this.mCtrlSocketInfo.mRegisteredPkg.remove(count);
                    updateRegisteredPkg();
                }
            }
        }

        private void getCtrlSocketPushWhiteList() {
            String wlPkg = Secure.getString(HwConnectivityService.this.mContext.getContentResolver(), "push_white_apps");
            if (wlPkg != null) {
                String[] str = wlPkg.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                if (str != null && str.length > 0) {
                    this.mCtrlSocketInfo.mPushWhiteListPkg.clear();
                    for (int i = 0; i < str.length; i++) {
                        this.mCtrlSocketInfo.mPushWhiteListPkg.add(str[i]);
                        String str2 = HwConnectivityService.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("CtrlSocket PushWhiteList[");
                        stringBuilder.append(i);
                        stringBuilder.append("] = ");
                        stringBuilder.append(str[i]);
                        Log.d(str2, stringBuilder.toString());
                    }
                }
            }
        }

        private void getCtrlSocketRegisteredPkg() {
            String registeredPkg = Secure.getString(HwConnectivityService.this.mContext.getContentResolver(), "registered_pkgs");
            if (registeredPkg != null) {
                String[] str = registeredPkg.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                if (str != null && str.length > 0) {
                    this.mCtrlSocketInfo.mRegisteredPkg.clear();
                    int i = 0;
                    this.mCtrlSocketInfo.mRegisteredCount = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 < str.length) {
                            this.mCtrlSocketInfo.mRegisteredPkg.add(str[i2]);
                            CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                            ctrlSocketInfo.mRegisteredCount++;
                            i = i2 + 1;
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        private void updateRegisteredPkg() {
            StringBuffer registeredPkg = new StringBuffer();
            for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                registeredPkg.append(pkg);
                registeredPkg.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            }
            Secure.putString(HwConnectivityService.this.mContext.getContentResolver(), "registered_pkgs", registeredPkg.toString());
        }

        private String getActPkgInWhiteList() {
            if (this.ALLOW_PART_CTRL_SOCKET_LEVEL != this.mCtrlSocketInfo.mAllowCtrlSocketLevel) {
                return null;
            }
            StringBuffer activePkg = new StringBuffer();
            for (String pkg : this.mCtrlSocketInfo.mScrOffActPkg) {
                activePkg.append(pkg);
                activePkg.append("\t");
            }
            return activePkg.toString();
        }

        public void updateStatus(Intent intent) {
            String action = intent.getAction();
            if (MSG_SCROFF_CTRLSOCKET_STATS.equals(action)) {
                int i = 0;
                if (intent.getBooleanExtra("ctrl_socket_status", false)) {
                    this.mCtrlSocketInfo.mScrOffActPkg.clear();
                    this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_PART_CTRL_SOCKET_LEVEL;
                    String actPkgs = intent.getStringExtra("ctrl_socket_list");
                    if (!TextUtils.isEmpty(actPkgs)) {
                        String[] whitePackages = actPkgs.split("\t");
                        if (whitePackages != null) {
                            while (i < whitePackages.length) {
                                this.mCtrlSocketInfo.mScrOffActPkg.add(whitePackages[i]);
                                i++;
                            }
                        }
                    }
                }
            } else if (MSG_ALL_CTRLSOCKET_ALLOWED.equals(action)) {
                this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_NO_CTRL_SOCKET_LEVEL;
            }
        }
    }

    private class WifiDisconnectManager {
        private static final String ACTION_SWITCH_TO_MOBILE_NETWORK = "android.intent.action.SWITCH_TO_MOBILE_NETWORK";
        private static final String ACTION_WIFI_NETWORK_CONNECTION_CHANGED = "huawei.intent.action.WIFI_NETWORK_CONNECTION_CHANGED";
        private static final String CONNECT_STATE = "connect_state";
        private static final String SWITCH_STATE = "switch_state";
        private static final int SWITCH_TO_WIFI_AUTO = 0;
        private static final String SWITCH_TO_WIFI_TYPE = "wifi_connect_type";
        private static final String WIFI_TO_PDP = "wifi_to_pdp";
        private static final int WIFI_TO_PDP_AUTO = 1;
        private static final int WIFI_TO_PDP_NEVER = 2;
        private static final int WIFI_TO_PDP_NOTIFY = 0;
        private boolean REMIND_WIFI_TO_PDP;
        private boolean mDialogHasShown;
        State mLastWifiState;
        private BroadcastReceiver mNetworkSwitchReceiver;
        private boolean mShouldStartMobile;
        private Handler mSwitchHandler;
        private OnDismissListener mSwitchPdpListener;
        protected AlertDialog mWifiToPdpDialog;
        private boolean shouldShowDialogWhenConnectFailed;

        private WifiDisconnectManager() {
            this.REMIND_WIFI_TO_PDP = false;
            this.mWifiToPdpDialog = null;
            this.mShouldStartMobile = false;
            this.shouldShowDialogWhenConnectFailed = true;
            this.mDialogHasShown = false;
            this.mLastWifiState = State.DISCONNECTED;
            this.mSwitchPdpListener = new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendMonitorWifiSwitchToMobileMessage(5000);
                    if (WifiDisconnectManager.this.mShouldStartMobile) {
                        HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, true);
                        HwConnectivityService.log("you have restart Mobile data service!");
                    }
                    WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.mWifiToPdpDialog = null;
                }
            };
            this.mNetworkSwitchReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (!WifiDisconnectManager.ACTION_SWITCH_TO_MOBILE_NETWORK.equals(intent.getAction())) {
                        return;
                    }
                    if (intent.getBooleanExtra(WifiDisconnectManager.SWITCH_STATE, true)) {
                        HwConnectivityService.this.wifiDisconnectManager.switchToMobileNetwork();
                    } else {
                        HwConnectivityService.this.wifiDisconnectManager.cancelSwitchToMobileNetwork();
                    }
                }
            };
            this.mSwitchHandler = new Handler() {
                public void handleMessage(Message msg) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("mSwitchHandler recieve msg =");
                    stringBuilder.append(msg.what);
                    HwConnectivityService.log(stringBuilder.toString());
                    if (msg.what == 0) {
                        WifiDisconnectManager.this.switchToMobileNetwork();
                    }
                }
            };
        }

        /* synthetic */ WifiDisconnectManager(HwConnectivityService x0, AnonymousClass1 x1) {
            this();
        }

        private boolean getAirplaneModeEnable() {
            boolean z = true;
            if (System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "airplane_mode_on", 0) != 1) {
                z = false;
            }
            boolean retVal = z;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getAirplaneModeEnable returning ");
            stringBuilder.append(retVal);
            HwConnectivityService.log(stringBuilder.toString());
            return retVal;
        }

        private AlertDialog createSwitchToPdpWarning() {
            HwConnectivityService.log("create dialog of switch to pdp");
            HwTelephonyFactory.getHwDataServiceChrManager().removeMonitorWifiSwitchToMobileMessage();
            Builder buider = new Builder(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this), 33947691);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013282, null);
            final CheckBox checkBox = (CheckBox) view.findViewById(34603157);
            buider.setView(view);
            buider.setTitle(33685520);
            buider.setIcon(17301543);
            buider.setPositiveButton(33685567, new OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    WifiDisconnectManager.this.mShouldStartMobile = true;
                    HwConnectivityService.log("setPositiveButton: mShouldStartMobile set true");
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), true);
                }
            });
            buider.setNegativeButton(33685568, new OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    HwConnectivityService.log("you have chose to disconnect Mobile data service!");
                    WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), false);
                }
            });
            AlertDialog dialog = buider.create();
            dialog.setCancelable(false);
            dialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
            return dialog;
        }

        private void checkUserChoice(boolean rememberChoice, boolean enableDataConnect) {
            int showPopState;
            if (!rememberChoice) {
                showPopState = 0;
            } else if (enableDataConnect) {
                showPopState = 1;
            } else {
                showPopState = 0;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("checkUserChoice showPopState:");
            stringBuilder.append(showPopState);
            stringBuilder.append(", enableDataConnect:");
            stringBuilder.append(enableDataConnect);
            HwConnectivityService.log(stringBuilder.toString());
            System.putInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, showPopState);
        }

        private void sendWifiBroadcast(boolean isConnectingOrConnected) {
            if (ActivityManagerNative.isSystemReady() && HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("notify settings:");
                stringBuilder.append(isConnectingOrConnected);
                HwConnectivityService.log(stringBuilder.toString());
                Intent intent = new Intent(ACTION_WIFI_NETWORK_CONNECTION_CHANGED);
                intent.putExtra(CONNECT_STATE, isConnectingOrConnected);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(intent);
            }
        }

        private boolean shouldNotifySettings() {
            if (!isSwitchToWifiSupported() || System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), SWITCH_TO_WIFI_TYPE, 0) == 0) {
                return false;
            }
            return true;
        }

        private boolean isSwitchToWifiSupported() {
            return "CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", "")) || HwConnectivityService.this.mCust.isSupportWifiConnectMode(HwConnectivityService.this.mContext);
        }

        private void switchToMobileNetwork() {
            if (getAirplaneModeEnable()) {
                HwConnectivityService.this.enableDefaultTypeAPN(true);
            } else if (this.shouldShowDialogWhenConnectFailed || !this.mDialogHasShown) {
                int value = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("WIFI_TO_PDP value =");
                stringBuilder.append(value);
                HwConnectivityService.log(stringBuilder.toString());
                int wifiplusvalue = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "wifi_csp_dispaly_state", 1);
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("wifiplus_csp_dispaly_state value =");
                stringBuilder2.append(wifiplusvalue);
                HwConnectivityService.log(stringBuilder2.toString());
                HwVSimManager hwVSimManager = HwVSimManager.getDefault();
                if (hwVSimManager != null && hwVSimManager.isVSimEnabled()) {
                    HwConnectivityService.log("vsim is enabled and following process will execute enableDefaultTypeAPN(true), so do nothing that likes value == WIFI_TO_PDP_AUTO");
                } else if (value == 0) {
                    if (wifiplusvalue == 0) {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't create WifiToPdpDialog");
                        HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork()  ");
                        HwConnectivityService.this.shouldEnableDefaultAPN();
                        return;
                    }
                    HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, false);
                    this.mShouldStartMobile = true;
                    this.mDialogHasShown = true;
                    this.mWifiToPdpDialog = createSwitchToPdpWarning();
                    this.mWifiToPdpDialog.setOnDismissListener(this.mSwitchPdpListener);
                    this.mWifiToPdpDialog.show();
                } else if (value != 1) {
                    if (1 == wifiplusvalue) {
                        HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, false);
                    } else {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't setMobileDataEnabled");
                    }
                }
                HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork( )");
                HwConnectivityService.this.shouldEnableDefaultAPN();
            }
        }

        private void cancelSwitchToMobileNetwork() {
            if (this.mWifiToPdpDialog != null) {
                Log.d(HwConnectivityService.TAG, "cancelSwitchToMobileNetwork and mWifiToPdpDialog is showing");
                this.mShouldStartMobile = true;
                this.mWifiToPdpDialog.dismiss();
            }
        }

        private void registerReceiver() {
            this.REMIND_WIFI_TO_PDP = "true".equals(Global.getString(HwConnectivityService.this.mContext.getContentResolver(), "hw_RemindWifiToPdp"));
            if (this.REMIND_WIFI_TO_PDP && isSwitchToWifiSupported()) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_SWITCH_TO_MOBILE_NETWORK);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).registerReceiver(this.mNetworkSwitchReceiver, filter);
            }
        }

        protected void hintUserSwitchToMobileWhileWifiDisconnected(State state, int type) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("hintUserSwitchToMobileWhileWifiDisconnected, state=");
            stringBuilder.append(state);
            stringBuilder.append("  type =");
            stringBuilder.append(type);
            HwConnectivityService.log(stringBuilder.toString());
            boolean shouldEnableDefaultTypeAPN = true;
            if (this.REMIND_WIFI_TO_PDP) {
                if (state == State.DISCONNECTED && type == 1 && HwConnectivityService.this.getMobileDataEnabled()) {
                    if (this.mLastWifiState == State.CONNECTED) {
                        int value = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("WIFI_TO_PDP value     =");
                        stringBuilder2.append(value);
                        HwConnectivityService.log(stringBuilder2.toString());
                        if (value == 1) {
                            HwConnectivityService.this.shouldEnableDefaultAPN();
                            return;
                        }
                        this.shouldShowDialogWhenConnectFailed = true;
                        HwConnectivityService.log("mShouldEnableDefaultTypeAPN was set false");
                        shouldEnableDefaultTypeAPN = false;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(false);
                    } else if (getAirplaneModeEnable()) {
                        shouldEnableDefaultTypeAPN = true;
                    } else {
                        this.mSwitchHandler.sendMessageDelayed(this.mSwitchHandler.obtainMessage(0), 5000);
                        HwConnectivityService.log("switch message will be send in 5 seconds");
                    }
                    if (this.mLastWifiState == State.CONNECTING) {
                        this.shouldShowDialogWhenConnectFailed = false;
                    }
                } else if ((state == State.CONNECTED || state == State.CONNECTING) && type == 1) {
                    if (state == State.CONNECTED) {
                        this.mDialogHasShown = false;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(true);
                    } else if (this.mSwitchHandler.hasMessages(0)) {
                        this.mSwitchHandler.removeMessages(0);
                        HwConnectivityService.log("switch message was removed");
                    }
                    if (this.mWifiToPdpDialog != null) {
                        this.mShouldStartMobile = true;
                        this.mWifiToPdpDialog.dismiss();
                    }
                }
                if (type == 1) {
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("mLastWifiState =");
                    stringBuilder3.append(this.mLastWifiState);
                    HwConnectivityService.log(stringBuilder3.toString());
                    this.mLastWifiState = state;
                }
            }
            if (shouldEnableDefaultTypeAPN && state == State.DISCONNECTED && type == 1) {
                HwConnectivityService.log("enableDefaultTypeAPN(true) in hintUserSwitchToMobileWhileWifiDisconnected");
                HwConnectivityService.this.shouldEnableDefaultAPN();
            }
        }

        protected void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        }
    }

    static {
        boolean z = true;
        if (!("389".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb")))) {
            z = false;
        }
        isVerizon = z;
    }

    private static void log(String s) {
        Slog.d(TAG, s);
    }

    private static void loge(String s) {
        Slog.e(TAG, s);
    }

    public HwConnectivityService(Context context, INetworkManagementService netd, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        super(context, netd, statsService, policyManager);
        this.mContext = context;
        this.mSimChangeAlertDataConnect = System.getString(context.getContentResolver(), "hw_sim_change_alert_data_connect");
        this.mRegisteredPushPkg.init(context);
        registerSimStateReceiver(context);
        if (isVerizon) {
            registerVerizonWifiDisconnectedReceiver(context);
        }
        this.wifiDisconnectManager.registerReceiver();
        registerPhoneStateListener(context);
        registerBootStateListener(context);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mHandler = new HwConnectivityServiceHandler(this, null);
        this.mDomainPreferHandler = new DomainPreferHandler(this.mHandlerThread.getLooper());
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)).booleanValue()) {
            registerMapconIntentReceiver(context);
        }
        this.mServer = Global.getString(context.getContentResolver(), "captive_portal_server");
        if (TextUtils.isEmpty(this.mServer) || this.mServer.startsWith("http")) {
            this.mServer = DEFAULT_SERVER;
        }
        SystemProperties.set("sys.defaultapn.enabled", "true");
        registerTetheringReceiver(context);
        initGCMFixer(context);
        if (true == HiCureEnabled) {
            this.mHiCureManager = HwHiCureManager.createInstance(context);
            log("HwHiCureManager setup.");
        }
    }

    private void initGCMFixer(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OF_ANDROID_BOOT_COMPLETED);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mGcmFixIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(HeartbeatReceiver.HEARTBEAT_FIXER_ACTION);
        this.mContext.registerReceiver(this.mHeartbeatReceiver, filter, "android.permission.CONNECTIVITY_INTERNAL", null);
    }

    private String[] getFeature(String str) {
        if (str != null) {
            String[] result = new String[2];
            int subId = 0;
            if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
                subId = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
                if (str.equals("enableMMS_sub2")) {
                    str = "enableMMS";
                    subId = 1;
                } else if (str.equals("enableMMS_sub1")) {
                    str = "enableMMS";
                    subId = 0;
                }
            }
            result[0] = str;
            result[1] = String.valueOf(subId);
            String str2 = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getFeature: return feature=");
            stringBuilder.append(str);
            stringBuilder.append(" subId=");
            stringBuilder.append(subId);
            Slog.d(str2, stringBuilder.toString());
            return result;
        }
        throw new IllegalArgumentException("getFeature() received null string");
    }

    protected String getMmsFeature(String feature) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("getMmsFeature HwFeatureConfig.dual_card_mms_switch");
        stringBuilder.append(HwFeatureConfig.dual_card_mms_switch);
        Slog.d(str, stringBuilder.toString());
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return feature;
        }
        String[] result = getFeature(feature);
        feature = result[0];
        this.phoneId = Integer.parseInt(result[1]);
        this.curMmsDataSub = -1;
        return feature;
    }

    protected boolean isAlwaysAllowMMSforRoaming(int networkType, String feature) {
        if (networkType == 0) {
            boolean isAlwaysAllowMMSforRoaming = isAlwaysAllowMMS;
            if (HwPhoneConstants.IS_CHINA_TELECOM) {
                boolean roaming = WrapperFactory.getMSimTelephonyManagerWrapper().isNetworkRoaming(this.phoneId);
                if (isAlwaysAllowMMSforRoaming) {
                }
            }
        }
        return true;
    }

    protected boolean isMmsAutoSetSubDiffFromDataSub(int networkType, String feature) {
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return false;
        }
        this.curPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        this.curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (!feature.equals("enableMMS") || networkType != 0) {
            return false;
        }
        if ((this.curMmsDataSub != 0 && 1 != this.curMmsDataSub) || this.phoneId == this.curMmsDataSub) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DSMMS dds is switching now, do not response request from another card, curMmsDataSub: ");
        stringBuilder.append(this.curMmsDataSub);
        log(stringBuilder.toString());
        return true;
    }

    protected boolean isMmsSubDiffFromDataSub(int networkType, String feature) {
        if (HwFeatureConfig.dual_card_mms_switch && feature.equals("enableMMS") && networkType == 0 && this.curPrefDataSubscription != this.phoneId) {
            return true;
        }
        return false;
    }

    protected boolean isNetRequestersPidsContainCurrentPid(List<Integer>[] mNetRequestersPids, int usedNetworkType, Integer currentPid) {
        if (!HwFeatureConfig.dual_card_mms_switch || !WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled() || mNetRequestersPids[usedNetworkType].contains(currentPid)) {
            return true;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("not tearing down special network - not found pid ");
        stringBuilder.append(currentPid);
        Slog.w(str, stringBuilder.toString());
        return false;
    }

    protected boolean isNeedTearMmsAndRestoreData(int networkType, String feature, Handler mHandler) {
        int lastPrefDataSubscription = 1;
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return true;
        }
        if (networkType != 0 || !feature.equals("enableMMS")) {
            return true;
        }
        if (!WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return true;
        }
        int curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (curMmsDataSub != 0 && 1 != curMmsDataSub) {
            return true;
        }
        if (curMmsDataSub != 0) {
            lastPrefDataSubscription = 0;
        }
        int curPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("isNeedTearDataAndRestoreData lastPrefDataSubscription");
        stringBuilder.append(lastPrefDataSubscription);
        stringBuilder.append("curPrefDataSubscription");
        stringBuilder.append(curPrefDataSubscription);
        log(stringBuilder.toString());
        if (lastPrefDataSubscription != curPrefDataSubscription) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("DSMMS >>>> disable a connection, after MMS net disconnected will switch back to phone ");
            stringBuilder.append(lastPrefDataSubscription);
            log(stringBuilder.toString());
            WrapperFactory.getMSimTelephonyManagerWrapper().setPreferredDataSubscription(lastPrefDataSubscription);
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("DSMMS unexpected case, data subscription is already on ");
            stringBuilder.append(curPrefDataSubscription);
            log(stringBuilder.toString());
        }
        WrapperFactory.getMSimTelephonyManagerWrapper().setMmsAutoSetDataSubscription(-1);
        return true;
    }

    private boolean isConnectedOrConnectingOrSuspended(NetworkInfo info) {
        boolean z;
        synchronized (this) {
            if (!(info.getState() == State.CONNECTED || info.getState() == State.CONNECTING)) {
                if (info.getState() != State.SUSPENDED) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    private AlertDialog createWarningRoamingToPdp() {
        Builder buider = new Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        buider.setTitle(33685962);
        buider.setMessage(33685963);
        buider.setIcon(17301543);
        buider.setPositiveButton(17040146, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                HwTelephonyManagerInner.getDefault().setDataRoamingEnabledWithoutPromp(true);
                Toast.makeText(HwConnectivityService.this.mContext, 33685965, 1).show();
            }
        });
        buider.setNegativeButton(17040145, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(HwConnectivityService.this.mContext, 33685966, 1).show();
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
        return dialog;
    }

    private AlertDialog createWarningToPdp() {
        Builder buider;
        final String enable_Not_Remind_Function = Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        CheckBox checkBox = null;
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function)) {
            int themeID = connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            buider = new Builder(new ContextThemeWrapper(connectivityServiceUtils.getContext(this), themeID), themeID);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013280, null);
            checkBox = (CheckBox) view.findViewById(34603158);
            buider.setView(view);
            buider.setTitle(17039380);
        } else {
            buider = new Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
            buider.setTitle(17039380);
            buider.setMessage(33685526);
        }
        final CheckBox finalBox = checkBox;
        buider.setIcon(17301543);
        buider.setPositiveButton(17040146, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(true);
                if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                    if (HwConnectivityService.this.mShowWarningRoamingToPdp) {
                        HwConnectivityService.this.createWarningRoamingToPdp().show();
                    }
                    Toast.makeText(HwConnectivityService.this.mContext, 33685967, 1).show();
                }
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                HwConnectivityService.this.mDataServiceToPdpDialog = null;
                HwConnectivityService.this.mShowWarningRoamingToPdp = false;
            }
        });
        buider.setNegativeButton(17040145, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
                if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                    Toast.makeText(HwConnectivityService.this.mContext, 33685968, 1).show();
                }
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                HwConnectivityService.this.mDataServiceToPdpDialog = null;
                HwConnectivityService.this.mShowWarningRoamingToPdp = false;
            }
        });
        buider.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                HwConnectivityService.this.mDataServiceToPdpDialog = null;
                HwConnectivityService.this.mShowWarningRoamingToPdp = false;
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
        return dialog;
    }

    protected void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 33);
    }

    private final void updateCallState(int state) {
        if (!this.mRemindService && !SystemProperties.getBoolean("gsm.huawei.RemindDataService", false) && !SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false)) {
            return;
        }
        if (state == 0) {
            if (this.mShowDlgEndCall && this.mDataServiceToPdpDialog == null) {
                this.mDataServiceToPdpDialog = createWarningToPdp();
                this.mDataServiceToPdpDialog.show();
                this.mShowDlgEndCall = false;
            }
        } else if (this.mDataServiceToPdpDialog != null) {
            this.mDataServiceToPdpDialog.dismiss();
            this.mDataServiceToPdpDialog = null;
            this.mShowDlgEndCall = true;
        }
    }

    protected void registerBootStateListener(Context context) {
        new MobileEnabledSettingObserver(new Handler()).register();
    }

    protected boolean needSetUserDataEnabled(boolean enabled) {
        int dataStatus = Global.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), "mobile_data", 1);
        if (!shouldShowThePdpWarning() || dataStatus != 0 || !enabled) {
            return true;
        }
        if (this.mShowDlgTurnOfDC) {
            this.mHandler.sendEmptyMessage(0);
            return false;
        }
        this.mShowDlgTurnOfDC = true;
        return true;
    }

    private void updateReminderSetting(boolean chooseNotRemind) {
        if (chooseNotRemind) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_NOT_SHOW_PDP);
        }
    }

    private boolean shouldShowThePdpWarning() {
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return shouldShowThePdpWarningMsim();
        }
        String enable_Not_Remind_Function = Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean z = false;
        boolean remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        int pdpWarningValue = System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function)) {
            return remindDataAllow;
        }
        if (remindDataAllow && pdpWarningValue == VALUE_SHOW_PDP) {
            z = true;
        }
        return z;
    }

    private boolean checkDataServiceRemindMsim() {
        int lDataVal = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        if (lDataVal == 0) {
            if (TelephonyManager.getDefault().hasIccCard(lDataVal)) {
                return SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
            }
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (1 == lDataVal) {
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else {
            return false;
        }
    }

    private boolean shouldShowThePdpWarningMsim() {
        String enableNotRemindFunction = Systemex.getString(this.mContext.getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean remindDataAllow = false;
        int lDataVal = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        boolean z = true;
        if (1 == lDataVal) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (lDataVal == 0) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        }
        int pdpWarningValue = System.getInt(this.mContext.getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction)) {
            return remindDataAllow;
        }
        if (!(remindDataAllow && pdpWarningValue == VALUE_SHOW_PDP)) {
            z = false;
        }
        return z;
    }

    private boolean shouldDisablePortalCheck(String ssid) {
        if (ssid != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("wifi ssid: ");
            stringBuilder.append(ssid);
            log(stringBuilder.toString());
            if (ssid.length() > 2 && ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"') {
                Object ssid2 = ssid2.substring(1, ssid2.length() - 1);
            }
        }
        if (1 == Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) && 1 == Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) && 1 == Global.getInt(this.mContext.getContentResolver(), "hw_disable_portal", 0)) {
            log("stop portal check for orange");
            return true;
        } else if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", "")) && "CMCC".equals(ssid2)) {
            log("stop portal check for CMCC");
            return true;
        } else if (1 == System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0)) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0);
            log("stop portal check for airsharing");
            return true;
        } else if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0 && "true".equals(Systemex.getString(this.mContext.getContentResolver(), "wifi.challenge.required"))) {
            log("setup guide wifi disable portal, and does not start browser!");
            return true;
        } else if (this.mIsTopAppSkytone) {
            log("stop start broswer for TopAppSkytone");
            return true;
        } else if (1 != System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0)) {
            return false;
        } else {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0);
            log("portal ap manual connect");
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ef A:{Catch:{ ActivityNotFoundException -> 0x01a1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ee A:{RETURN, Catch:{ ActivityNotFoundException -> 0x01a1 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean startBrowserForWifiPortal(Notification notification, String ssid) {
        if (shouldDisablePortalCheck(ssid)) {
            log("do not start browser, popup system notification");
            return false;
        }
        log("setNotificationVisible: cancel notification and start browser directly for TYPE_WIFI..");
        try {
            String usedUrl = Global.getString(this.mContext.getContentResolver(), "captive_portal_server");
            Intent intent;
            WifiInfo info;
            if (TextUtils.isEmpty(usedUrl) || !usedUrl.startsWith("http")) {
                StringBuilder stringBuilder;
                if (IS_CHINA) {
                    String operator = TelephonyManager.getDefault().getNetworkOperator();
                    if (operator == null || operator.length() == 0 || !operator.startsWith("460")) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("http://");
                        stringBuilder.append(this.mServer);
                        stringBuilder.append("/generate_204");
                        this.mURL = new URL(stringBuilder.toString());
                    } else {
                        this.mURL = new URL("http://connectivitycheck.platform.hicloud.com/generate_204");
                    }
                } else {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("http://");
                    stringBuilder2.append(this.mServer);
                    stringBuilder2.append("/generate_204");
                    this.mURL = new URL(stringBuilder2.toString());
                }
                intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                intent.setFlags(272629760);
                notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent, 0);
                String className;
                try {
                    info = ((WifiManager) connectivityServiceUtils.getContext(this).getSystemService(MODULE_WIFI)).getConnectionInfo();
                    if (info != null) {
                        return true;
                    }
                    if (1 == Secure.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 0)) {
                        log("browser has been launched by notification user clicked it, don't launch browser here again.");
                        return true;
                    }
                    Secure.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WifiProCommonUtils.PORTAL_NETWORK_FLAG, 1);
                    intent.putExtra(WifiProCommonUtils.BROWSER_LAUNCH_FROM, WifiProCommonUtils.BROWSER_LAUNCHED_BY_WIFI_PORTAL);
                    String packageName = "com.android.browser";
                    className = "com.android.browser.BrowserActivity";
                    if (Utils.isPackageInstalled("com.huawei.browser", this.mContext)) {
                        packageName = "com.huawei.browser";
                        className = "com.huawei.browser.Main";
                    }
                    if (1 != HiLinkUtil.getHiLinkSsidType(connectivityServiceUtils.getContext(this), WifiInfo.removeDoubleQuotes(info.getSSID()), info.getBSSID())) {
                        intent.setClassName(packageName, className);
                        connectivityServiceUtils.getContext(this).startActivity(intent);
                    } else {
                        String uri = HiLinkUtil.getLaunchAppForSsid(connectivityServiceUtils.getContext(this), WifiInfo.removeDoubleQuotes(info.getSSID()), info.getBSSID());
                        StringBuilder stringBuilder3 = new StringBuilder();
                        stringBuilder3.append("launch HILINK_ROUTER  ");
                        stringBuilder3.append(uri);
                        log(stringBuilder3.toString());
                        if (HiLinkUtil.SCHEME_GATEWAY.equals(uri)) {
                            intent.setClassName(packageName, className);
                            connectivityServiceUtils.getContext(this).startActivity(intent);
                        } else {
                            HiLinkUtil.startDeviceGuide(connectivityServiceUtils.getContext(this), uri);
                        }
                    }
                    return true;
                } catch (ActivityNotFoundException e) {
                    try {
                        log("default browser not exist..");
                        if (isSetupWizardCompleted()) {
                            notification.contentIntent.send();
                        } else {
                            log("setup wizard is not completed");
                            Network network = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetwork();
                            className = getCaptivePortalUserAgent(this.mContext);
                            Intent intentPortal = new Intent("android.net.conn.CAPTIVE_PORTAL");
                            intentPortal.putExtra("android.net.extra.NETWORK", network);
                            intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new ICaptivePortal.Stub() {
                                public void appResponse(int response) {
                                }
                            }));
                            intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", this.mURL.toString());
                            intent.putExtra("android.net.extra.CAPTIVE_PORTAL_USER_AGENT", className);
                            intentPortal.setFlags(272629760);
                            intentPortal.putExtra(FLAG_SETUP_WIZARD, true);
                            connectivityServiceUtils.getContext(this).startActivity(intentPortal);
                        }
                    } catch (CanceledException e2) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Sending contentIntent failed: ");
                        stringBuilder.append(e2);
                        log(stringBuilder.toString());
                    } catch (ActivityNotFoundException e3) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Activity not found: ");
                        stringBuilder.append(e3);
                        loge(stringBuilder.toString());
                    }
                }
            } else {
                log("use the portal url from the settings");
                this.mURL = new URL(usedUrl);
                intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                intent.setFlags(272629760);
                notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent, 0);
                info = ((WifiManager) connectivityServiceUtils.getContext(this).getSystemService(MODULE_WIFI)).getConnectionInfo();
                if (info != null) {
                }
            }
        } catch (MalformedURLException e4) {
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append("MalformedURLException ");
            stringBuilder4.append(e4);
            log(stringBuilder4.toString());
        }
    }

    public boolean isSystemBootComplete() {
        return this.sendWifiBroadcastAfterBootCompleted;
    }

    protected void hintUserSwitchToMobileWhileWifiDisconnected(State state, int type) {
        if (WifiProCommonUtils.isWifiSelfCuring() && state == State.DISCONNECTED && type == 1) {
            Log.d("HwSelfCureEngine", "DISCONNECTED, but enableDefaultTypeAPN-->UP is ignored due to wifi self curing.");
        } else {
            this.wifiDisconnectManager.hintUserSwitchToMobileWhileWifiDisconnected(state, type);
        }
    }

    protected void enableDefaultTypeApnWhenWifiConnectionStateChanged(State state, int type) {
        if (state == State.DISCONNECTED && type == 1) {
            this.mIsWifiConnected = false;
            this.mIsTopAppSkytone = false;
            SystemProperties.set("sys.defaultapn.enabled", "false");
        } else if (state == State.CONNECTED && type == 1) {
            this.mIsWifiConnected = true;
            String pktName = WifiProCommonUtils.getPackageName(this.mContext, WifiProCommonUtils.getForegroundAppUid(this.mContext));
            if (pktName != null && pktName.equals("com.huawei.hiskytone")) {
                this.mIsTopAppSkytone = true;
            }
            SystemProperties.set("sys.defaultapn.enabled", "true");
        }
    }

    private void shouldEnableDefaultAPN() {
        if (!this.mIsBlueThConnected) {
            enableDefaultTypeAPN(true);
        }
    }

    private void sendBlueToothTetheringBroadcast(boolean isBttConnected) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sendBroad bt_tethering_connect_state = ");
        stringBuilder.append(isBttConnected);
        log(stringBuilder.toString());
        Intent intent = new Intent("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED");
        intent.putExtra("btt_connect_state", isBttConnected);
        connectivityServiceUtils.getContext(this).sendBroadcast(intent);
    }

    protected void enableDefaultTypeApnWhenBlueToothTetheringStateChanged(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        if (newInfo.getType() == 7) {
            log("enter BlueToothTethering State Changed");
            State state = newInfo.getState();
            if (state == State.CONNECTED) {
                this.mIsBlueThConnected = true;
                sendBlueToothTetheringBroadcast(true);
            } else if (state == State.DISCONNECTED) {
                this.mIsBlueThConnected = false;
                sendBlueToothTetheringBroadcast(false);
                if (!this.mIsWifiConnected) {
                    enableDefaultTypeAPN(true);
                }
            }
        }
    }

    public void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        this.wifiDisconnectManager.makeDefaultAndHintUser(newNetwork);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1101) {
            data.enforceInterface(descriptor);
            int enableInt = data.readInt();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("needSetUserDataEnabled enableInt = ");
            stringBuilder.append(enableInt);
            Log.d(str, stringBuilder.toString());
            boolean result = needSetUserDataEnabled(enableInt == 1);
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("needSetUserDataEnabled result = ");
            stringBuilder2.append(result);
            Log.d(str2, stringBuilder2.toString());
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        } else if (this.mRegisteredPushPkg.onTransact(code, data, reply, flags)) {
            return true;
        } else {
            return super.onTransact(code, data, reply, flags);
        }
    }

    private void setMobileDataEnabled(String module, boolean enabled) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("module:");
        stringBuilder.append(module);
        stringBuilder.append(" setMobileDataEnabled enabled = ");
        stringBuilder.append(enabled);
        Log.d(str, stringBuilder.toString());
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            tm.setDataEnabled(enabled);
            tm.setDataEnabledProperties(module, enabled);
        }
    }

    private boolean getDataEnabled() {
        boolean ret = false;
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            ret = tm.getDataEnabled();
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CtrlSocket getMobileDataEnabled enabled = ");
        stringBuilder.append(ret);
        Log.d(str, stringBuilder.toString());
        return ret;
    }

    public boolean getMobileDataEnabled() {
        boolean ret = false;
        if (!this.mIsSimStateChanged) {
            return false;
        }
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            try {
                int phoneCount = tm.getPhoneCount();
                boolean ret2 = false;
                for (int slotId = 0; slotId < phoneCount; slotId++) {
                    if (tm.getSimState(slotId) == 5) {
                        ret2 = true;
                    }
                }
                if (ret2) {
                    ret = tm.getDataEnabled();
                } else {
                    Log.d(TAG, "all sim card not ready,return false");
                    return false;
                }
            } catch (NullPointerException e) {
                Log.d(TAG, "getMobileDataEnabled NPE");
            }
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CtrlSocket getMobileDataEnabled = ");
        stringBuilder.append(ret);
        Log.d(str, stringBuilder.toString());
        return ret;
    }

    private void enableDefaultTypeAPN(boolean enabled) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("enableDefaultTypeAPN= ");
        stringBuilder.append(enabled);
        Log.d(str, stringBuilder.toString());
        str = SystemProperties.get("sys.defaultapn.enabled", "true");
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("DEFAULT_MOBILE_ENABLE before state is ");
        stringBuilder2.append(str);
        Log.d(str2, stringBuilder2.toString());
        SystemProperties.set("sys.defaultapn.enabled", enabled ? "true" : "false");
        HwTelephonyManagerInner hwTm = HwTelephonyManagerInner.getDefault();
        if (hwTm != null) {
            hwTm.setDefaultMobileEnable(enabled);
        }
    }

    private void registerSimStateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        context.registerReceiver(this.mSimStateReceiver, filter);
    }

    private void handleShowEnablePdpDialog() {
        if (this.mDataServiceToPdpDialog == null) {
            this.mDataServiceToPdpDialog = createWarningToPdp();
            this.mDataServiceToPdpDialog.show();
        }
    }

    private void registerTetheringReceiver(Context context) {
        if (HwDeliverInfo.isIOTVersion() && SystemProperties.getBoolean("ro.config.persist_usb_tethering", false)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.hardware.usb.action.USB_STATE");
            context.registerReceiver(this.mTetheringReceiver, filter);
        }
    }

    protected void updataNetworkAgentInfoForHicure(NetworkAgentInfo nai) {
        if (nai.networkInfo.getType() != 0 || true != HiCureEnabled) {
            return;
        }
        if (State.CONNECTED == nai.networkInfo.getState()) {
            this.mHiCureManager.mDnsHiCureEngine.notifyConnectedInfo(nai);
        } else if (State.DISCONNECTED == nai.networkInfo.getState()) {
            this.mHiCureManager.mDnsHiCureEngine.notifyDisconnectedInfo();
        }
    }

    protected void setExplicitlyUnselected(NetworkAgentInfo nai) {
        if (nai != null) {
            nai.networkMisc.explicitlySelected = false;
            nai.networkMisc.acceptUnvalidated = false;
            if (nai.networkInfo != null && ConnectivityManager.getNetworkTypeName(1).equals(nai.networkInfo.getTypeName())) {
                log("setExplicitlyUnselected, WiFi+ switch from WiFi to Cellular, enableDefaultTypeAPN explicitly.");
                enableDefaultTypeAPN(true);
            }
        }
    }

    protected void updateNetworkConcurrently(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        NetworkInfo oldInfo;
        State state = newInfo.getState();
        INetworkManagementService netd = connectivityServiceUtils.getNetd(this);
        synchronized (networkAgent) {
            oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        if (oldInfo != null && oldInfo.getState() == state) {
            log("updateNetworkConcurrently, ignoring duplicate network state non-change");
        } else if (netd == null) {
            loge("updateNetworkConcurrently, invalid member, netd = null");
        } else {
            networkAgent.setCurrentScore(0);
            StringBuilder stringBuilder;
            try {
                String str;
                int i = networkAgent.network.netId;
                if (networkAgent.networkCapabilities.hasCapability(13)) {
                    str = null;
                } else {
                    str = "SYSTEM";
                }
                netd.createPhysicalNetwork(i, str);
                networkAgent.created = true;
                connectivityServiceUtils.updateLinkProperties(this, networkAgent, null);
                stringBuilder = new StringBuilder();
                stringBuilder.append("updateNetworkConcurrently, nai.networkInfo = ");
                stringBuilder.append(networkAgent.networkInfo);
                log(stringBuilder.toString());
                networkAgent.asyncChannel.sendMessage(528391, 4, 0, null);
            } catch (Exception e) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("updateNetworkConcurrently, Error creating network ");
                stringBuilder.append(networkAgent.network.netId);
                stringBuilder.append(": ");
                stringBuilder.append(e.getMessage());
                loge(stringBuilder.toString());
            }
        }
    }

    public void triggerRoamingNetworkMonitor(NetworkAgentInfo networkAgent) {
        if (networkAgent != null && networkAgent.networkMonitor != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("triggerRoamingNetworkMonitor, nai.networkInfo = ");
            stringBuilder.append(networkAgent.networkInfo);
            log(stringBuilder.toString());
            networkAgent.networkMonitor.sendMessage(532581);
        }
    }

    public void triggerInvalidlinkNetworkMonitor(NetworkAgentInfo networkAgent) {
        if (networkAgent != null && networkAgent.networkMonitor != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("triggerInvalidlinkNetworkMonitor, nai.networkInfo = ");
            stringBuilder.append(networkAgent.networkInfo);
            log(stringBuilder.toString());
            networkAgent.networkMonitor.sendMessage(532582);
        }
    }

    protected boolean reportPortalNetwork(NetworkAgentInfo nai, int result) {
        if (result != 2) {
            return false;
        }
        nai.asyncChannel.sendMessage(528391, 3, 0, null);
        return true;
    }

    protected boolean ignoreRemovedByWifiPro(NetworkAgentInfo nai) {
        if (nai.networkInfo.getType() != 1 || !WifiProCommonUtils.isWifiProSwitchOn(this.mContext)) {
            return nai.networkInfo.getType() == 0 && isMpLinkEnable();
        } else {
            NetworkInfo activeNetworkInfo = getActiveNetworkInfo();
            if (activeNetworkInfo == null || activeNetworkInfo.getType() != 7) {
                return true;
            }
            log("ignoreRemovedByWifiPro, bluetooth active, needs to remove wifi.");
            return false;
        }
    }

    protected boolean isMpLinkEnable() {
        boolean MplinkEnable = MpLinkCommonUtils.isMpLinkEnabled(this.mContext) && MpLinkCommonUtils.isMpLinkEnabledInternal(this.mContext);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("hCs isMpLinkEnable return:");
        stringBuilder.append(MplinkEnable);
        log(stringBuilder.toString());
        return MplinkEnable;
    }

    protected boolean canMpLink(NetworkAgentInfo newNetwork, NetworkAgentInfo oldNetwork) {
        if (newNetwork.networkInfo.getType() == 1 && oldNetwork.networkInfo.getType() == 0) {
            return true;
        }
        if (oldNetwork.networkInfo.getType() == 1 && newNetwork.networkInfo.getType() == 0) {
            return true;
        }
        return false;
    }

    protected void notifyMpLinkDefaultNetworkChange() {
        HwMpLinkContentAware.getInstance(this.mContext).notifyDefaultNetworkChange();
    }

    protected boolean isAppBindedNetwork() {
        if (HwMplinkManager.getInstance() != null) {
            return HwMplinkManager.getInstance().isAppBindedNetwork();
        }
        log("HwMplinkManager is null");
        return false;
    }

    protected NetworkInfo getActiveNetworkForMpLink(NetworkInfo info, int uid) {
        if (HwMplinkManager.getInstance() != null) {
            return HwMplinkManager.getInstance().getMpLinkNetworkInfo(info, uid);
        }
        log("HwMplinkManager is null");
        return info;
    }

    public Network getNetworkForTypeWifi() {
        Network activeNetwork = super.getActiveNetwork();
        NetworkInfo activeNetworkInfo = super.getActiveNetworkInfo();
        Network[] networks = super.getAllNetworks();
        int i = 0;
        if (activeNetworkInfo != null && activeNetwork != null) {
            NetworkCapabilities anc = super.getNetworkCapabilities(activeNetwork);
            boolean activeVpn = anc != null && anc.hasTransport(4);
            if (!activeVpn && activeNetworkInfo.getType() == 1) {
                return activeNetwork;
            }
            if (!activeVpn && activeNetworkInfo.getType() == 1) {
                return null;
            }
            while (i < networks.length) {
                if (!(activeNetwork == null || networks[i].netId == activeNetwork.netId)) {
                    NetworkCapabilities nc = super.getNetworkCapabilities(networks[i]);
                    if (!(nc == null || !nc.hasTransport(1) || nc.hasTransport(4))) {
                        return networks[i];
                    }
                }
                i++;
            }
            return null;
        } else if (networks.length >= 1) {
            return networks[0];
        } else {
            return null;
        }
    }

    private NetworkInfo getNetworkInfoForBackgroundWifi() {
        NetworkInfo activeNetworkInfo = super.getActiveNetworkInfo();
        Network[] networks = super.getAllNetworks();
        if (activeNetworkInfo != null || networks.length != 1) {
            return null;
        }
        NetworkInfo result = new NetworkInfo(1, 0, ConnectivityManager.getNetworkTypeName(1), "");
        result.setDetailedState(DetailedState.CONNECTED, null, null);
        return result;
    }

    protected void setVpnSettingValue(boolean enable) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("WiFi_PRO, setVpnSettingValue =");
        stringBuilder.append(enable);
        log(stringBuilder.toString());
        System.putInt(this.mContext.getContentResolver(), "wifipro_network_vpn_state", enable);
    }

    private boolean isRequestedByPkgName(int pID, String pkgName) {
        List<RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null || pkgName == null) {
            return false;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess != null && appProcess.pid == pID) {
                String[] pkgNameList = appProcess.pkgList;
                for (Object equals : pkgNameList) {
                    if (pkgName.equals(equals)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public NetworkInfo getActiveNetworkInfo() {
        NetworkInfo networkInfo = super.getActiveNetworkInfo();
        if (networkInfo != null || !isRequestedByPkgName(Binder.getCallingPid(), "com.huawei.systemmanager")) {
            return networkInfo;
        }
        Slog.d(TAG, "return the background wifi network info for system manager.");
        return getNetworkInfoForBackgroundWifi();
    }

    protected boolean isNetworkRequestBip(NetworkRequest nr) {
        if (nr == null) {
            loge("network request is null!");
            return false;
        } else if (nr.networkCapabilities.hasCapability(23) || nr.networkCapabilities.hasCapability(24) || nr.networkCapabilities.hasCapability(25) || nr.networkCapabilities.hasCapability(26) || nr.networkCapabilities.hasCapability(27) || nr.networkCapabilities.hasCapability(28) || nr.networkCapabilities.hasCapability(29)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean checkNetworkSupportBip(NetworkAgentInfo nai, NetworkRequest nri) {
        if (HwModemCapability.isCapabilitySupport(1)) {
            log("MODEM is support BIP!");
            return false;
        } else if (nai == null || nri == null || nai.networkInfo == null) {
            loge("network agent or request is null, just return false!");
            return false;
        } else if (nai.networkInfo.getType() != 0 || !nai.isInternet()) {
            loge("NOT support internet or NOT mobile!");
            return false;
        } else if (isNetworkRequestBip(nri)) {
            String defaultApn = SystemProperties.get("gsm.default.apn");
            String bipApn = SystemProperties.get("gsm.bip.apn");
            StringBuilder stringBuilder;
            if (defaultApn == null || bipApn == null) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("default apn is null or bip apn is null, default: ");
                stringBuilder.append(defaultApn);
                stringBuilder.append(", bip: ");
                stringBuilder.append(bipApn);
                loge(stringBuilder.toString());
                return false;
            } else if (MemoryConstant.MEM_SCENE_DEFAULT.equals(bipApn)) {
                log("bip use default network, return true");
                return true;
            } else {
                String[] buffers = bipApn.split(",");
                if (buffers.length <= 1 || !defaultApn.equalsIgnoreCase(buffers[1].trim())) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("network do NOT support bip, default: ");
                    stringBuilder.append(defaultApn);
                    stringBuilder.append(", bip: ");
                    stringBuilder.append(bipApn);
                    log(stringBuilder.toString());
                    return false;
                }
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("default apn support bip, default: ");
                stringBuilder2.append(defaultApn);
                stringBuilder2.append(", bip: ");
                stringBuilder2.append(buffers[1].trim());
                log(stringBuilder2.toString());
                return true;
            }
        } else {
            loge("network request is NOT bip!");
            return false;
        }
    }

    public void setLteMobileDataEnabled(boolean enable) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[enter]setLteMobileDataEnabled ");
        stringBuilder.append(enable);
        log(stringBuilder.toString());
        enforceChangePermission();
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(enable);
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    public int checkLteConnectState() {
        enforceAccessPermission();
        return mLteMobileDataState;
    }

    protected void handleLteMobileDataStateChange(NetworkInfo info) {
        if (info == null) {
            Slog.e(TAG, "NetworkInfo got null!");
            return;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[enter]handleLteMobileDataStateChange type=");
        stringBuilder.append(info.getType());
        stringBuilder.append(",subType=");
        stringBuilder.append(info.getSubtype());
        Slog.d(str, stringBuilder.toString());
        if (info.getType() == 0) {
            int lteState;
            if (13 == info.getSubtype()) {
                lteState = mapDataStateToLteDataState(info.getState());
            } else {
                lteState = 3;
            }
            setLteMobileDataState(lteState);
        }
    }

    private int mapDataStateToLteDataState(State state) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[enter]mapDataStateToLteDataState state=");
        stringBuilder.append(state);
        log(stringBuilder.toString());
        switch (AnonymousClass13.$SwitchMap$android$net$NetworkInfo$State[state.ordinal()]) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            default:
                String str = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("mapDataStateToLteDataState ignore state = ");
                stringBuilder2.append(state);
                Slog.d(str, stringBuilder2.toString());
                return 3;
        }
    }

    private synchronized void setLteMobileDataState(int state) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[enter]setLteMobileDataState state=");
        stringBuilder.append(state);
        Slog.d(str, stringBuilder.toString());
        mLteMobileDataState = state;
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    private void sendLteDataStateBroadcast(int state) {
        Intent intent = new Intent("android.net.wifi.LTEDATA_COMPLETED_ACTION");
        intent.putExtra("lte_mobile_data_status", state);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Send sticky broadcast from ConnectivityService. intent=");
        stringBuilder.append(intent);
        Slog.d(str, stringBuilder.toString());
        sendStickyBroadcast(intent);
    }

    public long getLteTotalRxBytes() {
        Slog.d(TAG, "[enter]getLteTotalRxBytes");
        enforceAccessPermission();
        long lteRxBytes = 0;
        try {
            Entry entry = getLteStatsEntry(2);
            if (entry != null) {
                lteRxBytes = entry.rxBytes;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("lteTotalRxBytes=");
        stringBuilder.append(lteRxBytes);
        Log.d(str, stringBuilder.toString());
        return lteRxBytes;
    }

    public long getLteTotalTxBytes() {
        Slog.d(TAG, "[enter]getLteTotalTxBytes");
        enforceAccessPermission();
        long lteTxBytes = 0;
        try {
            Entry entry = getLteStatsEntry(8);
            if (entry != null) {
                lteTxBytes = entry.txBytes;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("LteTotalTxBytes=");
        stringBuilder.append(lteTxBytes);
        Log.d(str, stringBuilder.toString());
        return lteTxBytes;
    }

    private Entry getLteStatsEntry(int fields) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[enter]getLteStatsEntry fields=");
        stringBuilder.append(fields);
        Log.d(str, stringBuilder.toString());
        Entry entry = null;
        INetworkStatsSession session = null;
        try {
            NetworkTemplate mobile4gTemplate = NetworkTemplate.buildTemplateMobileAll(((TelephonyManager) this.mContext.getSystemService("phone")).getSubscriberId());
            getStatsService().forceUpdate();
            session = getStatsService().openSession();
            if (session != null) {
                NetworkStatsHistory networkStatsHistory = session.getHistoryForNetwork(mobile4gTemplate, fields);
                if (networkStatsHistory != null) {
                    entry = networkStatsHistory.getValues(Long.MIN_VALUE, Long.MAX_VALUE, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            TrafficStats.closeQuietly(null);
        }
        TrafficStats.closeQuietly(session);
        return entry;
    }

    private static synchronized INetworkStatsService getStatsService() {
        INetworkStatsService iNetworkStatsService;
        synchronized (HwConnectivityService.class) {
            Log.d(TAG, "[enter]getStatsService");
            if (mStatsService == null) {
                mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
            }
            iNetworkStatsService = mStatsService;
        }
        return iNetworkStatsService;
    }

    protected void registerMapconIntentReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MAPCON_START_INTENT);
        filter.addAction(ACTION_MAPCON_SERVICE_FAILED);
        context.registerReceiver(this.mMapconIntentReceiver, filter);
    }

    private int getVoWifiServiceDomain(int phoneId, int type) {
        int domainPrefer = -1;
        if (this.mMapconService != null) {
            try {
                domainPrefer = this.mMapconService.getVoWifiServiceDomain(phoneId, type);
            } catch (RemoteException ex) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("getVoWifiServiceDomain failed, err = ");
                stringBuilder.append(ex.toString());
                loge(stringBuilder.toString());
            }
        }
        boolean isVoWifiRegistered = HwTelephonyManagerInner.getDefault().isWifiCallingAvailable(phoneId);
        boolean isMmsFollowVowifiPreferDomain = getBooleanCarrierConfig(phoneId, "mms_follow_vowifi_prefer_domain");
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("getVoWifiServiceDomain  isVoWifiRegistered:");
        stringBuilder2.append(isVoWifiRegistered);
        stringBuilder2.append(" isMmsFollowVowifiPreferDomain:");
        stringBuilder2.append(isMmsFollowVowifiPreferDomain);
        log(stringBuilder2.toString());
        if (!isMmsFollowVowifiPreferDomain || type != 0 || DomainPreferType.DOMAIN_PREFER_VOLTE.value() != domainPrefer || !isVoWifiRegistered) {
            return domainPrefer;
        }
        log("VoWiFi registered and set mms domain as DOMAIN_PREFER_WIFI");
        return DomainPreferType.DOMAIN_PREFER_WIFI.value();
    }

    private boolean getBooleanCarrierConfig(int subId, String key) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b == null || b.get(key) == null) {
            return false;
        }
        return b.getBoolean(key);
    }

    private int getVoWifiServiceState(int phoneId, int type) {
        if (this.mMapconService == null) {
            return -1;
        }
        try {
            return this.mMapconService.getVoWifiServiceState(phoneId, type);
        } catch (RemoteException ex) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getVoWifiServiceState failed, err = ");
            stringBuilder.append(ex.toString());
            loge(stringBuilder.toString());
            return -1;
        }
    }

    private int getPhoneIdFromNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        int subId = -1;
        int phoneId = -1;
        NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
        if (networkSpecifier != null) {
            try {
                if (networkSpecifier instanceof StringNetworkSpecifier) {
                    subId = Integer.parseInt(networkSpecifier.toString());
                }
            } catch (NumberFormatException ex) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("getPhoneIdFromNetworkCapabilities exception, ex = ");
                stringBuilder.append(ex.toString());
                loge(stringBuilder.toString());
            }
        }
        if (subId != -1) {
            phoneId = SubscriptionManager.getPhoneId(subId);
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("getPhoneIdFromNetworkCapabilities, subId = ");
        stringBuilder2.append(subId);
        stringBuilder2.append(" phoneId:");
        stringBuilder2.append(phoneId);
        log(stringBuilder2.toString());
        return phoneId;
    }

    private boolean isDomainPreferRequest(NetworkRequest request) {
        if (Type.REQUEST == request.type && request.networkCapabilities.hasCapability(0)) {
            return true;
        }
        return false;
    }

    private boolean rebuildNetworkRequestByPrefer(NetworkRequestInfo nri, DomainPreferType prefer) {
        NetworkRequest clientRequest = new NetworkRequest(nri.request);
        NetworkCapabilities networkCapabilities = nri.request.networkCapabilities;
        NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
        if (DomainPreferType.DOMAIN_ONLY_WIFI == prefer || DomainPreferType.DOMAIN_PREFER_WIFI == prefer) {
            networkCapabilities.setNetworkSpecifier(null);
            nri.mPreferType = prefer;
            nri.clientRequest = clientRequest;
            networkCapabilities.addTransportType(1);
            networkCapabilities.removeTransportType(0);
            if (networkSpecifier == null) {
                networkCapabilities.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(SubscriptionManager.getDefaultDataSubscriptionId())));
                return true;
            }
            networkCapabilities.setNetworkSpecifier(networkSpecifier);
            return true;
        } else if (DomainPreferType.DOMAIN_PREFER_CELLULAR != prefer && DomainPreferType.DOMAIN_PREFER_VOLTE != prefer) {
            return false;
        } else {
            networkCapabilities.setNetworkSpecifier(null);
            nri.mPreferType = prefer;
            nri.clientRequest = clientRequest;
            networkCapabilities.addTransportType(0);
            networkCapabilities.removeTransportType(1);
            networkCapabilities.setNetworkSpecifier(networkSpecifier);
            return true;
        }
    }

    protected void handleRegisterNetworkRequest(NetworkRequestInfo nri) {
        NetworkCapabilities cap = nri.request.networkCapabilities;
        int domainPrefer = -1;
        if (isWifiMmsUtOn && isMapconOn && isDomainPreferRequest(nri.request)) {
            int phoneId;
            if (cap.getNetworkSpecifier() != null) {
                phoneId = getPhoneIdFromNetworkCapabilities(cap);
            } else {
                phoneId = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
            }
            if (phoneId != -1 && cap.hasCapability(0) && 1 == getVoWifiServiceState(phoneId, 1)) {
                domainPrefer = getVoWifiServiceDomain(phoneId, 0);
            }
            DomainPreferType prefer = DomainPreferType.fromInt(domainPrefer);
            if (prefer == null || !rebuildNetworkRequestByPrefer(nri, prefer)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("request(");
                stringBuilder.append(nri.request);
                stringBuilder.append(") domainPrefer = ");
                stringBuilder.append(prefer != null ? prefer : "null");
                log(stringBuilder.toString());
            } else {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Update request(");
                stringBuilder2.append(nri.clientRequest);
                stringBuilder2.append(") to ");
                stringBuilder2.append(nri.request);
                stringBuilder2.append(" by ");
                stringBuilder2.append(prefer);
                log(stringBuilder2.toString());
                if (DomainPreferType.DOMAIN_PREFER_WIFI == prefer || DomainPreferType.DOMAIN_PREFER_CELLULAR == prefer || DomainPreferType.DOMAIN_PREFER_VOLTE == prefer) {
                    this.mDomainPreferHandler.sendMessageDelayed(this.mDomainPreferHandler.obtainMessage(2, prefer.value(), 0, nri.request), MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                }
            }
        }
        super.handleRegisterNetworkRequest(nri);
    }

    protected void handleReleaseNetworkRequest(NetworkRequest request, int callingUid) {
        NetworkRequest req = request;
        if (isWifiMmsUtOn && isMapconOn && isDomainPreferRequest(request)) {
            NetworkRequestInfo nri = findExistingNetworkRequestInfo(request.requestId);
            if (!(nri == null || nri.request.equals(request))) {
                if (nri.clientRequest == null || !nri.clientRequest.equals(request)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("BUG: Do not find request in mNetworkRequests for preferRequest:");
                    stringBuilder.append(request);
                    loge(stringBuilder.toString());
                } else {
                    req = nri.request;
                }
            }
        }
        super.handleReleaseNetworkRequest(req, callingUid);
    }

    protected void handleRemoveNetworkRequest(NetworkRequestInfo nri, int whichCallback) {
        if (isWifiMmsUtOn && isMapconOn && isDomainPreferRequest(nri.request)) {
            this.mDomainPreferHandler.removeMessages(2, nri.request);
        }
        super.handleRemoveNetworkRequest(nri, whichCallback);
    }

    protected void notifyNetworkAvailable(NetworkAgentInfo nai, NetworkRequestInfo nri) {
        if (isWifiMmsUtOn && isMapconOn && nri != null && isDomainPreferRequest(nri.request)) {
            this.mDomainPreferHandler.sendMessageAtFrontOfQueue(Message.obtain(this.mDomainPreferHandler, 0, nri.request));
        }
        super.notifyNetworkAvailable(nai, nri);
    }

    private NetworkRequestInfo findExistingNetworkRequestInfo(int requestId) {
        for (Map.Entry<NetworkRequest, NetworkRequestInfo> entry : this.mNetworkRequests.entrySet()) {
            if (((NetworkRequestInfo) entry.getValue()).request.requestId == requestId) {
                return (NetworkRequestInfo) entry.getValue();
            }
        }
        return null;
    }

    public void sendNetworkStickyBroadcastAsUser(String action, NetworkAgentInfo na) {
        if (isMatchedOperator()) {
            String intfName = "lo";
            if (!(na == null || na.linkProperties == null)) {
                intfName = na.linkProperties.getInterfaceName();
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sendNetworkStickyBroadcastAsUser ");
            stringBuilder.append(action);
            stringBuilder.append("-->");
            stringBuilder.append(intfName);
            log(stringBuilder.toString());
            Intent intent = new Intent(HW_CONNECTIVITY_ACTION);
            intent.putExtra("actions", action);
            intent.putExtra("intfName", intfName);
            intent.putExtra(HwCertification.KEY_DATE_FROM, "ConnectivityService");
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            } catch (Exception e) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("sendNetworkStickyBroadcastAsUser failed");
                stringBuilder2.append(e.getMessage());
                log(stringBuilder2.toString());
            }
        }
    }

    private boolean isMatchedOperator() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        String iccid = new StringBuilder();
        iccid.append("");
        iccid.append(tm.getSimSerialNumber());
        iccid = iccid.toString();
        if (HW_SIM_ACTIVATION && iccid.startsWith(VERIZON_ICCID_PREFIX)) {
            return true;
        }
        return false;
    }

    public boolean turnOffVpn(String packageName, int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "ConnectivityService");
        }
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        SparseArray<Vpn> mVpns = getmVpns();
        synchronized (mVpns) {
            Vpn vpn = (Vpn) mVpns.get(userId);
            if (vpn != null) {
                boolean turnOffAllVpn = vpn.turnOffAllVpn(packageName);
                return turnOffAllVpn;
            }
            return false;
        }
    }

    private void processWhenSimStateChange(Intent intent) {
        if (!TelephonyManager.getDefault().isMultiSimEnabled() && !this.isAlreadyPop) {
            if (AwareJobSchedulerConstants.SIM_STATUS_READY.equals(intent.getStringExtra("ss"))) {
                this.mIsSimReady = true;
                connectivityServiceUtils.getContext(this).sendBroadcast(new Intent(DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
            }
        }
    }

    private boolean isSetupWizardCompleted() {
        return 1 == Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) && 1 == Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0);
    }

    private static String getCaptivePortalUserAgent(Context context) {
        return getGlobalSetting(context, "captive_portal_user_agent", DEFAULT_USER_AGENT);
    }

    private static String getGlobalSetting(Context context, String symbol, String defaultValue) {
        String value = Global.getString(context.getContentResolver(), symbol);
        return value != null ? value : defaultValue;
    }

    private void registerVerizonWifiDisconnectedReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        context.registerReceiver(this.mVerizonWifiDisconnectReceiver, filter);
    }

    protected void updateDefaultNetworkRouting(NetworkAgentInfo oldDefaultNet, NetworkAgentInfo newDefaultNet) {
        StringBuilder stringBuilder;
        if (newDefaultNet == null || newDefaultNet.linkProperties == null || newDefaultNet.networkInfo == null) {
            log("invalid new defautl net");
            return;
        }
        if (!(oldDefaultNet == null || oldDefaultNet.linkProperties == null || oldDefaultNet.networkInfo == null || oldDefaultNet.network == null || oldDefaultNet.networkInfo.getType() != 0 || 1 != newDefaultNet.networkInfo.getType())) {
            String oldIface = oldDefaultNet.linkProperties.getInterfaceName();
            if (oldIface != null) {
                try {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Remove oldIface ");
                    stringBuilder2.append(oldIface);
                    stringBuilder2.append(" from network ");
                    stringBuilder2.append(oldDefaultNet.network.netId);
                    log(stringBuilder2.toString());
                    this.mNetd.removeInterfaceFromNetwork(oldIface, oldDefaultNet.network.netId);
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Clear oldIface ");
                    stringBuilder2.append(oldIface);
                    stringBuilder2.append(" addresses");
                    log(stringBuilder2.toString());
                    this.mNetd.clearInterfaceAddresses(oldIface);
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("recovery oldIface ");
                    stringBuilder2.append(oldIface);
                    stringBuilder2.append(" addresses, immediately");
                    log(stringBuilder2.toString());
                    for (LinkAddress oldAddr : oldDefaultNet.linkProperties.getLinkAddresses()) {
                        String oldAddrString = oldAddr.getAddress().getHostAddress();
                        try {
                            StringBuilder stringBuilder3 = new StringBuilder();
                            stringBuilder3.append("add addr_x:  to interface: ");
                            stringBuilder3.append(oldIface);
                            log(stringBuilder3.toString());
                            this.mNetd.getNetdService().interfaceAddAddress(oldIface, oldAddrString, oldAddr.getPrefixLength());
                        } catch (RemoteException | ServiceSpecificException e) {
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Failed to add addr : ");
                            stringBuilder.append(e);
                            loge(stringBuilder.toString());
                        } catch (Exception e2) {
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Failed to add addr : ");
                            stringBuilder.append(e2);
                            loge(stringBuilder.toString());
                        }
                    }
                    log("refresh linkproperties for recovery oldIface");
                    updateLinkPropertiesEx(oldDefaultNet, null);
                } catch (Exception e3) {
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("Exception clearing interface: ");
                    stringBuilder4.append(e3);
                    loge(stringBuilder4.toString());
                }
            }
        }
    }

    public void recordPrivateDnsEvent(Context context, int returnCode, int latencyMs, int netId) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetId(netId);
        if (nai != null) {
            LinkProperties activeLp = nai.linkProperties;
            if (activeLp == null) {
                return;
            }
            if (!activeLp.isPrivateDnsActive()) {
                updateBypassPrivateDnsState((BypassPrivateDnsInfo) this.mBypassPrivateDnsNetwork.get(Integer.valueOf(netId)));
            } else if (isPrivateDnsAutoMode()) {
                countPrivateDnsResponseDelay(returnCode, latencyMs, netId, activeLp);
            }
        }
    }

    private void countPrivateDnsResponseDelay(int returnCode, int latencyMs, int netId, LinkProperties lp) {
        BypassPrivateDnsInfo privateDnsInfo = (BypassPrivateDnsInfo) this.mBypassPrivateDnsNetwork.get(Integer.valueOf(netId));
        if (privateDnsInfo != null) {
            privateDnsInfo.updateDelayCount(returnCode, latencyMs);
            updateBypassPrivateDnsState(privateDnsInfo);
            return;
        }
        String assignedServers = Arrays.toString(NetworkUtils.makeStrings(lp.getDnsServers()));
        NetworkAgentInfo nai = getNetworkAgentInfoForNetId(netId);
        if (nai != null) {
            BypassPrivateDnsInfo newPrivateDnsInfo = new BypassPrivateDnsInfo(this.mContext, nai.networkInfo.getType(), assignedServers);
            synchronized (this.mLock) {
                this.mBypassPrivateDnsNetwork.put(Integer.valueOf(netId), newPrivateDnsInfo);
            }
            newPrivateDnsInfo.sendIntentPrivateDnsEvent();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" countPrivateDnsResponseDelay netId ");
            stringBuilder.append(netId);
            stringBuilder.append(", assignedServers ");
            stringBuilder.append(assignedServers);
            stringBuilder.append(" NetworkType = ");
            stringBuilder.append(nai.networkInfo.getType());
            log(stringBuilder.toString());
        }
    }

    private void updateBypassPrivateDnsState(BypassPrivateDnsInfo privateDnsInfo) {
        if (privateDnsInfo != null && privateDnsInfo.isNeedUpdatePrivateDnsSettings()) {
            updatePrivateDnsSettings();
        }
    }

    private boolean isPrivateDnsAutoMode() {
        DnsManager dnsManager = getDnsManager();
        boolean z = false;
        if (dnsManager != null) {
            PrivateDnsConfig cfg = dnsManager.getPrivateDnsConfig();
            if (cfg != null) {
                if (!cfg.useTls || cfg.inStrictMode()) {
                    boolean isPrivateDnsAutoMode = false;
                } else {
                    int i = 1;
                }
                if (cfg.useTls && !cfg.inStrictMode()) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    public void clearInvalidPrivateDnsNetworkInfo() {
        synchronized (this.mLock) {
            Iterator<Integer> it = this.mBypassPrivateDnsNetwork.keySet().iterator();
            while (it.hasNext()) {
                int networkId = ((Integer) it.next()).intValue();
                boolean invalidNetId = true;
                for (Network network : getAllNetworks()) {
                    if (network.netId == networkId) {
                        invalidNetId = false;
                        break;
                    }
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(" clearInvalidPrivateDnsNetworkInfo networkId ");
                stringBuilder.append(networkId);
                stringBuilder.append(" , invalidNetId ");
                stringBuilder.append(invalidNetId);
                log(stringBuilder.toString());
                if (invalidNetId) {
                    it.remove();
                }
            }
        }
    }

    public boolean isBypassPrivateDns(int netId) {
        BypassPrivateDnsInfo privateDnsInfo = (BypassPrivateDnsInfo) this.mBypassPrivateDnsNetwork.get(Integer.valueOf(netId));
        if (privateDnsInfo != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("isBypassPrivateDns netId: ");
            stringBuilder.append(netId);
            stringBuilder.append(", mBypassPrivateDns: ");
            stringBuilder.append(privateDnsInfo.mBypassPrivateDns);
            log(stringBuilder.toString());
            return privateDnsInfo.mBypassPrivateDns;
        }
        log("isBypassPrivateDns return false");
        return false;
    }
}
