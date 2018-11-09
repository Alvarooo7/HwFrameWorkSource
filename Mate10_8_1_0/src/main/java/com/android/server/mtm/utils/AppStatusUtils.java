package com.android.server.mtm.utils;

import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.-$Lambda$GchmWP7P_7Q7emixfFyZdxYlvNg.AnonymousClass1;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppLruBase;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMServiceImpl;
import huawei.com.android.server.fingerprint.FingerViewController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

public final class AppStatusUtils {
    private static final String TAG = "AppStatusUtils";
    private static volatile AppStatusUtils instance;
    private final String ADJTYPE_SERVICE = AwareAppMngSort.ADJTYPE_SERVICE;
    private final long EXPIRE_TIME_NANOS = 10000000;
    private final String FG_SERVICE = AwareAppMngSort.FG_SERVICE;
    private final String RECENT_TASK_ADJ_TYPE = "pers-top-activity";
    private final String SYSTEMUI_PACKAGE_NAME = FingerViewController.PKGNAME_OF_KEYGUARD;
    private final String TOP_ACTIVITY_ADJ_TYPE = "top-activity";
    private ArrayMap<Integer, AwareProcessInfo> mAllProcNeedSort = null;
    private ArrayMap<Integer, ProcessInfo> mAudioIn = null;
    private ArrayMap<Integer, ProcessInfo> mAudioOut = null;
    private Set<String> mBlindPkg = new ArraySet();
    private int mCurrentUserId = 0;
    private ArraySet<Integer> mForeGroundAssocPid = null;
    private ArrayMap<Integer, ArrayList<String>> mForeGroundUid = null;
    private List<String> mGcmAppList = null;
    private final HwActivityManagerService mHwAMS = HwActivityManagerService.self();
    private final AwareAppKeyBackgroup mKeyBackgroupInstance = AwareAppKeyBackgroup.getInstance();
    private Set<Integer> mKeyPercepServicePid = null;
    private AwareAppLruBase mPrevAmsBase = null;
    private AwareAppLruBase mPrevAwareBase = null;
    private Set<String> mRestrainedVisWinList = null;
    private ProcessInfo mSystemuiProcInfo = null;
    private volatile long mUpdateTime = -1;
    private Set<String> mVisibleWindowList = null;
    private Set<String> mWidgetList = null;
    private volatile Map<Status, Predicate<AwareProcessInfo>> predicates = new HashMap();

    public enum Status {
        NOT_IMPORTANT("not_important"),
        PERSIST("Persist"),
        TOP_ACTIVITY("TopActivity"),
        VISIBLE_APP("VisApp"),
        FOREGROUND("Fground"),
        VISIBLEWIN("VisWin"),
        WIDGET("Widget"),
        ASSOC_WITH_FG("FgAssoc"),
        KEYBACKGROUND("KeyBground"),
        BACKUP_APP("BackApp"),
        HEAVY_WEIGHT_APP("HeaWeiApp"),
        PREV_ONECLEAN("RecTask"),
        MUSIC_PLAY("Music"),
        SOUND_RECORD("Record"),
        GUIDE("Guide"),
        DOWN_UP_LOAD("Download"),
        KEY_SYS_SERVICE("KeySys"),
        HEALTH("Health"),
        PREVIOUS("LastUse"),
        SYSTEM_APP("SysApp"),
        NON_CURRENT_USER("non_cur_user"),
        LAUNCHER("Launcher"),
        INPUT_METHOD("InputMethed"),
        WALLPAPER("WallPaper"),
        FROZEN("Frozen"),
        BLUETOOTH("Btooth"),
        TOAST_WINDOW("ToastWin"),
        FOREGROUND_APP("FgApp"),
        BLIND("Blind"),
        MUSIC_INSTANT("MusicINS"),
        NONSYSTEMUSER("NonSystemUser"),
        GCM("gcm"),
        INSMALLSAMPLELIST("InSmallSampleList"),
        ACHSCRCHANGEDNUM("AchScrChangedNum"),
        SCREENRECORD("ScreenRecord"),
        CAMERARECORD("CameraRecord"),
        RESTRAINED_VIS_WIN("RestrainedVisWin"),
        OVERSEA_APP("OverseaApp"),
        DOWNLOAD_FREEZE("DownloadFreeze"),
        PROC_STATE_CACHED("PSCached");
        
        private String mDescription;

