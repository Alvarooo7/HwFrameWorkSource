package tmsdk.common.module.qscanner.impl;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class h extends JceStruct {
    static Map<Integer, ArrayList<byte[]>> hu = new HashMap();
    public int eB = 0;
    public Map<Integer, ArrayList<byte[]>> hs = null;
    public int ht = 0;
    public int version = 2;

    static {
        Object -l_0_R = Integer.valueOf(0);
        Object -l_1_R = new ArrayList();
        byte[] -l_2_R = (byte[]) new byte[1];
        -l_2_R[0] = (byte) 0;
        -l_1_R.add(-l_2_R);
        hu.put(-l_0_R, -l_1_R);
    }

    public JceStruct newInit() {
        return new h();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.version = jceInputStream.read(this.version, 0, true);
        this.hs = (Map) jceInputStream.read(hu, 1, false);
        this.ht = jceInputStream.read(this.ht, 2, false);
        this.eB = jceInputStream.read(this.eB, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.version, 0);
        if (this.hs != null) {
            jceOutputStream.write(this.hs, 1);
        }
        if (this.ht != 0) {
            jceOutputStream.write(this.ht, 2);
        }
        if (this.eB != 0) {
            jceOutputStream.write(this.eB, 3);
        }
    }
}
