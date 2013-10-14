package com.appspot.hachiko_schedule.ui;

import android.app.AlertDialog;
import android.content.Context;
import com.android.volley.VolleyError;

/**
 * @author Kazuki Nishiura
 */
public class HachikoDialogs {
    public static AlertDialog.Builder networkErrorDialogBuilder(
            Context context, VolleyError volleyError, String where) {
        String statusCode = volleyError.networkResponse == null ? ""
                : "(" + volleyError.networkResponse.statusCode + ")";
        return new AlertDialog.Builder(context)
                .setMessage((where == null ? "" : where)
                        + "通信中にエラーが発生しました " + statusCode + "\n時間をおいて再度お試しください");
    }

    public static AlertDialog.Builder networkErrorDialogBuilder(
            Context context, VolleyError volleyError) {
        return networkErrorDialogBuilder(context, volleyError, null);
    }

    public static AlertDialog showNetworkErrorDialog(
            Context context, VolleyError volleyError, String where) {
        return networkErrorDialogBuilder(context, volleyError, where).show();
    }


    public static AlertDialog showNetworkErrorDialog(Context context, VolleyError volleyError) {
        return showNetworkErrorDialog(context, volleyError, null);
    }
}
