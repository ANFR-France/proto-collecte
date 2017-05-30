package com.anfr.cartoradio.collectetm.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.anfr.cartoradio.collectetm.Constants;
import com.anfr.cartoradio.collectetm.MainActivity;
import com.anfr.cartoradio.collectetm.ParcoursNotification;
import com.anfr.cartoradio.collectetm.db.CollecteOpenHelper;
import com.anfr.cartoradio.collectetm.db.MesureContract;
import com.anfr.cartoradio.collectetm.db.ParcoursContract;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


public class ParcoursService extends IntentService
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "ParcoursService";
    public static final int LOCATION_INTERVAL = 10 * 1000;


    public static final String PARAM_PARCOURS_ID = "com.anfr.cartoradio.collectetm.parcours.ID";

    private GoogleApiClient mGoogleApiClient;
    private CellInfoUtil mCellInfoUtil = new CellInfoUtil();
    private TelephonyManager mTelephonyMgr;
    private LocationManager mLocationManager;
    private ArrayList<CommonCellInfo> mCellInfoList;
    private CollecteOpenHelper mCollecteOpenHelper = new CollecteOpenHelper(this);
    private UUID mParcoursId;
    private FinishParcoursReceiver mFinishParcoursReceiver;

    private Integer mCurrentSnrCount = null;
    private Integer mCurrentSnr = null;

    public ParcoursService() {
        super(TAG);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ParcoursNotification.notify(this, null, 0);
        refreshNotificationStatus();

        if (intent == null) {
            // XXX: étrange, ne devrait pas arriver
            stopSelf();
        }

        mParcoursId = UUID.fromString(intent.getStringExtra(PARAM_PARCOURS_ID));

        if (mFinishParcoursReceiver == null) {
            mFinishParcoursReceiver = new FinishParcoursReceiver();
        }
        IntentFilter mFinishParcoursIntentFilter = new IntentFilter(Constants.FINISH_PARCOURS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mFinishParcoursReceiver, mFinishParcoursIntentFilter);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        if (mTelephonyMgr == null) {
            mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());

            mTelephonyMgr.listen(new PhoneStateListener() {
                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    super.onSignalStrengthsChanged(signalStrength);
                    mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());
                }

                @Override
                public void onCellInfoChanged(List<CellInfo> cellInfo) {
                    super.onCellInfoChanged(cellInfo);
                    mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());
                }
            }, PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //noinspection MissingPermission
            mLocationManager.addGpsStatusListener(
                    new GpsStatus.Listener() {
                        @Override
                        public void onGpsStatusChanged(int event) {
                            switch (event) {
                                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                                    //noinspection MissingPermission
                                    GpsStatus xGpsStatus = mLocationManager.getGpsStatus(null);

                                    Iterable<GpsSatellite> iSatellites = xGpsStatus.getSatellites();
                                    Iterator<GpsSatellite> it = iSatellites.iterator();
                                    int count = 0;
                                    float snrSum = 0;
                                    while (it.hasNext()) {
                                        count = count + 1;
                                        GpsSatellite oSat = (GpsSatellite) it.next();
                                        snrSum += oSat.getSnr();
                                    }
                                    if (count == 0) {
                                        mCurrentSnrCount = null;
                                        mCurrentSnr = null;
                                    } else {
                                        mCurrentSnrCount = count;
                                        mCurrentSnr = Math.round(snrSum / count);
                                    }
                                    break;
                            }
                        }
                    }
            );
        }

        // If we get killed, after returning from here, restart
        return START_REDELIVER_INTENT;
    }

    private void refreshNotificationStatus() {
        mCollecteOpenHelper
                .getCurrentParcours()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ParcoursContract.Parcours>() {
                    @Override
                    public void call(final ParcoursContract.Parcours parcours) {
                        if (parcours == null) {
                            return;
                        }
                        mCollecteOpenHelper
                                .countMesuresParcours(parcours.getId())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Integer>() {
                                    @Override
                                    public void call(final Integer count) {
                                        ParcoursNotification.notify(ParcoursService.this, parcours, count);
                                    }
                                });
                    }
                });
    }

    // Broadcast receiver for receiving status updates from the IntentService
    private class FinishParcoursReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private FinishParcoursReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        configureLocationConnection();
    }

    private void configureLocationConnection() {
        LocationRequest locationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> locationSettingsResultPendingResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        locationSettingsResultPendingResult
                .setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        if (LocationSettingsStatusCodes.SUCCESS != result.getStatus().getStatusCode()) {
                            Intent localIntent = new Intent(Constants.GOOGLE_API).putExtra(Constants.GOOGLE_API_LOCATION_RESULT, result.getStatus());
                            LocalBroadcastManager.getInstance(ParcoursService.this).sendBroadcast(localIntent);
                        }
                    }
                });
        // noinspection MissingPermission : permissions dans le manifest
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: faire quelque chose ?
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Intent localIntent = new Intent(Constants.GOOGLE_API).putExtra(Constants.GOOGLE_API_CONNECTION_RESULT, connectionResult);
        LocalBroadcastManager.getInstance(ParcoursService.this).sendBroadcast(localIntent);
    }

    @Override
    public void onLocationChanged(Location location) {
        int tsLong = (int) (System.currentTimeMillis() / 1000);

        ArrayList<MesureContract.Mesure> mesures = new ArrayList<>();
        mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());
        for (CommonCellInfo cellInfo : mCellInfoList) {
            MesureContract.Mesure mesure = MesureContract.fromInfos(cellInfo, location, mCurrentSnrCount, mCurrentSnr, tsLong, mParcoursId);
            mesures.add(mesure);
            mCollecteOpenHelper.insertMesure(mesure);
        }

        refreshNotificationStatus();
        Intent localIntent = new Intent(Constants.PARCOURS_BROADCAST_GET_ACTION)
                .putParcelableArrayListExtra(Constants.PARCOURS_DATA_MESURE, mesures);
        LocalBroadcastManager.getInstance(ParcoursService.this).sendBroadcast(localIntent);
    }

    @Override
    public void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        ParcoursNotification.hide(this);
    }
}
