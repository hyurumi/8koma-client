package com.appspot.hachiko_schedule.plans;

import android.app.Fragment;

public abstract class PlanFragmentBase extends Fragment {

    abstract protected void queryAndUpdatePlans();

    @Override
    public void onResume() {
        queryAndUpdatePlans();
        super.onResume();
    }
}
