package com.android.server.wm;

import android.content.Context;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.SurfaceSession;

/* compiled from: HwScreenRotationAnimationImpl */
class HwScreenRotationAnimation extends ScreenRotationAnimation {
    private static final boolean ENABLE_ROUND_CORNER_DISPLAY = Environment.buildPath(null, new String[]{"/product/etc/display/RoundCornerDisplay/config.xml"}).exists();
    private static final int SCREEN_ROTATION_ANIMATION_END = 8001;
    private static final String TAG_RCD = "RoundCornerDisplay";

    public HwScreenRotationAnimation(Context context, DisplayContent displayContent, SurfaceSession session, boolean inTransaction, boolean forceDefaultOrientation, boolean isSecure, WindowManagerService service) {
        super(context, displayContent, session, inTransaction, forceDefaultOrientation, isSecure, service);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void kill() {
        super.kill();
        if (ENABLE_ROUND_CORNER_DISPLAY && this.mDisplayContent.getDisplayId() == 0) {
            Parcel dataIn = Parcel.obtain();
            try {
                IBinder sfBinder = ServiceManager.getService("SurfaceFlinger");
                if (!(sfBinder == null || (sfBinder.transact(SCREEN_ROTATION_ANIMATION_END, dataIn, null, 1) ^ 1) == 0)) {
                    Log.e(TAG_RCD, "Notify screen rotation animation end failed!");
                }
                dataIn.recycle();
            } catch (RemoteException e) {
                Log.e(TAG_RCD, "RemoteException on notify screen rotation animation end");
            } catch (Throwable th) {
                dataIn.recycle();
            }
        }
    }
}
