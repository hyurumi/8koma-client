package com.appspot.hachiko_schedule.plans;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.apis.HachikoAPI;
import com.appspot.hachiko_schedule.apis.base_requests.JSONStringRequest;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.FixedPlan;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hyurumi
 * Date: 11/28/13
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class UnfixedHostPlansFragment extends PlanFragmentBase
        implements UnfixedHostPlanView.OnConfirmListener {

    private PlanAdapter planAdapter;
    private PlansTableHelper plansTableHelper;
    private ListView eventList;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(this.getActivity());
        View view = inflater.inflate(R.layout.fragment_unfixed_host_plans, container, false);
        eventList = (ListView)view.findViewById(R.id.unfixed_host_plans_list);
        plansTableHelper = new PlansTableHelper(this.getActivity());
        return view;
    }

    @Override
    protected void queryAndUpdatePlans() {
        List<Plan> plans = plansTableHelper.queryUnfixedHostPlans();
        planAdapter = new PlanAdapter(this.getActivity(), plans, this,
                new UnfixedHostPlanView.OnDemandButtonClickListener() {
                    @Override
                    public void onDemandButtonClicked(final long planId) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage("未回答者に催促しますか？")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        showProgressDialog("通信中", false);
                                        sendDemand(planId);
                                    }
                                }).show();
                    }});
        eventList.setAdapter(planAdapter);
    }

    @Override
    public void onConfirm(final UnfixedPlan unfixedPlan, final CandidateDate candidateDate) {
        int answerId = candidateDate.getAnswerId();
        showProgressDialog("予定を確定中", false);
        HachikoLogger.debug("confirm: " + HachikoAPI.Plan.CONFIRM.getUrl() + answerId);
        Request request = new JSONStringRequest(UnfixedHostPlansFragment.this.getActivity(),
                HachikoAPI.Plan.CONFIRM.getMethod(),
                HachikoAPI.Plan.CONFIRM.getUrl() + answerId,
                null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        HachikoLogger.debug("Confirmed");
                        FixedPlan fixedPlan = new FixedPlan(
                                unfixedPlan.getPlanId(), unfixedPlan.getTitle(),
                                HachikoPreferences.getMyHachikoId(UnfixedHostPlansFragment.this.getActivity()),
                                candidateDate);
                        plansTableHelper.confirmCandidateDate(
                                unfixedPlan.getPlanId(), candidateDate.getAnswerId());
                        planAdapter.updatePlan(unfixedPlan, fixedPlan);
                        progressDialog.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.hide();
                        HachikoDialogs.showNetworkErrorDialog(
                                getActivity(), volleyError);
                        HachikoLogger.error("confirm fail", volleyError);
                    }
                });
        request.setRetryPolicy(HachikoAPI.RETRY_POLICY_LONG);
        HachikoApp.defaultRequestQueue().add(request);
    }

    private void showProgressDialog(String message, boolean cancelable) {
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    private void sendDemand(long planId) {
        Request request = new JSONStringRequest(
                getActivity(),
                HachikoAPI.Plan.DEMAND.getMethod(),
                HachikoAPI.Plan.DEMAND.getUrl() + planId,
                null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        HachikoLogger.debug("demand successfully sent");
                        progressDialog.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoDialogs.showNetworkErrorDialog(getActivity(), volleyError, "催促送信");
                        HachikoLogger.error("respond fail", volleyError);
                    }
                });
        HachikoApp.defaultRequestQueue().add(request);
    }
}
