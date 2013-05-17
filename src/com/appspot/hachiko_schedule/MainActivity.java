package com.appspot.hachiko_schedule;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import android.view.View;
import android.widget.Button;

/**
 * {@link Activity} that is displayed on launch.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button) findViewById(R.id.new_plan_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreatePlanActivity.class);
                startActivityForResult(intent, 0);
            }
        });


        ((Button) findViewById(R.id.host_detail)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HostPlanDetailActivity.class);
                startActivity(intent);
            }
        });


        ((Button) findViewById(R.id.guest_detail)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GuestPlanDetailActivity.class);
                startActivity(intent);
            }
        });
//
//        final ActionBar actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//
//        actionBar.addTab(actionBar.newTab()
//                .setText("Friends")
//                .setTabListener(new TabListener<FriendsFragment>(
//                        this, "friends", FriendsFragment.class)));
//        actionBar.addTab(actionBar.newTab()
//                .setText("Plans")
//                .setTabListener(new TabListener<PlansFragment>(
//                        this, "plans", PlansFragment.class)));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    static private class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        /**
         * コンストラクタ
         * @param activity
         * @param tag
         * @param clz
         */
        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            //FragmentManagerからFragmentを探す。  2012/12/11 追記
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
        }

        /**
         * @brief タブが選択されたときの処理
         */
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            //ftはnullなので使用できない
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                FragmentManager fm = mActivity.getFragmentManager();
                fm.beginTransaction().add(R.id.container, mFragment, mTag).commit();
            } else {
                //detachされていないときだけattachするよう変更   2012/12/11　変更
                //FragmentManager fm = mActivity.getFragmentManager();
                //fm.beginTransaction().attach(mFragment).commit();
                if (mFragment.isDetached()) {
                    FragmentManager fm = mActivity.getFragmentManager();
                    fm.beginTransaction().attach(mFragment).commit();
                }

            }
        }
        /**
         * @brief 　タブの選択が解除されたときの処理
         */
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                FragmentManager fm = mActivity.getFragmentManager();
                fm.beginTransaction().detach(mFragment).commit();
            }
        }
        /**
         * @brief タブが2度目以降に選択されたときの処理
         */
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }
    }
}