        private Status(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private AppStatusUtils() {
        this.predicates.put(Status.PERSIST, -$Lambda$GchmWP7P_7Q7emixfFyZdxYlvNg.$INST$0);
        this.predicates.put(Status.TOP_ACTIVITY, -$Lambda$GchmWP7P_7Q7emixfFyZdxYlvNg.$INST$1);
        this.predicates.put(Status.VISIBLE_APP, -$Lambda$GchmWP7P_7Q7emixfFyZdxYlvNg.$INST$2);
        this.predicates.put(Status.FOREGROUND, -$Lambda$GchmWP7P_7Q7emixfFyZdxYlvNg.$INST$3);
        this.predicates.put(Status.VISIBLEWIN, new AnonymousClass1((byte) 28, this));
        this.predicates.put(Status.WIDGET, new AnonymousClass1((byte) 29, this));
        this.predicates.put(Status.ASSOC_WITH_FG, new AnonymousClass1((byte) 30, this));
        this.predicates.put(Status.KEYBACKGROUND, new AnonymousClass1((byte) 31, this));
        this.predicates.put(Status.BACKUP_APP, -$Lambda$GchmWP7P_7Q7emixfFyZdxYlvNg.$INST$5);
        this.predicates.put(Status.HEAVY_WEIGHT_APP, -$Lambda$GchmWP7P_7Q7emixfFyZdxYlvNg.$INST$6);
        this.predicates.put(Status.PREV_ONECLEAN, new AnonymousClass1((byte) 0, this));
        this.predicates.put(Status.MUSIC_PLAY, new AnonymousClass1((byte) 1, this));
        this.predicates.put(Status.SOUND_RECORD, new AnonymousClass1((byte) 2, this));
        this.predicates.put(Status.GUIDE, new AnonymousClass1((byte) 3, this));
        this.predicates.put(Status.DOWN_UP_LOAD, new AnonymousClass1((byte) 4, this));
        this.predicates.put(Status.KEY_SYS_SERVICE, new AnonymousClass1((byte) 5, this));
        this.predicates.put(Status.HEALTH, new AnonymousClass1((byte) 6, this));
        this.predicates.put(Status.PREVIOUS, new AnonymousClass1((byte) 7, this));
        this.predicates.put(Status.SYSTEM_APP, new AnonymousClass1((byte) 8, this));
        this.predicates.put(Status.NON_CURRENT_USER, new AnonymousClass1((byte) 9, this));
        this.predicates.put(Status.LAUNCHER, new AnonymousClass1((byte) 10, this));
        this.predicates.put(Status.INPUT_METHOD, new AnonymousClass1(BluetoothMessage.NOTIFY_VAL_OFFS, this));
        this.predicates.put(Status.WALLPAPER, new AnonymousClass1((byte) 12, this));
        this.predicates.put(Status.FROZEN, new AnonymousClass1((byte) 13, this));
        this.predicates.put(Status.BLUETOOTH, new AnonymousClass1((byte) 14, this));
        this.predicates.put(Status.TOAST_WINDOW, new AnonymousClass1((byte) 15, this));
        this.predicates.put(Status.FOREGROUND_APP, new AnonymousClass1((byte) 16, this));
        this.predicates.put(Status.BLIND, new AnonymousClass1((byte) 17, this));
        this.predicates.put(Status.MUSIC_INSTANT, new AnonymousClass1((byte) 18, this));
        this.predicates.put(Status.NONSYSTEMUSER, new AnonymousClass1((byte) 19, this));
        this.predicates.put(Status.GCM, new AnonymousClass1((byte) 20, this));
        this.predicates.put(Status.INSMALLSAMPLELIST, new AnonymousClass1((byte) 21, this));
        this.predicates.put(Status.ACHSCRCHANGEDNUM, new AnonymousClass1((byte) 22, this));
        this.predicates.put(Status.SCREENRECORD, new AnonymousClass1((byte) 23, this));
        this.predicates.put(Status.CAMERARECORD, new AnonymousClass1((byte) 24, this));
        this.predicates.put(Status.RESTRAINED_VIS_WIN, new AnonymousClass1((byte) 25, this));
        this.predicates.put(Status.OVERSEA_APP, new AnonymousClass1((byte) 26, this));
        this.predicates.put(Status.DOWNLOAD_FREEZE, new AnonymousClass1((byte) 27, this));
        this.predicates.put(Status.PROC_STATE_CACHED, -$Lambda$GchmWP7P_7Q7emixfFyZdxYlvNg.$INST$4);
    }

    static /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_6830(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj < 0;
    }

    static /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_6988(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj == 0;
    }

    static /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_7146(AwareProcessInfo param) {
        if (param.mProcInfo.mCurAdj < 100 || param.mProcInfo.mCurAdj >= 200) {
            return false;
        }
        return true;
    }

    static /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_7394(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj < 200;
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_7558(AwareProcessInfo param) {
        return checkVisibleWindow(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_7656(AwareProcessInfo param) {
        return checkWidget(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_7754(AwareProcessInfo param) {
        return checkAssocWithFg(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_7857(AwareProcessInfo param) {
        return checkKeyBackGround(param);
    }

    static /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_7959(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj == 300;
    }

    static /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_8108(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj == 400;
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_8260(AwareProcessInfo param) {
        return checkPrevOneClean(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_8361(AwareProcessInfo param) {
        return checkMusicPlay(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_8461(AwareProcessInfo param) {
        return checkSoundRecord(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_8556(AwareProcessInfo param) {
        return checkKeyBackgroupByState(3, param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_8698(AwareProcessInfo param) {
        return checkKeyBackgroupByState(5, param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_8849(AwareProcessInfo param) {
        return checkKeySysProc(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_8944(AwareProcessInfo param) {
        return checkKeyBackgroupByState(4, param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_9085(AwareProcessInfo param) {
        return checkPrevious(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_9182(AwareProcessInfo param) {
        return checkSystemApp(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_9286(AwareProcessInfo param) {
        return checkNonCurrentUser(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_9387(AwareProcessInfo param) {
        return checkLauncher(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_9486(AwareProcessInfo param) {
        return checkInputMethod(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_9585(AwareProcessInfo param) {
        return checkWallPaper(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_9769(AwareProcessInfo param) {
        return checkFrozen(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_10041(AwareProcessInfo param) {
        return checkBluetooth(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_10141(AwareProcessInfo param) {
        return checkToastWindow(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_10245(AwareProcessInfo param) {
        return checkForeground(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_10427(AwareProcessInfo param) {
        return checkBlind(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_10524(AwareProcessInfo param) {
        return checkMusicInstant(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_10628(AwareProcessInfo param) {
        return checkNonSystemUser(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_10723(AwareProcessInfo param) {
        return checkGcm(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_10822(AwareProcessInfo param) {
        return checkInSmallSampleList(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_10934(AwareProcessInfo param) {
        return checkScreenChangedAcheive(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_11045(AwareProcessInfo param) {
        return checkScreenRecord(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_11148(AwareProcessInfo param) {
        return checkCameraRecord(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_11257(AwareProcessInfo param) {
        return checkRestrainedVisWin(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_11363(AwareProcessInfo param) {
        return checkOverseaApp(param);
    }

    /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_11467(AwareProcessInfo param) {
        return checkDownloadFrozen(param);
    }

    static /* synthetic */ boolean lambda$-com_android_server_mtm_utils_AppStatusUtils_11577(AwareProcessInfo param) {
        return param.mProcInfo.mSetProcState > 13;
    }

    public static AppStatusUtils getInstance() {
        if (instance == null) {
            synchronized (AppStatusUtils.class) {
                if (instance == null) {
                    instance = new AppStatusUtils();
                }
            }
        }
        return instance;
    }

    public boolean checkAppStatus(Status statusType, AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            AwareLog.e(TAG, "null AwareProcessInfo input!");
            return false;
        } else if (awareProcInfo.mProcInfo == null) {
            AwareLog.e(TAG, "null ProcessInfo input!");
            return false;
        } else if (-1 == awareProcInfo.mProcInfo.mCurAdj) {
            AwareLog.e(TAG, "mCurAdj of ProcessInfo not set!");
            return false;
        } else {
            Predicate<AwareProcessInfo> func = (Predicate) this.predicates.get(statusType);
            if (func == null) {
                AwareLog.e(TAG, "error status type input!");
                return false;
            } else if (getNewProcesses()) {
                ArrayMap<Integer, AwareProcessInfo> tmpAllProcNeedSort = this.mAllProcNeedSort;
                if (tmpAllProcNeedSort == null || tmpAllProcNeedSort.isEmpty()) {
                    AwareLog.e(TAG, "mAllProcNeedSort is null");
                    return false;
                }
                AwareProcessInfo newAwareProcInfo = (AwareProcessInfo) tmpAllProcNeedSort.get(Integer.valueOf(awareProcInfo.mPid));
                if (newAwareProcInfo != null) {
                    return func.test(newAwareProcInfo);
                }
                return func.test(awareProcInfo);
            } else {
                AwareLog.e(TAG, "update processes status failed!");
                return false;
            }
        }
    }

    private boolean checkSystemApp(AwareProcessInfo awareProcInfo) {
        int uid = awareProcInfo.mProcInfo.mUid;
        int tmpCurrentUserId = this.mCurrentUserId;
        if (uid <= 10000) {
            return true;
        }
        if (uid <= tmpCurrentUserId * LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS || uid > (tmpCurrentUserId * LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS) + 10000) {
            return false;
        }
        return true;
    }

    private boolean checkNonCurrentUser(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isCurrentUser(awareProcInfo.mProcInfo.mUid, this.mCurrentUserId) ^ 1;
    }

    private boolean checkPrevOneClean(AwareProcessInfo awareProcInfo) {
        ProcessInfo tmpSystemuiProcInfo = this.mSystemuiProcInfo;
        int tmpCurrentUserId = this.mCurrentUserId;
        if (tmpSystemuiProcInfo == null) {
            return false;
        }
        if (tmpCurrentUserId == 0) {
            if (!"pers-top-activity".equals(tmpSystemuiProcInfo.mAdjType)) {
                return false;
            }
        } else if (!"top-activity".equals(tmpSystemuiProcInfo.mAdjType)) {
            return false;
        }
        AwareAppLruBase tmpAppLruBase = this.mPrevAmsBase;
        if (tmpAppLruBase == null) {
            return false;
        }
        ArrayList<String> packageNames = awareProcInfo.mProcInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty()) {
            return false;
        }
        ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = this.mAllProcNeedSort;
        if (allProcNeedSort == null || allProcNeedSort.isEmpty()) {
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return false;
        } else if (awareProcInfo.mProcInfo.mUid != tmpAppLruBase.mUid) {
            return false;
        } else {
            if (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                return true;
            }
            AwareProcessInfo prevProcInfo = (AwareProcessInfo) allProcNeedSort.get(Integer.valueOf(tmpAppLruBase.mPid));
            if (prevProcInfo == null) {
                return false;
            }
            return isPkgIncludeForTgt(packageNames, prevProcInfo.mProcInfo.mPackageName);
        }
    }

    private boolean checkAssocWithFg(AwareProcessInfo awareProcInfo) {
        ArrayMap<Integer, ArrayList<String>> tmpForeGroundUid = this.mForeGroundUid;
        ArraySet<Integer> tmpForeGroundAssocPid = this.mForeGroundAssocPid;
        if (tmpForeGroundUid == null) {
            return false;
        }
        if ((!tmpForeGroundUid.containsKey(Integer.valueOf(awareProcInfo.mProcInfo.mUid)) && (tmpForeGroundAssocPid.contains(Integer.valueOf(awareProcInfo.mProcInfo.mPid)) ^ 1) != 0) || awareProcInfo.mProcInfo.mForegroundActivities) {
            return false;
        }
        if (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
            return true;
        }
        return isPkgIncludeForTgt(awareProcInfo.mProcInfo.mPackageName, (ArrayList) tmpForeGroundUid.get(Integer.valueOf(awareProcInfo.mProcInfo.mUid)));
    }

    private boolean checkKeySysProc(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mType != 2) {
            return false;
        }
        if (procInfo.mCurAdj == 500) {
            return true;
        }
        boolean condition = (awareProcInfo.mHasShownUi || procInfo.mCreatedTime == -1) ? false : true;
        long timeCost = SystemClock.elapsedRealtime() - procInfo.mCreatedTime;
        if (procInfo.mUid < 10000) {
            if (procInfo.mCurAdj == 800) {
                return true;
            }
            return condition && timeCost < AppMngConfig.getKeySysDecay();
        } else if (condition && timeCost < AppMngConfig.getSysDecay()) {
            return true;
        }
    }

    private boolean checkVisibleWindow(AwareProcessInfo awareProcInfo) {
        Set<String> tmpVisibleWindowList = this.mVisibleWindowList;
        if (tmpVisibleWindowList == null || tmpVisibleWindowList.isEmpty()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        for (String pkg : procInfo.mPackageName) {
            if (tmpVisibleWindowList.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRestrainedVisWin(AwareProcessInfo awareProcInfo) {
        Set<String> tmpRestrainedVisWinList = this.mRestrainedVisWinList;
        if (tmpRestrainedVisWinList == null || tmpRestrainedVisWinList.isEmpty()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        for (String pkg : procInfo.mPackageName) {
            if (tmpRestrainedVisWinList.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkWidget(AwareProcessInfo awareProcInfo) {
        Set<String> tmpWidgetList = this.mWidgetList;
        if (tmpWidgetList == null || tmpWidgetList.isEmpty()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        for (String pkg : procInfo.mPackageName) {
            if (tmpWidgetList.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkSoundRecord(AwareProcessInfo awareProcInfo) {
        ArrayMap<Integer, ProcessInfo> tmpAudioIn = this.mAudioIn;
        if (tmpAudioIn == null) {
            return false;
        }
        for (Entry<Integer, ProcessInfo> m : tmpAudioIn.entrySet()) {
            ProcessInfo info = (ProcessInfo) m.getValue();
            if (awareProcInfo.mProcInfo.mPid == info.mPid) {
                return true;
            }
            if (awareProcInfo.mProcInfo.mUid == info.mUid && (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid) || isPkgIncludeForTgt(awareProcInfo.mProcInfo.mPackageName, info.mPackageName))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMusicPlay(AwareProcessInfo awareProcInfo) {
        ArrayMap<Integer, ProcessInfo> tmpAudioOut = this.mAudioOut;
        if (tmpAudioOut == null) {
            return false;
        }
        for (Entry<Integer, ProcessInfo> m : tmpAudioOut.entrySet()) {
            ProcessInfo info = (ProcessInfo) m.getValue();
            if (awareProcInfo.mProcInfo.mUid == info.mUid && (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid) || isPkgIncludeForTgt(awareProcInfo.mProcInfo.mPackageName, info.mPackageName))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkKeyBackGround(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mCurAdj != 200) {
            return false;
        }
        if (!AwareAppMngSort.FG_SERVICE.equals(procInfo.mAdjType) && (AwareAppMngSort.ADJTYPE_SERVICE.equals(procInfo.mAdjType) ^ 1) != 0) {
            return true;
        }
        Set<Integer> tmpKeyPercepServicePid = this.mKeyPercepServicePid;
        return tmpKeyPercepServicePid != null && tmpKeyPercepServicePid.contains(Integer.valueOf(procInfo.mPid));
    }

    private boolean checkPrevious(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo.mProcInfo.mCurAdj == HwActivityManagerService.PREVIOUS_APP_ADJ) {
            return true;
        }
        if (awareProcInfo.mProcInfo.mCurAdj < 200) {
            return false;
        }
        ArrayList<String> packageNames = awareProcInfo.mProcInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty()) {
            return false;
        }
        AwareAppLruBase tmpAppLruBase = this.mPrevAwareBase;
        if (tmpAppLruBase == null) {
            return false;
        }
        ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = this.mAllProcNeedSort;
        if (allProcNeedSort == null || allProcNeedSort.isEmpty()) {
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return false;
        } else if (awareProcInfo.mProcInfo.mUid != tmpAppLruBase.mUid) {
            return false;
        } else {
            if (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                return true;
            }
            AwareProcessInfo prevProcInfo = (AwareProcessInfo) allProcNeedSort.get(Integer.valueOf(tmpAppLruBase.mPid));
            if (prevProcInfo == null) {
                return false;
            }
            return isPkgIncludeForTgt(packageNames, prevProcInfo.mProcInfo.mPackageName);
        }
    }

    private boolean checkLauncher(AwareProcessInfo awareProcInfo) {
        return awareProcInfo.mProcInfo.mUid == AwareAppAssociate.getInstance().getCurHomeProcessUid();
    }

    private String getMainPkgName(AwareProcessInfo awareProcInfo) {
        ArrayList<String> packageNames = awareProcInfo.mProcInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty()) {
            return null;
        }
        return (String) packageNames.get(0);
    }

    private boolean checkInputMethod(AwareProcessInfo awareProcInfo) {
        String mainPackageName = getMainPkgName(awareProcInfo);
        if (mainPackageName == null || !mainPackageName.equals(AwareIntelligentRecg.getInstance().getDefaultInputMethod())) {
            return false;
        }
        return true;
    }

    private boolean checkWallPaper(AwareProcessInfo awareProcInfo) {
        String mainPackageName = getMainPkgName(awareProcInfo);
        if (mainPackageName == null || !mainPackageName.equals(AwareIntelligentRecg.getInstance().getDefaultWallPaper())) {
            return false;
        }
        return true;
    }

    private boolean checkFrozen(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isAppFrozen(awareProcInfo.mProcInfo.mUid);
    }

    private boolean checkBluetooth(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isAppBluetooth(awareProcInfo.mProcInfo.mUid);
    }

    private boolean checkDownloadFrozen(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isNotBeClean(getMainPkgName(awareProcInfo));
    }

    private boolean checkToastWindow(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isToastWindow(awareProcInfo.mProcInfo.mPid);
    }

    private boolean checkKeyBackgroupByState(int state, AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (this.mKeyBackgroupInstance == null) {
            return false;
        }
        return this.mKeyBackgroupInstance.checkKeyBackgroupByState(state, procInfo.mPid, procInfo.mUid, procInfo.mPackageName);
    }

    public boolean getNewProcesses() {
        if (SystemClock.elapsedRealtimeNanos() - this.mUpdateTime <= 10000000) {
            return true;
        }
        if (this.mHwAMS == null) {
            return false;
        }
        return updateNewProcesses();
    }

    private boolean updateNewProcesses() {
        ArrayList<ProcessInfo> tmp_procInfos = ProcessInfoCollector.getInstance().getProcessInfoList();
        Map<Integer, AwareProcessBaseInfo> tmp_baseInfos = this.mHwAMS.getAllProcessBaseInfo();
        if (!tmp_procInfos.isEmpty() ? tmp_baseInfos.isEmpty() : true) {
            return false;
        }
        ArrayMap<Integer, AwareProcessInfo> tmp_allProcNeedSort = new ArrayMap();
        ArrayMap<Integer, ArrayList<String>> tmp_mForeGroundUid = new ArrayMap();
        ArraySet<Integer> tmp_mForeGroundAssocPid = new ArraySet();
        ArrayMap<Integer, ProcessInfo> tmp_mAudioIn = new ArrayMap();
        ArrayMap<Integer, ProcessInfo> tmp_mAudioOut = new ArrayMap();
        Set<Integer> tmp_keyPercepServicePid = new HashSet();
        Set<Integer> tmp_fgServiceUid = new HashSet();
        ArraySet<Integer> tmp_importUid = new ArraySet();
        ArrayMap<Integer, Integer> tmp_percepServicePid = new ArrayMap();
        int tmp_CurrentUserId = AwareAppAssociate.getInstance().getCurUserId();
        ProcessInfo tmp_systemuiProcInfo = null;
        for (ProcessInfo procInfo : tmp_procInfos) {
            if (procInfo != null) {
                AwareProcessBaseInfo updateInfo = (AwareProcessBaseInfo) tmp_baseInfos.get(Integer.valueOf(procInfo.mPid));
                if (updateInfo != null) {
                    procInfo.mCurAdj = updateInfo.mCurAdj;
                    procInfo.mSetProcState = updateInfo.mSetProcState;
                    procInfo.mForegroundActivities = updateInfo.mForegroundActivities;
                    procInfo.mAdjType = updateInfo.mAdjType;
                    procInfo.mAppUid = updateInfo.mAppUid;
                    updateForeGroundUid(procInfo, tmp_mForeGroundUid, tmp_mForeGroundAssocPid);
                    AwareProcessInfo awareProcInfo = new AwareProcessInfo(procInfo.mPid, procInfo);
                    awareProcInfo.mHasShownUi = updateInfo.mHasShownUi;
                    tmp_allProcNeedSort.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                    updateAudioIn(awareProcInfo, procInfo, tmp_mAudioIn);
                    updateAudioOut(awareProcInfo, procInfo, tmp_mAudioOut);
                    if (procInfo.mCurAdj < 200) {
                        tmp_importUid.add(Integer.valueOf(procInfo.mUid));
                    } else {
                        updatePerceptibleApp(procInfo, tmp_fgServiceUid, tmp_percepServicePid, tmp_importUid);
                    }
                    if (isSystemUI(procInfo, tmp_CurrentUserId)) {
                        tmp_systemuiProcInfo = procInfo;
                    }
                }
            }
        }
        for (Entry<Integer, Integer> m : tmp_percepServicePid.entrySet()) {
            int pid = ((Integer) m.getKey()).intValue();
            int uid = ((Integer) m.getValue()).intValue();
            if (!tmp_importUid.contains(Integer.valueOf(uid))) {
                if (tmp_fgServiceUid.contains(Integer.valueOf(uid))) {
                    Set<Integer> strong = new ArraySet();
                    AwareAppAssociate.getInstance().getAssocClientListForPid(pid, strong);
                    for (Integer clientPid : strong) {
                        AwareProcessInfo awareProcInfoItem = (AwareProcessInfo) tmp_allProcNeedSort.get(clientPid);
                        if (awareProcInfoItem != null && awareProcInfoItem.mProcInfo.mCurAdj <= 200) {
                            tmp_keyPercepServicePid.add(Integer.valueOf(pid));
                            break;
                        }
                    }
                } else {
                    tmp_keyPercepServicePid.add(Integer.valueOf(pid));
                }
            } else {
                tmp_keyPercepServicePid.add(Integer.valueOf(pid));
            }
        }
        this.mUpdateTime = SystemClock.elapsedRealtimeNanos();
        this.mCurrentUserId = tmp_CurrentUserId;
        this.mForeGroundUid = tmp_mForeGroundUid;
        this.mForeGroundAssocPid = tmp_mForeGroundAssocPid;
        this.mAllProcNeedSort = tmp_allProcNeedSort;
        this.mAudioIn = tmp_mAudioIn;
        this.mAudioOut = tmp_mAudioOut;
        this.mKeyPercepServicePid = tmp_keyPercepServicePid;
        this.mPrevAmsBase = AwareAppAssociate.getInstance().getPreviousByAmsInfo();
        this.mPrevAwareBase = AwareAppAssociate.getInstance().getPreviousAppInfo();
        this.mSystemuiProcInfo = tmp_systemuiProcInfo;
        updateVisibleWindowList();
        updateWidgetList();
        updateGcm();
        return true;
    }

    private void updateGcm() {
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            List<String> tmp = new ArrayList();
            List<String> result = habit.getGCMAppList();
            if (result != null) {
                tmp.addAll(result);
            }
            this.mGcmAppList = tmp;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSystemUI(ProcessInfo procInfo, int currentUserId) {
        ArrayList<String> packageNames = procInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty() || !FingerViewController.PKGNAME_OF_KEYGUARD.equals(packageNames.get(0)) || currentUserId != UserHandle.getUserId(procInfo.mUid)) {
            return false;
        }
        return true;
    }

    private void updateAudioOut(AwareProcessInfo awareProcInfo, ProcessInfo procInfo, ArrayMap<Integer, ProcessInfo> tmp_mAudioOut) {
        if (checkKeyBackgroupByState(2, awareProcInfo)) {
            tmp_mAudioOut.put(Integer.valueOf(procInfo.mPid), procInfo);
        }
    }

    private void updateAudioIn(AwareProcessInfo awareProcInfo, ProcessInfo procInfo, ArrayMap<Integer, ProcessInfo> tmp_mAudioIn) {
        if (checkKeyBackgroupByState(1, awareProcInfo)) {
            tmp_mAudioIn.put(Integer.valueOf(procInfo.mPid), procInfo);
        }
    }

    private void updatePerceptibleApp(ProcessInfo procInfo, Set<Integer> tmp_fgServiceUid, ArrayMap<Integer, Integer> tmp_percepServicePid, ArraySet<Integer> tmp_importUid) {
        if (procInfo.mCurAdj != 200) {
            return;
        }
        if (AwareAppMngSort.FG_SERVICE.equals(procInfo.mAdjType)) {
            tmp_fgServiceUid.add(Integer.valueOf(procInfo.mUid));
        } else if (AwareAppMngSort.ADJTYPE_SERVICE.equals(procInfo.mAdjType)) {
            tmp_percepServicePid.put(Integer.valueOf(procInfo.mPid), Integer.valueOf(procInfo.mUid));
        } else {
            tmp_importUid.add(Integer.valueOf(procInfo.mUid));
        }
    }

    private void updateForeGroundUid(ProcessInfo procInfo, ArrayMap<Integer, ArrayList<String>> tmp_mForeGroundUid, ArraySet<Integer> tmp_mForeGroundAssocPid) {
        if (procInfo.mForegroundActivities) {
            AwareAppAssociate.getInstance().getAssocProvider(procInfo.mPid, tmp_mForeGroundAssocPid);
            if (AwareAppAssociate.isDealAsPkgUid(procInfo.mUid)) {
                tmp_mForeGroundUid.put(Integer.valueOf(procInfo.mUid), procInfo.mPackageName);
            } else {
                tmp_mForeGroundUid.put(Integer.valueOf(procInfo.mUid), null);
            }
        }
    }

    private void updateWidgetList() {
        this.mWidgetList = AwareAppAssociate.getInstance().getWidgetsPkg();
    }

    private void updateVisibleWindowList() {
        Set<Integer> visibleWindows = new ArraySet();
        Set<Integer> restrainedVisWins = new ArraySet();
        Set<String> tmpVisibleWindowList = new ArraySet();
        Set<String> tmpRestrainedVisWinList = new ArraySet();
        ArrayMap<Integer, AwareProcessInfo> tmpAllProcNeedSort = this.mAllProcNeedSort;
        if (tmpAllProcNeedSort == null || tmpAllProcNeedSort.isEmpty()) {
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return;
        }
        AwareAppAssociate.getInstance().getVisibleWindows(visibleWindows, null);
        addVisibleWindowToList(visibleWindows, tmpVisibleWindowList, tmpAllProcNeedSort);
        this.mVisibleWindowList = tmpVisibleWindowList;
        AwareAppAssociate.getInstance().getVisibleWindowsInRestriction(restrainedVisWins);
        addVisibleWindowToList(restrainedVisWins, tmpRestrainedVisWinList, tmpAllProcNeedSort);
        this.mRestrainedVisWinList = tmpRestrainedVisWinList;
    }

    private void addVisibleWindowToList(Set<Integer> visibleWindows, Set<String> visibleWindowList, ArrayMap<Integer, AwareProcessInfo> procNeedSort) {
        for (Integer pid : visibleWindows) {
            AwareProcessInfo awareProcInfo = (AwareProcessInfo) procNeedSort.get(pid);
            if (!(awareProcInfo == null || awareProcInfo.mProcInfo == null || awareProcInfo.mProcInfo.mPackageName == null)) {
                visibleWindowList.addAll(awareProcInfo.mProcInfo.mPackageName);
            }
        }
    }

    private boolean isPkgIncludeForTgt(ArrayList<String> tgtPkg, ArrayList<String> dstPkg) {
        if (tgtPkg == null || tgtPkg.isEmpty() || dstPkg == null) {
            return false;
        }
        for (String pkg : dstPkg) {
            if (pkg != null && tgtPkg.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<AwareProcessInfo> getAllProcNeedSort() {
        if (getNewProcesses()) {
            ArrayMap<Integer, AwareProcessInfo> tmpAllProcNeedSort = this.mAllProcNeedSort;
            if (tmpAllProcNeedSort != null && !tmpAllProcNeedSort.isEmpty()) {
                return new ArrayList(tmpAllProcNeedSort.values());
            }
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return null;
        }
        AwareLog.e(TAG, "update processes status failed!");
        return null;
    }

    private boolean checkForeground(AwareProcessInfo awareProcInfo) {
        Set<Integer> forePids = new ArraySet();
        AwareAppAssociate.getInstance().getForeGroundApp(forePids);
        for (Integer pid : forePids) {
            if (pid.equals(Integer.valueOf(awareProcInfo.mPid))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkBlind(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        for (String pkg : procInfo.mPackageName) {
            if (this.mBlindPkg.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMusicInstant(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isAudioOutInstant(procInfo.mUid);
    }

    private boolean checkNonSystemUser(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareAppMngSort.getInstance().checkNonSystemUser(awareProcInfo);
    }

    private boolean checkInSmallSampleList(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isInSmallSampleList(awareProcInfo);
    }

    private boolean checkScreenChangedAcheive(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isAchScreenChangedNum(awareProcInfo);
    }

    private boolean checkScreenRecord(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isScreenRecord(awareProcInfo);
    }

    private boolean checkCameraRecord(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isCameraRecord(awareProcInfo);
    }

    public void updateBlind(Set<String> blindPkgs) {
        if (blindPkgs == null) {
            this.mBlindPkg = new ArraySet();
        } else {
            this.mBlindPkg = blindPkgs;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkGcm(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null || awareProcInfo.mProcInfo == null || this.mGcmAppList == null || awareProcInfo.mProcInfo.mPackageName == null) {
            return false;
        }
        for (String pkg : awareProcInfo.mProcInfo.mPackageName) {
            if (this.mGcmAppList.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverseaApp(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        boolean result = false;
        for (String packageName : awareProcInfo.mProcInfo.mPackageName) {
            if (AppTypeRecoManager.getInstance().getAppWhereFrom(packageName) != 0) {
                result = true;
                break;
            }
        }
        return result;
    }
}
