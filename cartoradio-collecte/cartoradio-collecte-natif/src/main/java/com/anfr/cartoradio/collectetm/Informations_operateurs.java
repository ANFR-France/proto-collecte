package com.anfr.cartoradio.collectetm;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JThom on 29/05/2017.
 */

public class Informations_operateurs  extends AlertDialog {

    TextView operateur;
    ListView maListe;
    String tv_operateur;
    List<Operateur> operateurs = new ArrayList<>();


    public Informations_operateurs(Context context, MyItem item, int index) {
        super(context);
        this.operateurs = item.getList_operateur();
      //  this.tv_operateur = item.getList_operateur().get(index).getName();
    }

    @Override
    public void setView(View view) {
        super.setView(view);

        operateur = (TextView) view.findViewById(R.id.operateur);
        maListe = (ListView) view.findViewById(R.id.liste_systemes);

        //On utilise notre classe adapter à qui on envoie la liste que l'on souhaite traiter
        OperatorAdapter sup_adapter = new OperatorAdapter(getContext() , this.operateurs);

     //   operateur.setText(tv_operateur);

        if(operateurs.size() == 0)
        {
            operateur.setText("Non renseigné");
        }
        else
        {
            operateur.setText("Liste des opérateurs");
        }
        maListe.setAdapter(sup_adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        maListe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Operateur persona = (Operateur)parent.getItemAtPosition(position);
                SupportAdapter support = new SupportAdapter(getContext(),  persona.getSystemes());
                maListe.setAdapter(support);

            }
        });
    }
}
