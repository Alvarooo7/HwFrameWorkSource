package tmsdkobf;

public class ns {
    private static nk DZ;
    private static nk Ea;
    private static b Eb;

    public static class a implements nk {
        private int Ec = 1;
        private Object mLock = new Object();

        public int fP() {
            int -l_2_I;
            synchronized (this.mLock) {
                -l_2_I = this.Ec;
                this.Ec++;
            }
            return -l_2_I;
        }
    }

    public static class b {
        private byte Ed = (byte) 0;
        private final Object mLock = new Object();

        public byte fZ() {
            byte b;
            synchronized (this.mLock) {
                if (this.Ed + 1 == 127) {
                    this.Ed = (byte) 0;
                }
                b = (byte) (this.Ed + 1);
                this.Ed = (byte) b;
            }
            return b;
        }
    }

    public static nk fW() {
        if (DZ == null) {
            Object -l_0_R = ns.class;
            synchronized (ns.class) {
                if (DZ == null) {
                    DZ = new a();
                }
            }
        }
        return DZ;
    }

    public static nk fX() {
        if (Ea == null) {
            Object -l_0_R = ns.class;
            synchronized (ns.class) {
                if (Ea == null) {
                    Ea = new a();
                }
            }
        }
        return Ea;
    }

    public static b fY() {
        if (Eb == null) {
            Object -l_0_R = ns.class;
            synchronized (ns.class) {
                if (Eb == null) {
                    Eb = new b();
                }
            }
        }
        return Eb;
    }
}
