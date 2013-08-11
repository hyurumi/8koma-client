package com.appspot.hachiko_schedule.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appspot.hachiko_schedule.*;
import com.appspot.hachiko_schedule.data.Friend;

import java.util.*;

/**
 * Friend list where user can choose friends to invite.
 */
public class FriendsFragment extends Fragment {
    private FriendsAdapter adapter;
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
                for (FriendsAdapter.Entry entry: adapter.getSelectedEntries()) {
                    friendsToInvite.add(new Friend(entry.getDisplayName(),  "DummyPhoneNo", "Dummy email"));
                }
                intent.putExtra(
                        Constants.EXTRA_KEY_FRIENDS,
                        friendsToInvite.toArray(new Friend[0]));
                startActivityForResult(intent, 0);
            }
        });
        // TODO: implement a means for managing user info with their name.
        ContactManager manager = ContactManager.getInstance(getActivity());
        adapter = new FriendsAdapter(getActivity(), R.layout.list_item_friend,
                manager.getListOfContactEntries());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnFriendItemClickListener());
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class OnFriendItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (adapter.notifySelect(view, position)) {

                addSelectedFriendNameView(
                        id,
                        ((FriendsAdapter.Entry)
                                listView.getItemAtPosition(position)).getDisplayName());
            } else {
                View unselectedFriendNameView = selectedFriendNameViews.get(id);
                selectedFriendsNameContainer.removeView(unselectedFriendNameView);
                selectedFriendNameViews.remove(id);
            }
            boolean shouldEnable = !selectedFriendNameViews.isEmpty();
            createPlanButtonWrapper.setVisibility(shouldEnable ? View.VISIBLE : View.GONE);
            createPlanButton.setEnabled(shouldEnable);
        }

        private void addSelectedFriendNameView(long friendId, String friendName) {
            TextView friendNameView = new TextView(getActivity());
            friendNameView.setText(friendName);
            friendNameView.setPadding(7, 5, 10, 5);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 20;
            friendNameView.setLayoutParams(params);
            friendNameView.setBackgroundColor(Color.rgb(230, 230, 230));
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
