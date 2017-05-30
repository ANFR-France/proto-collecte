package com.anfr.cartoradio.collectetm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by JThom on 04/05/2017.
 */

public class info_sup extends AlertDialog {

    Context ctx;
    protected info_sup(@NonNull Context context) {
        super(context);
        Builder test = new Builder(new ContextThemeWrapper(context, R.style.myDialog));
        ctx = context;

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.info_sup, null);
        this.setView(v);
        this.show();

    }
}
