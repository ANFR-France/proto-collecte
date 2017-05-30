package com.anfr.cartoradio.collectetm.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.anfr.cartoradio.collectetm.CollecteNotification;
import com.anfr.cartoradio.collectetm.Constants;
import com.anfr.cartoradio.collectetm.R;
import com.anfr.cartoradio.collectetm.SettingsActivity;
import com.anfr.cartoradio.collectetm.api.ApiManager;
import com.anfr.cartoradio.collectetm.db.CollecteOpenHelper;
import com.anfr.cartoradio.collectetm.db.MesureContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CollecteService extends IntentService
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "CollecteService";

    private GoogleApiClient mGoogleApiClient;
    private CellInfoUtil mCellInfoUtil = new CellInfoUtil();
    private SharedPreferences mSharedPref;
    private TelephonyManager mTelephonyMgr;
    private LocationManager mLocationManager;
    private ConnectivityManager mConnectivityMgr;
    private ArrayList<CommonCellInfo> mCellInfoList;
    private CollecteOpenHelper collecteOpenHelper = new CollecteOpenHelper(this);

    private Integer mCurrentSnrCount = null;
    private Integer mCurrentSnr = null;

    // Valeurs par défaut pour les settings
    private int collecteFrequencePref = 10;
    private boolean collecteActiverPref = false;

    public CollecteService() {
        super(TAG);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(collecteFrequencePref);
        locationRequest.setFastestInterval(collecteFrequencePref / 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (mSharedPref == null) {
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            mSharedPref.registerOnSharedPreferenceChangeListener(this);
            loadSettings();
        }

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
        } else {
            configureLocationConnection();
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
        if (mConnectivityMgr == null) {
            mConnectivityMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            mConnectivityMgr.addDefaultNetworkActiveListener(new ConnectivityManager.OnNetworkActiveListener() {
                @Override
                public void onNetworkActive() {
                    syncMesure();
                }
            });
        }

        // If we get killed, after returning from here, restart
        return START_REDELIVER_INTENT;
    }

    private void loadSettings() {
        collecteFrequencePref = Integer.parseInt(mSharedPref.getString(SettingsActivity.KEY_COLLECTE_FREQUENCE, "10")) * 1000;
        collecteActiverPref = mSharedPref.getBoolean(SettingsActivity.KEY_COLLECTE_ACTIVER, false);
        if (collecteActiverPref) {
            CollecteNotification.notify(this, 0);
            refreshNotificationStatus();
        } else {
            CollecteNotification.hide(this);
        }
    }

    private void refreshNotificationStatus() {
        if (!collecteActiverPref) {
            CollecteNotification.hide(CollecteService.this);
            return;
        }
        collecteOpenHelper
                .countMesures()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(final Integer count) {
                        CollecteNotification.notify(CollecteService.this, count);
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        configureLocationConnection();
    }

    private void configureLocationConnection() {
        LocationRequest locationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        if (collecteActiverPref) {
            com.google.android.gms.common.api.PendingResult<LocationSettingsResult> locationSettingsResultPendingResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            locationSettingsResultPendingResult
                    .setResultCallback(new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(LocationSettingsResult result) {
                            if (LocationSettingsStatusCodes.SUCCESS != result.getStatus().getStatusCode()) {
                                Intent localIntent = new Intent(Constants.GOOGLE_API).putExtra(Constants.GOOGLE_API_LOCATION_RESULT, result.getStatus());
                                LocalBroadcastManager.getInstance(CollecteService.this).sendBroadcast(localIntent);
                            }
                        }
                    });
            // noinspection MissingPermission : permissions dans le manifest
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            //noinspection MissingPermission
//            mLocationManager.addNmeaListener(new GpsStatus.NmeaListener() {
//                @Override
//                public void onNmeaReceived(long l, String s) {
//                    boolean a = false;
//                }
//            });
        } else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: faire quelque chose ?
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Intent localIntent = new Intent(Constants.GOOGLE_API).putExtra(Constants.GOOGLE_API_CONNECTION_RESULT, connectionResult);
        LocalBroadcastManager.getInstance(CollecteService.this).sendBroadcast(localIntent);
    }

    private boolean isConnected() {
        if (mConnectivityMgr.isDefaultNetworkActive()) {
            return true;
        }
        if (mConnectivityMgr.getActiveNetworkInfo() == null) {
            return false;
        }
        if (!mConnectivityMgr.getActiveNetworkInfo().isConnected()) {
            return false;
        }
        if (mConnectivityMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    private void syncMesure() {
        if (!isConnected()) {
            refreshNotificationStatus();
            return;
        }
        collecteOpenHelper
                .listMesures()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MesureContract.Mesure>>() {
                    @Override
                    public void call(final List<MesureContract.Mesure> mesures) {
                        Call<Void> call = ApiManager.getApiManager(CollecteService.this).postMesures(mesures);
                        try {
                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        collecteOpenHelper.deleteMesures(mesures);
                                        refreshNotificationStatus();
                                    } else {
                                        Toast.makeText(CollecteService.this, R.string.une_erreur_est_survenue, Toast.LENGTH_LONG).show();
                                        refreshNotificationStatus();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(CollecteService.this, R.string.une_erreur_est_survenue, Toast.LENGTH_LONG).show();
                                    refreshNotificationStatus();
                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(CollecteService.this, R.string.une_erreur_est_survenue, Toast.LENGTH_LONG).show();
                            refreshNotificationStatus();
                        }
                    }
                });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (collecteActiverPref) {
            int tsLong = (int) (System.currentTimeMillis() / 1000);
            mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());
            for (CommonCellInfo cellInfo : mCellInfoList) {
                collecteOpenHelper.insertMesure(MesureContract.fromInfos(cellInfo, location, mCurrentSnrCount, mCurrentSnr, tsLong, null));
            }
        }
        syncMesure();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (!SettingsActivity.KEY_COLLECTE_ACTIVER.equals(s) && !SettingsActivity.KEY_COLLECTE_FREQUENCE.equals(s)) {
            return;
        }
        loadSettings();
        configureLocationConnection();
    }
}
