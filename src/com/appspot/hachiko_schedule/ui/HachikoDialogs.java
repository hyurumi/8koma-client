package com.appspot.hachiko_schedule.ui;

import android.app.Activity;
import android.app.AlertDialog;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.Constants;

import java.io.PrintWriter;
import java.io.StringWriter;

public class HachikoDialogs {
    public static AlertDialog.Builder networkErrorDialogBuilder(
            Activity activity, VolleyError volleyError, String where) {
        where = where == null ? "" : where;
        String msg;
        if (volleyError instanceof TimeoutError) {
            msg = "通信がタイムアウトしました";
        } else {
            String statusCode = volleyError.networkResponse == null ? ""
                    : "(" + volleyError.networkResponse.statusCode + ")";
            msg = "通信中にエラーが発生しました " + statusCode + "\n時間をおいて再度お試しください";
        }
        return new AlertDialog.Builder(activity)
                .setMessage(where + msg);
    }

    public static AlertDialog.Builder networkErrorDialogBuilder(
            Activity activity, VolleyError volleyError) {
        return networkErrorDialogBuilder(activity, volleyError, null);
    }

    public static AlertDialog showNetworkErrorDialog(
            Activity activity, VolleyError volleyError, String where) {
        return networkErrorDialogBuilder(activity, volleyError, where).show();
    }


    public static AlertDialog showNetworkErrorDialog(Activity activity, VolleyError volleyError) {
        return showNetworkErrorDialog(activity, volleyError, null);
    }

    public static void showErrorDialogIfDeveloper(Activity activity, Exception e, String where) {
        if (!Constants.IS_ALPHA_USER) {
            return;
        }
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        new AlertDialog.Builder(activity)
                .setTitle("エラー (開発者にのみ表示)")
                .setMessage((where == null ? "" : (where + "\n")) + stringWriter.toString())
                .show();
    }
}
