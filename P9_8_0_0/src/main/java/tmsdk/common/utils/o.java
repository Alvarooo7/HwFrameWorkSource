package tmsdk.common.utils;

import android.os.Build.VERSION;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import tmsdk.common.TMSDKContext;
import tmsdkobf.kt;

public class o {
    static boolean LQ = false;
    private static final byte[] LR = new byte[]{(byte) 99, (byte) 111, (byte) 109, (byte) 46, (byte) 116, (byte) 101, (byte) 110, (byte) 99, (byte) 101, (byte) 110, (byte) 116, (byte) 46, (byte) 116, (byte) 109, (byte) 115, (byte) 101, (byte) 99, (byte) 117, (byte) 114, (byte) 101, (byte) 108, (byte) 105, (byte) 116, (byte) 101, (byte) 46, (byte) 98, (byte) 97, (byte) 115, (byte) 101, (byte) 46, (byte) 73, (byte) 84, (byte) 109, (byte) 115, (byte) 80, (byte) 114, (byte) 111, (byte) 118, (byte) 105, (byte) 100, (byte) 101, (byte) 114};
    private static final byte[] LS = new byte[]{(byte) 99, (byte) 111, (byte) 109, (byte) 46, (byte) 116, (byte) 101, (byte) 110, (byte) 99, (byte) 101, (byte) 110, (byte) 116, (byte) 46, (byte) 116, (byte) 109, (byte) 115, (byte) 101, (byte) 99, (byte) 117, (byte) 114, (byte) 101, (byte) 108, (byte) 105, (byte) 116, (byte) 101, (byte) 46, (byte) 99, (byte) 111, (byte) 109, (byte) 109, (byte) 111, (byte) 109, (byte) 46, (byte) 73, (byte) 84, (byte) 109, (byte) 115, (byte) 67, (byte) 97, (byte) 108, (byte) 108, (byte) 98, (byte) 97, (byte) 99, (byte) 107};
    private static final byte[] LT = new byte[]{(byte) 116, (byte) 109, (byte) 115, (byte) 100, (byte) 107, (byte) 46, (byte) 99, (byte) 111, (byte) 109, (byte) 109, (byte) 111, (byte) 110, (byte) 46, (byte) 114, (byte) 111, (byte) 97, (byte) 99, (byte) 104, (byte) 46, (byte) 110, (byte) 101, (byte) 115, (byte) 116, (byte) 46, (byte) 65, (byte) 99, (byte) 116, (byte) 105, (byte) 111, (byte) 110, (byte) 73};
    private static final byte[] LU = new byte[]{(byte) 99, (byte) 111, (byte) 109, (byte) 46, (byte) 113, (byte) 113, (byte) 46, (byte) 116, (byte) 97, (byte) 102, (byte) 46, (byte) 106, (byte) 99, (byte) 101, (byte) 46, (byte) 74, (byte) 99, (byte) 101, (byte) 83, (byte) 116, (byte) 114, (byte) 117, (byte) 99, (byte) 116};
    private static final byte[] LV = new byte[]{(byte) 99, (byte) 111, (byte) 109, (byte) 46, (byte) 113, (byte) 113, (byte) 46, (byte) 116, (byte) 97, (byte) 102, (byte) 46, (byte) 106, (byte) 99, (byte) 101, (byte) 46, (byte) 74, (byte) 99, (byte) 101, (byte) 73, (byte) 110, (byte) 112, (byte) 117, (byte) 116, (byte) 83, (byte) 116, (byte) 114, (byte) 101, (byte) 97, (byte) 109};

    public static boolean iY() {
        try {
            Object -l_1_R = Class.forName(new String(LT));
            Object -l_2_R = Class.forName(new String(LU));
            Object -l_3_R = Class.forName(new String(LV));
            Log.d("SanityUtil", "check proguard ok");
            return true;
        } catch (Throwable th) {
            Log.w("SanityUtil", "is re proguard");
            return false;
        }
    }

    public static boolean iZ() {
        if (LQ) {
            return LQ;
        }
        Object -l_0_R = TMSDKContext.getApplicaionContext().getPackageName();
        int -l_1_I = TMSDKContext.getApplicaionContext().getApplicationInfo().uid;
        Log.d("SanityUtil", "pkgName:[" + -l_0_R + "]uid: [" + -l_1_I + "]android version:[" + VERSION.SDK_INT + "]");
        if (-l_0_R.compareTo("com.tencent.tmsecure.demo") != 0 && VERSION.SDK_INT >= 21) {
            try {
                -l_1_I = ((Integer) Class.forName(UserHandle.class.getName()).getMethod("getAppId", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(-l_1_I)})).intValue();
                Log.d("SanityUtil", "appId:[" + -l_1_I + "]");
            } catch (Object -l_3_R) {
                Log.d("SanityUtil", "e:[" + -l_3_R + "]");
            }
            if (-l_1_I == CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY) {
                Log.d("SanityUtil", "check system uid ok");
                kt.aE(1320027);
            }
        }
        LQ = true;
        return LQ;
    }
}
