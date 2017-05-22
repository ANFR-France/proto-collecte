package com.anfr.cartoradio.collectetm;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.anfr.cartoradio.collectetm.R;

/**
 * Helper class for showing and canceling collecte
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class CollecteNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final int NOTIFICATION_ID = 001;

    public static void notify(final Context context, int nbNonSynchronise) {
        final Resources res = context.getResources();

        final String title = res.getString(R.string.collecte_notification_title);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)

                .setSmallIcon(R.drawable.ic_stat_collecte)
                .setContentTitle(title)
                .setContentText(title)

                .setOnlyAlertOnce(true)
                .setUsesChronometer(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                .setContentText(nbNonSynchronise > 0 ? res.getString(R.string.collecte_notification_text) : null)
                .setNumber(nbNonSynchronise)

                .setAutoCancel(false);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, SettingsActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        stackBuilder.addParentStack(SettingsActivity.class);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(154001, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the pending intent to be initiated when the user touches
        // the notification.
        builder.setContentIntent(resultPendingIntent);

        builder.addAction(
                android.R.drawable.ic_media_pause,
                res.getString(R.string.action_stop),
                PendingIntent.getActivity(context, 1576321, new Intent(context, StopCollecteActivity.class), PendingIntent.FLAG_ONE_SHOT));

        NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationMgr.notify(NOTIFICATION_ID, builder.build());
    }

    public static void hide(final Context context) {
        NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationMgr.cancel(NOTIFICATION_ID);
    }
}
