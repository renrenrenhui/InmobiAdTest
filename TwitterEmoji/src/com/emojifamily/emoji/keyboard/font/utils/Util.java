package com.emojifamily.emoji.keyboard.font.utils;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.TypedValue;

public class Util {

    private static final String TAG = "Util";

    public static int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else {
            NetworkInfo[] infos = cm.getAllNetworkInfo();
            if (infos != null) {
                for (int i = 0; i < infos.length; i++) {
                    if (infos[i].getState() == NetworkInfo.State.CONNECTED) {
                        Log.d(TAG, "isNetworkAvailable -  I " + i);
                    }
                }
            }

            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                Log.d(TAG, "isNetworkAvailable" + networkInfo.isAvailable());
            } else {
                Log.d(TAG, "isNetworkAvailable ");
                return false;
            }

            if (networkInfo != null
                    && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.d(TAG, "isNetworkAvailable - 3G network");
                return true;
            } else {
                Log.d(TAG, "isNetworkAvailable - no 3G network");
            }

            if (networkInfo != null
                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d(TAG, "isNetworkAvailable - wifi available");
                return true;
            } else {
                Log.d(TAG, "isNetworkAvailable - wifi unavailable");
            }
        }
        return false;
    }

}
