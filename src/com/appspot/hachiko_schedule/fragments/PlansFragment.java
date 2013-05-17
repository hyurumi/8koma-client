package com.appspot.hachiko_schedule.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.appspot.hachiko_schedule.R;

/**
 * List of plans.
 */
public class PlansFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.friend_list, container, false);
    }
}
