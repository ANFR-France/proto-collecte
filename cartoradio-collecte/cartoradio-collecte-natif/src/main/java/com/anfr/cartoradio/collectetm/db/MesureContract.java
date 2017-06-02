package com.anfr.cartoradio.collectetm.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import com.anfr.cartoradio.collectetm.service.CommonCellInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public final class MesureContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private MesureContract() {
    }

    /* Inner class that defines the table contents */
    public static class Mesure implements BaseColumns, Parcelable {
        public static final String TABLE_NAME = "mesure";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_PRECISION_POSITION = "precision_position";
        public static final String COLUMN_NAME_DATE_MESURE = "date_mesure";
        public static final String COLUMN_NAME_TYPE_POSITION = "type_position";
        public static final String COLUMN_NAME_TM_TYPE = "tm_type";
        public static final String COLUMN_NAME_TM_CID = "tm_cid";
        public static final String COLUMN_NAME_TM_LAC = "tm_lac";
        public static final String COLUMN_NAME_TM_MCC = "tm_mcc";
        public static final String COLUMN_NAME_TM_MNC = "tm_mnc";
        public static final String COLUMN_NAME_TM_DBM = "tm_dbm";
        public static final String COLUMN_NAME_TM_LEVEL = "tm_level";
        public static final String COLUMN_NAME_GPS_NUM = "gps_num";
        public static final String COLUMN_NAME_GPS_SNR = "gps_snr";
        public static final String COLUMN_NAME_APPAREIL = "appareil";
        public static final String COLUMN_NAME_PARCOURS_ID = "parcours_id";

        private UUID id;
        private Double longitude;
        private Double latitude;
        private Integer precisionPosition;
        private Integer dateMesure;
        private String typePosition;
        private String tmType;
        private Integer tmCid;
        private Integer tmLac;
        private Integer tmMcc;
        private Integer tmMnc;
        private Integer tmDbm;
        private Integer tmLevel;
        private Integer gpsNum;
        private Integer gpsSnr;
        private String appareil;
        private UUID parcoursId;

        public Mesure() {
        }

        protected Mesure(Parcel in) {
            int tmp;
            id = UUID.fromString(in.readString());
            longitude = in.readDouble();
            latitude = in.readDouble();
            precisionPosition = in.readInt();
            dateMesure = in.readInt();
            typePosition = in.readString();
            tmType = in.readString();
            tmCid = in.readInt();
            tmLac = in.readInt();
            tmMcc = in.readInt();
            tmMnc = in.readInt();
            tmDbm = in.readInt();
            tmLevel = in.readInt();
            tmp = in.readInt();
            gpsNum = tmp == -1 ? null : tmp;
            tmp = in.readInt();
            gpsSnr = tmp == -1 ? null : tmp;
            appareil = in.readString();
            String s = in.readString();
            parcoursId = s == null ? null : UUID.fromString(s);


            typePosition = in.readString();
            tmType = in.readString();
            appareil = in.readString();
        }

        public static final Creator<Mesure> CREATOR = new Creator<Mesure>() {
            @Override
            public Mesure createFromParcel(Parcel in) {
                return new Mesure(in);
            }

            @Override
            public Mesure[] newArray(int size) {
                return new Mesure[size];
            }
        };

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Integer getPrecisionPosition() {
            return precisionPosition;
        }

        public void setPrecisionPosition(Integer precisionPosition) {
            this.precisionPosition = precisionPosition;
        }

        public Integer getDateMesure() {
            return dateMesure;
        }

        public void setDateMesure(Integer dateMesure) {
            this.dateMesure = dateMesure;
        }

        public String getTypePosition() {
            return typePosition;
        }

        public void setTypePosition(String typePosition) {
            this.typePosition = typePosition;
        }

        public String getTmType() {
            return tmType;
        }

        public void setTmType(String tmType) {
            this.tmType = tmType;
        }

        public Integer getTmCid() {
            return tmCid;
        }

        public void setTmCid(Integer tmCid) {
            this.tmCid = tmCid;
        }

        public Integer getTmLac() {
            return tmLac;
        }

        public void setTmLac(Integer tmLac) {
            this.tmLac = tmLac;
        }

        public Integer getTmMcc() {
            return tmMcc;
        }

        public void setTmMcc(Integer tmMcc) {
            this.tmMcc = tmMcc;
        }

        public Integer getTmMnc() {
            return tmMnc;
        }

        public void setTmMnc(Integer tmMnc) {
            this.tmMnc = tmMnc;
        }

        public Integer getTmDbm() {
            return tmDbm;
        }

        public void setTmDbm(Integer tmDbm) {
            this.tmDbm = tmDbm;
        }

        public Integer getTmLevel() {
            return tmLevel;
        }

        public void setTmLevel(Integer tmLevel) {
            this.tmLevel = tmLevel;
        }

        public Integer getGpsNum() {
            return gpsNum;
        }

        public void setGpsNum(Integer gpsNum) {
            this.gpsNum = gpsNum;
        }

        public Integer getGpsSnr() {
            return gpsSnr;
        }

        public void setGpsSnr(Integer gpsSnr) {
            this.gpsSnr = gpsSnr;
        }

        public String getAppareil() {
            return appareil;
        }

        public void setAppareil(String appareil) {
            this.appareil = appareil;
        }

        public UUID getParcoursId() {
            return parcoursId;
        }

        public void setParcoursId(UUID parcoursId) {
            this.parcoursId = parcoursId;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id.toString());
            dest.writeDouble(longitude);
            dest.writeDouble(latitude);
            dest.writeInt(precisionPosition);
            dest.writeInt(dateMesure);
            dest.writeString(typePosition);
            dest.writeString(tmType);
            dest.writeInt(tmCid);
            dest.writeInt(tmLac);
            dest.writeInt(tmMcc);
            dest.writeInt(tmMnc);
            dest.writeInt(tmDbm);
            dest.writeInt(tmLevel);
            dest.writeInt(gpsNum == null ? -1 : gpsNum);
            dest.writeInt(gpsSnr == null ? -1 : gpsSnr);
            dest.writeString(appareil);
            dest.writeString(parcoursId == null ? null : parcoursId.toString());
        }
    }

    public static Callable<List<Mesure>> listParcours(final SQLiteDatabase db, final UUID parcoursID) {
        return new Callable<List<Mesure>>() {
            @Override
            public List<Mesure> call() {
                Cursor c = db.rawQuery("select " +
                                "m." + Mesure._ID + ", " +
                                "m." + Mesure.COLUMN_NAME_LONGITUDE + ", " +
                                "m." + Mesure.COLUMN_NAME_LATITUDE + ", " +
                                "m." + Mesure.COLUMN_NAME_PRECISION_POSITION + ", " +
                                "m." + Mesure.COLUMN_NAME_DATE_MESURE + ", " +
                                "m." + Mesure.COLUMN_NAME_TYPE_POSITION + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_TYPE + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_CID + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_LAC + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_MCC + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_MNC + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_DBM + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_LEVEL + ", " +
                                "m." + Mesure.COLUMN_NAME_GPS_NUM + ", " +
                                "m." + Mesure.COLUMN_NAME_GPS_SNR + ", " +
                                "m." + Mesure.COLUMN_NAME_APPAREIL + ", " +
                                "m." + Mesure.COLUMN_NAME_PARCOURS_ID + " " +
                                "from " + Mesure.TABLE_NAME + " m " +
                                "where m." + Mesure.COLUMN_NAME_PARCOURS_ID + " = '" + parcoursID.toString() + "'"
                        , null);
                List<Mesure> res = new ArrayList<>();
                while (c.moveToNext()) {
                    Mesure mesure = mesureFromCursor(c);

               //     Log.e("Mesure",String.valueOf(mesure.getParcoursId()));

                    res.add(mesure);
                }
                c.close();
                return res;
            }
        };
    }

    public static Callable<List<Mesure>> listMesures(final SQLiteDatabase db) {
        return new Callable<List<Mesure>>() {
            @Override
            public List<Mesure> call() {
                Cursor c = db.rawQuery("select " +
                                "m." + Mesure._ID + ", " +
                                "m." + Mesure.COLUMN_NAME_LONGITUDE + ", " +
                                "m." + Mesure.COLUMN_NAME_LATITUDE + ", " +
                                "m." + Mesure.COLUMN_NAME_PRECISION_POSITION + ", " +
                                "m." + Mesure.COLUMN_NAME_DATE_MESURE + ", " +
                                "m." + Mesure.COLUMN_NAME_TYPE_POSITION + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_TYPE + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_CID + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_LAC + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_MCC + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_MNC + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_DBM + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_LEVEL + ", " +
                                "m." + Mesure.COLUMN_NAME_GPS_NUM + ", " +
                                "m." + Mesure.COLUMN_NAME_GPS_SNR + ", " +
                                "m." + Mesure.COLUMN_NAME_APPAREIL + ", " +
                                "m." + Mesure.COLUMN_NAME_PARCOURS_ID + " " +
                                "from " + Mesure.TABLE_NAME + " m " +
                                "where m." + Mesure.COLUMN_NAME_PARCOURS_ID + " is null"
                        , null);
                List<Mesure> res = new ArrayList<>();
                while (c.moveToNext()) {
                    Mesure mesure = mesureFromCursor(c);
                    res.add(mesure);
                }
                c.close();
                return res;
            }
        };
    }

    public static Callable<Integer> countMesures(final SQLiteDatabase db) {
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                Cursor c = db.rawQuery("select count(*) " +
                                "from " + Mesure.TABLE_NAME + " m " +
                                "where m." + Mesure.COLUMN_NAME_PARCOURS_ID + " is null"
                        , null);
                int res;
                c.moveToNext();
                res = c.getInt(0);
                c.close();
                return res;
            }
        };
    }

    public static Callable<Integer> countMesures(final SQLiteDatabase db, final UUID parcoursId) {
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                Cursor c = db.rawQuery("select count(*) " +
                                "from " + Mesure.TABLE_NAME + " m " +
                                "where m." + Mesure.COLUMN_NAME_PARCOURS_ID + " = ?"
                        , new String[]{parcoursId.toString()});
                int res;
                c.moveToNext();
                res = c.getInt(0);
                c.close();
                return res;
            }
        };
    }

    public static Callable<List<Mesure>> listParcours(final SQLiteDatabase db) {
        return new Callable<List<Mesure>>() {
            @Override
            public List<Mesure> call() {
                Cursor c = db.rawQuery("select " +
                                "m." + Mesure._ID + ", " +
                                "m." + Mesure.COLUMN_NAME_LONGITUDE + ", " +
                                "m." + Mesure.COLUMN_NAME_LATITUDE + ", " +
                                "m." + Mesure.COLUMN_NAME_PRECISION_POSITION + ", " +
                                "m." + Mesure.COLUMN_NAME_DATE_MESURE + ", " +
                                "m." + Mesure.COLUMN_NAME_TYPE_POSITION + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_TYPE + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_CID + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_LAC + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_MCC + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_MNC + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_DBM + ", " +
                                "m." + Mesure.COLUMN_NAME_TM_LEVEL + ", " +
                                "m." + Mesure.COLUMN_NAME_GPS_NUM + ", " +
                                "m." + Mesure.COLUMN_NAME_GPS_SNR + ", " +
                                "m." + Mesure.COLUMN_NAME_APPAREIL + ", " +
                                "m." + Mesure.COLUMN_NAME_PARCOURS_ID + " " +
                                "from " + Mesure.TABLE_NAME + " m " +
                                "join " + ParcoursContract.Parcours.TABLE_NAME + " p on (p." + ParcoursContract.Parcours._ID + " = m." + Mesure.COLUMN_NAME_PARCOURS_ID + ") " +
                                "where p." + ParcoursContract.Parcours.COLUMN_NAME_DATE_FIN + " is null"
                        , null);
                List<Mesure> res = new ArrayList<>();
                while (c.moveToNext()) {
                    Mesure mesure = mesureFromCursor(c);
                    res.add(mesure);
                }
                c.close();
                return res;
            }
        };
    }

    @NonNull
    private static Mesure mesureFromCursor(Cursor c) {
        Mesure mesure = new Mesure();
        mesure.setId(UUID.fromString(c.getString(0)));
        mesure.setLongitude(c.getDouble(1));
        mesure.setLatitude(c.getDouble(2));
        mesure.setPrecisionPosition(c.getInt(3));
        mesure.setDateMesure(c.getInt(4));
        mesure.setTypePosition(c.getString(5));
        mesure.setTmType(c.getString(6));
        mesure.setTmCid(c.getInt(7));
        mesure.setTmLac(c.getInt(8));
        mesure.setTmMcc(c.getInt(9));
        mesure.setTmMnc(c.getInt(10));
        mesure.setTmDbm(c.getInt(11));
        mesure.setTmLevel(c.getInt(12));
        mesure.setGpsNum(c.isNull(13) ? null : c.getInt(13));
        mesure.setGpsSnr(c.isNull(14) ? null : c.getInt(14));
        mesure.setAppareil(c.getString(15));
        mesure.setParcoursId(c.isNull(16) ? null : UUID.fromString(c.getString(16)));
        return mesure;
    }

    public static Mesure fromInfos(CommonCellInfo commonCellInfo, Location location, Integer gpsNum, Integer gpsSnr, int dateMesure, UUID parcoursId) {
        Mesure mesure = new Mesure();
        mesure.setId(UUID.randomUUID());
        mesure.setLongitude(location.getLongitude());
        mesure.setLatitude(location.getLatitude());
        mesure.setPrecisionPosition((int) location.getAccuracy());
        mesure.setDateMesure(dateMesure);
        mesure.setTypePosition(location.getProvider());
        mesure.setTmType(commonCellInfo.getType());
        mesure.setTmCid(commonCellInfo.getCid());
        mesure.setTmLac(commonCellInfo.getLac());
        mesure.setTmMcc(commonCellInfo.getMcc());
        mesure.setTmMnc(commonCellInfo.getMnc());
        mesure.setTmDbm(commonCellInfo.getDbm());
        mesure.setTmLevel(commonCellInfo.getLevel());
        mesure.setGpsNum(gpsNum);
        mesure.setGpsSnr(gpsSnr);
        mesure.setAppareil(Build.MANUFACTURER + " " + Build.PRODUCT + " " + Build.MODEL);
        mesure.setParcoursId(parcoursId);
        return mesure;
    }

    public static void insert(final SQLiteDatabase db, Mesure mesure) {
        ContentValues values = new ContentValues();

        values.put(Mesure._ID, mesure.getId().toString());
        values.put(Mesure.COLUMN_NAME_LONGITUDE, mesure.getLongitude());
        values.put(Mesure.COLUMN_NAME_LATITUDE, mesure.getLatitude());
        values.put(Mesure.COLUMN_NAME_PRECISION_POSITION, mesure.getPrecisionPosition());
        values.put(Mesure.COLUMN_NAME_DATE_MESURE, mesure.getDateMesure());
        values.put(Mesure.COLUMN_NAME_TYPE_POSITION, mesure.getTypePosition());
        values.put(Mesure.COLUMN_NAME_TM_TYPE, mesure.getTmType());
        values.put(Mesure.COLUMN_NAME_TM_CID, mesure.getTmCid());
        values.put(Mesure.COLUMN_NAME_TM_LAC, mesure.getTmLac());
        values.put(Mesure.COLUMN_NAME_TM_MCC, mesure.getTmMcc());
        values.put(Mesure.COLUMN_NAME_TM_MNC, mesure.getTmMnc());
        values.put(Mesure.COLUMN_NAME_TM_DBM, mesure.getTmDbm());
        values.put(Mesure.COLUMN_NAME_TM_LEVEL, mesure.getTmLevel());
        if (mesure.getGpsNum() != null) {
            values.put(Mesure.COLUMN_NAME_GPS_NUM, mesure.getGpsNum());
        } else {
            values.putNull(Mesure.COLUMN_NAME_GPS_NUM);
        }
        if (mesure.getGpsSnr() != null) {
            values.put(Mesure.COLUMN_NAME_GPS_SNR, mesure.getGpsSnr());
        } else {
            values.putNull(Mesure.COLUMN_NAME_GPS_SNR);
        }
        values.put(Mesure.COLUMN_NAME_APPAREIL, mesure.getAppareil());
        if (mesure.getParcoursId() != null) {
            values.put(Mesure.COLUMN_NAME_PARCOURS_ID, mesure.getParcoursId().toString());
        } else {
            values.putNull(Mesure.COLUMN_NAME_PARCOURS_ID);
        }

        db.insert(Mesure.TABLE_NAME, null, values);
    }

}

