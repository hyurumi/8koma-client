package com.appspot.hachiko_schedule.plans;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.google.common.base.Joiner;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 自らが作成した予定の調整中のものを表すView
 */
public class UnfixedHostPlanView extends LinearLayout implements PlanView<UnfixedPlan> {
    public static interface OnConfirmListener {
        /**
         * ユーザがある日程を確定しようとしたら呼ばれる
         * @param candidateDate
         */
        public void onConfirm(UnfixedPlan unfixedPlan, CandidateDate candidateDate);
    }

    public static interface OnDemandButtonClickListener {
        public void onDemandButtonClicked(long planId);
    }

    private long planId;
    private TextView titleView;
    private ViewGroup candidateDateContainer;
    private OnConfirmListener onConfirmListener;
    private OnDemandButtonClickListener onDemandButtonClickListener;

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
    }

    @Override
    public UnfixedHostPlanView setPlan(UnfixedPlan plan) {
        planId = plan.getPlanId();
        titleView.setText(plan.getTitle());
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
            dateView.setCandidate(plan, candidateDate);
            candidateDateContainer.addView(dateView);
        }
        ((TextView) findViewById(R.id.event_potential_participants)).setText(
                Joiner.on(", ").join(plan.getpotentialParticipants()));
        Typeface fontForArrow= Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont.ttf");
        ((TextView)findViewById(R.id.icon_demand)).setTypeface(fontForArrow);
        findViewById(R.id.right_top_button).setVisibility(VISIBLE);
        findViewById(R.id.right_top_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDemandButtonClickListener != null) {
                    onDemandButtonClickListener.onDemandButtonClicked(planId);
                }
            }
        });
        return this;
    }

    public void setOnConfirmListener(OnConfirmListener confirmListener) {
        this.onConfirmListener = confirmListener;
    }

    public void setOnDemandButtonClickListener(OnDemandButtonClickListener onDemandButtonClickListener) {
        this.onDemandButtonClickListener = onDemandButtonClickListener;
    }

    private class CandidateDateView extends LinearLayout {
        private TextView dateTextView;
        private TextView numOfPositiveFriendsView;
        //private TextView numOfNeutralFriendsView;
        //private TextView numOfNegativeFriendsView;
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
            positiveFriendNames = (TextView) layout.findViewById(R.id.positive_friend_names);
        }

        private void setCandidate(final UnfixedPlan unfixedPlan, final CandidateDate candidateDate) {
            dateTextView.setText(candidateDate.getDateText());
            long myId = HachikoPreferences.getMyHachikoId(getContext());
            numOfPositiveFriendsView.setText(Integer.toString(candidateDate.getPositiveFriendsNum(myId)));
            positiveFriendNames.setText(candidateDate.getPositiveFriendNames(
                    getContext(), myId));
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmAnswerDialog(unfixedPlan, candidateDate);
                }
            });
        }

        private void confirmAnswerDialog(
                final UnfixedPlan unfixedPlan, final CandidateDate candidateDate) {
            new AlertDialog.Builder(getContext())
                    .setMessage("「" + titleView.getText() + "」の日程を" + dateTextView.getText()
                            + "\nに確定しますか？")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onConfirmListener.onConfirm(unfixedPlan, candidateDate);
                        }
                    }).show();
        }

    }
}
