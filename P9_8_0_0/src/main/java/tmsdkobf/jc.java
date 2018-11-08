package tmsdkobf;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.util.Map;

public class jc implements jx {
    private SharedPreferences sP;
    private Editor sQ;
    private boolean sR = false;

    public jc(Context context, String str, boolean z) {
        this.sP = context.getSharedPreferences(str, 0);
    }

    private Editor getEditor() {
        if (this.sQ == null) {
            this.sQ = this.sP.edit();
        }
        return this.sQ;
    }

    public void beginTransaction() {
        this.sR = true;
    }

    public void clear() {
        getEditor().clear().commit();
    }

    public void endTransaction() {
        this.sR = false;
        if (this.sQ != null) {
            this.sQ.commit();
        }
    }

    public Map<String, ?> getAll() {
        return this.sP.getAll();
    }

    public boolean getBoolean(String str, boolean z) {
        return this.sP.getBoolean(str, z);
    }

    public int getInt(String str) {
        return this.sP.getInt(str, 0);
    }

    public int getInt(String str, int i) {
        return this.sP.getInt(str, i);
    }

    public long getLong(String str, long j) {
        return this.sP.getLong(str, j);
    }

    public String getString(String str, String str2) {
        return this.sP.getString(str, str2);
    }

    public void putBoolean(String str, boolean z) {
        Object -l_3_R = getEditor();
        -l_3_R.putBoolean(str, z);
        if (!this.sR) {
            -l_3_R.commit();
        }
    }

    public void putInt(String str, int i) {
        Object -l_3_R = getEditor();
        -l_3_R.putInt(str, i);
        if (!this.sR) {
            -l_3_R.commit();
        }
    }

    public void putLong(String str, long j) {
        Object -l_4_R = getEditor();
        -l_4_R.putLong(str, j);
        if (!this.sR) {
            -l_4_R.commit();
        }
    }

    public void putString(String str, String str2) {
        Object -l_3_R = getEditor();
        -l_3_R.putString(str, str2);
        if (!this.sR) {
            -l_3_R.commit();
        }
    }

    public void remove(String str) {
        getEditor().remove(str).commit();
    }
}
