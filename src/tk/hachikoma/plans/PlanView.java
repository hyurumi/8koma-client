package tk.hachikoma.plans;

import tk.hachikoma.data.Plan;

public interface PlanView<T extends Plan> {
    public PlanView setPlan(T plan);
}
