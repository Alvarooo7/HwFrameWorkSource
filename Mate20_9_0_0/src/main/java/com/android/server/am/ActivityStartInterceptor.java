package com.android.server.am;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.HwPCUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.HarmfulAppWarningActivity;
import com.android.internal.app.SuspendedAppActivity;
import com.android.internal.app.UnlaunchableAppActivity;
import com.android.server.LocalServices;
import com.android.server.pm.PackageManagerService;

public class ActivityStartInterceptor extends AbsActivityStartInterceptor {
    static String REAL_CALLING_UID = "real_calling_uid";
    ActivityInfo mAInfo;
    ActivityOptions mActivityOptions;
    protected String mCallingPackage;
    int mCallingPid;
    int mCallingUid;
    TaskRecord mInTask;
    Intent mIntent;
    ResolveInfo mRInfo;
    protected int mRealCallingPid;
    protected int mRealCallingUid;
    String mResolvedType;
    protected final ActivityManagerService mService;
    private final Context mServiceContext;
    protected int mStartFlags;
    protected final ActivityStackSupervisor mSupervisor;
    private final UserController mUserController;
    protected int mUserId;
    private UserManager mUserManager;

    public ActivityStartInterceptor(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        this(service, supervisor, service.mContext, service.mUserController);
    }

    @VisibleForTesting
    ActivityStartInterceptor(ActivityManagerService service, ActivityStackSupervisor supervisor, Context context, UserController userController) {
        this.mService = service;
        this.mSupervisor = supervisor;
        this.mServiceContext = context;
        this.mUserController = userController;
    }

    void setStates(int userId, int realCallingPid, int realCallingUid, int startFlags, String callingPackage) {
        this.mRealCallingPid = realCallingPid;
        this.mRealCallingUid = realCallingUid;
        this.mUserId = userId;
        this.mStartFlags = startFlags;
        this.mCallingPackage = callingPackage;
    }

    private IntentSender createIntentSenderForOriginalIntent(int callingUid, int flags) {
        Bundle activityOptions = deferCrossProfileAppsAnimationIfNecessary();
        if (HwPCUtils.isPcCastModeInServer() && this.mActivityOptions != null && HwPCUtils.isValidExtDisplayId(this.mActivityOptions.getLaunchDisplayId())) {
            ActivityOptions aOs = ActivityOptions.makeBasic();
            aOs.setLaunchDisplayId(HwPCUtils.getPCDisplayID());
            aOs.setLaunchWindowingMode(10);
            activityOptions = aOs.toBundle();
        }
        return new IntentSender(this.mService.getIntentSenderLocked(2, this.mCallingPackage, callingUid, this.mUserId, null, null, 0, new Intent[]{this.mIntent}, new String[]{this.mResolvedType}, flags, activityOptions));
    }

    boolean intercept(Intent intent, ResolveInfo rInfo, ActivityInfo aInfo, String resolvedType, TaskRecord inTask, int callingPid, int callingUid, ActivityOptions activityOptions) {
        this.mUserManager = UserManager.get(this.mServiceContext);
        this.mIntent = intent;
        this.mCallingPid = callingPid;
        this.mCallingUid = callingUid;
        this.mRInfo = rInfo;
        this.mAInfo = aInfo;
        this.mResolvedType = resolvedType;
        this.mInTask = inTask;
        this.mActivityOptions = activityOptions;
        if (interceptStartActivityIfNeed(intent) || interceptSuspendedPackageIfNeeded() || interceptQuietProfileIfNeeded() || interceptHarmfulAppIfNeeded()) {
            return true;
        }
        return interceptWorkProfileChallengeIfNeeded();
    }

    private Bundle deferCrossProfileAppsAnimationIfNecessary() {
        if (this.mActivityOptions == null || this.mActivityOptions.getAnimationType() != 12) {
            return null;
        }
        this.mActivityOptions = null;
        return ActivityOptions.makeOpenCrossProfileAppsAnimation().toBundle();
    }

