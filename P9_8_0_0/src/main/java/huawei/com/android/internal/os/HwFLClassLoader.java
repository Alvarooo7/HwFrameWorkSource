package huawei.com.android.internal.os;

import android.util.Log;
import dalvik.system.PathClassLoader;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HwFLClassLoader extends PathClassLoader {
    public static final String TAG = "HwFLClassLoader";
    private static final String USE_FEATURE_LIST = "/feature/used-list";
    private static boolean mInitUsedList = false;
    private static List<String> mUsedFeatureList = new ArrayList();

    static {
        initUsedList();
    }

    public HwFLClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, parent);
    }

    public HwFLClassLoader(String dexPath, String librarySearchPath, ClassLoader parent) {
        super(dexPath, librarySearchPath, parent);
    }

    private static void initUsedList() {
        IOException e;
        Throwable th;
        File pathFile = HwCfgFilePolicy.getCfgFile(USE_FEATURE_LIST, 0);
        if (pathFile == null) {
            Log.d(TAG, "get used feature list :/feature/used-list failed!");
            return;
        }
        BufferedReader bufferedReader = null;
        try {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathFile), "UTF-8"), 256);
                try {
                    String str = "";
                    while (true) {
                        str = br.readLine();
                        if (str == null) {
                            break;
                        }
                        str = str.trim();
                        if (!(str.startsWith("#") || str.equals(""))) {
                            Log.v(TAG, "add package: " + str + " in FEATURE_USED_LIST");
                            mUsedFeatureList.add(str);
                        }
                    }
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "Error in close BufferedReader /feature/used-list.", e2);
                        }
                    }
                } catch (IOException e3) {
                    e2 = e3;
                    bufferedReader = br;
                    try {
                        Log.e(TAG, "Error reading /feature/used-list.", e2);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e22) {
                                Log.e(TAG, "Error in close BufferedReader /feature/used-list.", e22);
                            }
                        }
                        mInitUsedList = true;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e222) {
                                Log.e(TAG, "Error in close BufferedReader /feature/used-list.", e222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = br;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e222 = e4;
                Log.e(TAG, "Error reading /feature/used-list.", e222);
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                mInitUsedList = true;
            }
            mInitUsedList = true;
        } catch (FileNotFoundException e5) {
            Log.e(TAG, "Couldn't find /feature/used-list.");
        }
    }

    private static boolean isUseFeature(String dexPath) {
        if (!mInitUsedList) {
            Log.d(TAG, "USE_FEATURE_LIST had not init! ");
            return false;
        } else if (dexPath == null || dexPath.isEmpty()) {
            return false;
        } else {
            for (String feature : mUsedFeatureList) {
                if (dexPath.contains(feature)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static ClassLoader getHwFLClassLoaderParent(String dexPath) {
        if (isUseFeature(dexPath)) {
            return ClassLoader.getSystemClassLoader();
        }
        return null;
    }
}
