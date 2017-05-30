package com.anfr.cartoradio.collectetm;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JThom on 27/04/2017.
 * MyItem correspond à un support (une antenne)
 */

public class MyItem implements ClusterItem {

    //cell_id
    private String sup_id;

    //position cell
    private LatLng mPosition;

    //liste operateurs sur ce support
    private List<Operateur> list_operateur;


    public MyItem(LatLng mPosition, List<Operateur> buff, String sup_id)
    {
        this.list_operateur = new ArrayList<Operateur>(buff);
        this.mPosition = mPosition;
        this.sup_id = sup_id;
    }

    public void setList_operateur(List<Operateur> liste)
    {
        this.list_operateur = new ArrayList<>(liste);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getSup_id() {
        return sup_id;
    }

    public List<Operateur> getList_operateur() {
        return list_operateur;
    }
}
