package com.kiven.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kiven.kutils.activityHelper.KHelperActivity;
import com.kiven.kutils.custom.BaseHelper;

/**
 * Created by kiven on 16/5/6.
 */
public class ActivityHTestBase extends BaseHelper {

    private String key = "ActivityHTestBase_count_key";

    @Override
    public void onCreate(KHelperActivity activity, Bundle savedInstanceState) {
        super.onCreate(activity, savedInstanceState);

        setContentView(R.layout.activity_h_test_base);

        count = mActivity.getIntent().getIntExtra(key, 0);
        TextView textView = findViewById(R.id.tv_message);
        textView.setText("count = " + count);
    }

    private int count;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                ActivityHTestBase nextHelper = new ActivityHTestBase();
                nextHelper.getIntent().putExtra(key, count + 1);
                nextHelper.startActivity(mActivity);
                /*AppContext.getInstance().startSinkActivity(new ActivityHFloatView());*/
                break;
            case R.id.button2:
//                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.root), new Slide(Gravity.LEFT));
                finish();
                break;
        }
    }
}
