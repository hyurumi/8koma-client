package com.appspot.hachiko_schedule.plans;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.appspot.hachiko_schedule.R;

/**
 * Created with IntelliJ IDEA.
 * User: hyurumi
 * Date: 11/28/13
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class UnfixedGuestPlansFragment extends Fragment{
    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_unfixed_guest_plans, container, false);
    }
}
