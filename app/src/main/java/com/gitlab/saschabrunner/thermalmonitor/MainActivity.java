package com.gitlab.saschabrunner.thermalmonitor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<ThermalZone> thermalZones = getThermalZones();
        final List<CPU> cpus = getCpus();

        List<Object> listContents = new ArrayList<>();
        listContents.addAll(thermalZones);
        listContents.addAll(cpus);

        final ArrayAdapter listAdapter = new ArrayAdapter<>(this, R.layout.simple_text_view, listContents);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(listAdapter);

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
                            cpu.update();
                        } catch (IOException e) {
                            // TODO
                            e.printStackTrace();
                        }
                    }

                    // Can't touch views from separate thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listAdapter.notifyDataSetChanged();
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
