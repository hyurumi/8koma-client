package com.appspot.hachiko_schedule;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;

/**
 * 新しいイベントを作るときに，招待する友達を選ぶためのアクティビティ
 */
public class NewEventChooseGuestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event_choose_guest);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_plan_serch_friends, menu);
        SearchView searchView
                = (SearchView) menu.findItem(R.id.action_search_friends).getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.hint_on_search_friends));
        return super.onCreateOptionsMenu(menu);
    }
}
