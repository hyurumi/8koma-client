package com.appspot.hachiko_schedule.plans;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.Plan;

/**
 * 確定した予定を表すView
 */
public class FixedPlanView extends LinearLayout implements PlanView {
    private TextView titleView;
    private TextView participantsView;
    private TextView dateView;

    public FixedPlanView(Context context) {
        super(context);
        init(context);
    }

    public FixedPlanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FixedPlanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        View layout = LayoutInflater.from(context).inflate(R.layout.unfixed_host_plan_view, this);
        titleView = (TextView) layout.findViewById(R.id.event_title);
        participantsView = (TextView) layout.findViewById(R.id.event_participants);
        dateView = (TextView) layout.findViewById(R.id.event_date);
    }

    @Override
    public PlanView setPlan(Plan plan) {
        titleView.setText(plan.getTitle());
        return null;
    }
}
