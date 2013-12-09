package tk.hachikoma.friends;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import tk.hachikoma.R;
import tk.hachikoma.plans.PlanListActivity;
import tk.hachikoma.prefs.MainPreferenceActivity;
import tk.hachikoma.push.GoogleCloudMessagingHelper;
import tk.hachikoma.setup.SetupManager;
import tk.hachikoma.util.HachikoLogger;

/**
 * 新しいイベントを作るときに，招待する友達を選ぶためのアクティビティ
 */
public class ChooseGuestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentForSetup = new SetupManager(this).intentForRequiredSetupIfAny();
        if (intentForSetup != null) {
            startActivity(intentForSetup);
            finish();
            return;
        }
        initGoogleCloudMessagingIfNecessary();

        setContentView(R.layout.activity_new_event_choose_guest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.choose_friend_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_see_events:
                launchEventListActivity();
                return true;
            case R.id.action_config:
                launchMenuActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initGoogleCloudMessagingIfNecessary() {
        GoogleCloudMessagingHelper googleCloudMessagingHelper = new GoogleCloudMessagingHelper(this);
        if (!googleCloudMessagingHelper.checkPlayServices()) {
            HachikoLogger.debug("service not available");
            return;
        }
        String registrationId = googleCloudMessagingHelper.getRegistrationId();
        HachikoLogger.debug("regId: ", registrationId);
        if (registrationId.isEmpty()) {
            googleCloudMessagingHelper.registerInBackground();
        }
    }

    private void launchEventListActivity() {
        Intent intent = new Intent(this, PlanListActivity.class);
        startActivity(intent);
    }

    private void launchMenuActivity() {
        Intent intent = new Intent(this, MainPreferenceActivity.class);
        startActivity(intent);
    }
}
