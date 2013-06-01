package com.appspot.hachiko_schedule.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.ContactManager;
import com.appspot.hachiko_schedule.CreatePlanActivity;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.Friend;
import com.google.common.primitives.Longs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Friend list where user can choose friends to invite.
 */
public class FriendsFragment extends Fragment {
    private ArrayAdapter<String> adapter;
    private Map<Long, View> selectedFriendNameViews = new HashMap<Long, View>();

    private ListView listView;
    private View createPlanButtonWrapper;
    private HorizontalScrollView selectedFriendsNamesScrollView;
    private ViewGroup selectedFriendsNameContainer;
    private Button createPlanButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list, container, false);
        createPlanButtonWrapper = view.findViewById(R.id.new_plan_button_wrapper);
        selectedFriendsNamesScrollView
                = (HorizontalScrollView) view.findViewById(R.id.selected_friends_wrapper_scrollable);
        selectedFriendsNameContainer = (ViewGroup) view.findViewById(R.id.selected_friends);
        createPlanButton = (Button) view.findViewById(R.id.new_plan_button);
        listView = (ListView) view.findViewById(R.id.contact_list);

        createPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<Friend> friendsToInvite = new HashSet<Friend>();
                Intent intent = new Intent(getActivity(), CreatePlanActivity.class);
                int len = listView.getCount();
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                for (int i = 0; i < len; i++){
                    if (checked.get(i)) {
                        String item = adapter.getItem(i);
                        friendsToInvite.add(new Friend(item, "DummyPhoneNo", "Dummy email"));
                    }
                }
                intent.putExtra(
                        Constants.EXTRA_KEY_FRIENDS,
                        friendsToInvite.toArray(new Friend[0]));
                startActivityForResult(intent, 0);
            }
        });
        // TODO: implement a means for managing user info with their name.
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        adapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_multiple_choice);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnFriendItemClickListener());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ContactManager contactManager = new ContactManager(getActivity());
        contactManager.queryAllFriends(adapter);
    }

    @Override
    public void onPause() {
        listView.clearChoices();
        selectedFriendNameViews.clear();
        super.onPause();
    }

    private class OnFriendItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            boolean shouldEnable = listView.getCheckedItemCount() > 0;
            createPlanButtonWrapper.setVisibility(shouldEnable ? View.VISIBLE : View.GONE);
            createPlanButton.setEnabled(shouldEnable);
            if (selectedFriendNameViews.containsKey(id)) {
                View unselectedFriendNameView = selectedFriendNameViews.get(id);
                selectedFriendsNameContainer.removeView(unselectedFriendNameView);
                selectedFriendNameViews.remove(id);
            } else {
                addSelectedFriendNameView(id, (String) listView.getItemAtPosition(position));
            }
        }

        private void addSelectedFriendNameView(long friendId, String friendName) {
            TextView friendNameView = new TextView(getActivity());
            friendNameView.setText(friendName);
            friendNameView.setPadding(5, 3, 10, 3);
            selectedFriendsNameContainer.addView(friendNameView);
            selectedFriendsNamesScrollView.post(new Runnable() {
                @Override
                public void run() {
                    selectedFriendsNamesScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            });
            selectedFriendNameViews.put(friendId, friendNameView);
        }
    }
}
