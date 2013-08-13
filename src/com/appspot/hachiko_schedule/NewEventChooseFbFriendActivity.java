package com.appspot.hachiko_schedule;

import android.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.facebook.widget.HachikoFbFriendPickerFragment;

/**
 * 新しいイベントをつくるときに，招待する相手をFBの友達一覧から選ぶActivity
 */
public class NewEventChooseFbFriendActivity extends FragmentActivity {
    private HachikoFbFriendPickerFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = new HachikoFbFriendPickerFragment();
        fragmentManager.beginTransaction()
                .add(android.R.id.content, fragment, "Fb choose friend")
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fragment.loadData(false);
    }
}
