package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class DisplayEngineImageMetadataParam {
    public final byte[] confidence = new byte[16];
    public int faceNumber;
    public final short[] facePara = new short[4];
    public final byte[] flag = new byte[4];
    public int iso;
    public int makerNoteVersion;
    public int nightResult;
    public final byte[] reserved = new byte[48];
    public int sdResult;
    public int sdVersion;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != DisplayEngineImageMetadataParam.class) {
            return false;
        }
        DisplayEngineImageMetadataParam other = (DisplayEngineImageMetadataParam) otherObject;
        if (HidlSupport.deepEquals(this.flag, other.flag) && this.makerNoteVersion == other.makerNoteVersion && this.sdVersion == other.sdVersion && this.sdResult == other.sdResult && this.faceNumber == other.faceNumber && HidlSupport.deepEquals(this.facePara, other.facePara) && this.nightResult == other.nightResult && this.iso == other.iso && HidlSupport.deepEquals(this.confidence, other.confidence) && HidlSupport.deepEquals(this.reserved, other.reserved)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.flag)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.makerNoteVersion))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sdVersion))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sdResult))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.faceNumber))), Integer.valueOf(HidlSupport.deepHashCode(this.facePara)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.nightResult))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iso))), Integer.valueOf(HidlSupport.deepHashCode(this.confidence)), Integer.valueOf(HidlSupport.deepHashCode(this.reserved))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".flag = ");
        builder.append(Arrays.toString(this.flag));
        builder.append(", .makerNoteVersion = ");
        builder.append(this.makerNoteVersion);
        builder.append(", .sdVersion = ");
        builder.append(this.sdVersion);
        builder.append(", .sdResult = ");
        builder.append(this.sdResult);
        builder.append(", .faceNumber = ");
        builder.append(this.faceNumber);
        builder.append(", .facePara = ");
        builder.append(Arrays.toString(this.facePara));
        builder.append(", .nightResult = ");
        builder.append(this.nightResult);
        builder.append(", .iso = ");
        builder.append(this.iso);
        builder.append(", .confidence = ");
        builder.append(Arrays.toString(this.confidence));
        builder.append(", .reserved = ");
        builder.append(Arrays.toString(this.reserved));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(100), 0);
    }

    public static final ArrayList<DisplayEngineImageMetadataParam> readVectorFromParcel(HwParcel parcel) {
        ArrayList<DisplayEngineImageMetadataParam> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 100), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            DisplayEngineImageMetadataParam _hidl_vec_element = new DisplayEngineImageMetadataParam();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 100));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.copyToInt8Array(0 + _hidl_offset, this.flag, 4);
        this.makerNoteVersion = _hidl_blob.getInt32(4 + _hidl_offset);
        this.sdVersion = _hidl_blob.getInt32(8 + _hidl_offset);
        this.sdResult = _hidl_blob.getInt32(12 + _hidl_offset);
        this.faceNumber = _hidl_blob.getInt32(16 + _hidl_offset);
        _hidl_blob.copyToInt16Array(20 + _hidl_offset, this.facePara, 4);
        this.nightResult = _hidl_blob.getInt32(28 + _hidl_offset);
        this.iso = _hidl_blob.getInt32(32 + _hidl_offset);
        _hidl_blob.copyToInt8Array(36 + _hidl_offset, this.confidence, 16);
        _hidl_blob.copyToInt8Array(52 + _hidl_offset, this.reserved, 48);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(100);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<DisplayEngineImageMetadataParam> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 100);
        while (_hidl_index_0 < _hidl_vec_size) {
            ((DisplayEngineImageMetadataParam) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 100));
            _hidl_index_0++;
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8Array(0 + _hidl_offset, this.flag);
        _hidl_blob.putInt32(4 + _hidl_offset, this.makerNoteVersion);
        _hidl_blob.putInt32(8 + _hidl_offset, this.sdVersion);
        _hidl_blob.putInt32(12 + _hidl_offset, this.sdResult);
        _hidl_blob.putInt32(16 + _hidl_offset, this.faceNumber);
        _hidl_blob.putInt16Array(20 + _hidl_offset, this.facePara);
        _hidl_blob.putInt32(28 + _hidl_offset, this.nightResult);
        _hidl_blob.putInt32(32 + _hidl_offset, this.iso);
        _hidl_blob.putInt8Array(36 + _hidl_offset, this.confidence);
        _hidl_blob.putInt8Array(52 + _hidl_offset, this.reserved);
    }
}
