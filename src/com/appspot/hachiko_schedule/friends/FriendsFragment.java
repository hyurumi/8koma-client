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
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.plans.CreatePlanActivity;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.util.*;

/**
 * FriendIdentifier list where user can choose friends to invite.
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
                Set<FriendIdentifier> friendsToInvite = new HashSet<FriendIdentifier>();
                Intent intent = new Intent(getActivity(), CreatePlanActivity.class);
                UserTableHelper tableHelper = new UserTableHelper(getActivity());
                for (FriendItem entry: adapter.getSelectedEntries()) {
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
        adapter = new FriendsAdapter(getActivity(), R.layout.list_item_friend, getListOfFriends());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnFriendItemClickListener());
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

    private class OnFriendItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (adapter.notifySelect(view, position)) {

                addSelectedFriendNameView(
                        id,
                        ((FriendItem) listView.getItemAtPosition(position)).getDisplayName());
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
            friendNameView.setPadding(
                    (int) getResources().getDimension(R.dimen.friend_name_text_padding_left),
                    (int) getResources().getDimension(R.dimen.friend_name_text_padding_top),
                    (int) getResources().getDimension(R.dimen.friend_name_text_padding_right),
                    (int) getResources().getDimension(R.dimen.friend_name_text_padding_bottom));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin =
                    (int) getResources().getDimension(R.dimen.friend_name_text_margin_left);
            friendNameView.setLayoutParams(params);
            friendNameView.setBackgroundColor(getResources().getColor(R.color.friend_name_gray));
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
