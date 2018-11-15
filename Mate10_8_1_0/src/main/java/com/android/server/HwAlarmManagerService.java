package com.android.server;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Slog;
import com.android.server.AlarmManagerService.Alarm;
import com.android.server.AlarmManagerService.AlarmHandler;
import com.android.server.AlarmManagerService.Batch;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import com.android.server.rms.iaware.appmng.AwareWakeUpManager;
import com.android.server.rms.iaware.feature.AlarmManagerFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.android.app.IHwAlarmManagerEx.Stub;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

public class HwAlarmManagerService extends AlarmManagerService {
    private static final String ACTION_ALARM_WAKEUP = "com.android.deskclock.ALARM_ALERT";
    private static final String ALARM_ID = "intent.extra.alarm_id";
    private static final String ALARM_WHEN = "intent.extra.alarm_when";
    private static boolean DEBUG_SHB = false;
    private static final boolean DEBUG_ST = false;
    private static final String DESKCLOCK_PACKAGENAME = "com.android.deskclock";
    private static final String HWAIRPLANESTATE_PROPERTY = "persist.sys.hwairplanestate";
    private static final boolean IS_DEVICE_ENCRYPTED = "encrypted".equals(SystemProperties.get("ro.crypto.state", ""));
    private static final String IS_OUT_OF_DATA_ALARM = "is_out_of_data_alarm";
    private static final String IS_OWNER_ALARM = "is_owner_alarm";
    private static final boolean IS_POWEROFF_ALARM_ENABLED = StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get("ro.poweroff_alarm", StorageUtils.SDCARD_ROMOUNTED_STATE));
    private static final String KEY_BOOT_MDM = "boot_alarm_mdm";
    private static final String LAST_TIME_CHANGED_RTC = "last_time_changed_rtc";
    private static final int NONE_LISTVIEW = 1;
    private static final int ONE_BOOT_LISTVIEW = 5;
    private static final int ONE_DESKCLOCK_LISTVIEW = 4;
    private static final int ONE_TWO_BOOT_LISTVIEW = 7;
    private static final int ONE_TWO_DESKCLOCK_LISTVIEW = 6;
    private static final String REMOVE_POWEROFF_ALARM_ANYWAY = "remove_poweroff_alarm_anyway";
    private static final String SAVE_TO_REGISTER = "save_to_register";
    private static final String SETTINGS_PACKAGENAME = "com.android.providers.settings";
    static final String TAG = "HwAlarmManagerService";
    private static final int TRIM_ALARM_POST_MSG_DELAY = 10;
    private static final int TWO_BOOT_DLISTVIEW = 3;
    private static final int TWO_DESKCLOCK_DLISTVIEW = 2;
    private static HashMap<String, Alarm> mHwWakeupBoot = new HashMap();
    private static String mPropHwRegisterName = "error";
    private static String timeInRegister = null;
    private boolean hasDeskClocksetAlarm = false;
    private boolean isSetHwMDMAlarm = false;
    Context mContext;
    private long mFirstELAPSED;
    private long mFirstRTC;
    private boolean mHwAlarmLock = false;
    private Alarm mHwMDMAlarm = null;
    private boolean mIsFirstPowerOffAlarm = true;
    private PendingIntent mPendingAlarm = null;
    private SmartHeartBeatDummy mSmartHB = null;
    private HashSet<String> mTrimAlarmPkg = null;

    public class HwAlarmHandler extends AlarmHandler {
        public static final int TRIM_PKG_ALARM = 5;

        public HwAlarmHandler() {
            super(HwAlarmManagerService.this);
        }

        public void handleMessage(Message msg) {
            if (5 == msg.what) {
                HwAlarmManagerService.this.removeByPkg_hwHsm(HwAlarmManagerService.this.mTrimAlarmPkg);
                HwAlarmManagerService.this.mTrimAlarmPkg.clear();
            }
            super.handleMessage(msg);
        }
    }

    protected class HwBinderService extends Stub {
        protected HwBinderService() {
        }

        public void setAlarmsPending(List<String> pkgList, List<String> actionList, boolean pending, int type) {
            if (1000 != Binder.getCallingUid()) {
                Slog.e(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsPending, permission not allowed. uid = " + Binder.getCallingUid());
            } else if (pkgList == null || pkgList.size() <= 0) {
                Slog.i(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsPending, pkgList=" + pkgList);
            } else {
                if (HwAlarmManagerService.DEBUG_SHB) {
                    Slog.i(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsPending, pending=" + pending + ", type=" + type);
                }
                HwAlarmManagerService.this.mSmartHB.setAlarmsPending(pkgList, actionList, pending, type);
            }
        }

        public void removeAllPendingAlarms() {
            if (1000 != Binder.getCallingUid()) {
                Slog.e(HwAlarmManagerService.TAG, "SmartHeartBeat:removeAllPendingAlarms, permission not allowed. uid = " + Binder.getCallingUid());
                return;
            }
            if (HwAlarmManagerService.DEBUG_SHB) {
                Slog.i(HwAlarmManagerService.TAG, "SmartHeartBeat:remove all pending alarms");
            }
            HwAlarmManagerService.this.mSmartHB.removeAllPendingAlarms();
        }

        public void setAlarmsAdjust(List<String> pkgList, List<String> actionList, boolean adjust, int type, long interval, int mode) {
            if (1000 != Binder.getCallingUid()) {
                Slog.e(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsAdjust, uid: " + Binder.getCallingUid());
                return;
            }
            if (HwAlarmManagerService.DEBUG_SHB) {
                Slog.i(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsAdjust " + adjust);
            }
            HwAlarmManagerService.this.mSmartHB.setAlarmsAdjust(pkgList, actionList, adjust, type, interval, mode);
        }

        public void removeAllAdjustAlarms() {
            if (1000 != Binder.getCallingUid()) {
                Slog.e(HwAlarmManagerService.TAG, "SmartHeartBeat:removeAllAdjustAlarms, uid: " + Binder.getCallingUid());
                return;
            }
            if (HwAlarmManagerService.DEBUG_SHB) {
                Slog.i(HwAlarmManagerService.TAG, "SmartHeartBeat:remove all adjust alarms.");
            }
            HwAlarmManagerService.this.mSmartHB.removeAllAdjustAlarms();
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (HwAlarmManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump HwAlarmManagerService from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else {
                HwAlarmManagerService.this.mSmartHB.dump(pw);
            }
        }
    }

    public HwAlarmManagerService(Context context) {
        super(context);
        this.mSmartHB = SmartHeartBeat.getInstance(context, this);
        DEBUG_SHB = SmartHeartBeat.DEBUG_HEART_BEAT;
        this.mContext = context;
    }

    public void onStart() {
        super.onStart();
        publishBinderService("hwAlarmService", new HwBinderService());
    }

    public void onBootPhase(int phase) {
        super.onBootPhase(phase);
        if (phase == 1000 && IS_POWEROFF_ALARM_ENABLED && (this.hasDeskClocksetAlarm ^ 1) != 0 && ActivityManager.getCurrentUser() == 0) {
            int alarm_id = -1;
            long alarm_when = 0;
            String[] s = SystemProperties.get("persist.sys.shut_alarm", "none").split(" ");
            if (s.length == 2) {
                try {
                    alarm_id = Integer.parseInt(s[0]);
                    alarm_when = Long.parseLong(s[1]);
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "NumberFormatException : " + e);
                }
            }
            Slog.d(TAG, "boot completed, alarmId:" + alarm_id + " alarm_when:" + alarm_when);
            if (0 != alarm_when) {
                long now = System.currentTimeMillis();
                Intent intent = new Intent(ACTION_ALARM_WAKEUP);
                intent.putExtra(ALARM_ID, alarm_id);
                Slog.d(TAG, "putExtra alarm_when " + alarm_when);
                intent.putExtra(ALARM_WHEN, alarm_when);
                intent.putExtra(IS_OWNER_ALARM, true);
                if (alarm_when < now) {
                    intent.putExtra(IS_OUT_OF_DATA_ALARM, true);
                    Slog.d(TAG, "put is_out_of_data_alarm true");
                }
                synchronized (this.mLock) {
                    this.mPendingAlarm = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
                    setImpl(0, alarm_when, 0, 0, this.mPendingAlarm, null, null, 9, null, null, 1000, "android");
                }
            }
        }
    }

    protected void adjustAlarmLocked(Alarm a) {
        this.mSmartHB.adjustAlarmIfNeeded(a);
    }

    protected void rebatchPkgAlarmsLocked(List<String> pkgList) {
        if (pkgList != null && !pkgList.isEmpty()) {
            int i;
            if (DEBUG_SHB) {
                Slog.i(TAG, "SmartHeartBeat:rebatchPkgAlarmsLocked, pkgList: " + pkgList);
            }
            Alarm oldPendingIdleUntil = this.mPendingIdleUntil;
            ArrayList<Batch> batches = new ArrayList();
            for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = (Batch) this.mAlarmBatches.get(i);
                int alarmSize = b.alarms.size();
                for (int j = 0; j < alarmSize; j++) {
                    if (pkgList.contains(((Alarm) b.alarms.get(j)).packageName)) {
                        batches.add(b);
                        this.mAlarmBatches.remove(i);
                        break;
                    }
                }
            }
            long nowElapsed = SystemClock.elapsedRealtime();
            for (i = this.mPendingNonWakeupAlarms.size() - 1; i >= 0; i--) {
                Alarm a = (Alarm) this.mPendingNonWakeupAlarms.get(i);
                if (pkgList.contains(a.packageName) && this.mSmartHB.shouldPendingAlarm(a)) {
                    this.mPendingNonWakeupAlarms.remove(i);
                    if (DEBUG_SHB) {
                        Slog.i(TAG, "readd PendingNonWakeupAlarms of " + a.packageName + " " + a);
                    }
                    reAddAlarmLocked(a, nowElapsed, true);
                }
            }
            if (batches.size() != 0) {
                this.mCancelRemoveAction = true;
                int M = batches.size();
                for (int batchNum = 0; batchNum < M; batchNum++) {
                    Batch batch = (Batch) batches.get(batchNum);
                    int N = batch.size();
                    for (i = 0; i < N; i++) {
                        reAddAlarmLocked(batch.get(i), nowElapsed, true);
                    }
                }
                this.mCancelRemoveAction = false;
                if (!(oldPendingIdleUntil == null || oldPendingIdleUntil == this.mPendingIdleUntil)) {
                    Slog.wtf(TAG, "pkg Rebatching: idle until changed from " + oldPendingIdleUntil + " to " + this.mPendingIdleUntil);
                    if (this.mPendingIdleUntil == null) {
                        restorePendingWhileIdleAlarmsLocked();
                    }
                }
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
                if (DEBUG_SHB) {
                    Slog.i(TAG, "SmartHeartBeat:rebatchPkgAlarmsLocked end");
                }
            }
        }
    }

    public Object getHwAlarmHandler() {
        return new HwAlarmHandler();
    }

    public void postTrimAlarm(HashSet<String> pkgList) {
        if (pkgList != null && pkgList.size() != 0) {
        }
    }

    private void removeByPkg_hwHsm(HashSet<String> sPkgs) {
        if (sPkgs != null) {
            try {
                if (sPkgs.size() != 0) {
                }
            } catch (Exception e) {
                Slog.e(TAG, "AlarmManagerService removeByPkg_hwHsm", e);
            }
        }
    }

    private void decideRtcPrioritySet(Alarm decidingAlarm) {
        if (decidingAlarm != null && decidingAlarm.operation != null) {
            String mayInvolvedPackageName;
            String targetPackageName = decidingAlarm.operation.getTargetPackage();
            if (targetPackageName.equals("android")) {
                targetPackageName = DESKCLOCK_PACKAGENAME;
            }
            Alarm alarm = null;
            Alarm decideResultAlarm = null;
            if (DESKCLOCK_PACKAGENAME.equals(targetPackageName)) {
                mayInvolvedPackageName = SETTINGS_PACKAGENAME;
            } else if (SETTINGS_PACKAGENAME.equals(targetPackageName)) {
                mayInvolvedPackageName = DESKCLOCK_PACKAGENAME;
            } else {
                Slog.w(TAG, "decideRtcPrioritySet--packagename error, decideRtcPrioritySet 3, return directly");
                return;
            }
            if (mHwWakeupBoot.containsKey(targetPackageName)) {
                mHwWakeupBoot.remove(targetPackageName);
            }
            Slog.d(TAG, "decideRtcPrioritySet--put " + targetPackageName + ",to map");
            mHwWakeupBoot.put(targetPackageName, decidingAlarm);
            if (mayInvolvedPackageName != null && mHwWakeupBoot.containsKey(mayInvolvedPackageName)) {
                alarm = (Alarm) mHwWakeupBoot.get(mayInvolvedPackageName);
            }
            if (alarm == null) {
                decideResultAlarm = decidingAlarm;
            } else if (decidingAlarm.when > alarm.when) {
                decideResultAlarm = alarm;
            } else if (decidingAlarm.when < alarm.when) {
                decideResultAlarm = decidingAlarm;
            } else if (DESKCLOCK_PACKAGENAME.equals(decidingAlarm.operation.getTargetPackage())) {
                decideResultAlarm = decidingAlarm;
            } else if (DESKCLOCK_PACKAGENAME.equals(alarm.operation.getTargetPackage())) {
                decideResultAlarm = alarm;
            }
            if (decideResultAlarm != null) {
                Slog.d(TAG, "have calculate RTC result and will set to lock");
                hwSetRtcLocked(decideResultAlarm);
            }
        }
    }

    private boolean decideRtcPriorityEnable(Alarm a) {
        if (a == null || a.operation == null) {
            return false;
        }
        boolean ret = false;
        boolean deskClockEnable = false;
        boolean settingProviderEnable = false;
        if (a.type == 0) {
            if (IS_POWEROFF_ALARM_ENABLED && a.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, true)) {
                deskClockEnable = true;
            }
            if (a.operation.getTargetPackage().equals(SETTINGS_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, SAVE_TO_REGISTER, false)) {
                settingProviderEnable = true;
            }
        } else {
            ret = false;
        }
        if (settingProviderEnable || deskClockEnable) {
            ret = true;
        }
        return ret;
    }

    private void decideRtcPriorityRemove(PendingIntent operation) {
        if (operation != null) {
            String mayInvolvedPackageName;
            String targetPackageName = operation.getTargetPackage();
            if (targetPackageName.equals("android")) {
                targetPackageName = DESKCLOCK_PACKAGENAME;
            }
            if (DESKCLOCK_PACKAGENAME.equals(targetPackageName)) {
                mayInvolvedPackageName = SETTINGS_PACKAGENAME;
            } else if (SETTINGS_PACKAGENAME.equals(targetPackageName)) {
                mayInvolvedPackageName = DESKCLOCK_PACKAGENAME;
            } else {
                Slog.w(TAG, "packagename error, decideRtcPriorityRemove, return directly");
                return;
            }
            if (mHwWakeupBoot.isEmpty()) {
                Slog.w(TAG, "error, mHwWakeupBoot is empty, return directly");
                hwRemoveRtcLocked();
                return;
            }
            if (mHwWakeupBoot.containsKey(targetPackageName)) {
                Slog.w(TAG, "decideRtcPriorityRemove--remove " + targetPackageName);
                mHwWakeupBoot.remove(targetPackageName);
            }
            if (mayInvolvedPackageName == null || !mHwWakeupBoot.containsKey(mayInvolvedPackageName)) {
                mHwWakeupBoot.clear();
                Slog.d(TAG, "mHwWakeupBoot have clear");
                hwRemoveRtcLocked();
            } else {
                Alarm decideResultAlarm = (Alarm) mHwWakeupBoot.get(mayInvolvedPackageName);
                if (decideResultAlarm != null) {
                    Slog.d(TAG, "decideRtcPriorityRemove ,hwSetRtcLocked");
                    hwSetRtcLocked(decideResultAlarm);
                }
            }
        }
    }

    private boolean isDeskClock(Alarm a) {
        if (a == null || a.operation == null || !IS_POWEROFF_ALARM_ENABLED) {
            return false;
        }
        if (a.type == 0 && IS_POWEROFF_ALARM_ENABLED && a.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false)) {
            return true;
        }
        return a.type == 0 && IS_POWEROFF_ALARM_ENABLED && a.operation.getTargetPackage().equals("android") && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false);
    }

    private void saveDeskClock(Alarm a) {
        if (a != null && a.operation != null && a.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false)) {
            this.hasDeskClocksetAlarm = true;
            synchronized (this.mLock) {
                if (this.mPendingAlarm != null) {
                    Slog.d(TAG, "set deskclock after boot, remove Shutdown alarm from framework");
                    removeLocked(this.mPendingAlarm, null);
                    this.mPendingAlarm = null;
                }
            }
            long alarmWhen = a.when;
            int alarmId = resetIntExtraCallingIdentity(a.operation, ALARM_ID, -1);
            if (alarmId != -1) {
                Slog.d(TAG, "set shutdownAlarm " + alarmId + " " + alarmWhen);
                SystemProperties.set("persist.sys.shut_alarm", alarmId + " " + alarmWhen);
            }
        }
    }

    private void removeDeskClock(Alarm a) {
        if (a != null && a.operation != null && IS_POWEROFF_ALARM_ENABLED) {
            if (a.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false)) {
                Slog.d(TAG, "remove shutdownAlarm : none");
                SystemProperties.set("persist.sys.shut_alarm", "none");
            }
            if (a.operation.getTargetPackage().equals("android") && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false)) {
                Slog.d(TAG, "remove shutdownAlarm : none");
                SystemProperties.set("persist.sys.shut_alarm", "none");
            }
        }
    }

    protected void removeDeskClockFromFWK(PendingIntent operation) {
        if (operation != null && resetStrExtraCallingIdentity(operation, REMOVE_POWEROFF_ALARM_ANYWAY, false) && ActivityManager.getCurrentUser() == 0) {
            synchronized (this.mLock) {
                Slog.d(TAG, "no alarm enable, so remove Shutdown Alarm from framework");
                if (this.mPendingAlarm != null) {
                    removeLocked(this.mPendingAlarm, null);
                    this.mPendingAlarm = null;
                }
            }
            Slog.d(TAG, "remove shutdownAlarm : none");
            SystemProperties.set("persist.sys.shut_alarm", "none");
        }
    }

    private void hwSetRtcLocked(Alarm setAlarm) {
        if (setAlarm != null && setAlarm.operation != null) {
            long alarmWhen = setAlarm.when;
            String targetPackageName = setAlarm.operation.getTargetPackage();
            if (System.currentTimeMillis() > alarmWhen) {
                Slog.d(TAG, "hwSetRtcLocked--missed alarm, not set RTC to driver");
                return;
            }
            long alarmSeconds;
            long alarmNanoseconds;
            if (alarmWhen < 0) {
                alarmSeconds = 0;
                alarmNanoseconds = 0;
            } else {
                alarmSeconds = alarmWhen / 1000;
                alarmNanoseconds = ((alarmWhen % 1000) * 1000) * 1000;
            }
            Slog.d(TAG, "set RTC alarm Locked, time = " + getTimeString(alarmWhen));
            timeInRegister = getTimeString(alarmWhen);
            mPropHwRegisterName = targetPackageName;
            hwSetClockRTC(this.mNativeData, alarmSeconds, alarmNanoseconds);
        }
    }

    private void hwRemoveRtcLocked() {
        Slog.d(TAG, "remove RTC alarm time Locked");
        timeInRegister = "None";
        hwSetClockRTC(this.mNativeData, 0, 0);
    }

    private String getTimeString(long milliSec) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(milliSec)).toString();
    }

    protected void printHwWakeupBoot(PrintWriter pw) {
        if (!(this.mHwAlarmLock || mHwWakeupBoot == null || mHwWakeupBoot.size() <= 0)) {
            pw.println();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pw.print("HW WakeupBoot MAP: ");
            pw.println();
            pw.print("Register time:");
            pw.print(timeInRegister);
            pw.println();
            if (mHwWakeupBoot.size() > 0) {
                for (Entry entry : mHwWakeupBoot.entrySet()) {
                    String key = (String) entry.getKey();
                    Alarm PrintAlarms = (Alarm) entry.getValue();
                    pw.print("packageName=");
                    pw.print(key);
                    pw.print("  time=");
                    pw.print(sdf.format(new Date(PrintAlarms.when)));
                    pw.println();
                }
            }
        }
    }

    private boolean isMDMAlarm(Alarm alarm) {
        if (alarm == null || alarm.operation == null) {
            return false;
        }
        if ("android".equals(alarm.operation.getTargetPackage()) && resetStrExtraCallingIdentity(alarm.operation, KEY_BOOT_MDM, false)) {
            return true;
        }
        return false;
    }

    protected void hwRemoveRtcAlarm(Alarm alarm, boolean cancel) {
        if (alarm != null) {
            if (isMDMAlarm(alarm)) {
                this.isSetHwMDMAlarm = false;
                this.mHwMDMAlarm = null;
                Slog.i(TAG, "hwRemoveRtcAlarm MDM");
            }
            reportAlarmForAware(alarm, cancel ? 1 : 2);
            if (isDeskClock(alarm)) {
                removeDeskClock(alarm);
            }
            if (!this.mHwAlarmLock) {
                if (decideRtcPriorityEnable(alarm) || isDeskClock(alarm)) {
                    decideRtcPriorityRemove(alarm.operation);
                }
            }
        }
    }

    protected void hwSetRtcAlarm(Alarm alarm) {
        if (alarm != null) {
            if (alarm.listenerTag != null) {
                Slog.i(TAG, "hwSetAlarm listenerTag: " + alarm.listenerTag);
            }
            if (isMDMAlarm(alarm)) {
                this.isSetHwMDMAlarm = true;
                this.mHwMDMAlarm = alarm;
                Slog.i(TAG, "hwSetRtcAlarm MDM");
            }
            reportAlarmForAware(alarm, 0);
            if (isDeskClock(alarm)) {
                saveDeskClock(alarm);
            }
            if (!this.mHwAlarmLock) {
                if (decideRtcPriorityEnable(alarm) || isDeskClock(alarm)) {
                    decideRtcPrioritySet(alarm);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void hwRemoveAnywayRtcAlarm(PendingIntent operation) {
        if (operation != null && !this.mHwAlarmLock && IS_POWEROFF_ALARM_ENABLED && operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(operation, REMOVE_POWEROFF_ALARM_ANYWAY, false) && ActivityManager.getCurrentUser() == 0) {
            Slog.d(TAG, "DeskClock not receive boot broadcast,remove alarm anyway");
            decideRtcPriorityRemove(operation);
            Slog.d(TAG, "remove shutdownAlarm : none");
            SystemProperties.set("persist.sys.shut_alarm", "none");
        }
    }

    protected void hwAddFirstFlagForRtcAlarm(Alarm alarm, Intent backgroundIntent) {
        if (alarm != null && alarm.operation != null) {
            if (decideRtcPriorityEnable(alarm) && alarm.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME)) {
                if (this.mIsFirstPowerOffAlarm && "RTC".equals(SystemProperties.get("persist.sys.powerup_reason", SettingsMDMPlugin.CONFIG_NORMAL_VALUE))) {
                    backgroundIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", true);
                    this.mIsFirstPowerOffAlarm = false;
                } else {
                    backgroundIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", false);
                }
            } else if (isDeskClock(alarm)) {
                if (this.mIsFirstPowerOffAlarm && "RTC".equals(SystemProperties.get("persist.sys.powerup_reason", SettingsMDMPlugin.CONFIG_NORMAL_VALUE))) {
                    Slog.d(TAG, "FLAG_IS_FIRST_POWER_OFF_ALARM : true");
                    backgroundIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", true);
                    this.mIsFirstPowerOffAlarm = false;
                } else {
                    Slog.d(TAG, "FLAG_IS_FIRST_POWER_OFF_ALARM : false");
                    backgroundIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", false);
                }
            }
        }
    }

    protected long checkHasHwRTCAlarmLock(String packageName) {
        Alarm rtcAlarm = null;
        long time = -1;
        synchronized (this.mLock) {
            if (packageName != null) {
                if (mHwWakeupBoot.containsKey(packageName)) {
                    rtcAlarm = (Alarm) mHwWakeupBoot.get(packageName);
                }
            }
            if (rtcAlarm != null && (decideRtcPriorityEnable(rtcAlarm) || isDeskClock(rtcAlarm))) {
                time = rtcAlarm.when;
            }
        }
        return time;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void adjustHwRTCAlarmLock(boolean deskClockTime, boolean bootOnTime, int typeState) {
        Slog.d(TAG, "adjust RTC Alarm");
        synchronized (this.mLock) {
            this.mHwAlarmLock = true;
            switch (typeState) {
                case 1:
                    mHwWakeupBoot.clear();
                    Slog.d(TAG, "NONE_LISTVIEW--user cancel bootOnTime and deskClockTime RTC alarm  ");
                    hwRemoveRtcLocked();
                    break;
                case 2:
                case 3:
                case 6:
                case 7:
                    if (!deskClockTime || !bootOnTime) {
                        if (deskClockTime || bootOnTime) {
                            Alarm optAlarm;
                            if (!deskClockTime || bootOnTime) {
                                if (!deskClockTime && bootOnTime) {
                                    optAlarm = (Alarm) mHwWakeupBoot.get(DESKCLOCK_PACKAGENAME);
                                    Slog.d(TAG, "user cancel deskClockTime RTC alarm");
                                    hwRemoveRtcAlarmWhenShut(optAlarm);
                                    break;
                                }
                            }
                            optAlarm = (Alarm) mHwWakeupBoot.get(SETTINGS_PACKAGENAME);
                            Slog.d(TAG, "user cancel bootOnTime RTC alarm");
                            hwRemoveRtcAlarmWhenShut(optAlarm);
                            break;
                        }
                        mHwWakeupBoot.clear();
                        Slog.d(TAG, "user cancel bootOnTime and deskClockTime RTC alarm  ");
                        hwRemoveRtcLocked();
                        break;
                    }
                    setHwAirPlaneStatePropLock();
                    return;
                    break;
                case 4:
                    if (!deskClockTime) {
                        mHwWakeupBoot.clear();
                        Slog.d(TAG, "ONE_DESKCLOCK_LISTVIEW ---user cancel deskClockTime RTC alarm  ");
                        hwRemoveRtcLocked();
                        break;
                    }
                    break;
                case 5:
                    if (!bootOnTime) {
                        mHwWakeupBoot.clear();
                        Slog.d(TAG, "ONE_BOOT_LISTVIEW ---user cancel bootOnTime RTC alarm  ");
                        hwRemoveRtcLocked();
                        break;
                    }
                    break;
            }
        }
    }

    private void hwRemoveRtcAlarmWhenShut(Alarm alarm) {
        if (alarm != null) {
            if (decideRtcPriorityEnable(alarm) || isDeskClock(alarm)) {
                Slog.d(TAG, "remove RTC alarm in shutdown view");
                decideRtcPriorityRemove(alarm.operation);
                setHwAirPlaneStatePropLock();
            }
        }
    }

    protected void setHwAirPlaneStatePropLock() {
        SystemProperties.set(HWAIRPLANESTATE_PROPERTY, mPropHwRegisterName);
        Slog.d(TAG, "hw airplane prop locked = " + mPropHwRegisterName);
    }

    protected void setHwRTCAlarmLock() {
        if (this.isSetHwMDMAlarm) {
            hwSetRtcLocked(this.mHwMDMAlarm);
            return;
        }
        long deskTime = checkHasHwRTCAlarmLock(DESKCLOCK_PACKAGENAME);
        long settingsTime = checkHasHwRTCAlarmLock(SETTINGS_PACKAGENAME);
        if (deskTime == -1 && settingsTime == -1) {
            Slog.d(TAG, "setHwRTCAlarmLock-- not set RTC to driver");
            return;
        }
        Alarm rtcAlarm;
        if (deskTime == -1) {
            rtcAlarm = (Alarm) mHwWakeupBoot.get(SETTINGS_PACKAGENAME);
        } else if (settingsTime == -1) {
            rtcAlarm = (Alarm) mHwWakeupBoot.get(DESKCLOCK_PACKAGENAME);
        } else {
            Alarm optAlarm1 = (Alarm) mHwWakeupBoot.get(DESKCLOCK_PACKAGENAME);
            Alarm optAlarm2 = (Alarm) mHwWakeupBoot.get(SETTINGS_PACKAGENAME);
            if (optAlarm1.when <= optAlarm2.when) {
                rtcAlarm = optAlarm1;
            } else {
                rtcAlarm = optAlarm2;
            }
        }
        hwSetRtcLocked(rtcAlarm);
    }

    protected void hwRecordFirstTime() {
        this.mFirstRTC = System.currentTimeMillis();
        this.mFirstELAPSED = SystemClock.elapsedRealtime();
        if (0 == Global.getLong(getContext().getContentResolver(), LAST_TIME_CHANGED_RTC, 0)) {
            Flog.i(500, "hwRecordFirstTime init for TimeKeeper at " + this.mFirstRTC);
            Global.putLong(getContext().getContentResolver(), LAST_TIME_CHANGED_RTC, this.mFirstRTC);
        }
    }

    protected void hwRecordTimeChangeRTC(long nowRTC, long nowELAPSED, long lastTimeChangeClockTime, long expectedClockTime) {
        long expectedRTC;
        long maxOffset;
        if (lastTimeChangeClockTime == 0) {
            expectedRTC = this.mFirstRTC + (nowELAPSED - this.mFirstELAPSED);
            maxOffset = 30000;
        } else {
            expectedRTC = expectedClockTime;
            maxOffset = 5000;
        }
        long offset = nowRTC - expectedRTC;
        if (offset < (-maxOffset) || nowRTC > maxOffset) {
            Flog.i(500, "hwRecordTimeChangeRTC for TimeKeeper at " + nowRTC + ", offset=" + offset);
            Global.putLong(getContext().getContentResolver(), LAST_TIME_CHANGED_RTC, nowRTC);
        }
    }

    private boolean resetStrExtraCallingIdentity(PendingIntent operation, String extra, boolean value) {
        long identity = Binder.clearCallingIdentity();
        boolean extraRes = false;
        try {
            extraRes = operation.getIntent().getBooleanExtra(extra, value);
            Binder.restoreCallingIdentity(identity);
            return extraRes;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            return extraRes;
        }
    }

    private int resetIntExtraCallingIdentity(PendingIntent operation, String extra, int value) {
        long identity = Binder.clearCallingIdentity();
        int extraRes = -1;
        try {
            extraRes = operation.getIntent().getIntExtra(extra, value);
            Binder.restoreCallingIdentity(identity);
            return extraRes;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            return extraRes;
        }
    }

    protected boolean isContainsAppUidInWorksource(WorkSource workSource, String packageName) {
        if (workSource == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            if (containsUidInWorksource(workSource, getContext().getPackageManager().getApplicationInfo(packageName, 1).uid)) {
                Slog.i(TAG, "isContainsAppUidInWorksource-->worksource contains app's.uid");
                return true;
            }
        } catch (NameNotFoundException e) {
            Slog.w(TAG, "isContainsAppUidInWorksource-->happend NameNotFoundException");
        } catch (Exception e2) {
            Slog.w(TAG, "isContainsAppUidInWorksource-->happend Exception");
        }
        return false;
    }

    private boolean containsUidInWorksource(WorkSource workSource, int uid) {
        if (workSource == null || workSource.size() <= 0) {
            return false;
        }
        int length = workSource.size();
        for (int i = 0; i < length; i++) {
            if (uid == workSource.get(i)) {
                return true;
            }
        }
        return false;
    }

    public void removePackageAlarm(final String pkg, final List<String> tags, final int targetUid) {
        if (this.mHandler != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    synchronized (HwAlarmManagerService.this.mLock) {
                        HwAlarmManagerService.this.removeByTagLocked(pkg, tags, targetUid);
                    }
                }
            });
        }
    }

    private boolean removeSingleTagLocked(String packageName, String tag, int targetUid) {
        int i;
        boolean didRemove = false;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            didRemove |= removeFromBatch(b, packageName, tag, targetUid);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            Alarm a = (Alarm) this.mPendingWhileIdleAlarms.get(i);
            if (a.matches(packageName) && targetUid == a.creatorUid) {
                this.mPendingWhileIdleAlarms.remove(i);
            }
        }
        return didRemove;
    }

    private void removeByTagLocked(String packageName, List<String> tags, int targetUid) {
        boolean didRemove = false;
        if (packageName != null) {
            if (tags == null) {
                didRemove = removeSingleTagLocked(packageName, null, targetUid);
            } else {
                for (String tag : tags) {
                    didRemove |= removeSingleTagLocked(packageName, tag, targetUid);
                }
            }
            if (didRemove) {
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    private boolean canRemove(Alarm alarm, String tag) {
        if (tag == null) {
            return true;
        }
        String alarmTag = Alarm.makeTag(alarm.operation, alarm.listenerTag, alarm.type);
        if (alarmTag == null) {
            return false;
        }
        String[] splits = alarmTag.split(":");
        return splits.length > 1 && splits[1].equals(tag);
    }

    boolean removeFromBatch(Batch batch, String packageName, String tag, int targetUid) {
        if (packageName == null) {
            return false;
        }
        boolean didRemove = false;
        long newStart = 0;
        long newEnd = Long.MAX_VALUE;
        int newFlags = 0;
        for (int i = batch.alarms.size() - 1; i >= 0; i--) {
            Alarm alarm = (Alarm) batch.alarms.get(i);
            if (!alarm.matches(packageName) || targetUid != alarm.creatorUid) {
                if (alarm.whenElapsed > newStart) {
                    newStart = alarm.whenElapsed;
                }
                if (alarm.maxWhenElapsed < newEnd) {
                    newEnd = alarm.maxWhenElapsed;
                }
                newFlags |= alarm.flags;
            } else if (canRemove(alarm, tag)) {
                batch.alarms.remove(i);
                didRemove = true;
                if (tag != null) {
                    Slog.d(TAG, "remove package alarm " + packageName + "' (" + tag + ")" + " ,targetUid: " + targetUid);
                }
                hwRemoveRtcAlarm(alarm, true);
                if (alarm.alarmClock != null) {
                    this.mNextAlarmClockMayChange = true;
                }
            }
        }
        if (didRemove) {
            batch.start = newStart;
            batch.end = newEnd;
            batch.flags = newFlags;
        }
        return didRemove;
    }

    private void reportAlarmForAware(Alarm alarm, int operation) {
        if (alarm != null && alarm.packageName != null && alarm.statsTag != null) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString(MemoryConstant.MEM_PREREAD_ITEM_NAME, alarm.packageName);
                bundleArgs.putString("statstag", alarm.statsTag);
                bundleArgs.putInt("relationType", 22);
                bundleArgs.putInt("alarm_operation", operation);
                bundleArgs.putInt("tgtUid", alarm.creatorUid);
                CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    protected void modifyAlarmIfOverload(Alarm alarm) {
        AwareWakeUpManager.getInstance().modifyAlarmIfOverload(alarm);
    }

    protected void reportWakeupAlarms(ArrayList<Alarm> alarms) {
        AwareWakeUpManager.getInstance().reportWakeupAlarms(alarms);
    }

    protected boolean isAwareAlarmManagerEnabled() {
        return AlarmManagerFeature.isEnable();
    }
}
