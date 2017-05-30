package com.anfr.cartoradio.collectetm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.anfr.cartoradio.collectetm.db.CollecteOpenHelper;

public class FinishParcoursActivity extends AppCompatActivity {
    private final CollecteOpenHelper collecteOpenHelper = new CollecteOpenHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collecteOpenHelper.closeParcours();

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.FINISH_PARCOURS));

        startActivity(new Intent(this, ParcoursActivity.class));
    }
}
