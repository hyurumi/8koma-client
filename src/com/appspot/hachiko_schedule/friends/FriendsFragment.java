package com.appspot.hachiko_schedule.friends;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.FriendGroup;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.data.FriendOrGroup;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.plans.CreatePlanActivity;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.ui.EditTextDialog;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.util.*;

/**
 * FriendIdentifier list where user can choose friends to invite.
 */
public class FriendsFragment extends Fragment {
    private FriendsAdapter friendListAdapter;
    private FriendsAdapter suggestionAdapter;

    private ListView listView;
    private View createPlanButtonWrapper;
    private Set<String> selectedItems;
    private ChipsAutoCompleteTextView searchFriendView;
    private Button createGroupButton;
    private Button createPlanButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list, container, false);
        createPlanButtonWrapper = view.findViewById(R.id.new_plan_button_wrapper);
        createGroupButton = (Button) view.findViewById(R.id.create_group_button);
        createPlanButton = (Button) view.findViewById(R.id.new_plan_button);
        listView = (ListView) view.findViewById(R.id.contact_list);

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

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
        selectedItems = new HashSet<String>();
        List<FriendOrGroup> items = new ArrayList<FriendOrGroup>();
        UserTableHelper userTableHelper = new UserTableHelper(getActivity());
        items.addAll(userTableHelper.getListOfGroups());
        items.addAll(getListOfFriends());
        friendListAdapter = new FriendsAdapter(
                getActivity(), R.layout.list_item_friend, items, selectedItems);
        listView.setAdapter(friendListAdapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new OnFriendItemClickListener());

        searchFriendView = (ChipsAutoCompleteTextView) view.findViewById(R.id.search_friend);
        suggestionAdapter = new FriendsAdapter(
                getActivity(), R.layout.auto_complete_item_friend, items, selectedItems);
        searchFriendView.setAdapter(suggestionAdapter);
        searchFriendView.addOnItemClickListener(new OnFriendAutoCompleteClickListener());
        searchFriendView.setOnNameDeletedListener(new OnFriendNameDeletedListener());
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.contact_list) {
            AdapterView.AdapterContextMenuInfo info
                    = (AdapterView.AdapterContextMenuInfo) menuInfo;
            FriendOrGroup item = friendListAdapter.getItem(info.position);
            if (item instanceof FriendGroup) {
                menu.setHeaderTitle("グループ: " + ((FriendGroup) item).getGroupName());
                menu.add(Menu.NONE, 0, 0, "削除");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info
                = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        switch(menuItem.getItemId()) {
            case 0: // delete
                FriendGroup item = (FriendGroup) friendListAdapter.getItem(info.position);
                UserTableHelper tableHelper = new UserTableHelper(getActivity());
                friendListAdapter.remove(item);
                suggestionAdapter.remove(item);
                tableHelper.deleteGroup(item.getId());
            default:
                return super.onContextItemSelected(menuItem);
        }
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

    private void createGroup() {
        final Set<Long> friendIdsToBeGroup = new HashSet<Long>();
        final UserTableHelper tableHelper = new UserTableHelper(getActivity());
        final Collection<FriendItem> friends = friendListAdapter.getSelectedEntries();
        for (FriendItem item: friends) {
            friendIdsToBeGroup.add(tableHelper.getHachikoId(item.getLocalContactId()));
        }

        EditTextDialog.showDialog(getActivity(), "グループを作成", "グループ名", "作成",
                new EditTextDialog.PositiveButtonListener() {
                    @Override
                    public boolean onPositiveButtonClicked(DialogInterface dialog, String text) {
                        tableHelper.createGroup(text, friendIdsToBeGroup, null);
                        FriendGroup group = new FriendGroup(
                                0, text, null, new HashSet<FriendItem>(friends));
                        friendListAdapter.insert(group, 0);
                        suggestionAdapter.insert(group, 0);
                        clearSelectedFriends();
                        listView.setSelectionAfterHeaderView();
                        return true;
                    }
                });
    }

    private void clearSelectedFriends() {
        searchFriendView.setText("");
        selectedItems.clear();
        setConfirmButtonState();
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