    private boolean interceptQuietProfileIfNeeded() {
        if (!this.mUserManager.isQuietModeEnabled(UserHandle.of(this.mUserId))) {
            return false;
        }
        this.mIntent = UnlaunchableAppActivity.createInQuietModeDialogIntent(this.mUserId, createIntentSenderForOriginalIntent(this.mCallingUid, 1342177280));
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserManager.getProfileParent(this.mUserId).id, 0, this.mRealCallingUid);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private boolean interceptSuspendedByAdminPackage() {
        DevicePolicyManagerInternal devicePolicyManager = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (devicePolicyManager == null) {
            return false;
        }
        this.mIntent = devicePolicyManager.createShowAdminSupportIntent(this.mUserId, true);
        this.mIntent.putExtra("android.app.extra.RESTRICTION", "policy_suspend_packages");
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        UserInfo parent = this.mUserManager.getProfileParent(this.mUserId);
        if (parent != null) {
            this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, parent.id, 0, this.mRealCallingUid);
        } else {
            this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserId, 0, this.mRealCallingUid);
        }
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private boolean interceptSuspendedPackageIfNeeded() {
        if (this.mAInfo == null || this.mAInfo.applicationInfo == null || (this.mAInfo.applicationInfo.flags & 1073741824) == 0) {
            return false;
        }
        PackageManagerInternal pmi = this.mService.getPackageManagerInternalLocked();
        if (pmi == null) {
            return false;
        }
        String suspendedPackage = this.mAInfo.applicationInfo.packageName;
        String suspendingPackage = pmi.getSuspendingPackage(suspendedPackage, this.mUserId);
        if (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(suspendingPackage)) {
            return interceptSuspendedByAdminPackage();
        }
        this.mIntent = SuspendedAppActivity.createSuspendedAppInterceptIntent(suspendedPackage, suspendingPackage, pmi.getSuspendedDialogMessage(suspendedPackage, this.mUserId), this.mUserId);
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserId, 0, this.mRealCallingUid);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private boolean interceptWorkProfileChallengeIfNeeded() {
        Intent interceptingIntent = interceptWithConfirmCredentialsIfNeeded(this.mAInfo, this.mUserId);
        if (interceptingIntent == null) {
            return false;
        }
        this.mIntent = interceptingIntent;
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        if (this.mInTask != null) {
            this.mIntent.putExtra("android.intent.extra.TASK_ID", this.mInTask.taskId);
            this.mInTask = null;
        }
        if (this.mActivityOptions == null) {
            this.mActivityOptions = ActivityOptions.makeBasic();
        }
        ActivityRecord homeActivityRecord = this.mSupervisor.getHomeActivity();
        if (!(homeActivityRecord == null || homeActivityRecord.getTask() == null || HwPCUtils.isValidExtDisplayId(this.mActivityOptions.getLaunchDisplayId()))) {
            this.mActivityOptions.setLaunchTaskId(homeActivityRecord.getTask().taskId);
        }
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserManager.getProfileParent(this.mUserId).id, 0, this.mRealCallingUid);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private Intent interceptWithConfirmCredentialsIfNeeded(ActivityInfo aInfo, int userId) {
        if (!this.mUserController.shouldConfirmCredentials(userId)) {
            return null;
        }
        IntentSender target = createIntentSenderForOriginalIntent(Binder.getCallingUid(), 1409286144);
        Intent newIntent = ((KeyguardManager) this.mServiceContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userId);
        if (newIntent == null) {
            return null;
        }
        newIntent.setFlags(276840448);
        newIntent.putExtra("android.intent.extra.PACKAGE_NAME", aInfo.packageName);
        newIntent.putExtra("android.intent.extra.INTENT", target);
        return newIntent;
    }

    private boolean interceptHarmfulAppIfNeeded() {
        try {
            CharSequence harmfulAppWarning = this.mService.getPackageManager().getHarmfulAppWarning(this.mAInfo.packageName, this.mUserId);
            if (harmfulAppWarning == null) {
                return false;
            }
            this.mIntent = HarmfulAppWarningActivity.createHarmfulAppWarningIntent(this.mServiceContext, this.mAInfo.packageName, createIntentSenderForOriginalIntent(this.mCallingUid, 1409286144), harmfulAppWarning);
            this.mCallingPid = this.mRealCallingPid;
            this.mCallingUid = this.mRealCallingUid;
            this.mResolvedType = null;
            this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserId, 0, this.mRealCallingUid);
            this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
}
