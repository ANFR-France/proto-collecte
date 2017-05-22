package com.anfr.cartoradio.collectetm;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by JThom on 30/04/2017.
 */

public class OwnRendring extends DefaultClusterRenderer<MyItem> {
    public OwnRendring(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {

      //  markerOptions.icon(item.getMise_service());
      //  markerOptions.snippet(item.getSnippet());
        //  markerOptions.title(item.getMise_service().get(0));
          markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_black_black));
          super.onBeforeClusterItemRendered(item, markerOptions);
    }
}
