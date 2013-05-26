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

import java.util.HashSet;
import java.util.Set;

/**
 * Friend list where user can choose friends to invite.
 */
public class FriendsFragment extends Fragment {
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private Button createPlanButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list, container, false);

        createPlanButton = (Button) view.findViewById(R.id.new_plan_button);
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
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice);
        listView = (ListView) view.findViewById(R.id.contact_list);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createPlanButton.setEnabled(listView.getCheckedItemCount() > 0);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ContactManager contactManager = new ContactManager(getActivity());
        contactManager.queryAllFriends(adapter);
    }
}
