package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.util.PreferenceConstants;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class FirstTimeSetupActivity extends AppCompatActivity {
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

        if (!pagerAdapter.getItemRaw(currentItem).onNextScreenRequested()) {
            return;
        }

        if (currentItem < totalItems - 1) {
            viewPager.setCurrentItem(currentItem + 1);

            if (currentItem == totalItems - 2) {
                ((Button) findViewById(R.id.firstTimeSetupNextButton)).setText(R.string.finish);
            }
        } else {
            // Reached end of the setup, finish
            Utils.getGlobalPreferences(this)
                    .edit()
                    .putBoolean(PreferenceConstants.KEY_ROOT_FIRST_TIME_SETUP_COMPLETED, true)
                    .apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
