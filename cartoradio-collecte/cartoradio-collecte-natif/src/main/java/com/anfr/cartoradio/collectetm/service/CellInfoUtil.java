package com.anfr.cartoradio.collectetm.service;

import android.content.ContentResolver;
import android.provider.Settings;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CellInfoUtil {

    private static final String WCDMA = "wcdma";
    private static final String GSM = "gsm";
    private static final String LTE = "lte";

    public String getUuid(ContentResolver contentResolver) {
        return Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID);
    }

    public String getModel() {
        return android.os.Build.MODEL;
    }

    public String getProductName() {
        return android.os.Build.PRODUCT;
    }

    public String getManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    public String getSerialNumber() {
        return android.os.Build.SERIAL;
    }

    public String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public int getSDKVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public String getTimeZoneID() {
        return TimeZone.getDefault().getID();
    }

    public boolean isVirtual() {
        return android.os.Build.FINGERPRINT.contains("generic") || android.os.Build.PRODUCT.contains("sdk");
    }

    private static Integer valdiateDbm(Integer dbm) {
        if (dbm == null) {
            return null;
        }
        return dbm < 0 ? dbm : -dbm;
    }

    //Récupérer la meilleure force de signal
    public Integer getBestDbm(List<CommonCellInfo> cellInfoList) {
        if (cellInfoList == null || cellInfoList.isEmpty()) {
            return null;
        }
        Integer res = null;
        for (CommonCellInfo cellInfo : cellInfoList) {
            Integer currentDbm = valdiateDbm(cellInfo.getDbm());
            if (res == null || res < currentDbm) {
                res = currentDbm;
            }
        }
        return res;
    }

    public ArrayList<CommonCellInfo> toCellularInfo(List<CellInfo> cellInfoList) {
        final ArrayList<CommonCellInfo> res = new ArrayList<CommonCellInfo>();
        if (cellInfoList == null) {
            return res;
        }

        for (CellInfo cellInfo : cellInfoList) {
            if (!cellInfo.isRegistered()) {
                continue;
            }
            //Wcdma : Wideband Code Division Multiple Access : multiplexage par code à large bande
            if (cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                res.add(toCellularInfo(cellInfoWcdma));
            }

            if (cellInfo instanceof CellInfoGsm) {
                final CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                res.add(toCellularInfo(cellInfoGsm));
            }

            if (cellInfo instanceof CellInfoLte) {
                final CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                res.add(toCellularInfo(cellInfoLte));
            }

            // TODO: gérer CDMA
        }
        return res;
    }

    private static CommonCellInfo toCellularInfo(CellInfoWcdma cellInfo) {
        CommonCellInfo res = new CommonCellInfo();
        CellIdentityWcdma identityWcdma = cellInfo.getCellIdentity();
        CellSignalStrength signalStrength = cellInfo.getCellSignalStrength();

        res.setType(WCDMA);

        res.setCid(identityWcdma.getCid());
        res.setLac(identityWcdma.getLac());
        res.setMcc(identityWcdma.getMcc());
        res.setMnc(identityWcdma.getMnc());
        res.setDbm(valdiateDbm(signalStrength.getDbm()));
        res.setLevel(signalStrength.getLevel());
        return res;
    }

    private static CommonCellInfo toCellularInfo(CellInfoGsm cellInfo) {

        CommonCellInfo res = new CommonCellInfo();
        CellIdentityGsm identityGsm = cellInfo.getCellIdentity();
        CellSignalStrength signalStrength = cellInfo.getCellSignalStrength();

        res.setType(GSM);

        res.setCid(identityGsm.getCid());
        res.setLac(identityGsm.getLac());
        res.setMcc(identityGsm.getMcc());
        res.setMnc(identityGsm.getMnc());
        res.setDbm(valdiateDbm(signalStrength.getDbm()));
        res.setLevel(signalStrength.getLevel());
        return res;
    }

    private static CommonCellInfo toCellularInfo(CellInfoLte cellInfo) {

        CommonCellInfo res = new CommonCellInfo();
        CellIdentityLte identityLte = cellInfo.getCellIdentity();
        CellSignalStrength signalStrength = cellInfo.getCellSignalStrength();

        res.setType(LTE);

        res.setCid(identityLte.getCi());
        res.setLac(identityLte.getTac());
        res.setMcc(identityLte.getMcc());
        res.setMnc(identityLte.getMnc());
        res.setDbm(valdiateDbm(signalStrength.getDbm()));
        res.setLevel(signalStrength.getLevel());
        return res;
    }

    private static CommonCellInfo toCellularInfo(CellInfoCdma cellInfo) {

        CommonCellInfo res = new CommonCellInfo();
        CellIdentityCdma identityCdma = cellInfo.getCellIdentity();
        CellSignalStrength signalStrength = cellInfo.getCellSignalStrength();

        return res;
    }
}