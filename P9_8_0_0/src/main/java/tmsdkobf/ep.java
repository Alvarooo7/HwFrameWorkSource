package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class ep extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public int kn = 0;

    static {
        boolean z = false;
        if (!ep.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public ep() {
        f(this.kn);
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
        return d.equals(this.kn, ((ep) obj).kn);
    }

    public void f(int i) {
        this.kn = i;
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
        f(jceInputStream.read(this.kn, 0, true));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.kn, 0);
    }
}
