package tk.hachikoma.plans;

import android.app.Fragment;

public abstract class PlanFragmentBase extends Fragment {

    abstract protected void queryAndUpdatePlans();

    @Override
    public void onResume() {
        queryAndUpdatePlans();
        super.onResume();
    }
}
