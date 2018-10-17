package com.gitlab.saschabrunner.thermalmonitor;

import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Hard coded paths for OnePlus 3
    private static final String TEMPERATURE_FILE_PATH =
            "/sys/class/thermal/thermal_zone23/temp";
    private static final String CPU0_FREQ_FILE_PATH =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    private static final String CPU1_FREQ_FILE_PATH =
            "/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq";
    private static final String CPU2_FREQ_FILE_PATH =
            "/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq";
    private static final String CPU3_FREQ_FILE_PATH =
            "/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tvFilePath = findViewById(R.id.tvFilePath);
        tvFilePath.setText(TEMPERATURE_FILE_PATH);

        final List<CPU> cpus = getCpus();
        ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
        ConstraintSet set = new ConstraintSet();

        int prevId = R.id.tvFilePath; // We begin inserting the TextViews after this view
        final Map<CPU, TextView> tvByCpu = new HashMap<>();
        for (CPU cpu : cpus) {
            // Create label text view
            TextView tvCpuIdentifier = new TextView(this);
            tvCpuIdentifier.setText("CPU" + cpu.getId() + ":");
            tvCpuIdentifier.setId(View.generateViewId());

            // Generate layout params for value text view
            ConstraintLayout.LayoutParams valueLayoutParams =
                    new ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT);
            valueLayoutParams.setMarginStart(
                    (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));

            // Create value text view
            TextView tvCpuFrequency = new TextView(this);
            tvCpuFrequency.setId(View.generateViewId());
            tvCpuFrequency.setLayoutParams(new ConstraintLayout.LayoutParams(valueLayoutParams));

            // Add views and set constraints
            rootLayout.addView(tvCpuIdentifier);
            rootLayout.addView(tvCpuFrequency);
            set.clone(rootLayout);
            set.connect(tvCpuIdentifier.getId(), ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM);
            set.connect(tvCpuIdentifier.getId(), ConstraintSet.START, rootLayout.getId(), ConstraintSet.START);
            set.connect(tvCpuFrequency.getId(), ConstraintSet.TOP, tvCpuIdentifier.getId(), ConstraintSet.TOP);
            set.connect(tvCpuFrequency.getId(), ConstraintSet.START, tvCpuIdentifier.getId(), ConstraintSet.END);
            set.applyTo(rootLayout);

            prevId = tvCpuIdentifier.getId();
            tvByCpu.put(cpu, tvCpuFrequency);
        }


        Thread monitoringThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final TextView tvTemperature = findViewById(R.id.tvTemperature);
                final TextView tvTime = findViewById(R.id.tvTime);


                while (true) {
                    for (CPU cpu : cpus) {
                        try {
                            cpu.updateFrequency();
                        } catch (IOException e) {
                            // TODO
                            e.printStackTrace();
                        }
                    }

                    // Can't touch views from separate thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (Map.Entry<CPU, TextView> tvForCpu : tvByCpu.entrySet()) {
                                tvForCpu.getValue().setText(String.valueOf(tvForCpu.getKey().getLastFrequency()));
                            }

                            tvTemperature.setText(readFile(TEMPERATURE_FILE_PATH));
                            tvTime.setText(new Date().toString());
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        monitoringThread.start();
    }

    private String readFile(String filePath) {
        try (BufferedReader file = new BufferedReader(new FileReader(filePath))) {
            return file.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<CPU> getCpus() {
        File cpusFolder = new File("/sys/devices/system/cpu");

        File[] cpuFolders = cpusFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String lcName = name.toLowerCase();
                return lcName.matches("(cpu)[0-9]+");
            }
        });

        List<CPU> cpus = new ArrayList<>(cpuFolders.length);
        for (File folder : cpuFolders) {
            cpus.add(new CPU(folder));
        }

        return cpus;
    }
}
