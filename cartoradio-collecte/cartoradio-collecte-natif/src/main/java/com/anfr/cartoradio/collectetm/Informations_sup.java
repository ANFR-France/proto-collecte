package com.anfr.cartoradio.collectetm;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.anfr.cartoradio.collectetm.MyItem;
import com.anfr.cartoradio.collectetm.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JThom on 18/05/2017.
 * AlertDialog qui permet de mettre en forme la liste selon mon adapter et d'indiquer le titre de l'operateur
 */

public class Informations_sup extends AlertDialog {

    TextView operateur;
    ListView maListe;
    String tv_operateur;
    List<Operateur.systemes> prenoms = new ArrayList<>();


    public Informations_sup(Context context, MyItem item, int index) {
        super(context);
        this.prenoms = item.getList_operateur().get(index).getSystemes();
        this.tv_operateur = item.getList_operateur().get(index).getName();
    }

    @Override
    public void setView(View view) {
        super.setView(view);

        operateur = (TextView) view.findViewById(R.id.operateur);
        maListe = (ListView) view.findViewById(R.id.liste_systemes);

        //On utilise notre classe adapter à qui on envoie la liste que l'on souhaite traiter
        SupportAdapter sup_adapter = new SupportAdapter(getContext() , this.prenoms);

        operateur.setText(tv_operateur);
        maListe.setAdapter(sup_adapter);

    }
}
