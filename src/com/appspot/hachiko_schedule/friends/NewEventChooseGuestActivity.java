package com.appspot.hachiko_schedule.friends;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.plans.PlanListActivity;
import com.appspot.hachiko_schedule.prefs.MainPreferenceActivity;
import com.appspot.hachiko_schedule.push.GoogleCloudMessagingHelper;
import com.appspot.hachiko_schedule.setup.SetupManager;
import com.appspot.hachiko_schedule.util.HachikoLogger;

/**
 * 新しいイベントを作るときに，招待する友達を選ぶためのアクティビティ
 */
public class NewEventChooseGuestActivity extends Activity {
    private Filter filter;

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
        SearchView searchView = (SearchView) findViewById(R.id.search_friend);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchTextListener());
        searchView.setSubmitButtonEnabled(false);

        ListView friendListView = (ListView) findViewById(R.id.contact_list);
        // 安全でないキャスト… ListView#setFilterTextを直に叩くと出てくるポップアップを表示したくないため，
        // Filterを直に触っている．たぶんListViewのサブクラスをよしなにつくるほうがベター
        filter = ((Filterable) friendListView.getAdapter()).getFilter();
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
            return;
        }
        String registrationId = googleCloudMessagingHelper.getRegistrationId();
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

    private class SearchTextListener implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextChange(String newText) {
            if (TextUtils.isEmpty(newText)) {
                // Note: ListView#setFilterText実行時のポップアップが不要のため，直にFilterを触っている
                filter.filter("");
            } else {
                HachikoLogger.debug("filter by ", newText);
                filter.filter(newText);
            }
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }
    }
}
