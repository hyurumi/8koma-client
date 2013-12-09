package tk.hachikoma.plans;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import tk.hachikoma.R;
import tk.hachikoma.data.FixedPlan;
import tk.hachikoma.prefs.HachikoPreferences;

/**
 * 確定した予定を表すView
 */
public class FixedPlanView extends LinearLayout implements PlanView<FixedPlan> {
    private TextView titleView;
    private TextView participantsView;
    private TextView dateView;

    public FixedPlanView(Context context) {
        super(context);
        init(context);
    }

    public FixedPlanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FixedPlanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        View layout = LayoutInflater.from(context).inflate(R.layout.fixed_plan_view, this);
        titleView = (TextView) layout.findViewById(R.id.event_title);
        participantsView = (TextView) layout.findViewById(R.id.event_participants);
        dateView = (TextView) layout.findViewById(R.id.event_date);
    }

    @Override
    public PlanView setPlan(FixedPlan plan) {
        titleView.setText(plan.getTitle());
        String source =  ("<b>" + plan.getOwnerName(getContext()) + "</b>, ")
                + (plan.getOwnerId() != HachikoPreferences.getMyHachikoId(getContext()) ? "あなた, " : "")
                + plan.getPositiveFriendNames(getContext());
        participantsView.setText(Html.fromHtml(source));
        Typeface fontForIcon= Typeface.createFromAsset(getContext().getAssets(), "fonts/fontawesome-webfont.ttf");
        TextView iconView = (TextView)findViewById(R.id.icon_in_right_top_button);
        iconView.setTypeface(fontForIcon);
        iconView.setText(R.string.icon_remind);
        findViewById(R.id.right_top_button).setVisibility(VISIBLE);
        ((TextView)findViewById(R.id.text_in_right_top_button)).setText(R.string.remind);
        findViewById(R.id.right_top_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "未実装", Toast.LENGTH_SHORT).show();
            }
        });
        dateView.setText(plan.getDateText());
        return null;
    }
}
