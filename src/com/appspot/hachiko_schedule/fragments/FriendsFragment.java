package com.appspot.hachiko_schedule.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.ContactManager;
import com.appspot.hachiko_schedule.CreatePlanActivity;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.Friend;

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
                // TODO set selected Friend info
                Intent intent = new Intent(getActivity(), CreatePlanActivity.class);
                intent.putExtra(
                        Constants.EXTRA_KEY_FRIENDS,
                        new Friend[] {new Friend("Toriaezu Ugokasu", "090-1111-2222", "hoge@fuga")});
                startActivityForResult(intent, 0);
            }
        });
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
