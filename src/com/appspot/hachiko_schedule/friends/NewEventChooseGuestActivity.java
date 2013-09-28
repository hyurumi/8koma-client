package com.appspot.hachiko_schedule.friends;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import com.appspot.hachiko_schedule.R;
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
        SearchView searchView = (SearchView) findViewById(R.id.search_friend);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchTextListener());
        searchView.setSubmitButtonEnabled(false);

        ListView friendListView = (ListView) findViewById(R.id.contact_list);
        // 安全でないキャスト… ListView#setFilterTextを直に叩くと出てくるポップアップを表示したくないため，
        // Filterを直に触っている．たぶんListViewのサブクラスをよしなにつくるほうがベター
        filter = ((Filterable) friendListView.getAdapter()).getFilter();
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
