package com.appspot.hachiko_schedule.plans;

import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.EventCategory;
import com.appspot.hachiko_schedule.data.UnsettledEvent;
import static com.appspot.hachiko_schedule.util.DateUtils.dateAfterDaysAndHoursFromNow;
import static com.appspot.hachiko_schedule.util.ViewUtils.findViewById;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UnsettledEventsFragment extends ListFragment {
    // TODO: sqliteとかでしまってある予定一覧をとりだす
    // TODO: 以前は未確定だったけど，確定になった情報を更新する
    UnsettledEvent[] events = new UnsettledEvent[] {
            new UnsettledEvent("新宿女子会", EventCategory.COFFEE, 150,
                    new ImmutableListMultimap.Builder<Date, String>()
                            .putAll(dateAfterDaysAndHoursFromNow(1, 5), "ひろこ", "香菜", "理沙")
                            .putAll(dateAfterDaysAndHoursFromNow(2, 3), "理沙", "瑞穂")
                            .putAll(dateAfterDaysAndHoursFromNow(5, 0), "ひろこ", "理沙")
                            .build()),
            new UnsettledEvent("渋谷男子会", EventCategory.COFFEE, 80,
                    new ImmutableListMultimap.Builder<Date, String>()
                            .putAll(dateAfterDaysAndHoursFromNow(3, 8), "ガイア", "ひろし", "鈴木")
                            .putAll(dateAfterDaysAndHoursFromNow(3, 11), "ゆうすけ", "鈴木")
                            .putAll(dateAfterDaysAndHoursFromNow(7, 9), "ひろし", "正規")
                            .build())
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        UnsettledEventAdapter adapter = new UnsettledEventAdapter(getActivity(), events);
        setListAdapter(adapter);
    }

    /**
     * 未確定の予定一覧を表示する用のリストadapter
     */
    private class UnsettledEventAdapter extends ArrayAdapter<UnsettledEvent> {

        public UnsettledEventAdapter(Context context, UnsettledEvent[] events) {
            super(context, R.layout.list_item_unsettled_plan, events);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View listItem = inflater.inflate(R.layout.list_item_unsettled_plan, parent, false);
            TextView title = findViewById(listItem, R.id.event_title);
            TextView duration = findViewById(listItem, R.id.event_duration);

            UnsettledEvent event = events[position];
            title.setText(event.getTitle());
            duration.setText("所要時間: " + event.getDurationInMinutes() + "分");
            Multimap<Date, String> dateToParticipants = event.getDayToParticipants();
            // TODO: 現状のデザインがある程度生き残るようなら，ListViewを使ったまともなViewに書き換え
            View[] candidateDays = new View[] {
                    findViewById(listItem, R.id.candidate0),
                    findViewById(listItem, R.id.candidate1),
                    findViewById(listItem, R.id.candidate2)
            };
            int index = 0;
            for (Date date: dateToParticipants.keySet()) {
                View candidateDayView = candidateDays[index++];
                TextView answer = findViewById(candidateDayView, R.id.unsettled_plan_day_answer);
                TextView day = findViewById(candidateDayView, R.id.unsettled_plan_day_day);
                TextView participants
                        = findViewById(candidateDayView, R.id.unsettled_plan_day_participants);

                SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm〜");
                day.setText(format.format(date));
                participants.setText(Joiner.on(", ").join(dateToParticipants.get(date)));

                setRandomAnswer(answer);

                candidateDayView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        TextView answerText = findViewById(v, R.id.unsettled_plan_day_answer);
                        Answer answer = getAnswer(answerText);
                        if (answer.equals(Answer.OK)) {
                            setAnswer(answerText, Answer.NG);
                        } else {
                            setAnswer(answerText, Answer.OK);
                        }
                    }
                });
            }

            return listItem;
        }

        private void setRandomAnswer(TextView answerText) {
            int rand = (int) (Math.random() * 3);
            Answer answer = Answer.values()[rand];
            setAnswer(answerText, answer);
        }

        private Answer getAnswer(TextView answerText) {
            String answer = answerText.getText().toString();
            if (answer.equals("OK"))
                return Answer.OK;
            else if (answer.equals("NG"))
                return Answer.NG;
            else
                return Answer.UNKNOWN;
        }

        private void setAnswer(TextView answerText, Answer answer) {
            switch (answer) {
                case OK:
                    answerText.setText("OK");
                    answerText.setTextColor(Color.GREEN);
                    return;
                case NG:
                    answerText.setText("NG");
                    answerText.setTextColor(Color.RED);
                    return;
                case UNKNOWN:
                    answerText.setText("  ");
                    answerText.setTextColor(Color.WHITE);
                    return;
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }

    private enum Answer {OK, NG, UNKNOWN};
}
