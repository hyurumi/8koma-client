package tk.hachikoma.setup;

import android.support.v4.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import tk.hachikoma.friends.ChooseGuestActivity;

public class SetupBaseActivity extends FragmentActivity {
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
