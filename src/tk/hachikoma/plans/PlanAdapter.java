package tk.hachikoma.plans;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import tk.hachikoma.R;
import tk.hachikoma.data.FixedPlan;
import tk.hachikoma.data.Plan;
import tk.hachikoma.data.UnfixedPlan;

import java.util.List;

/**
 * {@link PlanListActivity}で使われる，予定一覧を表示するためのAdapter
 */
class PlanAdapter extends ArrayAdapter<Plan> {
    private final List<Plan> plans;
    private final UnfixedHostPlanView.OnConfirmListener onConfirmListener;
    private final UnfixedHostPlanView.OnDemandButtonClickListener onDemandButtonClickListener;


    public PlanAdapter(Context context, List<Plan> plans) {
        this(context, plans, null, null);
    }

    public PlanAdapter(
            Context context,
            List<Plan> plans,
            UnfixedHostPlanView.OnConfirmListener onConfirmListener,
            UnfixedHostPlanView.OnDemandButtonClickListener onDemandButtonClickListener) {
        super(context, R.layout.unfixed_guest_plan_view, plans);
        this.plans = plans;
        this.onConfirmListener = onConfirmListener;
        this.onDemandButtonClickListener = onDemandButtonClickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Plan plan = plans.get(position);
        if (plan.isFixed() && (convertView == null || !(convertView instanceof FixedPlanView))) {
            convertView = new FixedPlanView(getContext());
        } else if (!plan.isFixed() && plan.isHost(getContext())
                && (convertView == null || !(convertView instanceof UnfixedHostPlanView))) {
            convertView = new UnfixedHostPlanView(getContext());
            ((UnfixedHostPlanView) convertView).setOnConfirmListener(onConfirmListener);
            ((UnfixedHostPlanView) convertView)
                    .setOnDemandButtonClickListener(onDemandButtonClickListener);
        } else if (!plan.isFixed() && !plan.isHost(getContext())
            && (convertView == null || !(convertView instanceof UnfixedGuestPlanView))) {
            convertView = new UnfixedGuestPlanView(getContext());
        }
        ((PlanView) convertView).setPlan(plan);
        return convertView;
    }

    public void updatePlan(UnfixedPlan unfixedPlan, FixedPlan fixedplan) {
        plans.remove(getPosition(unfixedPlan));
        //plans.set(getPosition(unfixedPlan), fixedplan);
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
