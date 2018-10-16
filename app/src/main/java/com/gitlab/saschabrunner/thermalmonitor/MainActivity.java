package com.gitlab.saschabrunner.thermalmonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

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

        Thread monitoringThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final TextView tvTemperature = findViewById(R.id.tvTemperature);
                final TextView tvTime = findViewById(R.id.tvTime);
                final TextView tvCpu0Freq = findViewById(R.id.tvCpu0Freq);
                final TextView tvCpu1Freq = findViewById(R.id.tvCpu1Freq);
                final TextView tvCpu2Freq = findViewById(R.id.tvCpu2Freq);
                final TextView tvCpu3Freq = findViewById(R.id.tvCpu3Freq);

                while (true) {
                    // Can't touch views from separate thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTemperature.setText(readFile(TEMPERATURE_FILE_PATH));
                            tvCpu0Freq.setText(readFile(CPU0_FREQ_FILE_PATH));
                            tvCpu1Freq.setText(readFile(CPU1_FREQ_FILE_PATH));
                            tvCpu2Freq.setText(readFile(CPU2_FREQ_FILE_PATH));
                            tvCpu3Freq.setText(readFile(CPU3_FREQ_FILE_PATH));
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
            return  file.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
