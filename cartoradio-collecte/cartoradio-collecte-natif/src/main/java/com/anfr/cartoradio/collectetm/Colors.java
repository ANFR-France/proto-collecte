package com.anfr.cartoradio.collectetm;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Colors {
    private static final int TM_BORNE_RED = -105;
    private static final int TM_BORNE_ORANGE = -90;
    private static final int TM_BORNE_YELLOW = -61;

    private static final int SNR_BORNE_RED = 10;
    private static final int SNR_BORNE_ORANGE = 20;
    private static final int SNR_BORNE_YELLOW = 30;

    public static BitmapDescriptor bitmapForDbm(Integer dbm, Integer snr) {
        if (dbm == null) {
            if (snr == null) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_black_black);
            }
            if (snr < SNR_BORNE_RED) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_black_red);
            }
            if (snr < SNR_BORNE_ORANGE) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_black_orange);
            }
            if (snr < SNR_BORNE_YELLOW) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_black_yellow);
            }
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_black_green);
        }
        if (dbm < TM_BORNE_RED) {
            if (snr == null) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red_black);
            }
            if (snr < SNR_BORNE_RED) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red_red);
            }
            if (snr < SNR_BORNE_ORANGE) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red_orange);
            }
            if (snr < SNR_BORNE_YELLOW) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red_yellow);
            }
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red_green);
        }
        if (dbm < TM_BORNE_ORANGE) {
            if (snr == null) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange_black);
            }
            if (snr < SNR_BORNE_RED) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange_red);
            }
            if (snr < SNR_BORNE_ORANGE) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange_orange);
            }
            if (snr < SNR_BORNE_YELLOW) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange_yellow);
            }
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange_green);
        }
        if (dbm < TM_BORNE_YELLOW) {
            if (snr == null) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_yellow_black);
            }
            if (snr < SNR_BORNE_RED) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_yellow_red);
            }
            if (snr < SNR_BORNE_ORANGE) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_yellow_orange);
            }
            if (snr < SNR_BORNE_YELLOW) {
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_yellow_yellow);
            }
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_yellow_green);
        }
        if (snr == null) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green_black);
        }
        if (snr < SNR_BORNE_RED) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green_red);
        }
        if (snr < SNR_BORNE_ORANGE) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green_orange);
        }
        if (snr < SNR_BORNE_YELLOW) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green_yellow);
        }
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green_green);
    }
}
