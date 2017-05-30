package com.anfr.cartoradio.collectetm;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;

public class StopCollecteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean(SettingsActivity.KEY_COLLECTE_ACTIVER, false)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(SettingsActivity.KEY_COLLECTE_ACTIVER, false);
            editor.commit();
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.startActivities();
    }
}
