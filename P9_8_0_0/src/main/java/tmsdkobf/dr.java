package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class dr extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public String hQ = "";

    static {
        boolean z = false;
        if (!dr.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public dr() {
        a(this.hQ);
    }

    public void a(String str) {
        this.hQ = str;
    }

    public Object clone() {
        Object -l_1_R = null;
        try {
            -l_1_R = super.clone();
        } catch (CloneNotSupportedException e) {
            if (!bF) {
                throw new AssertionError();
            }
        }
        return -l_1_R;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return d.equals(this.hQ, ((dr) obj).hQ);
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Object -l_1_R) {
            -l_1_R.printStackTrace();
            return 0;
        }
    }

    public void readFrom(JceInputStream jceInputStream) {
        a(jceInputStream.readString(0, true));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.hQ, 0);
    }
}
