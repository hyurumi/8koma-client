package com.appspot.hachiko_schedule.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.appspot.hachiko_schedule.CreatePlanActivity;
import com.appspot.hachiko_schedule.R;

/**
 * Friend list where user can choose friends to invite.
 */
public class FriendsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list, container, false);

        ((Button) view.findViewById(R.id.new_plan_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreatePlanActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        return view;
    }
}
