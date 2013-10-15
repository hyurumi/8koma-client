package com.appspot.hachiko_schedule.plans;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.apis.JSONStringRequest;
import com.appspot.hachiko_schedule.apis.PlanAPI;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableBiMap;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.appspot.hachiko_schedule.data.CandidateDate.AnswerState;

/**
 * 調整中の予定の，誘われた人にとっての見え方を表すView
 */
public class UnfixedGuestPlanView extends LinearLayout implements PlanView<UnfixedPlan> {
    private TextView titleView;

    private TextView participantsView;
    private ViewGroup candidateDateContainer;
    private PlansTableHelper plansTableHelper;

    public UnfixedGuestPlanView(Context context) {
        super(context);
        init(context);
    }

    public UnfixedGuestPlanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UnfixedGuestPlanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        View layout = LayoutInflater.from(context).inflate(R.layout.unfixed_guest_plan_view, this);
        titleView = (TextView) layout.findViewById(R.id.event_title);
        participantsView = (TextView) layout.findViewById(R.id.event_potential_participants);
        layout.findViewById(R.id.event_show_detail_button)
                .setOnClickListener(new OnExpandButtonClick());
        candidateDateContainer = (ViewGroup) layout.findViewById(R.id.candidate_date_container);
        plansTableHelper = new PlansTableHelper(context);
    }

    @Override
    public UnfixedGuestPlanView setPlan(UnfixedPlan plan) {
        titleView.setText(plan.getTitle());
        participantsView.setText(Joiner.on(", ").join(plan.getpotentialParticipants()));
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
            CandidateDateAnswerView answerView = new CandidateDateAnswerView(getContext());
            answerView.setCandidate(plan.getPlanId(), candidateDate, i);
            candidateDateContainer.addView(answerView);
        }
        View v = new View(getContext());
        candidateDateContainer.addView(v);
        return this;
    }

    private class CandidateDateAnswerView extends RelativeLayout {
        private final ImmutableBiMap<Integer, AnswerState> BUTTON_TO_ANSWER = ImmutableBiMap.of(
                R.id.answer_ok, AnswerState.OK,
                R.id.answer_tentative, AnswerState.NEUTRAL,
                R.id.answer_ng, AnswerState.NG);
        private TextView candidateText;
        private TextView numOfPositiveFriends;
        private TextView positiveFriendNamesView;
        private RadioGroup answerRadioGroup;
        private CandidateDate candidateDate;
        private int index;
        private long planId;
        private AnswerState lastPersistedState;

        private CandidateDateAnswerView(Context context) {
            super(context);
            init(context);
        }

        private CandidateDateAnswerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        private CandidateDateAnswerView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init(context);
        }

        private void init(Context context) {
            View layout = LayoutInflater.from(context).inflate(R.layout.date_answer_view, this);
            numOfPositiveFriends = (TextView) layout.findViewById(R.id.date_candidate_answer_yes);
            positiveFriendNamesView = (TextView) layout.findViewById(R.id.positive_friend_names);
            candidateText = (TextView) layout.findViewById(R.id.candidate_date_body);
            answerRadioGroup = (RadioGroup) layout.findViewById(R.id.answer_selections);
            answerRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    AnswerState answerState = BUTTON_TO_ANSWER.get(checkedId);
                    if (lastPersistedState == null // RadioGroupの初期化時に呼ばれるのを防ぐ
                            || lastPersistedState == answerState) {
                        return;
                    }
                    plansTableHelper.updateOwnAnswer(planId, candidateDate.getAnswerId(), answerState);
                    sendResponse(planId, index, answerState);
                }
            });
        }

        private void setCandidate(long planId, CandidateDate candidateDate, int index) {
            this.planId = planId;
            this.candidateDate = candidateDate;
            this.index = index;
            candidateText.setText(candidateDate.getDateText());
            numOfPositiveFriends.setText(Integer.toString(candidateDate.getPositiveFriendsNum()));
            positiveFriendNamesView.setText(candidateDate.getPositiveFriendNames(getContext()));
            lastPersistedState = candidateDate.getMyAnswerState();
            answerRadioGroup.check(BUTTON_TO_ANSWER.inverse().get(lastPersistedState));
        }
    }

    private void sendResponse(long planId, long answerId, AnswerState answerState) {
        JSONObject param = new JSONObject();
        try {
            param.put("planId", planId);
            JSONObject responses = new JSONObject();
            responses.put(Long.toString(answerId), answerState.toString());
            param.put("responses", responses);
        } catch (JSONException e) {
            HachikoLogger.error("JSONERROR", e);
            return;
        }
        HachikoLogger.debug("respond", param);
        Request request = new JSONStringRequest(
                getContext(),
                PlanAPI.RESPOND.getMethod(),
                PlanAPI.RESPOND.getUrl(),
                param,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        HachikoLogger.debug("respond success: ", s);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoDialogs.showNetworkErrorDialog(
                                (Activity) getContext(), volleyError, "回答");
                        HachikoLogger.error("respond", volleyError);
                    }
                });
        HachikoApp.defaultRequestQueue().add(request);
    }

    private class OnExpandButtonClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO
            Toast.makeText(getContext(), "未実装", Toast.LENGTH_SHORT).show();
        }
    }
}
