package com.anfr.cartoradio.collectetm;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JThom on 27/04/2017.
 */

public class MyItem implements ClusterItem {

    private String sup_id;
    private LatLng mPosition;
    private List<String> mise_service = new ArrayList<>();
    private List<String> operateur  = new ArrayList<>();
    private List<String> techno  = new ArrayList<>();

    private List<Operateur> list_operateur;


    public MyItem(LatLng mPosition, List<String> operateur, List<String> mise_service, List<String> techno, String sup_id) {
        this.mPosition = mPosition;
        this.mise_service = new ArrayList<>(mise_service);
        this.operateur = new ArrayList<>(operateur);
        this.techno = new ArrayList<>(techno);
        this.sup_id = sup_id;
    }

    public MyItem(LatLng mPosition, List<Operateur> buff)
    {
        this.list_operateur = new ArrayList<Operateur>(buff);
        this.mPosition = mPosition;

    }


    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public List<String> getOperateur() {
        return operateur;
    }

    public List<String> getTechno() {
        return techno;
    }

    public List<String> getMise_service() {
        return mise_service;
    }

    public void setOperateur(List<String> operateurs) {
        this.operateur = operateurs;
    }

    public void setTechno(List<String> technos){
        this.techno = technos;
    }

    public void setMise_service(List<String> mes)
    {
        this.mise_service = mes;
    }

    public String getSup_id() {
        return sup_id;
    }

    public List<Operateur> getList_operateur() {
        return list_operateur;
    }
}
