package com.appspot.hachiko_schedule;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity that is used by a guest to check event date.
 */
public class GuestPlanDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_plan_detail);
    }
}
