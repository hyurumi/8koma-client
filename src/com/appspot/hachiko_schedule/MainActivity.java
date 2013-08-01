package com.appspot.hachiko_schedule;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import com.appspot.hachiko_schedule.fragments.SettledEventsFragment;
import com.appspot.hachiko_schedule.fragments.UnsettledEventsFragment;
import com.appspot.hachiko_schedule.util.NotImplementedActivity;

/**
 * {@link Activity} that is displayed on launch.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.settled_schedules))
                .setTabListener(new TabListener<SettledEventsFragment>(
                        this, "settled_events", SettledEventsFragment.class)));
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.unsettled_schedules))
                .setTabListener(new TabListener<UnsettledEventsFragment>(
                        this, "unsettled_events", UnsettledEventsFragment.class)));
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
                startCreatingEvent();
                return true;
            case R.id.action_config:
                launchMenuActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startCreatingEvent() {
        Intent intent = new Intent(this, NewEventChooseGuestActivity.class);
        startActivity(intent);
    }

    private void launchMenuActivity() {
        Intent intent = new Intent(this, NotImplementedActivity.class);
        intent.putExtra(NotImplementedActivity.EXTRA_KEY_DETAILED_MESSAGE, "設定画面的なやつ");
        startActivity(intent);
    }

    static private class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        /** Constructor used each time a new tab is created.
         * @param activity  The host Activity, used to instantiate the fragment
         * @param tag  The identifier tag for the fragment
         * @param clz  The fragment's Class, used to instantiate the fragment
         */
        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

    /* The following are each of the ActionBar.TabListener callbacks */

        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }
    }
}
