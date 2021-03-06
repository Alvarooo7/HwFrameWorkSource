package com.huawei.nb.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ResourceSync extends AManagedObject {
    public static final Creator<ResourceSync> CREATOR = new Creator<ResourceSync>() {
        public ResourceSync createFromParcel(Parcel in) {
            return new ResourceSync(in);
        }

        public ResourceSync[] newArray(int size) {
            return new ResourceSync[size];
        }
    };
    private Long dataType;
    private String dbName;
    private Integer electricity;
    private Integer id;
    private Long isAllowOverWrite;
    private Long networkMode;
    private String remoteUrl;
    private String startTime;
    private String syncField;
    private Long syncMode;
    private Long syncPoint;
    private Long syncTime;
    private String tableName;
    private Integer tilingTime;

    public ResourceSync(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.tableName = cursor.getString(2);
        this.dbName = cursor.getString(3);
        this.syncMode = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.syncTime = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.syncPoint = cursor.isNull(6) ? null : Long.valueOf(cursor.getLong(6));
        this.remoteUrl = cursor.getString(7);
        this.dataType = cursor.isNull(8) ? null : Long.valueOf(cursor.getLong(8));
        this.isAllowOverWrite = cursor.isNull(9) ? null : Long.valueOf(cursor.getLong(9));
        this.networkMode = cursor.isNull(10) ? null : Long.valueOf(cursor.getLong(10));
        this.syncField = cursor.getString(11);
        this.startTime = cursor.getString(12);
        this.electricity = cursor.isNull(13) ? null : Integer.valueOf(cursor.getInt(13));
        if (!cursor.isNull(14)) {
            num = Integer.valueOf(cursor.getInt(14));
        }
        this.tilingTime = num;
    }

    public ResourceSync(Parcel in) {
        Integer num = null;
        super(in);
        if (in.readByte() == (byte) 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.tableName = in.readByte() == (byte) 0 ? null : in.readString();
        this.dbName = in.readByte() == (byte) 0 ? null : in.readString();
        this.syncMode = in.readByte() == (byte) 0 ? null : Long.valueOf(in.readLong());
        this.syncTime = in.readByte() == (byte) 0 ? null : Long.valueOf(in.readLong());
        this.syncPoint = in.readByte() == (byte) 0 ? null : Long.valueOf(in.readLong());
        this.remoteUrl = in.readByte() == (byte) 0 ? null : in.readString();
        this.dataType = in.readByte() == (byte) 0 ? null : Long.valueOf(in.readLong());
        this.isAllowOverWrite = in.readByte() == (byte) 0 ? null : Long.valueOf(in.readLong());
        this.networkMode = in.readByte() == (byte) 0 ? null : Long.valueOf(in.readLong());
        this.syncField = in.readByte() == (byte) 0 ? null : in.readString();
        this.startTime = in.readByte() == (byte) 0 ? null : in.readString();
        this.electricity = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        if (in.readByte() != (byte) 0) {
            num = Integer.valueOf(in.readInt());
        }
        this.tilingTime = num;
    }

    private ResourceSync(Integer id, String tableName, String dbName, Long syncMode, Long syncTime, Long syncPoint, String remoteUrl, Long dataType, Long isAllowOverWrite, Long networkMode, String syncField, String startTime, Integer electricity, Integer tilingTime) {
        this.id = id;
        this.tableName = tableName;
        this.dbName = dbName;
        this.syncMode = syncMode;
        this.syncTime = syncTime;
        this.syncPoint = syncPoint;
        this.remoteUrl = remoteUrl;
        this.dataType = dataType;
        this.isAllowOverWrite = isAllowOverWrite;
        this.networkMode = networkMode;
        this.syncField = syncField;
        this.startTime = startTime;
        this.electricity = electricity;
        this.tilingTime = tilingTime;
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
        setValue();
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        setValue();
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
        setValue();
    }

    public Long getSyncMode() {
        return this.syncMode;
    }

    public void setSyncMode(Long syncMode) {
        this.syncMode = syncMode;
        setValue();
    }

    public Long getSyncTime() {
        return this.syncTime;
    }

    public void setSyncTime(Long syncTime) {
        this.syncTime = syncTime;
        setValue();
    }

    public Long getSyncPoint() {
        return this.syncPoint;
    }

    public void setSyncPoint(Long syncPoint) {
        this.syncPoint = syncPoint;
        setValue();
    }

    public String getRemoteUrl() {
        return this.remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
        setValue();
    }

    public Long getDataType() {
        return this.dataType;
    }

    public void setDataType(Long dataType) {
        this.dataType = dataType;
        setValue();
    }

    public Long getIsAllowOverWrite() {
        return this.isAllowOverWrite;
    }

    public void setIsAllowOverWrite(Long isAllowOverWrite) {
        this.isAllowOverWrite = isAllowOverWrite;
        setValue();
    }

    public Long getNetworkMode() {
        return this.networkMode;
    }

    public void setNetworkMode(Long networkMode) {
        this.networkMode = networkMode;
        setValue();
    }

    public String getSyncField() {
        return this.syncField;
    }

    public void setSyncField(String syncField) {
        this.syncField = syncField;
        setValue();
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
        setValue();
    }

    public Integer getElectricity() {
        return this.electricity;
    }

    public void setElectricity(Integer electricity) {
        this.electricity = electricity;
        setValue();
    }

    public Integer getTilingTime() {
        return this.tilingTime;
    }

    public void setTilingTime(Integer tilingTime) {
        this.tilingTime = tilingTime;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.tableName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.tableName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dbName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.dbName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.syncMode != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.syncMode.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.syncTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.syncTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.syncPoint != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.syncPoint.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.remoteUrl != null) {
            out.writeByte((byte) 1);
            out.writeString(this.remoteUrl);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dataType != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.dataType.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isAllowOverWrite != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.isAllowOverWrite.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.networkMode != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.networkMode.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.syncField != null) {
            out.writeByte((byte) 1);
            out.writeString(this.syncField);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.startTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.startTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.electricity != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.electricity.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.tilingTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.tilingTime.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ResourceSync> getHelper() {
        return ResourceSyncHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.ResourceSync";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ResourceSync { id: ").append(this.id);
        sb.append(", tableName: ").append(this.tableName);
        sb.append(", dbName: ").append(this.dbName);
        sb.append(", syncMode: ").append(this.syncMode);
        sb.append(", syncTime: ").append(this.syncTime);
        sb.append(", syncPoint: ").append(this.syncPoint);
        sb.append(", remoteUrl: ").append(this.remoteUrl);
        sb.append(", dataType: ").append(this.dataType);
        sb.append(", isAllowOverWrite: ").append(this.isAllowOverWrite);
        sb.append(", networkMode: ").append(this.networkMode);
        sb.append(", syncField: ").append(this.syncField);
        sb.append(", startTime: ").append(this.startTime);
        sb.append(", electricity: ").append(this.electricity);
        sb.append(", tilingTime: ").append(this.tilingTime);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
