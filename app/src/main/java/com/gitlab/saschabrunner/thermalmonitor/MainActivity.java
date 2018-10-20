package com.gitlab.saschabrunner.thermalmonitor;

import android.support.annotation.NonNull;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<ThermalZone> thermalZones = getThermalZones();
        final Map<ThermalZone, TextView> tvByThermalZone = createTemperatureTextViews(thermalZones);

        final List<CPU> cpus = getCpus();
        final Map<CPU, TextView> tvByCpu = createCpuTextViews(cpus);


        Thread monitoringThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final TextView tvTime = findViewById(R.id.tvTime);

                while (true) {
                    for (ThermalZone thermalZone : thermalZones) {
                        try {
                            thermalZone.updateTemperature();
                        } catch (IOException e) {
                            // TODO
                            e.printStackTrace();
                        }
                    }

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
                            for (Map.Entry<ThermalZone, TextView> tvForThermalZone : tvByThermalZone.entrySet()) {
                                tvForThermalZone.getValue().setText(String.valueOf(tvForThermalZone.getKey().getLastTemperature()));
                            }

                            for (Map.Entry<CPU, TextView> tvForCpu : tvByCpu.entrySet()) {
                                tvForCpu.getValue().setText(String.valueOf(tvForCpu.getKey().getLastFrequency()));
                            }

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

    @NonNull
    private Map<ThermalZone, TextView> createTemperatureTextViews(List<ThermalZone> thermalZones) {
        // TODO: Use different layout
        ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
        ConstraintSet set = new ConstraintSet();

        int prevId = R.id.tvTime; // We begin inserting the TextViews after this view
        final Map<ThermalZone, TextView> tvByThermalZone = new HashMap<>();
        for (ThermalZone thermalZone : thermalZones) {
            // Create label text view
            TextView tvZoneIdentifier = new TextView(this);
            tvZoneIdentifier.setText(thermalZone.getId() + " (" + thermalZone.getType() + "):");
            tvZoneIdentifier.setId(View.generateViewId());

            // Generate layout params for value text view
            ConstraintLayout.LayoutParams valueLayoutParams =
                    new ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT);
            valueLayoutParams.setMarginStart(
                    (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));

            // Create value text view
            TextView tvZoneTemp = new TextView(this);
            tvZoneTemp.setId(View.generateViewId());
            tvZoneTemp.setLayoutParams(new ConstraintLayout.LayoutParams(valueLayoutParams));

            // Add views and set constraints
            rootLayout.addView(tvZoneIdentifier);
            rootLayout.addView(tvZoneTemp);
            set.clone(rootLayout);
            set.connect(tvZoneIdentifier.getId(), ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM);
            set.connect(tvZoneIdentifier.getId(), ConstraintSet.START, rootLayout.getId(), ConstraintSet.START);
            set.connect(tvZoneTemp.getId(), ConstraintSet.TOP, tvZoneIdentifier.getId(), ConstraintSet.TOP);
            set.connect(tvZoneTemp.getId(), ConstraintSet.START, tvZoneIdentifier.getId(), ConstraintSet.END);
            set.applyTo(rootLayout);

            prevId = tvZoneIdentifier.getId();
            tvByThermalZone.put(thermalZone, tvZoneTemp);
        }
        return tvByThermalZone;
    }

    @NonNull
    private Map<CPU, TextView> createCpuTextViews(List<CPU> cpus) {
        // TODO: Use different layout
        ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
        ConstraintSet set = new ConstraintSet();

        int prevId = R.id.tvTime; // We begin inserting the TextViews after this view
        Map<CPU, TextView> tvByCpu = new HashMap<>();
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
        return tvByCpu;
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

    private static List<ThermalZone> getThermalZones() {
        File thermalZonesFolder = new File("/sys/class/thermal");

        File[] thermalZoneFolders = thermalZonesFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String lcName = name.toLowerCase();
                return lcName.matches("(thermal_zone)[0-9]+");
            }
        });

        List<ThermalZone> thermalZones = new ArrayList<>(thermalZoneFolders.length);
        for (File folder : thermalZoneFolders) {
            try {
                thermalZones.add(new ThermalZone(folder));
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
        }

        return thermalZones;
    }

    private static List<CPU> getCpus() {
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
