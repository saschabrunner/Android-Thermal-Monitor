package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.cpufreq.CPUFreqMonitor;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorService;
import com.gitlab.saschabrunner.thermalmonitor.root.RootAccessException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootIPCSingleton;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalMonitor;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String FRAGMENT_HOME = "home";
    private static final String FRAGMENT_SETTINGS = "settings";
    private static final String FRAGMENT_ABOUT = "about";

    private final Map<String, Fragment> fragmentMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.mainBottomNavigation);
        navigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
        switchToHomeFragment();
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottomNavigationHome:
                switchToHomeFragment();
                return true;
            case R.id.bottomNavigationSettings:
                switchToSettingsFragment();
                return true;
            case R.id.bottomNavigationAbout:
                switchToAboutFragment();
                return true;
        }
        return false;
    }

    private HomeFragment getHomeFragment() {
        if (fragmentMap.get(FRAGMENT_HOME) == null) {
            fragmentMap.put(FRAGMENT_HOME, new HomeFragment());
        }

        return (HomeFragment) fragmentMap.get(FRAGMENT_HOME);
    }

    private void switchToHomeFragment() {
        showFragment(getHomeFragment(), R.string.home);
    }

    private SettingsFragment getSettingsFragment() {
        if (fragmentMap.get(FRAGMENT_SETTINGS) == null) {
            fragmentMap.put(FRAGMENT_SETTINGS, new SettingsFragment());
        }

        return (SettingsFragment) fragmentMap.get(FRAGMENT_SETTINGS);
    }

    private void switchToSettingsFragment() {
        showFragment(getSettingsFragment(), R.string.settings);
    }

    private AboutFragment getAboutFragment() {
        if (fragmentMap.get(FRAGMENT_ABOUT) == null) {
            fragmentMap.put(FRAGMENT_ABOUT, new AboutFragment());
        }

        return (AboutFragment) fragmentMap.get(FRAGMENT_ABOUT);
    }

    private void switchToAboutFragment() {
        showFragment(getAboutFragment(), R.string.about);
    }

    private void showFragment(Fragment fragment, @StringRes int title) {
        // Set the action bar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Hide all fragments except the one to show
        boolean fragmentFound = false;
        for (Fragment curFragment : fragments) {
            if (curFragment == fragment) {
                fragmentTransaction.show(curFragment);
                fragmentFound = true;
            } else {
                fragmentTransaction.hide(curFragment);
            }
        }

        if (!fragmentFound) {
            // The fragment is new and needs to be added instead
            fragmentTransaction.add(R.id.mainContent, fragment);
        }

        fragmentTransaction.commit();
    }

    private void checkMonitoringAvailable() {
        boolean success = true;

        try {
            if (GlobalPreferences.getInstance().thermalMonitorEnabled()) {
                success &= checkThermalMonitoringAvailable();
            }

            if (GlobalPreferences.getInstance().cpuFreqMonitorEnabled()) {
                success &= checkCpuFreqMonitoringAvailable();
            }
        } catch (MonitorException e) {
            showInfoDialog("Monitor configuration invalid", e.getMessage(this));
            success = false;
        }

        if (success) {
            showInfoDialog("Checks succeeded",
                    "All enabled modules should be working with the current " +
                            "configuration.");
        }
    }

    private boolean checkThermalMonitoringAvailable() throws MonitorException {
        boolean success = true;

        ThermalMonitor thermalMonitor;
        if (GlobalPreferences.getInstance().rootEnabled()) {
            Log.v(TAG, "Root enabled, initializing Thermal Monitor with Root IPC");
            try {
                thermalMonitor = new ThermalMonitor(RootIPCSingleton.getInstance(this));
            } catch (RootAccessException e) {
                Log.e(TAG, "Root access has been denied", e);
                showInfoDialog("Root access denied", "Could not acquire root access");
                return false;
            }
        } else {
            Log.v(TAG, "Root disabled, initializing Thermal Monitor without Root IPC");
            thermalMonitor = new ThermalMonitor();
        }

        int thermalMonitoringAvailable =
                thermalMonitor.checkSupported(Utils.getGlobalPreferences(this));
        switch (thermalMonitoringAvailable) {
            case ThermalMonitor.FAILURE_REASON_OK:
                break;
            case ThermalMonitor.FAILURE_REASON_DIR_NOT_EXISTS:
                showInfoDialog("Thermal Monitoring disabled",
                        "/sys/class/thermal does not exist");
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_NO_ENABLED_THERMAL_ZONES:
                showInfoDialog("Thermal Monitoring disabled",
                        "No valid thermal zones are enabled");
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_THERMAL_ZONES_NOT_READABLE:
                showInfoDialog("Thermal Monitoring disabled",
                        "Couldn't read thermal zones");
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TYPE_NO_PERMISSION:
                showInfoDialog("Thermal Monitoring disabled",
                        "Can't read type of a thermal zone");
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TEMP_NO_PERMISSION:
                showInfoDialog("Thermal Monitoring disabled",
                        "Can't read temp of a thermal zone");
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_NO_ROOT_IPC:
                showInfoDialog("Thermal Monitoring disabled",
                        "No root IPC object passed to monitor " +
                                "(root globally disabled?)");
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TEMP_NOT_READABLE:
                showInfoDialog("Thermal Monitoring disabled",
                        "Can't read temp of a thermal zone");
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TYPE_NOT_READABLE:
                showInfoDialog("Thermal Monitoring disabled",
                        "Can't read type of a thermal zone");
                success = false;
                break;
            default:
                showInfoDialog("Thermal Monitoring disabled",
                        "Unknown error");
                success = false;
                break;
        }

        return success;
    }

    private boolean checkCpuFreqMonitoringAvailable() throws MonitorException {
        boolean success = true;

        CPUFreqMonitor cpuFreqMonitor = new CPUFreqMonitor(this);
        int cpuFreqMonitoringAvailable =
                cpuFreqMonitor.checkSupported(Utils.getGlobalPreferences(this));
        switch (cpuFreqMonitoringAvailable) {
            case CPUFreqMonitor.FAILURE_REASON_OK:
                break;
            case CPUFreqMonitor.FAILURE_REASON_DIR_NOT_EXISTS:
                showInfoDialog("CPU frequency monitoring disabled",
                        "/sys/devices/system/cpu does not exist");
                success = false;
                break;
            case CPUFreqMonitor.FAILURE_REASON_DIR_EMPTY:
                showInfoDialog("CPU frequency monitoring disabled",
                        "Can't find any CPUs in /sys/devices/system/cpu");
                success = false;
                break;
            case CPUFreqMonitor.FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION:
                showInfoDialog("CPU frequency monitoring disabled",
                        "Can't read frequency of a CPU");
                success = false;
                break;
        }

        return success;
    }

    private void showInfoDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MonitorService.class));
        } else {
            startService(new Intent(this, MonitorService.class));
        }
    }

    private void stopService() {
        stopService(new Intent(this, MonitorService.class));
    }

    public void startService(View view) {
        checkMonitoringAvailable();
        startService();
    }

    public void stopService(View view) {
        stopService();
    }

    public void showLicenses(View view) {
        getAboutFragment().showLicenses(view);
    }
}
