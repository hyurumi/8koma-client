package com.appspot.hachiko_schedule.friends;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.FriendGroup;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.data.FriendOrGroup;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.plans.CreatePlanActivity;
import com.appspot.hachiko_schedule.ui.EditTextDialog;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.util.*;

/**
 * FriendIdentifier list where user can choose friends to invite.
 */
public class FriendsFragment extends Fragment {
    private FriendListAdapter friendListAdapter;

    private ListView listView;
    private View createPlanButtonWrapper;
    private ChipsFriendNameEditText searchFriendView;
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

        searchFriendView = (ChipsFriendNameEditText) view.findViewById(R.id.search_friend);
        searchFriendView.registerListViewAndAdapter(listView, friendListAdapter);
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
                searchFriendView.removeItem(item);
                friendListAdapter.remove(item);
                UserTableHelper tableHelper = new UserTableHelper(getActivity());
                tableHelper.deleteGroup(item.getId());
            default:
                return super.onContextItemSelected(menuItem);
        }
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
        for (FriendOrGroup item: searchFriendView.getSelectedItems()) {
            if (item instanceof FriendItem) {
                friends.add((FriendItem) item);
            } else {
                friends.addAll(((FriendGroup) item).getMembers());
            }
        }
        return friends;
    }


    private void setConfirmButtonState() {
        boolean shouldEnable = searchFriendView.getSelectedItems().size() > 0;
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

    protected class FriendListAdapter extends ArrayAdapter<FriendOrGroup> {
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
            HachikoLogger.debug(item + " " + searchFriendView.getSelectedItems().contains(item)
                    + " " + searchFriendView.getSelectedItems().toString());
            ((FriendItemView) convertView).setChecked(
                    searchFriendView.getSelectedItems().contains(item));
            HachikoLogger.debug(item + " " + searchFriendView.getSelectedItems().contains(item)
                    + " " + searchFriendView.getSelectedItems().toString());
            return convertView;
        }
    }

    private class OnFriendItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            FriendOrGroup item = friendListAdapter.getItem(position);
            searchFriendView.toggleSelection(item);
            setConfirmButtonState();
        }
    }

    private class OnFriendNameDeletedListener
            implements ChipsFriendNameEditText.OnNameDeletedListener {
        @Override
        public void onNameDeleted(FriendOrGroup item) {
            listView.setItemChecked(friendListAdapter.getPosition(item), false);
            setConfirmButtonState();
        }
    }
}
