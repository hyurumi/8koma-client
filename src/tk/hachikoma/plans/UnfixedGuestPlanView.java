package tk.hachikoma.plans;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import tk.hachikoma.HachikoApp;
import tk.hachikoma.R;
import tk.hachikoma.apis.HachikoAPI;
import tk.hachikoma.apis.base_requests.JSONStringRequest;
import tk.hachikoma.data.CandidateDate;
import tk.hachikoma.data.Event;
import tk.hachikoma.data.UnfixedPlan;
import tk.hachikoma.db.PlansTableHelper;
import tk.hachikoma.ui.HachikoDialogs;
import tk.hachikoma.util.HachikoLogger;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableBiMap;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static tk.hachikoma.data.CandidateDate.AnswerState;

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
        candidateDateContainer = (ViewGroup) layout.findViewById(R.id.candidate_date_container);
        plansTableHelper = new PlansTableHelper(context);
    }

    @Override
    public UnfixedGuestPlanView setPlan(UnfixedPlan plan) {
        titleView.setText(plan.getTitle());
        String source = "<b>" + plan.getOwnerName(getContext()) + "</b>, " + Joiner.on(", ").join(plan.getpotentialParticipants());
        participantsView.setText(Html.fromHtml(source));
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
            answerView.setCandidate(plan.getPlanId(), plan.getOwnerId(), candidateDate, i);
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
        private Typeface fontForAnswer;

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

                    if (CandidateDateAnswerView.this.candidateDate != null) {
                        CandidateDateAnswerView.this.candidateDate.setMyAnswerState(answerState);
                    }
                    sendResponse(planId, index, answerState);
                    numOfPositiveFriends.setText(Integer.toString(candidateDate.getPositiveFriendsNumWithSelf()));
                }
            });
            fontForAnswer = Typeface.createFromAsset( context.getAssets(), "fonts/fontawesome-webfont.ttf" );
            ((RadioButton)layout.findViewById(R.id.answer_ok)).setTypeface(fontForAnswer);
            ((RadioButton)layout.findViewById(R.id.answer_tentative)).setTypeface(fontForAnswer);
            ((RadioButton)layout.findViewById(R.id.answer_ng)).setTypeface(fontForAnswer);

            candidateText.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final View calendarView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.calendar_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder((Activity) getContext());
                    builder.setIcon(null);
                    builder.setTitle(R.string.recent_schedules);
                    builder.setView(calendarView);
                    EventManager eventManager = new EventManager(getContext());
                    Event prevEvent = eventManager.getPreviousEvent(candidateDate.getStartDate());
                    if (prevEvent != null) {
                        ((TextView) calendarView.findViewById(R.id.previous_event_text)).setText(
                                "前: " + prevEvent.toString());
                    }
                    ((TextView) calendarView.findViewById(R.id.candidate_date)).setText(
                            candidateText.getText() + "(回答待ち): " + titleView.getText());
                    Event nextEvent = eventManager.getNextEvent(candidateDate.getEndDate());
                    if (nextEvent != null) {
                        ((TextView) calendarView.findViewById(R.id.next_event_text)).setText(
                                "次: " + nextEvent.toString());
                    }
                    builder.setPositiveButton(R.string.icon_smile, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            answerRadioGroup.check(R.id.answer_ok);
                        }
                    });
                    builder.setNeutralButton(R.string.icon_question_sign, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            answerRadioGroup.check(R.id.answer_tentative);
                        }
                    });

                    builder.setNegativeButton(R.string.icon_frown, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            answerRadioGroup.check(R.id.answer_ng);
                        }
                    });

                    AlertDialog dialog = builder.create();

                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTypeface(fontForAnswer);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(24);
                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTypeface(fontForAnswer);
                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize(24);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTypeface(fontForAnswer);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(24);
                }
            });
        }

        private void setCandidate(long planId, long ownerId, CandidateDate candidateDate, int index) {
            this.planId = planId;
            this.candidateDate = candidateDate;
            this.index = index;
            candidateText.setText(candidateDate.getDateText());
            numOfPositiveFriends.setText(Integer.toString(candidateDate.getPositiveFriendsNumWithSelf()));
            positiveFriendNamesView.setText(
                    candidateDate.getPositiveFriendNames(getContext(), ownerId));
            lastPersistedState = candidateDate.getMyAnswerState();
            answerRadioGroup.check(BUTTON_TO_ANSWER.inverse().get(lastPersistedState));
        }

        private void sendResponse(long planId, long answerId, final AnswerState answerState) {
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
                    HachikoAPI.Plan.RESPOND.getMethod(),
                    HachikoAPI.Plan.RESPOND.getUrl(),
                    param,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            CandidateDateAnswerView.this.lastPersistedState = answerState;
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
            request.setRetryPolicy(HachikoAPI.RETRY_POLICY_LONG);
            HachikoApp.defaultRequestQueue().add(request);
        }
    }
}
