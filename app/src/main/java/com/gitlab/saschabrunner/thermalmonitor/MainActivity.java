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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tvTemperature = findViewById(R.id.tvTemperature);
        final TextView tvFilePath = findViewById(R.id.tvFilePath);
        final TextView tvTime = findViewById(R.id.tvTime);

        // Hard coded path for sensor 'msm_therm' on OnePlus 3
        final String filePath = "/sys/class/thermal/thermal_zone23/temp";
        tvFilePath.setText(filePath);

        Thread monitoringThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // Can't touch views from separate thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTemperature.setText(readFile(filePath));
                            tvTime.setText(new Date().toString());
                        }
                    });
                    try {
                        Thread.sleep(1000);
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
