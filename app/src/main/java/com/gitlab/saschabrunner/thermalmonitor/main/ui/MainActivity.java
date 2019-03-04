package com.gitlab.saschabrunner.thermalmonitor.main.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.cpufreq.CPUFreqMonitor;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootAccessException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootIPCSingleton;
import com.gitlab.saschabrunner.thermalmonitor.thermal.ThermalMonitor;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class MainActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
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
                fragmentFound = true;
            } else {
                fragmentTransaction.hide(curFragment);
            }
        }

        if (!fragmentFound) {
            // The fragment is new and needs to be added first
            fragmentTransaction.add(R.id.mainContent, fragment);
        }

        // Make sure the fragment is visible
        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }

    // TODO: Refactor and move somewhere central
    public boolean checkMonitoringAvailable() {
        boolean success = true;

        try {
            if (GlobalPreferences.getInstance().thermalMonitorEnabled()) {
                success &= checkThermalMonitoringAvailable();
            }

            if (GlobalPreferences.getInstance().cpuFreqMonitorEnabled()) {
                success &= checkCpuFreqMonitoringAvailable();
            }
        } catch (MonitorException e) {
            MessageUtils.showInfoDialog(this, R.string.monitorConfigurationInvalid,
                    e.getResourceId());
            success = false;
        }

        return success;
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
                MessageUtils.showInfoDialog(this, R.string.rootAccessDenied,
                        R.string.couldNotAcquireRootAccess);
                return false;
            }
        } else {
            Log.v(TAG, "Root disabled, initializing Thermal Monitor without Root IPC");
            thermalMonitor = new ThermalMonitor();
        }

        int thermalMonitoringAvailable =
                thermalMonitor.checkSupported(ThermalMonitor.Preferences
                        .getPreferences(Utils.getGlobalPreferences(this)));
        switch (thermalMonitoringAvailable) {
            case ThermalMonitor.FAILURE_REASON_OK:
                break;
            case ThermalMonitor.FAILURE_REASON_DIR_NOT_EXISTS:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.sysClassThermalDoesNotExist);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_NO_ENABLED_THERMAL_ZONES:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.noValidThermalZonesAreEnabled);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_THERMAL_ZONES_NOT_READABLE:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.couldNotReadThermalZones);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TYPE_NO_PERMISSION:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.canNotReadTypeOfAThermalZone);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TEMP_NO_PERMISSION:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.canNotReadTemperatureOfAThermalZone);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_NO_ROOT_IPC:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.noRootIpcObjectPassedToMonitor);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TEMP_NOT_READABLE:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.canNotReadTemperatureOfAThermalZone);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_TYPE_NOT_READABLE:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.canNotReadTypeOfAThermalZone);
                success = false;
                break;
            case ThermalMonitor.FAILURE_REASON_ILLEGAL_CONFIGURATION:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.monitorConfigurationInvalid);
                break;
            default:
                MessageUtils.showInfoDialog(this, R.string.thermalMonitoringDisabled,
                        R.string.unknownError);
                success = false;
                break;
        }

        return success;
    }

    private boolean checkCpuFreqMonitoringAvailable() throws MonitorException {
        boolean success = true;

        CPUFreqMonitor cpuFreqMonitor = new CPUFreqMonitor(this);
        int cpuFreqMonitoringAvailable =
                cpuFreqMonitor.checkSupported(CPUFreqMonitor.Preferences
                        .getPreferences(Utils.getGlobalPreferences(this)));
        switch (cpuFreqMonitoringAvailable) {
            case CPUFreqMonitor.FAILURE_REASON_OK:
                break;
            case CPUFreqMonitor.FAILURE_REASON_DIR_NOT_EXISTS:
                MessageUtils.showInfoDialog(this, R.string.cpuFrequencyMonitoringDisabled,
                        R.string.sysDevicesSystemCpuDoesNotExist);
                success = false;
                break;
            case CPUFreqMonitor.FAILURE_REASON_DIR_EMPTY:
                MessageUtils.showInfoDialog(this, R.string.cpuFrequencyMonitoringDisabled,
                        R.string.canNotFindAnyCpusInSysDevicesSystemCpu);
                success = false;
                break;
            case CPUFreqMonitor.FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION:
                MessageUtils.showInfoDialog(this, R.string.cpuFrequencyMonitoringDisabled,
                        R.string.canNotReadFrequencyOfACpu);
                success = false;
                break;
            case CPUFreqMonitor.FAILURE_REASON_ILLEGAL_CONFIGURATION:
                MessageUtils.showInfoDialog(this, R.string.cpuFrequencyMonitoringDisabled,
                        R.string.monitorConfigurationInvalid);
                break;
            default:
                MessageUtils.showInfoDialog(this, R.string.cpuFrequencyMonitoringDisabled,
                        R.string.unknownError);
                success = false;
                break;
        }

        return success;
    }

    public void toggleService(View view) {
        getHomeFragment().toggleService(view);
    }

    public void showLicenses(View view) {
        getAboutFragment().showLicenses(view);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment(),
                args);
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);

        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.mainContent, fragment)
                .hide(caller)
                .addToBackStack(null)
                .commit();
        return true;
    }
}
