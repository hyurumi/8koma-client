package com.appspot.hachiko_schedule;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.facebook.model.GraphUser;
import com.facebook.widget.HachikoFbFriendPickerFragment;
import com.facebook.widget.SimplifiedPickerFragment;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * 新しいイベントをつくるときに，招待する相手をFBの友達一覧から選ぶActivity
 */
public class NewEventChooseFbFriendActivity extends FragmentActivity {
    private HachikoFbFriendPickerFragment fragment;
    private Map<String, View> selectedFriendNameViews = new HashMap<String, View>();
    private View createPlanButtonWrapper;
    private HorizontalScrollView selectedFriendsNamesScrollView;
    private ViewGroup selectedFriendsNameContainer;
    private Button createPlanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = new HachikoFbFriendPickerFragment();
        fragmentManager.beginTransaction()
                .add(android.R.id.content, fragment, "Fb choose friend")
                .commit();
        fragment.setOnSelectionChangedListener(new SelectionChangedListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        fragment.loadData(false);

        if (createPlanButtonWrapper == null) {
            View fragmentRootView = fragment.getView();
            createPlanButtonWrapper = fragmentRootView.findViewById(R.id.new_plan_button_wrapper);
            selectedFriendsNamesScrollView = (HorizontalScrollView) fragmentRootView.findViewById(
                    R.id.selected_friends_wrapper_scrollable);
            selectedFriendsNameContainer
                    = (ViewGroup) fragmentRootView.findViewById(R.id.selected_friends);
            createPlanButton = (Button) fragmentRootView.findViewById(R.id.new_plan_button);
            createPlanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Set<FriendIdentifier> friendsToInvite = new HashSet<FriendIdentifier>();
                    Intent intent = new Intent(NewEventChooseFbFriendActivity.this, CreatePlanActivity.class);

                    List<GraphUser> selectedUsers
                            = NewEventChooseFbFriendActivity.this.fragment.getSelectedGraphObjects();
                    for (GraphUser user : selectedUsers) {
                        friendsToInvite.add(
                                // TODO: consider
                                // 1) this cast is appropriate?
                                // 2) using FB id is appropriate?
                                new FriendIdentifier(Long.parseLong(user.getId()), user.getName()));
                    }
                    intent.putExtra(
                            Constants.EXTRA_KEY_FRIENDS,
                            friendsToInvite.toArray(new FriendIdentifier[0]));
                    startActivityForResult(intent, 0);
                }
            });
        }
    }

    private class SelectionChangedListener
            implements SimplifiedPickerFragment.OnSelectionChangedListener {

        @Override
        public void onSelectionChanged(SimplifiedPickerFragment<?> fragment) {
            List<GraphUser> selectedUsersList
                    = NewEventChooseFbFriendActivity.this.fragment.getSelectedGraphObjects();
            Map<String, GraphUser> selectedUsers
                    = Maps.uniqueIndex(selectedUsersList, new Function<GraphUser, String>() {
                @Override
                public String apply(GraphUser graphUser) {
                    return graphUser.getId();
                }
            });
            // Sets.differenceは元のCollectionのviewを返すだけなので，ここでコピーしないと，別スレッドから
            // selectedFriendsNameViewを操作されたときにConcurrentErrorになる
            Collection<String> idsToAddView = new ArrayList<String>(Sets.difference(
                    selectedUsers.keySet(), selectedFriendNameViews.keySet()));
            Collection<String> idsToRemove = new ArrayList<String>(Sets.difference(
                    selectedFriendNameViews.keySet(), selectedUsers.keySet()));
            for (String id : idsToAddView) {
                addSelectedFriendNameView(id, selectedUsers.get(id).getName());
            }
            for (String id : idsToRemove) {
                View unselectedFriendNameView = selectedFriendNameViews.get(id);
                selectedFriendsNameContainer.removeView(unselectedFriendNameView);
                selectedFriendNameViews.remove(id);
            }

            boolean shouldEnable = !selectedFriendNameViews.isEmpty();
            createPlanButtonWrapper.setVisibility(shouldEnable ? View.VISIBLE : View.GONE);
            createPlanButton.setEnabled(shouldEnable);
        }

        // TODO: このへんFriendsFragmentからのひどいコピペなのをなんとかする
        private void addSelectedFriendNameView(String friendId, String friendName) {
            TextView friendNameView = new TextView(NewEventChooseFbFriendActivity.this);
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
