package com.anfr.cartoradio.collectetm;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by JThom on 29/05/2017.
 */

public class OperatorAdapter extends ArrayAdapter<Operateur> {

    public OperatorAdapter(Context context, List<Operateur> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_operators, parent, false);
        }

        //On connecte notre controller aux TexView afin d'éviter d'appeler findViewById pour chaque vue
        //Lors du premier appel, getTag() retournera null, on connect le tout et la prochaine fois il n'y aura pas d'appel à findViewById
        OperatorAdapter.SupportViewHolder viewHolder = (OperatorAdapter.SupportViewHolder) convertView.getTag();

        if(viewHolder == null){
            viewHolder = new OperatorAdapter.SupportViewHolder();
            viewHolder.System = (TextView) convertView.findViewById(R.id.operateur);
            viewHolder.Date = (TextView) convertView.findViewById(R.id.generation);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.image_operateur);

            //On définit un tag à notre vue (row_system) comme ça la prochaine fois il repasse pas dans ce IF
            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        Operateur opt = getItem(position);

        //Remplissage de la vue
        viewHolder.System.setText(opt.getName());
        viewHolder.Date.setText(opt.getTechnoString());

        //En fonction du nom de l'opérateur je vais choisir l'image à insérer dans l'imageView
        if(opt.getName().contains("SFR"))
        {
            viewHolder.image.setImageResource(R.drawable.sfr);
        }
        else if(opt.getName().contains("FREE MOBILE"))
        {
            viewHolder.image.setImageResource(R.drawable.free);
        }
        else if(opt.getName().contains("BOUYGUES"))
        {
            viewHolder.image.setImageResource(R.drawable.bouygue);
        }
        else if(opt.getName().contains("ORANGE"))
        {
            viewHolder.image.setImageResource(R.drawable.orange);
        }

        return convertView;
    }

    //classe permettant de controller les cellules, afin de ne pas
    class SupportViewHolder{
        ImageView image;
        public TextView System;
        public TextView Date;
    }
}
