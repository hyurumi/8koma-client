package com.appspot.hachiko_schedule;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import android.widget.Toast;
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
                        "settled_events", SettledEventsFragment.class)));
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.unsettled_schedules))
                .setTabListener(new TabListener<UnsettledEventsFragment>(
                        "unsettled_events", UnsettledEventsFragment.class)));
        checkNewEvent(getIntent());
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
