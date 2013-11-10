package com.appspot.hachiko_schedule.friends;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.appspot.hachiko_schedule.ui.EditTextDialog;

import java.util.*;

/**
 * FriendIdentifier list where user can choose friends to invite.
 */
public class FriendsFragment extends Fragment {
    private FriendListAdapter friendListAdapter;
    private FriendListAdapter suggestionAdapter;

    private ListView listView;
    private View createPlanButtonWrapper;
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
                for (FriendItem entry: getSelectedEntries()) {
                    long hachikoId = tableHelper.getHachikoId(entry.getLocalContactId());
                    friendsToInvite.add(new FriendIdentifier(hachikoId, entry.getEmailAddress(),
                                    entry.getDisplayName()));
                }
                intent.putExtra(
                        Constants.EXTRA_KEY_FRIENDS,
                        friendsToInvite.toArray(new FriendIdentifier[0]));
                startActivityForResult(intent, 0);
            }
        });
        List<FriendOrGroup> items = new ArrayList<FriendOrGroup>();
        UserTableHelper userTableHelper = new UserTableHelper(getActivity());
        items.addAll(userTableHelper.getListOfGroups());
        items.addAll(userTableHelper.getListOfContactEntries());
        friendListAdapter = new FriendListAdapter(getActivity(), R.layout.list_item_friend, items);
        listView.setAdapter(friendListAdapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new OnFriendItemClickListener());

        searchFriendView = (ChipsAutoCompleteTextView) view.findViewById(R.id.search_friend);
        suggestionAdapter = new FriendMultipleSuggestAdapter(
                getActivity(), R.layout.auto_complete_item_friend, items);
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
                Set<Integer> checkedIndices = copyCheckedItemPositions();
                if (checkedIndices.contains(info.position)) {
                    searchFriendView.removeName(item.getDisplayName());
                }
                friendListAdapter.remove(item);
                listView.clearChoices();
                for (Integer i: checkedIndices) {
                    if (i < info.position) {
                        listView.setItemChecked(i, true);
                    } else if (i > info.position) {
                        listView.setItemChecked(i - 1, true);
                    }
                }
                suggestionAdapter.remove(item);
                UserTableHelper tableHelper = new UserTableHelper(getActivity());
                tableHelper.deleteGroup(item.getId());
            default:
                return super.onContextItemSelected(menuItem);
        }
    }

    private Set<Integer> copyCheckedItemPositions() {
        SparseBooleanArray selection = listView.getCheckedItemPositions();
        Set<Integer> checkedIndices = new HashSet<Integer>();
        for (int i = 0; i < selection.size(); i++) {
            if (selection.get(selection.keyAt(i))) {
                checkedIndices.add(selection.keyAt(i));
            }
        }
        return checkedIndices;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * @return 選択されている友達を返す. グループが選択されているときは，そのグループのメンバに対応する友達を展開して返す
     */
    private Set<FriendItem> getSelectedEntries() {
        Set<FriendItem> friends = new HashSet<FriendItem>();
        SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
        for (int i = 0; i < checkedItemPositions.size(); i++) {
            if (!checkedItemPositions.get(checkedItemPositions.keyAt(i))) {
                continue;
            }
            FriendOrGroup item = friendListAdapter.getItem(checkedItemPositions.keyAt(i));
            if (item instanceof FriendItem) {
                friends.add((FriendItem) item);
            } else {
                friends.addAll(((FriendGroup) item).getMembers());
            }
        }
        return friends;
    }

    private boolean isItemChecked(FriendOrGroup item) {
        SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
        return checkedItemPositions.get(friendListAdapter.getPosition(item));
    }

    private void setConfirmButtonState() {
        boolean shouldEnable = listView.getCheckedItemCount() > 0;
        createPlanButtonWrapper.setVisibility(shouldEnable ? View.VISIBLE : View.GONE);
        createPlanButton.setEnabled(shouldEnable);
    }

    private void createGroup() {
        final Set<Long> friendIdsToBeGroup = new HashSet<Long>();
        final UserTableHelper tableHelper = new UserTableHelper(getActivity());
        final Collection<FriendItem> friends = getSelectedEntries();
        for (FriendItem item: friends) {
            friendIdsToBeGroup.add(tableHelper.getHachikoId(item.getLocalContactId()));
        }

        EditTextDialog.showDialog(getActivity(), "グループを作成", "グループ名", "作成",
                new EditTextDialog.PositiveButtonListener() {
                    @Override
                    public boolean onPositiveButtonClicked(DialogInterface dialog, String text) {
                        long id = tableHelper.createGroup(text, friendIdsToBeGroup, null);
                        FriendGroup group = new FriendGroup(
                                (int) id, text, null, new HashSet<FriendItem>(friends));
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
        listView.clearChoices();
        setConfirmButtonState();
    }

    private class FriendListAdapter extends ArrayAdapter<FriendOrGroup> {
        private int layoutResourceId;

        public FriendListAdapter(Context context, int resource, List<FriendOrGroup> entries) {
            super(context, resource, entries);
            layoutResourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new FriendItemView(getContext(), layoutResourceId);
            }
            FriendOrGroup item = getItem(position);
            ((FriendItemView) convertView).setItem(item);
            return convertView;
        }
    }

    private class FriendMultipleSuggestAdapter extends FriendListAdapter {
        private FriendMultipleSuggestAdapter(Context context, int resource, List<FriendOrGroup> entries) {
            super(context, resource, entries);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            ((FriendItemView) v).setChecked(isItemChecked(getItem(position)));
            return v;
        }
    }

    private class OnFriendItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CharSequence name = ((FriendItemView) view).getName();
            if (((FriendItemView) view).isChecked()) {
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
            boolean shouldBeChecked = !((FriendItemView) view).isChecked();
            listView.setItemChecked(
                    friendListAdapter.getPosition(suggestionAdapter.getItem(position)),
                    shouldBeChecked);
            if (!shouldBeChecked) {
                searchFriendView.removeName(((FriendItemView) view).getName());
            }
            setConfirmButtonState();
        }
    }

    private class OnFriendNameDeletedListener
            implements ChipsAutoCompleteTextView.OnNameDeletedListener {
        @Override
        public void onNameDeleted(String name) {
            unselectByName(name);
            setConfirmButtonState();
        }

        private void unselectByName(String name) {
            SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
            for (int i = 0; i < checkedItemPositions.size(); i++) {
                int position = checkedItemPositions.keyAt(i);
                if (!checkedItemPositions.get(position)) {
                    continue;
                }
                FriendOrGroup item = friendListAdapter.getItem(position);
                if (name.equals(item.getDisplayName())) {
                    listView.setItemChecked(position, false);
                }
            }
        }
    }
}
