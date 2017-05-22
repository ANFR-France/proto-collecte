package com.anfr.cartoradio.collectetm;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anfr.cartoradio.collectetm.db.CollecteOpenHelper;
import com.anfr.cartoradio.collectetm.db.MesureContract;
import com.anfr.cartoradio.collectetm.db.ParcoursContract;
import com.anfr.cartoradio.collectetm.service.CellInfoUtil;
import com.anfr.cartoradio.collectetm.service.CollecteService;
import com.anfr.cartoradio.collectetm.service.CommonCellInfo;
import com.anfr.cartoradio.collectetm.service.ParcoursService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.text.Text;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    public static final int LOCATION_REFRESH_INTERVAL = 10 * 1000;
    public static final int DEFAULT_ZOOM_LEVEL = 15;
    public static final String CAMERA_POSITION = "com.anfr.cartoradio.collectetm.CAMERA_POSITION";
    public static final String IS_TRACKING = "com.anfr.cartoradio.collectetm.IS_TRACKING";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;


    private Intent mCollecteServiceIntent;
    private Intent mParcoursServiceIntent;

    private CellInfoUtil mCellInfoUtil = new CellInfoUtil();
    private TelephonyManager mTelephonyMgr;
    private LocationManager mLocationManager;
    private PhoneStateListener mPhoneStateListener;
    private ArrayList<CommonCellInfo> mCellInfoList;
    private GoogleMap mMap;
    private Marker marker;
    private List<Marker> mMarkers = new ArrayList<>();
    private boolean mIsTrackingPosition = true;
    private boolean mMapIsTouched;
    private GoogleApiClient mGoogleApiClient;
    private LocationResponseReceiver mLocationReceiver;
    private GoogleApiResponseReceiver mGoogleApiReceiver;
    private final CollecteOpenHelper collecteOpenHelper = new CollecteOpenHelper(this);
    private FloatingActionButton parcoursFab;
    private ParcoursContract.Parcours mCurrentParcours;
    private CameraPosition mInitialCameraPosition;

    private Integer mCurrentSnrCount = null;
    private Integer mCurrentSnr = null;
    private GpsStatus.Listener mGpsStatusListener;
    private GnssStatus.Callback mGnsStatus;

    private TextView tv_coord;
    private FloatingActionButton float_picture;
    private ClusterManager<MyItem> mClusterManager;
    HttpHandler sh = null;
    String jsonStr = null;
    double A,Ap,B,Bp,C,Cp,D,Dp;
    boolean requesting = false;
    boolean firstCall = true;
    LatLng currentLocation;
    Polyline line = null;
    MyItem itemSelected;


    public void loadDataRegion(double a,double ap,double b,double bp, double c, double cp, double d, double dp)
    {
        sh = new HttpHandler();
        List<String> operateur = new ArrayList<>();
        List<String> mise_service = new ArrayList<>();
        List<String> techno = new ArrayList<>();
        HttpHandler sh2 = new HttpHandler();
                /* sup_support */
               jsonStr = sh.makeServiceCall("https://data.anfr.fr/api/records/1.0/search/?dataset=sup_support&rows=9990&geofilter.polygon=("+String.valueOf(a)+"%2C"+String.valueOf(b)+")%2C("+String.valueOf(ap)+"%2C"+String.valueOf(bp)+")%2C("+String.valueOf(cp)+"%2C"+String.valueOf(dp)+")%2C("+String.valueOf(c)+"%2C"+String.valueOf(d)+")");
                // Log.e("TAG",String.valueOf(a)+"%2C"+String.valueOf(b)+")%2C("+String.valueOf(ap)+"%2C"+String.valueOf(bp)+")%2C("+String.valueOf(cp)+"%2C"+String.valueOf(dp));


        if(jsonStr != null)
        {

            try{
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONArray contatcs = jsonObj.getJSONArray("records");

                for(int i=0; i< contatcs.length(); i++)
                {

                    final JSONObject objs = contatcs.getJSONObject(i);
                    final JSONObject geo = objs.getJSONObject("geometry");
                    final JSONObject field = objs.getJSONObject("fields");

                    final String sup_id = field.getString("sup_id");
                    final JSONArray coor = geo.getJSONArray("coordinates");


                    //Get coordinate of Support
                    LatLng coordonnee = new LatLng((Double)coor.get(1),(Double)coor.get(0));

                    String jjsonStr = sh.makeServiceCall("https://data.anfr.fr/api/records/1.0/search/?dataset=observatoire_2g_3g_4g&refine.sup_id="+sup_id);
                    Log.e("Request","https://data.anfr.fr/api/records/1.0/search/?dataset=observatoire_2g_3g_4g&refine.sup_id="+sup_id);
                    if(jsonStr != null)
                    {
                        Log.e("Request","passage dans la deuxieme requete");
                        JSONObject jjsonObj = new JSONObject(jjsonStr);
                        JSONArray info_marker = jjsonObj.getJSONArray("records");

                        Log.e("Request:", String.valueOf(info_marker.length()));
                        List<Operateur> operateur_l = new ArrayList<Operateur>();

                        //Je préleve les operateurs
                        for(int j=0; j< info_marker.length(); j++)
                        {

                            final JSONObject obj = info_marker.getJSONObject(j);
                            final JSONObject ffield = obj.getJSONObject("fields");

                            if(!operateur.contains(ffield.getString("adm_lb_nom")))
                            {
                                operateur.add(ffield.getString("adm_lb_nom"));
                                operateur_l.add(new Operateur(ffield.getString("adm_lb_nom")));
                                Log.i("operateur",ffield.getString("adm_lb_nom"));
                            }

                            Log.e("Request info",ffield.getString("adm_lb_nom"));
                        }

                        //Je reparcours mais cette fois-ci je peux ranger dans les operateurs la technologie
                        for(int j=0; j< info_marker.length(); j++)
                        {
                            final JSONObject obj = info_marker.getJSONObject(j);
                            final JSONObject ffield = obj.getJSONObject("fields");

                            for(int k=0;k<operateur_l.size(); k++)
                            {
                                if(ffield.getString("adm_lb_nom").contains(operateur_l.get(k).getName()))
                                {
                                    if(!operateur_l.get(k).getTechno().contains(ffield.getString("generation"))){
                                        operateur_l.get(k).getTechno().add(ffield.getString("generation"));
                                        operateur_l.get(k).setSystemes_l(ffield.getString("emr_lb_systeme"),"test");

                                    }
                                }
                            }
                        }

                        //Affichage
                        for(int y=0; y<operateur_l.size(); y++)
                        {
                            Log.i("momoOPERATOR",operateur_l.get(y).getName());
                            for(int j=0; j < operateur_l.get(y).getTechno().size(); j++)
                            {
                                Log.i("momoTECHNO", operateur_l.get(y).getTechno().get(j).toString());
                            }

                            for(int v=0; v < operateur_l.get(y).getSystemes().size(); v++ )
                            {
                                Log.i("momoSYSTEME", operateur_l.get(y).getSystemes().get(v).toString()  );
                                Log.i("momoDATE", operateur_l.get(y).getSystemes().get(v).toString());
                            }
                        }

                        MyItem newItem = new MyItem(new LatLng((Double) coor.get(1), (Double) coor.get(0)),operateur_l);
                        mClusterManager.addItem(newItem);
                    }
                    else
                    {
                        Log.e("Request","passage dans la deuxieme NULL");
                    }


                  // MyItem newItem = new MyItem(new LatLng((Double) coor.get(1), (Double) coor.get(0)), operateur, mise_service, techno, sup_id);
                  // mClusterManager.addItem(newItem);


                    if(operateur.size() > 0) operateur.clear();
                    if(mise_service.size() > 0) mise_service.clear();
                    if(techno.size() > 0) techno.clear();
                }



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mClusterManager.setRenderer(new OwnRendring(getApplicationContext(),mMap,mClusterManager));
                        mClusterManager.cluster();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mInitialCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION);
        }

        setContentView(R.layout.activity_main);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TexView
        tv_coord = (TextView) findViewById(R.id.tv_coord);

        //Bouton pour prendre photo support
        float_picture = (FloatingActionButton) findViewById(R.id.float_picture);
        float_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dispatchTakePictureIntent();
                //setUpCluster();
                //getJSONsupport();
                //getMyCell();
                //info_sup fD = new info_sup(getApplicationContext());
                final Dialog dialog = new Dialog(getApplicationContext());
                dialog.setContentView(R.layout.info_sup);
                dialog.setTitle("Title...");
                dialog.show();
            }
        });

        //Bouton pour lancer le parcours
        parcoursFab = (FloatingActionButton) findViewById(R.id.activity_main_parcours_action);
        parcoursFab.hide();
        parcoursFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {

                if (mCurrentParcours != null) {
                    collecteOpenHelper.closeParcours();
                    loadCurrentParcours();
                    startActivity(new Intent(MainActivity.this, ParcoursActivity.class));
                    return;
                }

                LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                //On récupère la view de la dialog pour la set dans l'AlterDialog
                final View dialogContentView = inflater.inflate(R.layout.dialog_parcours, null);

                //On affiche que le parcours a bien été crée
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.dialog_titre_parcours)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                EditText titreInput = (EditText) dialogContentView.findViewById(R.id.titre_input);
                                if (titreInput.getText() == null || titreInput.getText().toString() == null || titreInput.getText().toString().isEmpty()) {
                                    return;
                                }
                                collecteOpenHelper.insertParcours(titreInput.getText().toString());
                                Snackbar.make(view, R.string.msg_parcours_cree, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                loadCurrentParcours();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setView(dialogContentView)
                        .create();

                //Listener pour activer ou pas le bouton OK (enable dès que l'utilisateur a commencé à taper un titre)
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialogInterface) {
                        dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                        final EditText titreInput = (EditText) dialogContentView.findViewById(R.id.titre_input);
                        titreInput.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                if (editable.length() == 0) {
                                    titreInput.setError(getString(R.string.msg_titre_obligatoire));
                                    dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                                } else {
                                    titreInput.setError(null);
                                    dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                                }
                            }
                        });

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(titreInput, InputMethodManager.SHOW_IMPLICIT);
                        titreInput.requestFocus();

                    }
                });
                dialog.show();
            }
        });

        //Preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.READ_PHONE_STATE,
                            android.Manifest.permission.ACCESS_NETWORK_STATE,
                            android.Manifest.permission.VIBRATE,
                            android.Manifest.permission.INTERNET
                    },
                    REQUEST_ALL_PERMISSIONS);
            return;
        }
    }

    //Initialisation des cluster Google map
    private void setUpCluster(){

        mClusterManager = new ClusterManager<MyItem>(this, mMap);
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                return false;
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
              //  Toast.makeText(MainActivity.this, myItem.getPosition().toString(), Toast.LENGTH_SHORT).show();
                itemSelected = myItem;
                return false;
            }
        });
        //mMap.setOnCameraIdleListener(mClusterManager);



    }

    //Methode qui permet de lancer la tâche asynchrone
    public void getJSONsupport()
    {
        //requesting = true;
        GetSuppoer objet = new GetSuppoer(A,Ap,B,Bp,C,Cp,D,Dp);
        objet.execute((String[]) null);
    }

    //Classe qui va réaliser une requête en asynchrone
    private class GetSuppoer extends  AsyncTask<String, Void, Void>{

        double Aa;
        double Aap;
        double Bb;
        double Bbp;
        double Cc;
        double Ccp;
        double Dd;
        double Ddp;

        public GetSuppoer(double a,double aa, double b, double bb, double c, double cc, double d, double dd)
        {
            Aa =a;
            Aap =aa;
            Bb = b;
            Bbp =bb;
            Cc = c;
            Ccp = cc;
            Dd = d;
            Ddp = dd;
        }
/*
        @Override
        protected Void doInBackground(Void... params) {
          loadDataRegion(Aa,Aap,Bb,Bbp,Cc,Ccp,Dd,Ddp);
          return null;
        }
*/
        @Override
        protected Void doInBackground(String... params) {
            Log.e("Request","debut thread");
            if(params != null)
            {
                sh = new HttpHandler();
                jsonStr =  sh.makeServiceCall(params[0]);
                Log.e("Request",params[0]);
                if(jsonStr != null) {
                    Log.e("Request result", "JSON non null");
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(jsonStr);
                        JSONArray contatcs = jsonObj.getJSONArray("records");

                        for (int i = 0; i < contatcs.length(); i++) {
                            final JSONObject objs = contatcs.getJSONObject(i);
                            final JSONObject field = objs.getJSONObject("fields");

                            final String operateur = field.getString("adm_lb_nom");
                            final String mise_service = field.getString("emr_dt_service");
                            final String techno = field.getString("emr_lb_systeme");
                            /*
                            itemSelected.setOperateur(operateur);
                            itemSelected.setMise_service(mise_service);
                            itemSelected.setTechno(techno); */
                            Log.e("Request result", field.toString());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, operateur, Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Log.e("Request result","JSON null");
                }
            }
            else
            {
                loadDataRegion(Aa,Aap,Bb,Bbp,Cc,Ccp,Dd,Ddp);
                Log.e("Request","null");

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            requesting = false;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!hasPermissions()) {
            return;
        }

        initMap();
        setUpCluster();

    }


    private void initMap() {

        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (mInitialCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mInitialCameraPosition));
        }

        //noinspection MissingPermission
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mIsTrackingPosition = true;
                return false;
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker markers) {
                // Getting view from the layout file info_window_layout
                if(!markers.equals(marker)) {
                    View v = getLayoutInflater().inflate(R.layout.info_sup, null);

                    // Getting the position from the marker
                    LatLng latLng = markers.getPosition();
                    TextView tv_mise_service = (TextView) v.findViewById(R.id.tv_mes);
                    TextView tv_operateur = (TextView) v.findViewById(R.id.tv_operateur);
                    TextView tv_technologie = (TextView) v.findViewById(R.id.tv_technologie);
                    String test = "";

                    for(int i=0;i<itemSelected.getList_operateur().size();i++)
                    {
                        String techno = "";

                            test += itemSelected.getList_operateur().get(i).getName() + "\n";
                            for(int j=0; j<itemSelected.getList_operateur().get(i).getTechno().size(); j++)
                            {
                                if(!techno.contains(itemSelected .getList_operateur().get(i).getTechno().get(j).toString())) {
                                    techno += itemSelected .getList_operateur().get(i).getTechno().get(j).toString() + (j+1 != itemSelected .getList_operateur().get(i).getTechno().size() ? "/" : "");
                                }
                            }
                            test+= techno + "\n";
                            //tv_technologie.append(test);

                    }

                    tv_operateur.append(test);
                    test = "";

                    for(int i=1; i<itemSelected.getMise_service().size(); i++)
                    {
                        test += itemSelected.getMise_service().get(i) + "\n";
                    }
                    tv_mise_service.setText(test);
                    test = "";

                    // Returning the view containing InfoWindow contents
                    return v;
                }

                return null;
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    mMapIsTouched = true;
                }

            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mMapIsTouched = false;
                //Toast.makeText(MainActivity.this, String.valueOf(mMap.getProjection().getVisibleRegion().farLeft.latitude)+","+String.valueOf(mMap.getProjection().getVisibleRegion().farLeft.longitude), Toast.LENGTH_SHORT).show();
                //Toast.makeText(MainActivity.this, "CANCELED", Toast.LENGTH_SHORT).show();
                if(mClusterManager != null)
                {
                    if(requesting == false && mMap.getCameraPosition().zoom >= 15)
                    {
                        Toast.makeText(MainActivity.this, "Request !", Toast.LENGTH_SHORT).show();
                        mClusterManager.clearItems();
                        mClusterManager.onCameraIdle();
                        requesting = true;
                        getJSONsupport();
                    }
                    else
                    {
                        mClusterManager.clearItems();
                        mClusterManager.cluster();
                        if(line != null)
                        {
                            line.remove();
                        }
                        Toast.makeText(MainActivity.this, "Zoomez pour voir les supports", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mClusterManager.onMarkerClick(marker);
               // marker.showInfoWindow();

                marker.showInfoWindow();
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_black_black));

             //   GetSuppoer objet = new GetSuppoer(0,0,0,0,1,2,3,4);
              //  objet.execute("https://data.anfr.fr/api/records/1.0/search/?dataset=observatoire_2g_3g_4g&refine.sup_id="+marker.getTitle());
                return true;
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (mMapIsTouched) {
                    mIsTrackingPosition = false;
                }
                A = mMap.getProjection().getVisibleRegion().farLeft.latitude;
                Ap = mMap.getProjection().getVisibleRegion().farRight.latitude;

                B = mMap.getProjection().getVisibleRegion().farLeft.longitude;
                Bp = mMap.getProjection().getVisibleRegion().farRight.longitude;

                C = mMap.getProjection().getVisibleRegion().nearLeft.latitude;
                Cp = mMap.getProjection().getVisibleRegion().nearRight.latitude;

                D = mMap.getProjection().getVisibleRegion().nearLeft.longitude;
                Dp = mMap.getProjection().getVisibleRegion().nearRight.longitude;

               // Toast.makeText(MainActivity.this, "Move", Toast.LENGTH_SHORT).show();
                Log.e("ZOOM",String.valueOf(mMap.getCameraPosition().zoom));
            }
        });


        // TODO: recharger les données si on a un parcours en cours
        refreshData();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //On construit l'objet GoogleAPI
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.e("espion onStat", String.valueOf(A));

        }


        //Lorsqu'on arrive dans onStart() on check si on est toujours connecté
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            Log.e("espion onStat", String.valueOf(A));

        } else {
            onConnected(null);
        }


        if (mLocationReceiver == null) {
            mLocationReceiver = new LocationResponseReceiver();
        }
        if (mGoogleApiReceiver == null) {
            mGoogleApiReceiver = new GoogleApiResponseReceiver();
        }

        //On filtre
        IntentFilter mLocationIntentFilter = new IntentFilter(Constants.PARCOURS_BROADCAST_GET_ACTION); //BROADCAST
        IntentFilter mGoogleApiIntentFilter = new IntentFilter(Constants.GOOGLE_API); //GOOGLE_API

        //On connect le filtre au receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationReceiver, mLocationIntentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(mGoogleApiReceiver, mGoogleApiIntentFilter);

        // Gestion de la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.activity_main_map);
        mapFragment.setMenuVisibility(true);
        mapFragment.getMapAsync(this);

        // Récupération des infos du parcours
        loadCurrentParcours();

        // Démarrage du service de collecte
        if (mCollecteServiceIntent == null) {
            mCollecteServiceIntent = new Intent(this, CollecteService.class);
            //startService(mCollecteServiceIntent);
        }

        //On écoute l'appareil
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                //On appel toCellular de la classe CellInfoUtil et on lui donne une List<CellInfo> pour qu'il puisse analyser si c'est du GSM, LTE, ...
                mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());
            }

            //Cette methode est appelé lorsque on detecte que une nouvelle cellule a été ajouté ou changé
            @Override
            public void onCellInfoChanged(List<CellInfo> cellInfo) {
                super.onCellInfoChanged(cellInfo);
                //On appel toCellular de la classe CellInfoUtil et on lui donne une List<CellInfo> pour qu'il puisse analyser si c'est du GSM, LTE, ...

                mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());
            }
        };

        //Permet d'avoir accès aux informations du téléphone
        if (mTelephonyMgr == null) {
            //I do not instantiate this class directly
            mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());
        }


        //mTelephonyMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        //Permet d'avoir accès au système de localisation
        if (mLocationManager == null) {
            //I do not instantiate this class directly
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        //Check des permissions
        if (hasPermissions()) {
            finishIntialisation();
        }
    }

    private class GoogleApiResponseReceiver extends BroadcastReceiver {

        private GoogleApiResponseReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Parcelable connectionResult = intent.getParcelableExtra(Constants.GOOGLE_API_CONNECTION_RESULT);
            Parcelable status = intent.getParcelableExtra(Constants.GOOGLE_API_LOCATION_RESULT);
            if (status != null) {
                handleLocationStatusResult((Status) status);
            } else if (connectionResult != null) {
                handleConnectionResult((ConnectionResult) connectionResult);
            }
        }

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Toast.makeText(this, "Thank you !", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishIntialisation() {
        // Démarrage du service de collecte
        startService(mCollecteServiceIntent);

        //On appel toCellular de la classe CellInfoUtil et on lui donne une List<CellInfo> pour qu'il puisse analyser si c'est du GSM, LTE, ...
        mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());

        /* Je commence à écouter, je place mon listener en 1er parametre puis les events à écouter */
        /* Ici on att un changement de Signal Strength ou d'information de cellule */
        mTelephonyMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


        //Récupérer l'Etat du/des GPS
        mGpsStatusListener = new GpsStatus.Listener() {
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

                        //Toast.makeText(MainActivity.this, "nombre de GPS: " + String.valueOf(count), Toast.LENGTH_SHORT).show();
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
        };
        //noinspection MissingPermission
        mLocationManager.addGpsStatusListener(mGpsStatusListener);

    }

    //Permet de check les permissions (return true or false)
    private boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // TODO: sauvegarder l'état de la carte
        if (mMap != null) {
            mInitialCameraPosition = mMap.getCameraPosition();
            savedInstanceState.putParcelable(CAMERA_POSITION, mInitialCameraPosition);
        }
        savedInstanceState.putBoolean(IS_TRACKING, mIsTrackingPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mInitialCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION);
            mIsTrackingPosition = savedInstanceState.getBoolean(IS_TRACKING);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }
        //On récupère les nouvelles coordonnées
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        tv_coord.setText(String.valueOf(currentLocation.latitude)+","+String.valueOf(currentLocation.longitude));

        //On appel toCellular de la classe CellInfoUtil et on lui donne une List<CellInfo> pour qu'il puisse analyser si c'est du GSM, LTE, ...
        mCellInfoList = mCellInfoUtil.toCellularInfo(mTelephonyMgr.getAllCellInfo());

        //On récupère la meilleur force de signal du signal
        Integer bestDbm = mCellInfoUtil.getBestDbm(mCellInfoList);
        if(marker != null)
        {
            marker.remove();
            marker = null;
        }

        //firstCall ==  true si marker == null; Sinon firstCall == false
        // boolean firstCall = marker == null;

        if (marker == null) {

            if (mMap != null) {
                marker = mMap.addMarker(new MarkerOptions()
                        .icon(Colors.bitmapForDbm(bestDbm, mCurrentSnr))
                        .title("TM : " + (bestDbm == null ? "-" : bestDbm) + " dBm ; " +
                                "GPS : " + (mCurrentSnr == null ? "-" : mCurrentSnr))
                        .anchor(0.5f, 0.5f)
                        .position(currentLocation));
                if(mClusterManager.getMarkerCollection().getMarkers().size() > 0) {
                    getMyCell();
                }

                Log.e("TM: ",(bestDbm == null ? "-" : bestDbm) + " dBm ; " +
                        "GPS : " + (mCurrentSnr == null ? "-" : mCurrentSnr));
            }


        } else {
            marker.setPosition(currentLocation);
            marker.setIcon(Colors.bitmapForDbm(bestDbm, mCurrentSnr));
            marker.setTitle("TM : " + (bestDbm == null ? "-" : bestDbm) + " dBm ; " +
                    "GPS : " + (mCurrentSnr == null ? "-" : mCurrentSnr));

            if (marker.isInfoWindowShown()) {
                marker.showInfoWindow();
            }



        }

        //Toast.makeText(MainActivity.this, String.valueOf(mMap.getProjection().getVisibleRegion().farLeft.latitude)+","+String.valueOf(mMap.getProjection().getVisibleRegion().farLeft.longitude), Toast.LENGTH_SHORT).show();

        // XXX: affichage de notre propre point de précision de la localisation ?
        //On bouge la camera pour être toujours au centre

        if (mIsTrackingPosition && mMap != null && !mMap.getCameraPosition().equals(currentLocation)) {
            if (firstCall) {
                //Toast.makeText(this, "FIRST CALL", Toast.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM_LEVEL));
                firstCall = false;
            } else {
                //Toast.makeText(this, "SECOND CALL", Toast.LENGTH_SHORT).show();
                // TODO: ajuster le zoom
               // mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
               // mMap.stopAnimation();
            }
        }
    }

    // Gestion du menu
    private void goToParcours() {
        Intent intent = new Intent(this, ParcoursActivity.class);
        startActivity(intent);
    }

    private void goToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                goToSettings();
                return true;
            case R.id.action_parcours:
                goToParcours();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Gestion des dialogues d'erreur
    private static final int REQUEST_ALL_PERMISSIONS = 1003;
    //    private static final int REQUEST_LOCATION_PERMISSION = 1002;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onErrorDialogDismissed() {
        mResolvingError = false;
    }

    //This method is invoked for every call on requestPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ALL_PERMISSIONS: {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.persmissions_non_accordees, Toast.LENGTH_LONG);
                        return;
                    }
                }

                finishIntialisation();
                initMap();
                // noinspection MissingPermission
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_REFRESH_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REFRESH_INTERVAL / 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> locationSettingsResultPendingResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        locationSettingsResultPendingResult
                .setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        if (LocationSettingsStatusCodes.SUCCESS != result.getStatus().getStatusCode()) {
                            if (result.getStatus().hasResolution()) {
                                handleLocationStatusResult(result.getStatus());
                            } else {
                                // TODO: faire quelque chose
                            }
                        }
                    }
                });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // la demande des droits est faite ailleurs
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTelephonyMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        //noinspection MissingPermission
        mLocationManager.removeGpsStatusListener(mGpsStatusListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGoogleApiReceiver);
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: faire quelque chose ?
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        handleConnectionResult(connectionResult);
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onErrorDialogDismissed();
        }
    }

    public Marker getMyCell() {
        if(line != null)
        {
            line.remove();
            line = null;
        }
        Marker marker = null;

        java.util.Collection<Marker> userCollection = mClusterManager.getMarkerCollection().getMarkers();
        ArrayList<Marker> userList = new ArrayList<Marker>(userCollection);

        if(userList.size() == 0) return null;

        double res = 100000000;
        for(Marker obj : userList)
        {
          //  Log.e("Check best membre: ", obj.getTitle());
            double distance = calculateDistanceInKilometer(obj.getPosition().latitude, obj.getPosition().longitude,currentLocation.latitude, currentLocation.longitude);
            Log.e("Best_ distance: ", String.valueOf(distance));
            if(distance < res)
            {
                //Log.e("Check Best: ", obj.getTitle());
                res = distance;
                marker = obj;
            }

        }
        if(marker != null && line == null) {
            line = mMap.addPolyline(new PolylineOptions()
                    .add(marker.getPosition(), currentLocation)
                    .width(10)
                    .color(Color.GREEN));
        }

      //  Log.d("Best marker: ", marker.getTitle());
        return marker;
    }


    public double calculateDistanceInKilometer(double userLat, double userLng,
                                            double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (double) (AVERAGE_RADIUS_OF_EARTH_KM * c);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResolvingError = false;
    }

    // Broadcast receiver for receiving status updates from the IntentService
    private class LocationResponseReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private LocationResponseReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {
            drawMesures(intent.<MesureContract.Mesure>getParcelableArrayListExtra(Constants.PARCOURS_DATA_MESURE));
        }
    }

    private void loadCurrentParcours() {
        collecteOpenHelper.getCurrentParcours()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ParcoursContract.Parcours>() {
                    @Override
                    public void call(ParcoursContract.Parcours parcours) {
                        mCurrentParcours = parcours;
                        if (parcours == null) {
                            parcoursFab.setImageResource(android.R.drawable.ic_media_play);
                            if (mParcoursServiceIntent != null) {
                                stopService(mParcoursServiceIntent);
                                mParcoursServiceIntent = null;
                                removeParcoursMarker();
                            }
                        } else {
                            parcoursFab.setImageResource(android.R.drawable.ic_media_pause);
                            if (mParcoursServiceIntent == null) {
                                mParcoursServiceIntent = new Intent(MainActivity.this, ParcoursService.class);
                                mParcoursServiceIntent.putExtra(ParcoursService.PARAM_PARCOURS_ID, parcours.getId().toString());
                                startService(mParcoursServiceIntent);
                            }
                        }
                        parcoursFab.show();
                    }
                });
    }

    private void refreshData() {
        collecteOpenHelper.listMesuresParcours()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MesureContract.Mesure>>() {
                    @Override
                    public void call(List<MesureContract.Mesure> mesures) {
                        removeParcoursMarker();
                        drawMesures(mesures);
                        mMap.clear();


                    }
                });
    }

    private void drawMesures(List<MesureContract.Mesure> mesures) {
        for (MesureContract.Mesure mesure : mesures) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .icon(Colors.bitmapForDbm(mesure.getTmDbm(), mesure.getGpsSnr()))
                    .anchor(0.5f, 0.5f)
                    .title(
                            "TM : " + (mesure.getTmDbm() == null ? "-" : mesure.getTmDbm()) + " dBm ; " +
                                    "GPS : " + (mesure.getGpsSnr() == null ? "-" : mesure.getGpsSnr())
                    )
                    .position(buildLatLng(mesure.getLatitude(), mesure.getLongitude())));
            mMarkers.add(marker);
        }
    }

    private void removeParcoursMarker() {
        for (Marker marker : mMarkers) {
            marker.remove();
        }
        mMarkers = new ArrayList<>();
    }

    private LatLng buildLatLng(Double lat, Double lng) {
        return new LatLng(lat, lng);
    }

    private Cursor getQuery() {
        return new CollecteOpenHelper(this).getReadableDatabase()
                .query(MesureContract.Mesure.TABLE_NAME,
                        new String[]{MesureContract.Mesure._ID,
                                MesureContract.Mesure.COLUMN_NAME_DATE_MESURE,
                                MesureContract.Mesure.COLUMN_NAME_LATITUDE,
                                MesureContract.Mesure.COLUMN_NAME_LONGITUDE,
                                MesureContract.Mesure.COLUMN_NAME_PRECISION_POSITION,
                                MesureContract.Mesure.COLUMN_NAME_TM_DBM,
                                MesureContract.Mesure.COLUMN_NAME_GPS_SNR
                        },
                        null, null, null, null, null, null);
    }



    private void handleConnectionResult(@NonNull ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        }
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                // TODO: gérer mResolvingError
                mResolvingError = true;
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                // TODO: mieux notifier le service
                // mGoogleApiClient.connect();
                MainActivity.this.startService(mCollecteServiceIntent);
            }
        } else {
            // TODO: couper l'application ?
            mResolvingError = true;
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void handleLocationStatusResult(@NonNull Status status) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        }
        if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {

            try {
                status.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                // TODO: mieux gérer mResolvingError (attendre la résolution)
                mResolvingError = true;
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                // TODO: mieux notifier le service
                // mGoogleApiClient.connect();
                MainActivity.this.startService(mCollecteServiceIntent);
                mResolvingError = false;
            }

        } else {
            // TODO: couper l'application ?
            mResolvingError = true;
            showErrorDialog(status.getStatusCode());
        }
    }
}
