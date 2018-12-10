package com.gitlab.saschabrunner.thermalmonitor;

import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import androidx.appcompat.app.AppCompatActivity;

public class Licenses extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        StringBuilder licensesText = new StringBuilder();
        try (BufferedReader licenses = new BufferedReader(new InputStreamReader(
                getAssets().open("licenses")))) {
            while (licenses.ready()) {
                licensesText.append(licenses.readLine());
                licensesText.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextView licensesTextView = findViewById(R.id.licenses_text);
        licensesTextView.setText(licensesText.toString());
    }
}
