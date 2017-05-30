package com.anfr.cartoradio.collectetm.api;

import android.content.Context;

import com.anfr.cartoradio.collectetm.R;

import java.util.List;

import com.anfr.cartoradio.collectetm.db.MesureContract;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ApiManager {

    public interface ApiManagerService {
        @POST("/parcours")
        Call<Void> postParcours(@Body ParcoursWithMesures parcoursWithMesures);

        @POST("/mesures")
        Call<Void> postMesures(@Body List<MesureContract.Mesure> mesures);
    }

    public static ApiManagerService getApiManager(Context context) {
        Retrofit restAdapter = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(context.getString(R.string.remote_url_baseurl_anfr))
                .build();

        return restAdapter.create(ApiManagerService.class);
    }
}


