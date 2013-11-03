package com.appspot.hachiko_schedule.friends;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.data.FriendOrGroup;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.plans.CreatePlanActivity;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FriendIdentifier list where user can choose friends to invite.
 */
public class FriendsFragment extends Fragment {
    private FriendsAdapter friendListAdapter;

    private ListView listView;
    private View createPlanButtonWrapper;
    private ChipsAutoCompleteTextView searchFriendView;
    private Button createPlanButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list, container, false);
        createPlanButtonWrapper = view.findViewById(R.id.new_plan_button_wrapper);
        createPlanButton = (Button) view.findViewById(R.id.new_plan_button);
        listView = (ListView) view.findViewById(R.id.contact_list);

        createPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<FriendIdentifier> friendsToInvite = new HashSet<FriendIdentifier>();
                Intent intent = new Intent(getActivity(), CreatePlanActivity.class);
                UserTableHelper tableHelper = new UserTableHelper(getActivity());
                for (FriendItem entry: friendListAdapter.getSelectedEntries()) {
                    long hachikoId = tableHelper.getHachikoId(entry.getLocalContactId());
                    if (hachikoId == 0) {
                        //TODO: たぶんサーバからHachikoID（まだ）取得できてないので，ちゃんと対応する必要
                        HachikoLogger.error("Invlaid hachiko ID!!");
                    }
                    friendsToInvite.add(new FriendIdentifier(hachikoId, entry.getEmailAddress(),
                                    entry.getDisplayName()));
                }
                intent.putExtra(
                        Constants.EXTRA_KEY_FRIENDS,
                        friendsToInvite.toArray(new FriendIdentifier[0]));
                startActivityForResult(intent, 0);
            }
        });
        // 2つのアダプタで選択された友達を共有するためのSet, もっと良い感じにリファクタしたい…
        Set<String> selectedItems = new HashSet<String>();
        List<FriendOrGroup> items = new ArrayList<FriendOrGroup>();
        items.addAll(getListOfFriends());
        friendListAdapter = new FriendsAdapter(
                getActivity(), R.layout.list_item_friend, items, selectedItems);
        listView.setAdapter(friendListAdapter);
        listView.setOnItemClickListener(new OnFriendItemClickListener());

        searchFriendView = (ChipsAutoCompleteTextView) view.findViewById(R.id.search_friend);
        searchFriendView.setAdapter(new FriendsAdapter(
                getActivity(), R.layout.auto_complete_item_friend, items, selectedItems));
        searchFriendView.addOnItemClickListener(new OnFriendAutoCompleteClickListener());
        searchFriendView.setOnNameDeletedListener(new OnFriendNameDeletedListener());
        return view;
    }

    private List<FriendItem> getListOfFriends() {
        if (HachikoPreferences.getDefault(getActivity()).getBoolean(
                HachikoPreferences.KEY_IS_LOCAL_USER_TABLE_SETUP,
                HachikoPreferences.IS_LOCAL_USER_TABLE_SETUP_DEFAULT
        )) {
            UserTableHelper userTableHelper = new UserTableHelper(getActivity());
            return userTableHelper.getListOfContactEntries();
        }
        // ローカルデータベースがまだセットアップされてないので，標準のContactを利用
        ContactManager manager = ContactManager.getInstance(getActivity());
        return manager.getListOfContactEntries();
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    private void setConfirmButtonState() {
        boolean shouldEnable = !friendListAdapter.getSelectedEntries().isEmpty();
        createPlanButtonWrapper.setVisibility(shouldEnable ? View.VISIBLE : View.GONE);
        createPlanButton.setEnabled(shouldEnable);
    }

    private class OnFriendItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CharSequence name = friendListAdapter.getNameTextFromItem(view);
            if (friendListAdapter.notifySelect(view, position)) {
                String textInSearchField = searchFriendView.getText().toString();
                String trimmed = trimEndOfTextToComma(textInSearchField);
                searchFriendView.setText(trimmed.trim() + (trimmed.length() > 0 ? ", " : "") + name + ", ");
                searchFriendView.setupChips();
            } else {
                searchFriendView.removeName(name.toString());
            }
            setConfirmButtonState();
        }

        private String trimEndOfTextToComma(String str) {
            if (str.length() == 0) {
                return str;
            }
            int last = str.length() - 1;
            while (last > 0 && str.charAt(last) != ',') {
                last--;
            }
            return str.substring(0, last);
        }
    }

    private class OnFriendAutoCompleteClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!friendListAdapter.notifySelect(view, position)) {
                searchFriendView.removeName(friendListAdapter.getNameTextFromItem(view).toString());
            }
            setConfirmButtonState();
        }
    }

    private class OnFriendNameDeletedListener
            implements ChipsAutoCompleteTextView.OnNameDeletedListener {
        @Override
        public void onNameDeleted(String name) {
            friendListAdapter.unselectByName(name);
            setConfirmButtonState();
        }
    }
}
