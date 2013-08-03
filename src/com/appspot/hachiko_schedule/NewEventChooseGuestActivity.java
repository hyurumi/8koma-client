package com.appspot.hachiko_schedule;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.*;
import com.appspot.hachiko_schedule.util.HachikoLogger;

/**
 * 新しいイベントを作るときに，招待する友達を選ぶためのアクティビティ
 */
public class NewEventChooseGuestActivity extends Activity {
    private Filter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event_choose_guest);
        ListView friendListView = (ListView) findViewById(R.id.contact_list);
        // 安全でないキャスト… ListView#setFilterTextを直に叩くと出てくるポップアップを表示したくないため，
        // Filterを直に触っている．たぶんListViewのサブクラスをよしなにつくるほうがベター
        filter = ((Filterable) friendListView.getAdapter()).getFilter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_plan_serch_friends, menu);
        SearchView searchView
                = (SearchView) menu.findItem(R.id.action_search_friends).getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.hint_on_search_friends));
        searchView.setOnQueryTextListener(new SearchTextListener());
        searchView.setSubmitButtonEnabled(false);
        return super.onCreateOptionsMenu(menu);
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
