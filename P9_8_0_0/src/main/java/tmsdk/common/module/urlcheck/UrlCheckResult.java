package tmsdk.common.module.urlcheck;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdkobf.ez;

public class UrlCheckResult implements Parcelable {
    public static Creator<UrlCheckResult> CREATOR = new Creator<UrlCheckResult>() {
        public UrlCheckResult[] bM(int i) {
            return new UrlCheckResult[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return q(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return bM(i);
        }

        public UrlCheckResult q(Parcel parcel) {
            Object -l_2_R = new UrlCheckResult();
            -l_2_R.mUrl = parcel.readString();
            -l_2_R.mainHarmId = parcel.readInt();
            -l_2_R.result = parcel.readInt();
            -l_2_R.mErrCode = parcel.readInt();
            -l_2_R.mEvilType = parcel.readInt();
            return -l_2_R;
        }
    };
    public static final int EVIL_CHEAT1 = 1;
    public static final int EVIL_CHEAT2 = 2;
    public static final int EVIL_FAULTSALES = 3;
    public static final int EVIL_ILLEGALCONTENT = 8;
    public static final int EVIL_LOTTERYWEB = 5;
    public static final int EVIL_MALICEFILE = 4;
    public static final int EVIL_NOEVIL = 0;
    public static final int EVIL_RISKWEB = 7;
    public static final int EVIL_SEXYWEB = 6;
    public static final int RESULT_HARM = 3;
    public static final int RESULT_REGULAR = 0;
    public static final int RESULT_SHADINESS = 2;
    public static final int RESULT_UNKNOWN = Integer.MAX_VALUE;
    public int mErrCode;
    public int mEvilType;
    public String mUrl;
    public int mainHarmId;
    public int result;

    private UrlCheckResult() {
        this.mainHarmId = Integer.MAX_VALUE;
        this.result = Integer.MAX_VALUE;
        this.mErrCode = 0;
        this.mEvilType = 0;
    }

    public UrlCheckResult(int i) {
        this.mainHarmId = Integer.MAX_VALUE;
        this.result = Integer.MAX_VALUE;
        this.mErrCode = 0;
        this.mEvilType = 0;
        this.mErrCode = i;
    }

    public UrlCheckResult(ez ezVar) {
        this.mainHarmId = Integer.MAX_VALUE;
        this.result = Integer.MAX_VALUE;
        this.mErrCode = 0;
        this.mEvilType = 0;
        this.mUrl = ezVar.url;
        this.mainHarmId = ezVar.mainHarmId;
        if (this.mainHarmId == 13) {
            this.mainHarmId = 0;
        }
        this.result = ezVar.lb;
        if (this.result == 1) {
            this.result = 0;
        }
        this.mEvilType = ezVar.d();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mUrl);
        parcel.writeInt(this.mainHarmId);
        parcel.writeInt(this.result);
        parcel.writeInt(this.mErrCode);
        parcel.writeInt(this.mEvilType);
    }
}
