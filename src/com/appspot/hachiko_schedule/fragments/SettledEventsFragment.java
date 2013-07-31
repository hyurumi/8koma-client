package com.appspot.hachiko_schedule.fragments;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.EventCategory;
import com.appspot.hachiko_schedule.data.SettledEvent;
import com.appspot.hachiko_schedule.util.NotImplementedActivity;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.appspot.hachiko_schedule.util.ViewUtils.findViewById;

/**
 * 確定した予定一覧用フラグメント
 */
public class SettledEventsFragment extends ListFragment {

    // TODO: どっかにしまっているイベント情報をとりだす
    // TODO: 以前は未確定だったけど，確定になった情報を更新する？
    private SettledEvent[] events = new SettledEvent[] {
        new SettledEvent("後楽園焼肉会", EventCategory.GRILLED_BEEF,
                ImmutableList.of("ひろし", "たけし", "ゆうた"), new Date(), 120),
                new SettledEvent("カフェ会", EventCategory.COFFEE,
                        ImmutableList.of("Yuta Sasaki", "西川茂雄"), new Date(), 190),
                new SettledEvent("一人で網と向かい合う", EventCategory.GRILLED_BEEF,
                        ImmutableList.of("ひろし"), new Date(), 100),
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SettledEventAdapter adapter = new SettledEventAdapter(getActivity(), events);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), NotImplementedActivity.class);
        intent.putExtra(NotImplementedActivity.EXTRA_KEY_DETAILED_MESSAGE,
                "イベント: 「" + events[position].getTitle() + "」に関する詳細が表示される予定");
        startActivity(intent);
    }

    /**
     * 確定した予定一覧を表示する用のリストadapter
     */
    private static class SettledEventAdapter extends ArrayAdapter<SettledEvent> {
        private final Context context;
        private final SettledEvent[] events;

        public SettledEventAdapter(
                Context context, SettledEvent[] events) {
            super(context, R.layout.list_item_settled_plan, events);
            this.context = context;
            this.events = events;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View listItem = inflater.inflate(R.layout.list_item_settled_plan, parent, false);
            ImageView icon = findViewById(listItem, R.id.event_icon);
            TextView title = findViewById(listItem, R.id.event_title);
            TextView participants = findViewById(listItem, R.id.event_participants);
            TextView date = findViewById(listItem, R.id.event_date);

            SettledEvent event = events[position];
            icon.setImageResource(event.getCategory().getIconResourceId());
            title.setText(event.getTitle());
            participants.setText(Joiner.on(", ").join(event.getParticipants()));
            SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm〜");
            date.setText(format.format(event.getWhen()));

            return listItem;
        }
    }
}
