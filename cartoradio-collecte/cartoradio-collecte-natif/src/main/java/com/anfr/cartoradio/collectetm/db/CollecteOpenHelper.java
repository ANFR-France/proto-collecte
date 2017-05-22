package com.anfr.cartoradio.collectetm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class CollecteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "CollecteOpenHelper";

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "Collecte.db";
    private static final String MESURE_TABLE_DROP = "DROP TABLE IF EXISTS " + MesureContract.Mesure.TABLE_NAME + ";";
    private static final String MESURE_TABLE_CREATE =
            "CREATE TABLE " + MesureContract.Mesure.TABLE_NAME + " (" +
                    MesureContract.Mesure._ID + " TEXT, " +
                    MesureContract.Mesure.COLUMN_NAME_LONGITUDE + " REAL, " +
                    MesureContract.Mesure.COLUMN_NAME_LATITUDE + " REAL, " +
                    MesureContract.Mesure.COLUMN_NAME_PRECISION_POSITION + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_DATE_MESURE + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_TYPE_POSITION + " TEXT, " +
                    MesureContract.Mesure.COLUMN_NAME_TM_TYPE + " TEXT, " +
                    MesureContract.Mesure.COLUMN_NAME_TM_CID + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_TM_LAC + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_TM_MCC + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_TM_MNC + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_TM_DBM + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_TM_LEVEL + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_GPS_NUM + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_GPS_SNR + " INTEGER, " +
                    MesureContract.Mesure.COLUMN_NAME_APPAREIL + " TEXT, " +
                    MesureContract.Mesure.COLUMN_NAME_PARCOURS_ID + " TEXT ); ";

    private static final String PARCOURS_TABLE_DROP = "DROP TABLE IF EXISTS " + ParcoursContract.Parcours.TABLE_NAME + ";";
    private static final String PARCOURS_TABLE_CREATE =
            "CREATE TABLE " + ParcoursContract.Parcours.TABLE_NAME + " (" +
                    ParcoursContract.Parcours._ID + " TEXT, " +
                    ParcoursContract.Parcours.COLUMN_NAME_DATE_DEBUT + " INTEGER NOT NULL, " +
                    ParcoursContract.Parcours.COLUMN_NAME_DATE_FIN + " INTEGER, " +
                    ParcoursContract.Parcours.COLUMN_NAME_TITRE + " TEXT NOT NULL, " +
                    ParcoursContract.Parcours.COLUMN_NAME_SYNCHRONISE + " INTEGER NOT NULL DEFAULT 0);";

    public CollecteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MESURE_TABLE_CREATE);
        db.execSQL(PARCOURS_TABLE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(MESURE_TABLE_DROP);
        db.execSQL(PARCOURS_TABLE_DROP);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    private static <T> Observable<T> makeObservable(final Callable<T> func) {
        return Observable.create(
                new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        try {
                            subscriber.onNext(func.call());
                        } catch (Exception ex) {
                            Log.e(TAG, "Error reading from the database", ex);
                        }
                    }
                });
    }

    public Observable<List<MesureContract.Mesure>> listMesures() {

        return makeObservable(MesureContract.listMesures(getReadableDatabase()))
                .subscribeOn(Schedulers.computation());
    }

    public Observable<Integer> countMesures() {

        return makeObservable(MesureContract.countMesures(getReadableDatabase()))
                .subscribeOn(Schedulers.computation());

    }

    public Observable<Integer> countMesuresParcours(UUID parcoursId) {

        return makeObservable(MesureContract.countMesures(getReadableDatabase(), parcoursId))
                .subscribeOn(Schedulers.computation());
    }



//--------------------------------
    //Fonction qui retourne un observable, on choisi de l'éxécuter dans un certain thread avec "Schedulers.computation()"
    public Observable< List<ParcoursContract.Parcours>  > listParcours() {

        //On fabrique un observable qui va observer une liste dans la base de donnée SQLite
        return makeObservable(ParcoursContract.list(getReadableDatabase()))
                .subscribeOn(Schedulers.computation());
    }
//------------------------------------





    public Observable<List<MesureContract.Mesure>> listMesuresParcours() {
        return makeObservable(MesureContract.listParcours(getReadableDatabase()))
                .subscribeOn(Schedulers.computation());
    }

    public Observable<List<MesureContract.Mesure>> listMesuresParcours(UUID parcoursId) {
        return makeObservable(MesureContract.listParcours(getReadableDatabase(), parcoursId))
                .subscribeOn(Schedulers.computation());
    }

    public void insertMesure(MesureContract.Mesure mesure) {
        MesureContract.insert(getWritableDatabase(), mesure);
    }

    public Observable<ParcoursContract.Parcours> getCurrentParcours() {
        return makeObservable(ParcoursContract.getCurrent(getReadableDatabase()))
                .subscribeOn(Schedulers.computation());
    }

    public void insertParcours(String titre) {
        ParcoursContract.insert(getWritableDatabase(), titre);
    }

    public void closeParcours() {
        ParcoursContract.close(getWritableDatabase());
    }


    public void markParcoursSynchronised(UUID parcoursId) {
        ParcoursContract.markSynchronised(getWritableDatabase(), parcoursId);
    }


    public void deleteParcours(UUID parcoursId) {
        getWritableDatabase().delete(ParcoursContract.Parcours.TABLE_NAME, ParcoursContract.Parcours._ID + " = ?", new String[]{parcoursId.toString()});
        getWritableDatabase().delete(MesureContract.Mesure.TABLE_NAME, MesureContract.Mesure.COLUMN_NAME_PARCOURS_ID + " = ?", new String[]{parcoursId.toString()});
    }

    public void deleteMesures(List<MesureContract.Mesure> mesures) {
        String mesureIdsClause = "";
        List<String> mesureIds = new ArrayList<>();
        boolean first = true;
        for (MesureContract.Mesure mesure : mesures) {
            if (!first) {
                mesureIdsClause += ",";
            }
            mesureIds.add(mesure.getId().toString());
            mesureIdsClause += "?";
            first = false;
        }
        getWritableDatabase().delete(MesureContract.Mesure.TABLE_NAME, MesureContract.Mesure._ID + " in (" + mesureIdsClause + ")", mesureIds.toArray(new String[mesureIds.size()]));
    }
}