package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gitlab.saschabrunner.thermalmonitor.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class FirstTimeSetupActivity extends AppCompatActivity {
    public static final int RESULT_CODE_SETUP_FINISHED = 1;

    private FirstTimeSetupPagerAdapter pagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_setup);

        pagerAdapter = new FirstTimeSetupPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.firstTimeSetupViewPager);
        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void next(View view) {
        int currentItem = viewPager.getCurrentItem();
        int totalItems = pagerAdapter.getCount();

        if (!pagerAdapter.getItemCasted(currentItem).onNextScreenRequested()) {
            return;
        }

        if (currentItem < totalItems - 1) {
            viewPager.setCurrentItem(currentItem + 1);

            if (currentItem == totalItems - 2) {
                ((Button) findViewById(R.id.firstTimeSetupNextButton)).setText(R.string.finish);
            }
        } else {
            // Reached end of the setup, finish
            setResult(RESULT_CODE_SETUP_FINISHED);
            finish();
        }
    }
}
