package com.appspot.hachiko_schedule.plans;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.apis.JSONStringRequest;
import com.appspot.hachiko_schedule.apis.PlanAPI;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 自らが作成した予定の調整中のものを表すView
 */
public class UnfixedHostPlanView extends LinearLayout implements PlanView<UnfixedPlan> {
    private TextView titleView;
    private ViewGroup candidateDateContainer;
    private int numOfPotentialParticipants;
    private ProgressDialog progressDialog;

    public UnfixedHostPlanView(Context context) {
        super(context);
        init(context);
    }

    public UnfixedHostPlanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UnfixedHostPlanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        View layout = LayoutInflater.from(context).inflate(R.layout.unfixed_host_plan_view, this);
        titleView = (TextView) layout.findViewById(R.id.event_title);
        candidateDateContainer = (ViewGroup) layout.findViewById(R.id.candidate_date_container);
        progressDialog = new ProgressDialog(context);
    }

    @Override
    public UnfixedHostPlanView setPlan(UnfixedPlan plan) {
        titleView.setText(plan.getTitle());
        numOfPotentialParticipants = plan.getpotentialParticipants().size();
        candidateDateContainer.removeAllViews();
        List<CandidateDate> candidateDates = plan.getCandidateDates();
        Collections.sort(candidateDates, new Comparator<CandidateDate>() {
            @Override
            public int compare(CandidateDate lhs, CandidateDate rhs) {
                return (int) (lhs.getStartDate().getTime() - rhs.getStartDate().getTime());
            }
        });
        for (int i = 0; i < candidateDates.size(); i++) {
            CandidateDate candidateDate = candidateDates.get(i);
            CandidateDateView dateView = new CandidateDateView(getContext());
            dateView.setCandidate(candidateDate);
            candidateDateContainer.addView(dateView);
        }
        return this;
    }

    private class CandidateDateView extends LinearLayout {
        private TextView dateTextView;
        private TextView numOfPositiveFriendsView;
        private TextView numOfNeutralFriendsView;
        private TextView numOfNegativeFriendsView;
        private TextView positiveFriendNames;

        private CandidateDateView(Context context) {
            super(context);
            init(context);
        }

        private CandidateDateView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        private CandidateDateView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init(context);
        }

        private void init(Context context) {
            View layout = LayoutInflater.from(context).inflate(R.layout.date_answer_state_view, this);
            dateTextView = (TextView) layout.findViewById(R.id.event_title);
            numOfPositiveFriendsView = (TextView) layout.findViewById(R.id.num_of_ok);
            numOfNeutralFriendsView = (TextView) layout.findViewById(R.id.num_of_neutral);
            numOfNegativeFriendsView = (TextView) layout.findViewById(R.id.num_of_ng);
            positiveFriendNames = (TextView) layout.findViewById(R.id.positive_friend_names);
        }

        private void setCandidate(final CandidateDate candidateDate) {
            dateTextView.setText(candidateDate.getDateText());
            numOfPositiveFriendsView.setText(Integer.toString(candidateDate.getPositiveFriendsNum()));
            numOfNegativeFriendsView.setText(Integer.toString(candidateDate.getNegativeFriendsNum()));
            numOfNeutralFriendsView.setText(Integer.toString(numOfPotentialParticipants
                    - candidateDate.getPositiveFriendsNum() - candidateDate.getNegativeFriendsNum()));
            positiveFriendNames.setText(candidateDate.getPositiveFriendNames(getContext()));
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSendAnswerDialog(candidateDate.getAnswerId());
                }
            });
        }

        private void showSendAnswerDialog(final int answerId) {
            new AlertDialog.Builder(getContext())
                    .setMessage("「" + titleView.getText() + "」の日程を" + dateTextView.getText()
                            + "\nに確定しますか？")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendAnswer(answerId);
                        }
                    }).show();
        }

        private void sendAnswer(final int answerId) {
            progressDialog.setMessage("予定を確定中");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            HachikoLogger.debug("confirm: " + PlanAPI.CONFIRM.getUrl() + answerId);
            Request request = new JSONStringRequest(getContext(),
                    PlanAPI.CONFIRM.getMethod(),
                    PlanAPI.CONFIRM.getUrl() + answerId,
                    null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            HachikoLogger.debug("Confirmed");
                            progressDialog.hide();
                            Toast.makeText(getContext(), "サーバに予定の日程が登録されました．", Toast.LENGTH_SHORT).show();
                            // TODO: 確定した予定のView
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            progressDialog.hide();
                            HachikoDialogs.showNetworkErrorDialog(
                                    getContext(), volleyError);
                            HachikoLogger.error("confirm fail", volleyError);
                        }
                    });
            HachikoApp.defaultRequestQueue().add(request);
        }
    }
}
