package com.appspot.hachiko_schedule.plans;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.Plan;

/**
 * {@link PlanListActivity}で使われる，予定一覧を表示するためのAdapter
 */
class PlanAdapter extends ArrayAdapter<Plan> {
    private final Plan[] plans;

    public PlanAdapter(
            Context context, Plan[] plans) {
        super(context, R.layout.unfixed_guest_plan_view, plans);
        this.plans = plans;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Plan plan = plans[position];
        if (plan.isFixed() && (convertView == null || !(convertView instanceof FixedPlanView))) {
            convertView = new FixedPlanView(getContext());
        } else if (!plan.isFixed() && plan.isHost()
                && (convertView == null || !(convertView instanceof UnfixedHostPlanView))) {
            convertView = new UnfixedHostPlanView(getContext());
        } else if (!plan.isFixed() && !plan.isHost()
            && (convertView == null || !(convertView instanceof UnfixedGuestPlanView))) {
            convertView = new UnfixedGuestPlanView(getContext());
        }
        ((PlanView) convertView).setPlan(plan);
        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
