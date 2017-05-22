package com.anfr.cartoradio.collectetm.service;

import android.os.Parcel;
import android.os.Parcelable;

public class CommonCellInfo implements Parcelable {

    private String type;
    private Integer cid;
    private Integer lac;
    private Integer mcc;
    private Integer mnc;
    private Integer dbm;
    private Integer level;

    public CommonCellInfo() {
    }

    protected CommonCellInfo(Parcel in) {
        type = in.readString();
    }

    public static final Creator<CommonCellInfo> CREATOR = new Creator<CommonCellInfo>() {
        @Override
        public CommonCellInfo createFromParcel(Parcel in) {
            return new CommonCellInfo(in);
        }

        @Override
        public CommonCellInfo[] newArray(int size) {
            return new CommonCellInfo[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public Integer getLac() {
        return lac;
    }

    public void setLac(Integer lac) {
        this.lac = lac;
    }

    public Integer getMcc() {
        return mcc;
    }

    public void setMcc(Integer mcc) {
        this.mcc = mcc;
    }

    public Integer getMnc() {
        return mnc;
    }

    public void setMnc(Integer mnc) {
        this.mnc = mnc;
    }

    public Integer getDbm() {
        return dbm;
    }

    public void setDbm(Integer dbm) {
        this.dbm = dbm;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(type);
        dest.writeInt(cid);
        dest.writeInt(lac);
        dest.writeInt(mcc);
        dest.writeInt(mnc);
        dest.writeInt(dbm);
        dest.writeInt(level);
    }
}
