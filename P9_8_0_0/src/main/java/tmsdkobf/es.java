package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;
import java.util.ArrayList;

public final class es extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    static ArrayList<en> fv;
    static ArrayList<fb> fw;
    public int dp = 0;
    public int fk = 0;
    public int fl = 0;
    public int fm = 0;
    public int fn = 0;
    public ArrayList<en> fo = null;
    public int fp = 0;
    public ArrayList<fb> fq = null;
    public String fr = "";
    public int fs = 0;
    public String sender = "";
    public String sms = "";

    static {
        boolean z = false;
        if (!es.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public es() {
        t(this.sender);
        u(this.sms);
        q(this.fk);
        r(this.fl);
        s(this.fm);
        t(this.fn);
        d(this.fo);
        u(this.fp);
        e(this.fq);
        setComment(this.fr);
        v(this.fs);
        a(this.dp);
    }

    public void a(int i) {
        this.dp = i;
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

    public void d(ArrayList<en> arrayList) {
        this.fo = arrayList;
    }

    public void e(ArrayList<fb> arrayList) {
        this.fq = arrayList;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        es -l_2_R = (es) obj;
        if (d.equals(this.sender, -l_2_R.sender) && d.equals(this.sms, -l_2_R.sms) && d.equals(this.fk, -l_2_R.fk) && d.equals(this.fl, -l_2_R.fl) && d.equals(this.fm, -l_2_R.fm) && d.equals(this.fn, -l_2_R.fn) && d.equals(this.fo, -l_2_R.fo) && d.equals(this.fp, -l_2_R.fp) && d.equals(this.fq, -l_2_R.fq) && d.equals(this.fr, -l_2_R.fr) && d.equals(this.fs, -l_2_R.fs) && d.equals(this.dp, -l_2_R.dp)) {
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

    public void q(int i) {
        this.fk = i;
    }

    public void r(int i) {
        this.fl = i;
    }

    public void readFrom(JceInputStream jceInputStream) {
        t(jceInputStream.readString(0, true));
        u(jceInputStream.readString(1, true));
        q(jceInputStream.read(this.fk, 2, true));
        r(jceInputStream.read(this.fl, 3, true));
        s(jceInputStream.read(this.fm, 4, true));
        t(jceInputStream.read(this.fn, 5, false));
        if (fv == null) {
            fv = new ArrayList();
            fv.add(new en());
        }
        d((ArrayList) jceInputStream.read(fv, 6, false));
        u(jceInputStream.read(this.fp, 7, false));
        if (fw == null) {
            fw = new ArrayList();
            fw.add(new fb());
        }
        e((ArrayList) jceInputStream.read(fw, 8, false));
        setComment(jceInputStream.readString(9, false));
        v(jceInputStream.read(this.fs, 10, false));
        a(jceInputStream.read(this.dp, 11, false));
    }

    public void s(int i) {
        this.fm = i;
    }

    public void setComment(String str) {
        this.fr = str;
    }

    public void t(int i) {
        this.fn = i;
    }

    public void t(String str) {
        this.sender = str;
    }

    public void u(int i) {
        this.fp = i;
    }

    public void u(String str) {
        this.sms = str;
    }

    public void v(int i) {
        this.fs = i;
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.sender, 0);
        jceOutputStream.write(this.sms, 1);
        jceOutputStream.write(this.fk, 2);
        jceOutputStream.write(this.fl, 3);
        jceOutputStream.write(this.fm, 4);
        jceOutputStream.write(this.fn, 5);
        if (this.fo != null) {
            jceOutputStream.write(this.fo, 6);
        }
        jceOutputStream.write(this.fp, 7);
        if (this.fq != null) {
            jceOutputStream.write(this.fq, 8);
        }
        if (this.fr != null) {
            jceOutputStream.write(this.fr, 9);
        }
        jceOutputStream.write(this.fs, 10);
        jceOutputStream.write(this.dp, 11);
    }
}
