package com.appspot.hachiko_schedule.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.appspot.hachiko_schedule.GuestPlanDetailActivity;
import com.appspot.hachiko_schedule.HostPlanDetailActivity;
import com.appspot.hachiko_schedule.R;

/**
 * List of plans.
 */
public class PlansFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.plans_list, container, false);
        ((Button) v.findViewById(R.id.host_detail)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HostPlanDetailActivity.class);
                startActivity(intent);
            }
        });

        ((Button) v.findViewById(R.id.guest_detail)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GuestPlanDetailActivity.class);
                startActivity(intent);
            }
        });
        return v;

    }
}
