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
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.ui.HorizontalSwipeListener;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Joiner;

/**
 * 調整中の予定を表すView
 */
public class UnfixedPlanView extends LinearLayout {
    private TextView titleView;
    private TextView participantsView;
    private ViewGroup candidateDateContainer;

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
    }

    public UnfixedPlanView setPlan(UnfixedPlan plan) {
        titleView.setText(plan.getTitle());
        participantsView.setText(Joiner.on(", ").join(plan.getpotentialParticipants()));
        candidateDateContainer.removeAllViews();
        for (CandidateDate candidateDate: plan.getCandidateDates()) {
            CandidateDateAnswerView answerView = new CandidateDateAnswerView(getContext());
            answerView.setCandidate(candidateDate);
            candidateDateContainer.addView(answerView);
            HachikoLogger.debug("add candidate " + candidateDate.getDateText());
        }
        View v = new View(getContext());
        candidateDateContainer.addView(v);
        return this;
    }

    protected int calcMeasuredSize(int measureSpec, int desired) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            return size;
        } else if (mode == MeasureSpec.AT_MOST) {
            return Math.min(size, desired);
        } else {
            return desired;
        }
    }

    private class CandidateDateAnswerView extends RelativeLayout {
        private TextView numOfNgText;
        private TextView numOfOkText;
        private TextView candidateText;

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
            setAnswerState(AnswerState.NEUTRAL);
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
            });

            numOfNgText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAnswerState(AnswerState.NG);
                }
            });
            numOfOkText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAnswerState(AnswerState.OK);
                }
            });
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int width = calcMeasuredSize(widthMeasureSpec, 100);
            int height = calcMeasuredSize(heightMeasureSpec, 100);
            // TODO: 下の+40はレイアウト崩れに対するワークアラウンド、原因究明してちゃんとなおす
            setMeasuredDimension(width, height + 40);
        }

        private void setCandidate(CandidateDate candidateDate) {
            candidateText.setText(candidateDate.getDateText());
            numOfNgText.setText(Integer.toString(candidateDate.getNegativeFriendsNum()));
            numOfOkText.setText(Integer.toString(candidateDate.getPositiveFriendsNum()));
        }

        private void setAnswerState(AnswerState answerState) {
            candidateText.setBackgroundColor(getResources().getColor(answerState.colorResource));
        }
    }

    private enum AnswerState {
        OK(R.color.ok_green), NEUTRAL(R.color.neutral_yellow), NG(R.color.ng_red);

        private final int colorResource;
        private AnswerState(int colorResource) {
            this.colorResource = colorResource;
        }
    }

    private class OnExpandButtonClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO
            Toast.makeText(getContext(), "未実装", Toast.LENGTH_SHORT).show();
        }
    }
}
