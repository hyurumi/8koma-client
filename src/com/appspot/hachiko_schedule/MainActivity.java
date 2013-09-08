package com.appspot.hachiko_schedule;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import android.widget.Toast;
import com.appspot.hachiko_schedule.plans.SettledEventsFragment;
import com.appspot.hachiko_schedule.plans.UnsettledEventsFragment;
import com.appspot.hachiko_schedule.friends.NewEventChooseFbFriendActivity;
import com.appspot.hachiko_schedule.friends.NewEventChooseGuestActivity;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.prefs.MainPreferenceActivity;
import com.appspot.hachiko_schedule.setup.SetupManager;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

/**
 * {@link Activity} that is displayed on launch.
 */
public class MainActivity extends Activity {

    private static final String KEY_SELECTED_TAB = "selected_tab";
    private boolean pickFriendsWhenSessionOpened;
    private UiLifecycleHelper lifecycleHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentForSetup = new SetupManager(this).intentForRequiredSetupIfAny();
        if (intentForSetup != null) {
            startActivity(intentForSetup);
            finish();
            return;
        }
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.settled_schedules))
                .setTabListener(new TabListener<SettledEventsFragment>(
                        "settled_events", SettledEventsFragment.class)));
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.unsettled_schedules))
                .setTabListener(new TabListener<UnsettledEventsFragment>(
                        "unsettled_events", UnsettledEventsFragment.class)));
        checkNewEvent(getIntent());

        lifecycleHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (pickFriendsWhenSessionOpened && state.isOpened()) {
                    pickFriendsWhenSessionOpened = false;

                    startCreatingEventWithFbFriends();
                }
            }
        });
        lifecycleHelper.onCreate(savedInstanceState);

        ensureOpenSession();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_TAB,getActionBar().getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            int lastSelectedTabIndex = savedInstanceState.getInt(KEY_SELECTED_TAB, -1);
            if (lastSelectedTabIndex >= 0) {
                getActionBar().selectTab(getActionBar().getTabAt(lastSelectedTabIndex));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkNewEvent(intent);
    }

    private void checkNewEvent(Intent intent) {
        if (intent.getBooleanExtra(Constants.EXTRA_KEY_NEW_EVENT, false)) {
            Toast.makeText(this,
                    "新しいイベントが登録されました！(データ記憶する部分はまだ作ってないので表示は変わりません...)",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_event:
                if (HachikoPreferences.getDefault(this).getBoolean(
                        HachikoPreferences.KEY_USE_FB_CONTACT,
                        HachikoPreferences.USE_FB_CONTACT_DEFAULT
                )) {
                    startCreatingEventWithFbFriends();
                } else {
                    startCreatingEventWithContacts();
                }
                return true;
            case R.id.action_config:
                launchMenuActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean ensureOpenSession() {
        if (Session.getActiveSession() == null ||
                !Session.getActiveSession().isOpened()) {
            Session.openActiveSession(this, true, new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    if (pickFriendsWhenSessionOpened && state.isOpened()) {
                        pickFriendsWhenSessionOpened = false;

                        startCreatingEventWithFbFriends();
                    }
                }
            });
            return false;
        }
        return true;
    }

    private void startCreatingEventWithFbFriends() {
        if (ensureOpenSession()) {
            Intent intent = new Intent(this, NewEventChooseFbFriendActivity.class);
            startActivity(intent);
        } else {
            pickFriendsWhenSessionOpened = true;
        }
    }

    private void startCreatingEventWithContacts() {
        Intent intent = new Intent(this, NewEventChooseGuestActivity.class);
        startActivity(intent);
    }

    private void launchMenuActivity() {
        Intent intent = new Intent(this, MainPreferenceActivity.class);
        startActivity(intent);
    }

    private class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final String mTag;
        private final Class<T> mClass;

        /** Constructor used each time a new tab is created.
         *
         * @param tag  The identifier tag for the fragment
         * @param clz  The fragment's Class, used to instantiate the fragment
         */
        public TabListener(String tag, Class<T> clz) {
            mTag = tag;
            mClass = clz;
        }

        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mFragment = getFragmentManager().findFragmentByTag(mTag);
            if (mFragment == null) {
                mFragment = Fragment.instantiate(MainActivity.this, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }
    }
}
