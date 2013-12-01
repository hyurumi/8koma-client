package com.appspot.hachiko_schedule.plans;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.db.PlansTableHelper;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hyurumi
 * Date: 11/28/13
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class UnfixedGuestPlansFragment extends PlanFragmentBase{

    private PlanAdapter planAdapter;
    private PlansTableHelper plansTableHelper;
    private ListView eventList;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unfixed_guest_plans, container, false);
        eventList = (ListView)view.findViewById(R.id.unfixed_guest_plans_list);
        plansTableHelper = new PlansTableHelper(this.getActivity());
        return view;
    }
    @Override
    protected void queryAndUpdatePlans() {
        List<Plan> plans = plansTableHelper.queryUnfixedGuestPlans();
        planAdapter = new PlanAdapter(this.getActivity(), plans);
        eventList.setAdapter(planAdapter);
    }
}
