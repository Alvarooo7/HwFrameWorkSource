package com.android.internal.telephony;

import android.app.Notification.Action;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.HwTelephony.VirtualNets;
import android.provider.Settings.Global;
import android.rms.HwSysResManager;
import android.rms.iaware.NetLocationStrategy;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.HwLog;
import com.android.internal.telephony.cdma.HwCdmaServiceStateManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.gsm.HwGsmServiceStateManager;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccCardStatusUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccCardApplicationUtils;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HwServiceStateManager extends Handler {
    protected static final int CT_NUM_MATCH_HOME = 11;
    protected static final int CT_NUM_MATCH_ROAMING = 10;
    protected static final int CT_SID_1st_END = 14335;
    protected static final int CT_SID_1st_START = 13568;
    protected static final int CT_SID_2nd_END = 26111;
    protected static final int CT_SID_2nd_START = 25600;
    protected static final int DEFAULT_SID = 0;
    protected static final int DELAYED_TIME_DEFAULT_VALUE = SystemProperties.getInt("ro.lostnetwork.default_timer", 20);
    protected static final int DELAYED_TIME_NETWORKSTATUS_CS_2G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_cs2G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_CS_3G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_cs3G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_CS_4G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_cs4G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_PS_2G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_ps2G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_PS_3G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_ps3G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    protected static final int DELAYED_TIME_NETWORKSTATUS_PS_4G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_ps4G", DELAYED_TIME_DEFAULT_VALUE) * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
    private static final String DUAL_CARDS_SETTINGS_ACTIVITY = "com.huawei.settings.intent.DUAL_CARD_SETTINGS";
    private static final int DUAL_SIM = 2;
    protected static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 0;
    protected static final int EVENT_ICC_RECORDS_EONS_UPDATED = 1;
    private static final int EVENT_NETWORK_REJINFO_DONE = 156;
    private static final int EVENT_NETWORK_REJINFO_TIMEOUT = 157;
    protected static final int EVENT_RESUME_DATA = 203;
    protected static final int EVENT_SET_PRE_NETWORKTYPE = 202;
    private static final int EVENT_SIM_HOTPLUG = 158;
    private static final String EXTRA_SHOW_WIFI = "showWifi";
    private static final String EXTRA_WIFI = "wifi";
    private static final int INTERVAL_TIME = 120000;
    protected static final String INVAILD_PLMN = "1023127-123456-1023456-123127-";
    private static final int INVALID = -1;
    protected static final boolean IS_CHINATELECOM = SystemProperties.get("ro.config.hw_opta", "0").equals("92");
    protected static final boolean IS_MULTI_SIM_ENABLED = TelephonyManager.getDefault().isMultiSimEnabled();
    private static final String KEY_WFC_FORMAT_WIFI_STRING = "wfc_format_wifi_string";
    private static final String KEY_WFC_HIDE_WIFI_BOOL = "wfc_hide_wifi_bool";
    private static final String KEY_WFC_IS_SHOW_AIRPLANE = "wfc_is_show_air_plane";
    private static final String KEY_WFC_IS_SHOW_EMERGENCY_ONLY = "wfc_is_show_emergency_only";
    private static final String KEY_WFC_IS_SHOW_NO_SERVICE = "wfc_is_show_no_service";
    private static final String KEY_WFC_SPN_STRING = "wfc_spn_string";
    private static final int MAX_TOP_PACKAGE = 10;
    protected static final long MODE_REQUEST_CELL_LIST_STRATEGY_INVALID = -1;
    protected static final long MODE_REQUEST_CELL_LIST_STRATEGY_VALID = 0;
    private static final int[] NETWORK_REJINFO_CAUSES = new int[]{7, 11, 12, 13, 14, 15, 17};
    private static final int NETWORK_REJINFO_MAX_COUNT = 3;
    private static final int NETWORK_REJINFO_MAX_TIME = 1200000;
    private static final int REJINFO_LEN = 4;
    protected static final int RESUME_DATA_TIME = 8000;
    protected static final int SET_PRE_NETWORK_TIME = 5000;
    protected static final int SET_PRE_NETWORK_TIME_DELAY = 2000;
    protected static final int SPN_RULE_SHOW_BOTH = 3;
    protected static final int SPN_RULE_SHOW_PLMN_ONLY = 2;
    protected static final int SPN_RULE_SHOW_PNN_PRIOR = 4;
    protected static final int SPN_RULE_SHOW_SPN_ONLY = 1;
    protected static final int SPN_RULE_SHOW_SPN_PRIOR = 5;
    private static final String TAG = "HwServiceStateManager";
    protected static final long VALUE_CELL_INFO_LIST_MAX_AGE_MS = 2000;
    protected static final int VALUE_SCREEN_OFF_TIME_DEFAULT = 10;
    private static final int WIFI_IDX = 1;
    private static Map<Object, HwCdmaServiceStateManager> cdmaServiceStateManagers = new HashMap();
    private static Map<Object, HwGsmServiceStateManager> gsmServiceStateManagers = new HashMap();
    private static final boolean isScreenOffNotUpdateLocation = SystemProperties.getBoolean("ro.config.updatelocation", false);
    private static Map<Object, HwServiceStateManager> serviceStateManagers = new HashMap();
    protected static final UiccCardApplicationUtils uiccCardApplicationUtils = new UiccCardApplicationUtils();
    private static final boolean voice_reg_state_for_ons = "true".equals(SystemProperties.get("ro.hwpp.voice_reg_state_for_ons", "false"));
    private Map<String, Integer> mCellInfoMap = new HashMap();
    private Context mContext;
    protected boolean mCurShowWifi = false;
    protected String mCurWifi = "";
    protected int mMainSlot;
    private boolean mNeedHandleRejInfoFlag = true;
    protected int mPendingPreNwType = 0;
    protected Message mPendingsavemessage;
    private Phone mPhoneBase;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("subscription", -1);
                ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
                if (slotId == HwServiceStateManager.this.mPhoneBase.getPhoneId() && serviceState.getState() == 0) {
                    HwServiceStateManager.this.clearNetworkRejInfo();
                    ((NotificationManager) HwServiceStateManager.this.mContext.getSystemService("notification")).cancel(HwServiceStateManager.TAG, slotId);
                }
            }
        }
    };
    protected boolean mRefreshState = false;
    private ServiceStateTracker mServiceStateTracker;
    protected boolean mSetPreNwTypeRequested = false;
    private long mStartTime = MODE_REQUEST_CELL_LIST_STRATEGY_VALID;
    private ArrayList<String> networkRejInfoList = new ArrayList();

    private enum HotplugState {
        STATE_PLUG_OUT,
        STATE_PLUG_IN
    }

    protected HwServiceStateManager(Phone phoneBase) {
        super(Looper.getMainLooper());
        this.mPhoneBase = phoneBase;
        this.mContext = phoneBase.getContext();
        initNetworkRejInfo();
    }

    public String getPlmn() {
        return "";
    }

    public void sendDualSimUpdateSpnIntent(boolean showSpn, String spn, boolean showPlmn, String plmn) {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            Intent intent = null;
            int phoneId = this.mPhoneBase.getPhoneId();
            if (phoneId == 0) {
                intent = new Intent("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED");
            } else if (1 == phoneId) {
                intent = new Intent("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED");
            } else {
                Rlog.e(TAG, "unsupport SUB ID :" + phoneId);
            }
            if (intent != null) {
                intent.addFlags(536870912);
                intent.putExtra("showSpn", showSpn);
                intent.putExtra(VirtualNets.SPN, spn);
                intent.putExtra("showPlmn", showPlmn);
                intent.putExtra("plmn", plmn);
                intent.putExtra("subscription", phoneId);
                intent.putExtra(EXTRA_SHOW_WIFI, this.mCurShowWifi);
                intent.putExtra(EXTRA_WIFI, this.mCurWifi);
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                Rlog.d(TAG, "Send updateSpnIntent for SUB :" + phoneId);
            }
        }
    }

    public OnsDisplayParams getOnsDisplayParamsHw(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        return new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
    }

    public HwServiceStateManager(ServiceStateTracker serviceStateTracker, Phone phoneBase) {
        super(Looper.getMainLooper());
        this.mPhoneBase = phoneBase;
        this.mServiceStateTracker = serviceStateTracker;
        this.mContext = phoneBase.getContext();
        initNetworkRejInfo();
    }

    public static synchronized HwServiceStateManager getHwServiceStateManager(ServiceStateTracker serviceStateTracker, Phone phoneBase) {
        HwServiceStateManager hwServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwServiceStateManager = (HwServiceStateManager) serviceStateManagers.get(serviceStateTracker);
            if (hwServiceStateManager == null) {
                hwServiceStateManager = new HwServiceStateManager(serviceStateTracker, phoneBase);
                serviceStateManagers.put(serviceStateTracker, hwServiceStateManager);
            }
        }
        return hwServiceStateManager;
    }

    public static synchronized HwGsmServiceStateManager getHwGsmServiceStateManager(ServiceStateTracker serviceStateTracker, GsmCdmaPhone phone) {
        HwGsmServiceStateManager hwGsmServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwGsmServiceStateManager = (HwGsmServiceStateManager) gsmServiceStateManagers.get(serviceStateTracker);
            if (hwGsmServiceStateManager == null) {
                hwGsmServiceStateManager = new HwGsmServiceStateManager(serviceStateTracker, phone);
                gsmServiceStateManagers.put(serviceStateTracker, hwGsmServiceStateManager);
            }
        }
        return hwGsmServiceStateManager;
    }

    public static synchronized HwCdmaServiceStateManager getHwCdmaServiceStateManager(ServiceStateTracker serviceStateTracker, GsmCdmaPhone phone) {
        HwCdmaServiceStateManager hwCdmaServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwCdmaServiceStateManager = (HwCdmaServiceStateManager) cdmaServiceStateManagers.get(serviceStateTracker);
            if (hwCdmaServiceStateManager == null) {
                hwCdmaServiceStateManager = new HwCdmaServiceStateManager(serviceStateTracker, phone);
                cdmaServiceStateManagers.put(serviceStateTracker, hwCdmaServiceStateManager);
            }
        }
        return hwCdmaServiceStateManager;
    }

    public static synchronized void dispose(ServiceStateTracker serviceStateTracker) {
        synchronized (HwServiceStateManager.class) {
            if (serviceStateTracker == null) {
                return;
            }
            HwGsmServiceStateManager hwGsmServiceStateManager = (HwGsmServiceStateManager) gsmServiceStateManagers.get(serviceStateTracker);
            if (hwGsmServiceStateManager != null) {
                hwGsmServiceStateManager.dispose();
            }
            gsmServiceStateManagers.put(serviceStateTracker, null);
            HwCdmaServiceStateManager hwCdmaServiceStateManager = (HwCdmaServiceStateManager) cdmaServiceStateManagers.get(serviceStateTracker);
            if (hwCdmaServiceStateManager != null) {
                hwCdmaServiceStateManager.dispose();
            }
            cdmaServiceStateManagers.put(serviceStateTracker, null);
        }
    }

    public int getCombinedRegState(ServiceState serviceState) {
        if (serviceState == null) {
            return 1;
        }
        int regState = serviceState.getVoiceRegState();
        int dataRegState = serviceState.getDataRegState();
        if (voice_reg_state_for_ons && serviceState.getRilDataRadioTechnology() != 14 && serviceState.getRilDataRadioTechnology() != 19) {
            return regState;
        }
        if (regState == 1 && dataRegState == 0) {
            Rlog.d(TAG, "getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
            regState = dataRegState;
        }
        return regState;
    }

    public void processCTNumMatch(boolean roaming, UiccCardApplication uiccCardApplication) {
    }

    protected void checkMultiSimNumMatch() {
        int[] matchArray = new int[]{SystemProperties.getInt("gsm.hw.matchnum0", -1), SystemProperties.getInt("gsm.hw.matchnum.short0", -1), SystemProperties.getInt("gsm.hw.matchnum1", -1), SystemProperties.getInt("gsm.hw.matchnum.short1", -1)};
        Arrays.sort(matchArray);
        int numMatch = matchArray[3];
        int numMatchShort = numMatch;
        int i = 2;
        while (i >= 0) {
            if (matchArray[i] < numMatch && matchArray[i] > 0) {
                numMatchShort = matchArray[i];
            }
            i--;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(numMatch));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(numMatchShort));
        Rlog.d(TAG, "checkMultiSimNumMatch: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum", 0) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short", 0));
    }

    protected void setCTNumMatchHomeForSlot(int slotId) {
        if (IS_MULTI_SIM_ENABLED) {
            SystemProperties.set("gsm.hw.matchnum" + slotId, Integer.toString(11));
            SystemProperties.set("gsm.hw.matchnum.short" + slotId, Integer.toString(11));
            checkMultiSimNumMatch();
            return;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(11));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(11));
    }

    protected void setCTNumMatchRoamingForSlot(int slotId) {
        if (IS_MULTI_SIM_ENABLED) {
            SystemProperties.set("gsm.hw.matchnum" + slotId, Integer.toString(10));
            SystemProperties.set("gsm.hw.matchnum.short" + slotId, Integer.toString(10));
            checkMultiSimNumMatch();
            return;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(10));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(10));
    }

    public static boolean isCustScreenOff(GsmCdmaPhone phoneBase) {
        if (!(!isScreenOffNotUpdateLocation || phoneBase == null || phoneBase.getContext() == null)) {
            PowerManager powerManager = (PowerManager) phoneBase.getContext().getSystemService("power");
            if (!(powerManager == null || (powerManager.isScreenOn() ^ 1) == 0)) {
                Rlog.d(TAG, " ScreenOff do nothing");
                return true;
            }
        }
        return false;
    }

    public void setOOSFlag(boolean flag) {
    }

    private void setPreferredNetworkType(int networkType, int phoneId, Message response) {
        if (!HwModemCapability.isCapabilitySupport(9) || TelephonyManager.getDefault().getPhoneCount() <= 1) {
            this.mPhoneBase.mCi.setPreferredNetworkType(networkType, response);
            return;
        }
        Rlog.d(TAG, "PhoneCount > 1");
        HwModemBindingPolicyHandler.getInstance().setPreferredNetworkType(networkType, phoneId, response);
    }

    public void setPreferredNetworkTypeSafely(Phone phoneBase, int networkType, Message response) {
        this.mPhoneBase = phoneBase;
        DcTracker dcTracker = this.mPhoneBase.mDcTracker;
        if (this.mServiceStateTracker == null) {
            Rlog.d(TAG, "mServiceStateTracker is null, it is unexpected!");
        }
        if (networkType != 10) {
            if (this.mSetPreNwTypeRequested) {
                removeMessages(EVENT_SET_PRE_NETWORKTYPE);
                Rlog.d(TAG, "cancel setPreferredNetworkType");
            }
            this.mSetPreNwTypeRequested = false;
            Rlog.d(TAG, "PreNetworkType is not LTE, setPreferredNetworkType now!");
            setPreferredNetworkType(networkType, this.mPhoneBase.getPhoneId(), response);
        } else if (!this.mSetPreNwTypeRequested) {
            if (dcTracker.isDisconnected()) {
                setPreferredNetworkType(networkType, this.mPhoneBase.getPhoneId(), response);
                Rlog.d(TAG, "data is Disconnected, setPreferredNetworkType now!");
                return;
            }
            dcTracker.setInternalDataEnabled(false);
            Rlog.d(TAG, "Data is disabled and wait up to 8s to resume data.");
            sendMessageDelayed(obtainMessage(EVENT_RESUME_DATA), 8000);
            this.mPendingsavemessage = response;
            this.mPendingPreNwType = networkType;
            Message msg = Message.obtain(this);
            msg.what = EVENT_SET_PRE_NETWORKTYPE;
            msg.arg1 = networkType;
            msg.obj = response;
            Rlog.d(TAG, "Wait up to 5s for data disconnect to setPreferredNetworkType.");
            sendMessageDelayed(msg, 5000);
            this.mSetPreNwTypeRequested = true;
        }
    }

    public void checkAndSetNetworkType() {
        if (this.mSetPreNwTypeRequested) {
            Rlog.d(TAG, "mSetPreNwTypeRequested is true and wait a few seconds to setPreferredNetworkType");
            removeMessages(EVENT_SET_PRE_NETWORKTYPE);
            Message msg = Message.obtain(this);
            msg.what = EVENT_SET_PRE_NETWORKTYPE;
            msg.arg1 = this.mPendingPreNwType;
            msg.obj = this.mPendingsavemessage;
            sendMessageDelayed(msg, 2000);
            return;
        }
        Rlog.d(TAG, "No need to setPreferredNetworkType");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_NETWORK_REJINFO_DONE /*156*/:
                logd("EVENT_NETWORK_REJINFO_DONE");
                onNetworkRejInfoDone(msg);
                return;
            case EVENT_NETWORK_REJINFO_TIMEOUT /*157*/:
                logd("EVENT_NETWORK_REJINFO_TIMEOUT");
                onNetworkRejInfoTimeout(msg);
                return;
            case EVENT_SIM_HOTPLUG /*158*/:
                logd("EVENT_SIM_HOTPLUG");
                onSimHotPlug(msg);
                return;
            case EVENT_SET_PRE_NETWORKTYPE /*202*/:
                if (this.mSetPreNwTypeRequested) {
                    Rlog.d(TAG, "EVENT_SET_PRE_NETWORKTYPE, setPreferredNetworkType now.");
                    setPreferredNetworkType(msg.arg1, this.mPhoneBase.getPhoneId(), (Message) msg.obj);
                    this.mSetPreNwTypeRequested = false;
                    return;
                }
                Rlog.d(TAG, "No need to setPreferredNetworkType");
                return;
            case EVENT_RESUME_DATA /*203*/:
                this.mPhoneBase.mDcTracker.setInternalDataEnabled(true);
                Rlog.d(TAG, "EVENT_RESUME_DATA, resume data now.");
                return;
            default:
                Rlog.d(TAG, "Unhandled message with number: " + msg.what);
                return;
        }
    }

    public boolean isCardInvalid(boolean isSubDeactivated, int subId) {
        CardState newState = CardState.CARDSTATE_ABSENT;
        UiccCard newCard = UiccController.getInstance().getUiccCard(subId);
        if (newCard != null) {
            newState = newCard.getCardState();
        }
        boolean isCardPresent = IccCardStatusUtils.isCardPresent(newState);
        Rlog.d(TAG, "isCardPresent : " + isCardPresent + "  subId : " + subId);
        return isCardPresent ? isSubDeactivated : true;
    }

    protected OnsDisplayParams getOnsDisplayParamsForVoWifi(OnsDisplayParams ons) {
        int voiceIdx = 0;
        String spnConfiged = "";
        boolean z = false;
        String wifiConfiged = "";
        boolean z2 = false;
        boolean z3 = false;
        boolean isShowAirplane = false;
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configLoader != null) {
            try {
                PersistableBundle b = configLoader.getConfigForSubId(this.mPhoneBase.getSubId());
                if (b != null) {
                    voiceIdx = b.getInt("wfc_spn_format_idx_int");
                    spnConfiged = b.getString(KEY_WFC_SPN_STRING);
                    z = b.getBoolean(KEY_WFC_HIDE_WIFI_BOOL);
                    wifiConfiged = b.getString(KEY_WFC_FORMAT_WIFI_STRING);
                    z2 = b.getBoolean(KEY_WFC_IS_SHOW_NO_SERVICE);
                    z3 = b.getBoolean(KEY_WFC_IS_SHOW_EMERGENCY_ONLY);
                    isShowAirplane = b.getBoolean(KEY_WFC_IS_SHOW_AIRPLANE);
                }
            } catch (Exception e) {
                Rlog.e(TAG, "getGsmOnsDisplayParams: carrier config error: " + e);
            }
        }
        Rlog.d(TAG, "updateSpnDisplay, voiceIdx = " + voiceIdx + " spnConfiged = " + spnConfiged + " hideWifi = " + z + " wifiConfiged = " + wifiConfiged + " isShowNoService = " + z2 + " isShowEmergency = " + z3 + " isShowAirplane = " + isShowAirplane);
        String formatWifi = "%s";
        if (!z) {
            boolean useGoogleWifiFormat = voiceIdx == 1;
            String[] wfcSpnFormats = this.mContext.getResources().getStringArray(17236076);
            if (!TextUtils.isEmpty(wifiConfiged)) {
                formatWifi = wifiConfiged;
            } else if (!useGoogleWifiFormat || wfcSpnFormats == null) {
                formatWifi = this.mContext.getResources().getString(17041117);
            } else {
                formatWifi = wfcSpnFormats[1];
            }
        }
        String combineWifi = "";
        boolean inService = getCombinedRegState(this.mServiceStateTracker.mSS) == 0;
        boolean noService = false;
        boolean emergencyOnly = false;
        boolean airplaneMode = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        int combinedRegState = getCombinedRegState(this.mServiceStateTracker.mSS);
        if (combinedRegState == 1 || combinedRegState == 2) {
            if (this.mServiceStateTracker.mSS == null || !this.mServiceStateTracker.mSS.isEmergencyOnly()) {
                noService = true;
            } else {
                emergencyOnly = true;
            }
        }
        if (!TextUtils.isEmpty(spnConfiged)) {
            combineWifi = spnConfiged;
        } else if (!TextUtils.isEmpty(ons.mSpn)) {
            combineWifi = ons.mSpn;
        } else if (inService && (TextUtils.isEmpty(ons.mPlmn) ^ 1) != 0) {
            combineWifi = ons.mPlmn;
        }
        if (airplaneMode && isShowAirplane) {
            combineWifi = Resources.getSystem().getText(17040086).toString();
        } else if (noService && z2) {
            combineWifi = Resources.getSystem().getText(17040285).toString();
        } else if (emergencyOnly && z3) {
            combineWifi = Resources.getSystem().getText(17039946).toString();
        }
        try {
            ons.mWifi = String.format(formatWifi, new Object[]{combineWifi}).trim();
            ons.mShowWifi = true;
        } catch (RuntimeException e2) {
            Rlog.e(TAG, "combine wifi fail, " + e2);
        }
        return ons;
    }

    public void countPackageUseCellInfo(String packageName) {
        if (this.mPhoneBase != null) {
            boolean isMainSub = this.mPhoneBase.getSubId() == HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            if (isMainSub || this.mCellInfoMap.size() != 0) {
                Rlog.d(TAG, "countPackageUseCellInfo packageName is :" + packageName);
                if (TextUtils.isEmpty(packageName)) {
                    Rlog.d(TAG, "countPackageUseCellInfo packageName is null");
                    return;
                }
                if (this.mStartTime == MODE_REQUEST_CELL_LIST_STRATEGY_VALID) {
                    this.mStartTime = SystemClock.elapsedRealtime();
                }
                if (Math.abs(SystemClock.elapsedRealtime() - this.mStartTime) >= HwVSimConstants.INITIAL_TIMEOUT) {
                    String topPackageString = "";
                    int topPackageNum = 0;
                    int count = this.mCellInfoMap.size();
                    Rlog.d(TAG, "countPackageUseCellInfo size:" + count);
                    for (Entry<String, Integer> entry : this.mCellInfoMap.entrySet()) {
                        topPackageString = topPackageString + " name=" + ((String) entry.getKey()) + " num=" + entry.getValue();
                        topPackageNum++;
                        if (10 == topPackageNum) {
                            HwLog.dubaie("DUBAI_TAG_LOCATION_COUNTER", "count=10" + topPackageString);
                            Rlog.d(TAG, "countPackageUseCellInfo topPackageString:count=10" + topPackageString);
                            topPackageNum = 0;
                            topPackageString = "";
                            count -= 10;
                        } else if (count / 10 == 0) {
                            count--;
                            if (count == 0) {
                                HwLog.dubaie("DUBAI_TAG_LOCATION_COUNTER", "count=" + topPackageNum + topPackageString);
                                Rlog.d(TAG, "countPackageUseCellInfo topPackageString:count=" + topPackageNum + topPackageString);
                            }
                        }
                    }
                    this.mStartTime = SystemClock.elapsedRealtime();
                    this.mCellInfoMap.clear();
                }
                int isCharging = 0;
                BatteryManager batteryManager = (BatteryManager) this.mPhoneBase.getContext().getSystemService("batterymanager");
                if (batteryManager != null) {
                    isCharging = batteryManager.isCharging();
                }
                if (isMainSub && isCustScreenOff((GsmCdmaPhone) this.mPhoneBase) && (r4 ^ 1) != 0) {
                    if (this.mCellInfoMap.containsKey(packageName)) {
                        this.mCellInfoMap.put(packageName, Integer.valueOf(((Integer) this.mCellInfoMap.get(packageName)).intValue() + 1));
                    } else {
                        this.mCellInfoMap.put(packageName, Integer.valueOf(1));
                    }
                }
            }
        }
    }

    private boolean isCellAgeTimePassed(ServiceStateTracker stateTracker, GsmCdmaPhone phoneBase) {
        long cellInfoListMaxAgeTime;
        long curSysTime = SystemClock.elapsedRealtime();
        long lastRequestTime = stateTracker.getLastCellInfoListTime();
        boolean isScreenOff = isCustScreenOff(phoneBase);
        int screenOffTimes = SystemProperties.getInt("ro.config.screen_off_times", 10);
        if (isScreenOff) {
            cellInfoListMaxAgeTime = 2000 * ((long) screenOffTimes);
        } else {
            cellInfoListMaxAgeTime = 2000;
        }
        Rlog.d(TAG, "isCellAgeTimePassed(): isScreenOff=" + isScreenOff + " cellInfoListMaxAgeTime=" + cellInfoListMaxAgeTime + "ms.");
        if (curSysTime - lastRequestTime > cellInfoListMaxAgeTime) {
            Rlog.d(TAG, "isCellAgeTimePassed():return true.");
            return true;
        }
        Rlog.d(TAG, "isCellAgeTimePassed():return false,because already requested CellInfoList within " + cellInfoListMaxAgeTime + "ms.");
        return false;
    }

    public boolean isCellRequestStrategyPassed(ServiceStateTracker stateTracker, WorkSource workSource, GsmCdmaPhone phoneBase) {
        if (stateTracker == null || workSource == null || phoneBase == null || TextUtils.isEmpty(workSource.getName(0))) {
            Rlog.e(TAG, "isCellRequestStrategyPassed():return false.Because null-pointer params");
            return false;
        } else if (stateTracker.getLastCellInfoList() == null) {
            Rlog.d(TAG, "isCellRequestStrategyPassed():return true.Because request first time.");
            return true;
        } else if (isCellAgeTimePassed(stateTracker, phoneBase)) {
            long curSysTime = SystemClock.elapsedRealtime();
            long lastRequestTime = stateTracker.getLastCellInfoListTime();
            String pkgName = workSource.getName(0);
            int uid = workSource.get(0);
            long id = Binder.clearCallingIdentity();
            NetLocationStrategy strategy = HwSysResManager.getInstance().getNetLocationStrategy(pkgName, uid, 2);
            Binder.restoreCallingIdentity(id);
            if (strategy != null) {
                Rlog.d(TAG, "isCellRequestStrategyPassed():get iAware strategy result = " + strategy.toString());
                if (MODE_REQUEST_CELL_LIST_STRATEGY_INVALID == strategy.getCycle()) {
                    Rlog.d(TAG, "isCellRequestStrategyPassed():return false.Because iAware strategy return NOT_ALLOWED");
                    return false;
                } else if (MODE_REQUEST_CELL_LIST_STRATEGY_VALID >= strategy.getCycle() || curSysTime - lastRequestTime >= strategy.getCycle()) {
                    Rlog.d(TAG, "isCellRequestStrategyPassed():return true.");
                    return true;
                } else {
                    Rlog.d(TAG, "isCellRequestStrategyPassed():return false.Because already requested within iAware strategy cycle");
                    return false;
                }
            }
            Rlog.e(TAG, "isCellRequestStrategyPassed():get iAware strategy result = null.");
            return true;
        } else {
            Rlog.d(TAG, "isCellRequestStrategyPassed():return false.Because isCellAgeTime is not passed.");
            return false;
        }
    }

    private void initNetworkRejInfo() {
        if (TelephonyManager.getDefault().getSimCount() == 2) {
            this.mPhoneBase.mCi.setOnNetReject(this, EVENT_NETWORK_REJINFO_DONE, null);
            this.mPhoneBase.mCi.registerForSimHotPlug(this, EVENT_SIM_HOTPLUG, Integer.valueOf(this.mPhoneBase.getPhoneId()));
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SERVICE_STATE"));
        }
    }

    private void onNetworkRejInfoDone(Message msg) {
        AsyncResult ar = msg.obj;
        if (ar.exception == null) {
            String[] data = ar.result;
            String str = null;
            int rejectdomain = -1;
            int rejectcause = -1;
            int rejectrat = -1;
            if (data.length >= 4) {
                try {
                    if (data[0] != null && data[0].length() > 0) {
                        str = data[0];
                    }
                    if (data[1] != null && data[1].length() > 0) {
                        rejectdomain = Integer.parseInt(data[1]);
                    }
                    if (data[2] != null && data[2].length() > 0) {
                        rejectcause = Integer.parseInt(data[2]);
                    }
                    if (data[3] != null && data[3].length() > 0) {
                        rejectrat = Integer.parseInt(data[3]);
                    }
                } catch (Exception ex) {
                    Rlog.e(TAG, "error parsing NetworkReject!", ex);
                }
                handleNetworkRejinfoNotification(str, rejectdomain, rejectcause, rejectrat);
            }
        }
    }

    private void onNetworkRejInfoTimeout(Message msg) {
        if (this.networkRejInfoList.size() > 3) {
            showNetworkRejInfoNotification();
            clearNetworkRejInfo();
            return;
        }
        this.networkRejInfoList.clear();
    }

    private void clearNetworkRejInfo() {
        logd("clearNetworkRejInfo");
        this.networkRejInfoList.clear();
        removeMessages(EVENT_NETWORK_REJINFO_TIMEOUT);
        setNeedHandleRejInfoFlag(false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void handleNetworkRejinfoNotification(String rejectplmn, int rejectdomain, int rejectcause, int rejectrat) {
        logd("handleNetworkRejinfoNotification:PLMN = " + rejectplmn + " domain = " + rejectdomain + " cause = " + rejectcause + " RAT = " + rejectrat);
        boolean needHandleRejectCause = Arrays.binarySearch(NETWORK_REJINFO_CAUSES, rejectcause) >= 0;
        if (this.mNeedHandleRejInfoFlag && !TextUtils.isEmpty(rejectplmn) && (needHandleRejectCause ^ 1) == 0) {
            if (!hasMessages(EVENT_NETWORK_REJINFO_TIMEOUT)) {
                sendEmptyMessageDelayed(EVENT_NETWORK_REJINFO_TIMEOUT, 1200000);
            }
            if (!this.networkRejInfoList.contains(rejectplmn)) {
                this.networkRejInfoList.add(rejectplmn);
                logd("handleNetworkRejinfoNotification: add " + rejectplmn + " rejinfoList:" + this.networkRejInfoList);
            }
        }
    }

    private void showNetworkRejInfoNotification() {
        if (this.mContext != null) {
            logd("showNetworkRejinfoNotification");
            Intent resultIntent = new Intent(DUAL_CARDS_SETTINGS_ACTIVITY);
            resultIntent.setFlags(335544320);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this.mContext, 0, resultIntent, 134217728);
            int showSubId = this.mPhoneBase.getPhoneId() + 1;
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(TAG, this.mPhoneBase.getPhoneId(), new Builder(this.mContext).setSmallIcon(33751804).setAppName(this.mContext.getString(33686060)).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).setDefaults(-1).setContentTitle(this.mContext.getString(33686064, new Object[]{Integer.valueOf(showSubId)})).setContentText(this.mContext.getString(33686063, new Object[]{Integer.valueOf(showSubId)})).setContentIntent(resultPendingIntent).setStyle(new BigTextStyle()).addAction(new Action.Builder(null, this.mContext.getString(33686059), resultPendingIntent).build()).build());
        }
    }

    private void onSimHotPlug(Message msg) {
        AsyncResult ar = msg.obj;
        if (ar != null && ar.result != null && ((int[]) ar.result).length > 0) {
            if (HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.result)[0]) {
                setNeedHandleRejInfoFlag(true);
            } else if (HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) ar.result)[0]) {
                clearNetworkRejInfo();
            }
        }
    }

    private void setNeedHandleRejInfoFlag(boolean needHandleRejInfoFlag) {
        if (this.mNeedHandleRejInfoFlag != needHandleRejInfoFlag) {
            this.mNeedHandleRejInfoFlag = needHandleRejInfoFlag;
            logd("set mNeedHandleRejInfoFlag =" + this.mNeedHandleRejInfoFlag);
        }
    }

    private void logd(String msg) {
        Rlog.d(TAG, "[SUB" + this.mPhoneBase.getPhoneId() + "]" + msg);
    }
}
