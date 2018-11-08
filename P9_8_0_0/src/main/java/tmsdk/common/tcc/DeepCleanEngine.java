package tmsdk.common.tcc;

public class DeepCleanEngine {
    public static final int INIT_FOR_DEEPCLEAN = 0;
    public static final int INIT_FOR_SPACE_MGR = 1;
    public static final int INIT_FOR_SPACE_MGR_VISIT_ALL = 2;
    private Callback mCallback;
    private long mNativePtr;

    public interface Callback {
        String getDetailRule(String str);

        void onDirectoryChange(String str, int i);

        void onFoundComRubbish(String str, String str2, long j);

        void onFoundEmptyDir(String str, long j);

        void onFoundKeySoftRubbish(String str, String[] strArr, long j);

        void onFoundSoftRubbish(String str, String str2, String str3, long j);

        void onProcessChange(int i);

        void onVisit(QFile qFile);
    }

    public DeepCleanEngine(Callback callback) {
        this.mCallback = callback;
    }

    private native void cancel(long j);

    private native long create(int i);

    private native void release(long j);

    private native void scanPath(long j, String str, String str2);

    private native void setComRubRule(long j, String[] strArr);

    private native void setOtherFilterRule(long j, String[] strArr);

    private native void setRootPaths(long j, String[] strArr);

    private native void setWhitePaths(long j, String[] strArr);

    public void cancel() {
        if (0 != this.mNativePtr) {
            cancel(this.mNativePtr);
        }
    }

    public native String[] findMatchDir(String str, String str2);

    public String getDetailRule(String str) {
        return this.mCallback != null ? this.mCallback.getDetailRule(str) : null;
    }

    public boolean init() {
        return init(0);
    }

    public boolean init(int i) {
        try {
            this.mNativePtr = create(i);
        } catch (Throwable th) {
            this.mNativePtr = 0;
        }
        return this.mNativePtr != 0;
    }

    public native boolean isMatchComRule(String str, String str2, String str3);

    public native boolean isMatchFile(String str, String str2);

    public native boolean isMatchFileSize(long j, String str);

    public native boolean isMatchPath(String str, String str2);

    public native boolean isMatchTime(long j, String str);

    public void onDirectoryChange(String str, int i) {
        if (this.mCallback != null) {
            this.mCallback.onDirectoryChange(str, i);
        }
    }

    public void onFoundComRubbish(String str, String str2, long j) {
        if (this.mCallback != null) {
            this.mCallback.onFoundComRubbish(str, str2, j);
        }
    }

    public void onFoundEmptyDir(String str, long j) {
        if (this.mCallback != null) {
            this.mCallback.onFoundEmptyDir(str, j);
        }
    }

    public void onFoundKeySoftRubbish(String str, String[] strArr, long j) {
        if (this.mCallback != null) {
            this.mCallback.onFoundKeySoftRubbish(str, strArr, j);
        }
    }

    public void onFoundSoftRubbish(String str, String str2, String str3, long j) {
        if (this.mCallback != null) {
            this.mCallback.onFoundSoftRubbish(str, str2, str3, j);
        }
    }

    void onProcessChange(int i) {
        if (this.mCallback != null) {
            this.mCallback.onProcessChange(i);
        }
    }

    void onVisit(String str, boolean z, long j, long j2, long j3, long j4) {
        int i = 0;
        if (this.mCallback != null) {
            Object -l_11_R = new QFile(str);
            if (z) {
                i = 4;
            }
            -l_11_R.type = i;
            -l_11_R.size = j;
            -l_11_R.createTime = j2;
            -l_11_R.modifyTime = j3;
            -l_11_R.accessTime = j4;
            this.mCallback.onVisit(-l_11_R);
        }
    }

    public void release() {
        if (this.mNativePtr != 0) {
            release(this.mNativePtr);
            this.mNativePtr = 0;
        }
    }

    public void scanPath(String str, String str2) {
        scanPath(this.mNativePtr, str, str2);
    }

    public void setComRubRule(String[] strArr) {
        setComRubRule(this.mNativePtr, strArr);
    }

    public void setOtherFilterRule(String[] strArr) {
        setOtherFilterRule(this.mNativePtr, strArr);
    }

    public void setRootPaths(String[] strArr) {
        setRootPaths(this.mNativePtr, strArr);
    }

    public void setUninstallFilterSuffix(String[] strArr) {
    }

    public void setWhitePaths(String[] strArr) {
        setWhitePaths(this.mNativePtr, strArr);
    }
}
