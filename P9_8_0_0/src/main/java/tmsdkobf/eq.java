package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class eq extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public int L = 0;
    public String dl = "";
    public String dn = "";
    public String do = "";
    public String dq = "";
    public String imsi = "";
    public int kA = 0;
    public int kB = 0;
    public String kz = "";
    public String version = "";

    static {
        boolean z = false;
        if (!eq.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public eq() {
        d(this.dl);
        o(this.dn);
        setPhone(this.do);
        p(this.kz);
        q(this.dq);
        e(this.imsi);
        r(this.version);
        m(this.L);
        n(this.kA);
        o(this.kB);
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

    public void d(String str) {
        this.dl = str;
    }

    public void e(String str) {
        this.imsi = str;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        eq -l_2_R = (eq) obj;
        if (d.equals(this.dl, -l_2_R.dl) && d.equals(this.dn, -l_2_R.dn) && d.equals(this.do, -l_2_R.do) && d.equals(this.kz, -l_2_R.kz) && d.equals(this.dq, -l_2_R.dq) && d.equals(this.imsi, -l_2_R.imsi) && d.equals(this.version, -l_2_R.version) && d.equals(this.L, -l_2_R.L) && d.equals(this.kA, -l_2_R.kA) && d.equals(this.kB, -l_2_R.kB)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Object -l_1_R) {
            -l_1_R.printStackTrace();
            return 0;
        }
    }

    public void m(int i) {
        this.L = i;
    }

    public void n(int i) {
        this.kA = i;
    }

    public void o(int i) {
        this.kB = i;
    }

    public void o(String str) {
        this.dn = str;
    }

    public void p(String str) {
        this.kz = str;
    }

    public void q(String str) {
        this.dq = str;
    }

    public void r(String str) {
        this.version = str;
    }

    public void readFrom(JceInputStream jceInputStream) {
        d(jceInputStream.readString(0, true));
        o(jceInputStream.readString(1, false));
        setPhone(jceInputStream.readString(2, false));
        p(jceInputStream.readString(3, false));
        q(jceInputStream.readString(4, false));
        e(jceInputStream.readString(5, false));
        r(jceInputStream.readString(6, false));
        m(jceInputStream.read(this.L, 7, false));
        n(jceInputStream.read(this.kA, 8, false));
        o(jceInputStream.read(this.kB, 9, false));
    }

    public void setPhone(String str) {
        this.do = str;
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.dl, 0);
        if (this.dn != null) {
            jceOutputStream.write(this.dn, 1);
        }
        if (this.do != null) {
            jceOutputStream.write(this.do, 2);
        }
        if (this.kz != null) {
            jceOutputStream.write(this.kz, 3);
        }
        if (this.dq != null) {
            jceOutputStream.write(this.dq, 4);
        }
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 5);
        }
        if (this.version != null) {
            jceOutputStream.write(this.version, 6);
        }
        jceOutputStream.write(this.L, 7);
        jceOutputStream.write(this.kA, 8);
        jceOutputStream.write(this.kB, 9);
    }
}
