package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class HighBitsDetailModeID {
    public static final int MODE_AGAINSTLIGHT = 65536;
    public static final int MODE_FOLIAGE = 262144;
    public static final int MODE_NIGHT = 131072;
    public static final int MODE_SKY = 196608;

    public static final String toString(int o) {
        if (o == 65536) {
            return "MODE_AGAINSTLIGHT";
        }
        if (o == 131072) {
            return "MODE_NIGHT";
        }
        if (o == MODE_SKY) {
            return "MODE_SKY";
        }
        if (o == MODE_FOLIAGE) {
            return "MODE_FOLIAGE";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0x");
        stringBuilder.append(Integer.toHexString(o));
        return stringBuilder.toString();
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 65536) == 65536) {
            list.add("MODE_AGAINSTLIGHT");
            flipped = 0 | 65536;
        }
        if ((o & 131072) == 131072) {
            list.add("MODE_NIGHT");
            flipped |= 131072;
        }
        if ((o & MODE_SKY) == MODE_SKY) {
            list.add("MODE_SKY");
            flipped |= MODE_SKY;
        }
        if ((o & MODE_FOLIAGE) == MODE_FOLIAGE) {
            list.add("MODE_FOLIAGE");
            flipped |= MODE_FOLIAGE;
        }
        if (o != flipped) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("0x");
            stringBuilder.append(Integer.toHexString((~flipped) & o));
            list.add(stringBuilder.toString());
        }
        return String.join(" | ", list);
    }
}
