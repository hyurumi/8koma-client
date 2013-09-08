package com.appspot.hachiko_schedule.dev;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.appspot.hachiko_schedule.R;

/**
 * 未実装のアクティビティのかわりに仮に利用するアクティビティ
 */
public class NotImplementedActivity extends Activity {

    public static final String EXTRA_KEY_DETAILED_MESSAGE = "not implemented detail";

    public static Intent getIntentWithMessage(Context context, String message) {
        Intent intent = new Intent(context, NotImplementedActivity.class);
        intent.putExtra(EXTRA_KEY_DETAILED_MESSAGE, message);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_implemented);
        updateMessage(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateMessage(intent);
    }

    private void updateMessage(Intent intent) {
        if (intent != null) {
            String detail = intent.getExtras().getString(EXTRA_KEY_DETAILED_MESSAGE, "");
            if (detail.length() > 0) {
                ((TextView) findViewById(R.id.not_implemented_detail)).setText(detail);
            }
        }
    }
}
