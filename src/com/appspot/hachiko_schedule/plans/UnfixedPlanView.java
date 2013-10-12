package com.appspot.hachiko_schedule.plans;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.ui.HorizontalSwipeListener;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Joiner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.appspot.hachiko_schedule.data.CandidateDate.AnswerState;

/**
 * 調整中の予定を表すView
 */
public class UnfixedPlanView extends LinearLayout {
    private TextView titleView;
    private TextView participantsView;
    private ViewGroup candidateDateContainer;
    private PlansTableHelper plansTableHelper;

    public UnfixedPlanView(Context context) {
        super(context);
        init(context);
    }

    public UnfixedPlanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UnfixedPlanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        View layout = LayoutInflater.from(context).inflate(R.layout.unfixed_plan_view, this);
        titleView = (TextView) layout.findViewById(R.id.event_title);
        participantsView = (TextView) layout.findViewById(R.id.event_potential_participants);
        layout.findViewById(R.id.event_show_detail_button)
                .setOnClickListener(new OnExpandButtonClick());
        candidateDateContainer = (ViewGroup) layout.findViewById(R.id.candidate_date_container);
        plansTableHelper = new PlansTableHelper(context);
    }

    public UnfixedPlanView setPlan(UnfixedPlan plan) {
        titleView.setText(plan.getTitle());
        participantsView.setText(Joiner.on(", ").join(plan.getpotentialParticipants()));
        candidateDateContainer.removeAllViews();
        for (CandidateDate candidateDate: plan.getCandidateDates()) {
            CandidateDateAnswerView answerView = new CandidateDateAnswerView(getContext());
            answerView.setCandidate(plan.getPlanId(), candidateDate);
            candidateDateContainer.addView(answerView);
        }
        View v = new View(getContext());
        candidateDateContainer.addView(v);
        return this;
    }

    private class CandidateDateAnswerView extends RelativeLayout {
        private TextView numOfNgText;
        private TextView numOfOkText;
        private TextView candidateText;
        private CandidateDate candidateDate;
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
            numOfNgText = (TextView) layout.findViewById(R.id.date_candidate_answer_no);
            numOfOkText = (TextView) layout.findViewById(R.id.date_candidate_answer_yes);
            candidateText = (TextView) layout.findViewById(R.id.candidate_date_body);
            candidateText.setOnTouchListener(new HorizontalSwipeListener(getContext()) {

                @Override
                protected void onSwipeMove(View v, MotionEvent e) {
                    float deltaX = e.getX() + v.getTranslationX() - getSwipeStartX();
                    if (Math.abs(deltaX / v.getWidth()) > 0.1) {
                        setAnswerState(deltaX > 0 ? AnswerState.OK : AnswerState.NG);
                    } else {
                        setAnswerState(AnswerState.NEUTRAL);
                    }
                }

                @Override
                protected boolean onSwipeEnd(View v, MotionEvent e) {
                    return false;
                }

                @Override
                protected void onTouchEnd(View v, MotionEvent e, boolean swiping) {
                    if (!swiping) {
                        setAnswerState(AnswerState.NEUTRAL);
                    }
                    persistCurrentState();
                }
            });

            numOfNgText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAnswerState(AnswerState.NG);
                    persistCurrentState();
                }
            });
            numOfOkText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAnswerState(AnswerState.OK);
                    persistCurrentState();
                }
            });
        }

        private void setCandidate(long planId, CandidateDate candidateDate) {
            this.planId = planId;
            this.candidateDate = candidateDate;
            updateTextAndBgColor();
        }

        private void updateTextAndBgColor() {
            candidateText.setText(candidateDate.getDateText());
            numOfNgText.setText(Integer.toString(candidateDate.getNegativeFriendsNum())
                    + (candidateDate.getMyAnswerState() == AnswerState.NG ? 1 : 0));
            numOfOkText.setText(Integer.toString(candidateDate.getPositiveFriendsNum())
                    + (candidateDate.getMyAnswerState() == AnswerState.OK ? 1 : 0));
            candidateText.setBackgroundColor(
                    getResources().getColor(candidateDate.getMyAnswerState().getColorResource()));
        }

        private void setAnswerState(AnswerState answerState) {
            if (answerState == candidateDate.getMyAnswerState()) {
                return;
            }
            candidateDate.setMyAnswerState(answerState);
            updateTextAndBgColor();
        }

        private void persistCurrentState() {
            AnswerState answerState = candidateDate.getMyAnswerState();
            if (lastPersistedState == answerState) {
                return;
            }
            plansTableHelper.updateOwnAnswer(planId, candidateDate.getAnswerId(), answerState);
            sendResponse(planId, candidateDate.getAnswerId(), answerState);
            lastPersistedState = answerState;
        }
    }

    private void sendResponse(long planId, long answerId, AnswerState answerState) {
        JSONObject param = new JSONObject();
        try {
            param.put("planId", planId);
            JSONArray responses = new JSONArray();
            JSONObject response = new JSONObject();
            response.put(Long.toString(answerId), answerState.toString());
            responses.put(response);
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
