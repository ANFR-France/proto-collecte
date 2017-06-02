package com.anfr.cartoradio.collectetm;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.anfr.cartoradio.collectetm.api.ApiManager;
import com.anfr.cartoradio.collectetm.api.ParcoursWithMesures;
import com.anfr.cartoradio.collectetm.db.CollecteOpenHelper;
import com.anfr.cartoradio.collectetm.db.MesureContract;
import com.anfr.cartoradio.collectetm.db.ParcoursContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


public class ParcoursActivity extends AppCompatActivity {

    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

    private ListView listView;
    private final CollecteOpenHelper collecteOpenHelper = new CollecteOpenHelper(this);
    private ArrayAdapter<ParcoursContract.Parcours> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pacours);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.activity_parcours_listview);
        arrayAdapter = new ArrayAdapter<ParcoursContract.Parcours>(this, R.layout.parcours_fragment) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final ParcoursContract.Parcours parcours = getItem(position);

                final View finalConvertView;
                if (convertView == null) {
                    finalConvertView = getLayoutInflater().inflate(R.layout.parcours_fragment, null);
                } else {
                    finalConvertView = convertView;
                }
                TextView titreText = (TextView) finalConvertView.findViewById(R.id.frag_parcours_titre);
                final TextView statutText = (TextView) finalConvertView.findViewById(R.id.frag_parcours_statut);
                TextView dateDebutText = (TextView) finalConvertView.findViewById(R.id.frag_parcours_date_debut);
                TextView dateFinText = (TextView) finalConvertView.findViewById(R.id.frag_parcours_date_fin);
                LinearLayout dateFinLayout = (LinearLayout) finalConvertView.findViewById(R.id.frag_date_fin_layout);
                TextView dateFinLabelText = (TextView) finalConvertView.findViewById(R.id.frag_parcours_date_fin_label);
                final ImageButton deleteButton = (ImageButton) finalConvertView.findViewById(R.id.frag_delete);
                final ImageButton shareButton = (ImageButton) finalConvertView.findViewById(R.id.frag_share);
                final ImageButton viewButton = (ImageButton) finalConvertView.findViewById(R.id.frag_view);

                titreText.setText(parcours.getTitre());

                if (parcours.isSynchronise()) {
                    statutText.setText(R.string.synchronise);
                } else {
                    statutText.setText(R.string.non_synchronise);
                }
                if (parcours.getDateFin() == null) {
                    statutText.setText(R.string.actif);
                    dateFinLabelText.setText("");
                    dateFinLayout.setVisibility(View.INVISIBLE);
                    deleteButton.setVisibility(View.INVISIBLE);
                    shareButton.setVisibility(View.INVISIBLE);
                    viewButton.setVisibility(View.INVISIBLE);
                } else {
                    dateFinLayout.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.VISIBLE);
                    shareButton.setVisibility(View.VISIBLE);
                    viewButton.setVisibility(View.VISIBLE);
                    dateFinText.setText(DATE_FORMAT.format(new Date((long) parcours.getDateFin() * 1000)));
                }
                dateDebutText.setText(DATE_FORMAT.format(new Date((long) parcours.getDateDebut() * 1000)));

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TOOD: ajouter une demande de confirmation
                        collecteOpenHelper.deleteParcours(parcours.getId());
                        Snackbar.make(finalConvertView, R.string.parcours_supprime, Snackbar.LENGTH_LONG).show();
                        loadData();
                    }
                });
                viewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (parcours.isSynchronise()) {
                            viewParcours(parcours);
                        } else {
                            syncParcours(parcours, finalConvertView, false, finalConvertView);
                        }
                    }
                });
                shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (parcours.isSynchronise()) {
                            shareParcours(parcours);
                        } else {
                            syncParcours(parcours, finalConvertView, true, finalConvertView);
                        }
                    }
                });

                return finalConvertView;
            }
        };
        listView.setAdapter(arrayAdapter);
        loadData();
    }

    private void shareParcours(ParcoursContract.Parcours parcours) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");

        share.putExtra(Intent.EXTRA_SUBJECT, parcours.getTitre());
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.remote_url_baseurl) + "/parcours/" + parcours.getId().toString());

        startActivity(Intent.createChooser(share, getString(R.string.partager_parcours)));
    }

    private void viewParcours(ParcoursContract.Parcours parcours) {
        Intent share = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.remote_url_baseurl) + "/parcours/" + parcours.getId().toString()));

        startActivity(Intent.createChooser(share, getString(R.string.visualiser_le_parcours)));
    }

    private void syncParcours(final ParcoursContract.Parcours parcours, final View finalConvertView, final boolean share, final View convertView) {
        finalConvertView.findViewById(R.id.frag_view).setEnabled(false);
        finalConvertView.findViewById(R.id.frag_share).setEnabled(false);
        finalConvertView.findViewById(R.id.frag_delete).setEnabled(false);
        ((TextView) finalConvertView.findViewById(R.id.frag_parcours_statut)).setText(R.string.synchronisation_en_cours);

        collecteOpenHelper.listMesuresParcours(parcours.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MesureContract.Mesure>>() {
                    @Override
                    public void call(List<MesureContract.Mesure> mesures) {
                        ParcoursWithMesures parcoursWithMesures = new ParcoursWithMesures();
                        parcoursWithMesures.setParcours(parcours);
                        parcoursWithMesures.setMesures(mesures);
                        Log.e("apqm Parcours",String.valueOf(parcours.getId()));
                        Log.e("apqm Mesure",String.valueOf(mesures.get(0).getParcoursId()));

                        Call<Void> call = ApiManager.getApiManager(ParcoursActivity.this).postParcours(parcoursWithMesures);
                        try {
                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        finalConvertView.findViewById(R.id.frag_view).setEnabled(true);
                                        finalConvertView.findViewById(R.id.frag_share).setEnabled(true);
                                        finalConvertView.findViewById(R.id.frag_delete).setEnabled(true);
                                        collecteOpenHelper.markParcoursSynchronised(parcours.getId());
                                        loadData();
                                        if (share) {
                                            shareParcours(parcours);
                                        } else {
                                            viewParcours(parcours);
                                        }
                                    } else {

                                        handleSyncFailed(finalConvertView);
                                       Log.i("ERREUUR",String.valueOf(response.code()));
                                   }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {


                                    handleSyncFailed(finalConvertView);
                                }
                            });

                        } catch (Exception e) {
                            handleSyncFailed(finalConvertView);
                        }
                    }
                });
    }



    private void handleSyncFailed(View finalConvertView) {
        finalConvertView.findViewById(R.id.frag_view).setEnabled(true);
        finalConvertView.findViewById(R.id.frag_share).setEnabled(true);
        finalConvertView.findViewById(R.id.frag_delete).setEnabled(true);
        ((TextView) finalConvertView.findViewById(R.id.frag_parcours_statut)).setText(R.string.non_synchronise);
        Snackbar.make(finalConvertView, R.string.une_erreur_est_survenue, Snackbar.LENGTH_LONG).show();
    }


    //On appel listParcours qui lui va envoyer une requête vers la base de donnée SQLite pour charger la liste des parcours (on utilise les observer)
    private void loadData() {
        collecteOpenHelper.listParcours()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ParcoursContract.Parcours>>() {
                    @Override
                    public void call(List<ParcoursContract.Parcours> parcours) {
                        arrayAdapter.clear();
                        arrayAdapter.addAll(parcours);
                        arrayAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        navigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {
            TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
        } else {
            NavUtils.navigateUpTo(this, upIntent);
        }
    }
}
