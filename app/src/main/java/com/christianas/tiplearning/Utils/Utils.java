package com.christianas.tiplearning.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Patterns;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static ProgressDialog pDialog;

    public static boolean isEmailVerified(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static void showSnackBar(View view, String msg){
        Snackbar.make(view,msg,Snackbar.LENGTH_LONG)
                .show();
    }
    public static String getCurrentTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static void initpDialog(Context context, String msg) {

        pDialog = new ProgressDialog(context);
        pDialog.setMessage(msg);
        pDialog.setCancelable(false);
    }

    public static void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    public static void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }
}
