package com.anfr.cartoradio.collectetm;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by JThom on 18/05/2017.
 * SupportAdapter permet de mettre en forme la cellule qui se trouve dans ma liste
 */

public class SupportAdapter extends ArrayAdapter<Operateur.systemes> {


    public SupportAdapter(Context context, List<Operateur.systemes> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_system, parent, false);
        }

        //On connecte notre controller aux TexView afin d'éviter d'appeler findViewById pour chaque vue
        //Lors du premier appel, getTag() retournera null, on connect le tout et la prochaine fois il n'y aura pas d'appel à findViewById
        SupportViewHolder viewHolder = (SupportViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new SupportViewHolder();
            viewHolder.System = (TextView) convertView.findViewById(R.id.system);
            viewHolder.Date = (TextView) convertView.findViewById(R.id.date);

            //On définit un tag à notre vue (row_system) comme ça la prochaine fois il repasse pas dans ce IF
            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        Operateur.systemes sys = getItem(position);

        //Remplissage de la vue
        viewHolder.System.setText(sys.getNames()+"("+sys.getTechno()+")");
        viewHolder.Date.setText(sys.getDate());

        return convertView;
    }


    //classe permettant de controller les cellules, afin de ne pas
    class SupportViewHolder{
        public TextView System;
        public TextView Date;
    }
}
