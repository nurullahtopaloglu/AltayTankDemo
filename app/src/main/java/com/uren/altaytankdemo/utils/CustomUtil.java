package com.uren.altaytankdemo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.uren.altaytankdemo.R;


public class CustomUtil {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }

        return false;
    }

    public static void networkErrorSnackBar(View view, Context context) {
        try {
            Snackbar snackbar = Snackbar.make(view,
                    context.getResources().getString(R.string.networkError),
                    Snackbar.LENGTH_SHORT);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(context.getResources().getColor(R.color.Red));
            TextView tv = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(context.getResources().getColor(R.color.White));
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
