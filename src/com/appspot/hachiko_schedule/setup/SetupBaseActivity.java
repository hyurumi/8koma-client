package com.appspot.hachiko_schedule.setup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import com.appspot.hachiko_schedule.friends.ChooseGuestActivity;

public class SetupBaseActivity extends Activity {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
    }

    protected void hideProgressDialog() {
        progressDialog.hide();
    }

    protected void showProgressDialog(String msg) {
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    protected void transitToNextActivity() {
        Intent intent = new SetupManager(this).intentForRequiredSetupIfAny();
        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            intent = new Intent(this, ChooseGuestActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
