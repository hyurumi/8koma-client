package com.appspot.hachiko_schedule.plans;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.FixedPlan;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.data.UnfixedPlan;

/**
 * {@link PlanListActivity}で使われる，予定一覧を表示するためのAdapter
 */
class PlanAdapter extends ArrayAdapter<Plan> {
    private final Plan[] plans;
    private final UnfixedHostPlanView.OnConfirmListener onConfirmListener;

    public PlanAdapter(
            Context context, Plan[] plans, UnfixedHostPlanView.OnConfirmListener onConfirmListener) {
        super(context, R.layout.unfixed_guest_plan_view, plans);
        this.plans = plans;
        this.onConfirmListener = onConfirmListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Plan plan = plans[position];
        if (plan.isFixed() && (convertView == null || !(convertView instanceof FixedPlanView))) {
            convertView = new FixedPlanView(getContext());
        } else if (!plan.isFixed() && plan.isHost()
                && (convertView == null || !(convertView instanceof UnfixedHostPlanView))) {
            convertView = new UnfixedHostPlanView(getContext());
            ((UnfixedHostPlanView) convertView).setOnConfirmListener(onConfirmListener);
        } else if (!plan.isFixed() && !plan.isHost()
            && (convertView == null || !(convertView instanceof UnfixedGuestPlanView))) {
            convertView = new UnfixedGuestPlanView(getContext());
        }
        ((PlanView) convertView).setPlan(plan);
        return convertView;
    }

    public void updatePlan(UnfixedPlan unfixedPlan, FixedPlan fixedplan) {
        plans[getPosition(unfixedPlan)] = fixedplan;
        notifyDataSetChanged();
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
