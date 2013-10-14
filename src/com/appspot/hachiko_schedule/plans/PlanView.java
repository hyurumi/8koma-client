package com.appspot.hachiko_schedule.plans;

import com.appspot.hachiko_schedule.data.Plan;

public interface PlanView<T extends Plan> {
    public PlanView setPlan(T plan);
}
