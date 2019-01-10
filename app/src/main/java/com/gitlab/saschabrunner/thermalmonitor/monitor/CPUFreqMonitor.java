package com.gitlab.saschabrunner.thermalmonitor.monitor;

import android.content.SharedPreferences;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.MonitorService;
import com.gitlab.saschabrunner.thermalmonitor.OverlayListItem;
import com.gitlab.saschabrunner.thermalmonitor.PreferenceConstants;
import com.gitlab.saschabrunner.thermalmonitor.R;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.IntDef;

public class CPUFreqMonitor implements Runnable, Monitor {
    private static final String TAG = "CPUFreqMonitor";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FAILURE_REASON_OK, FAILURE_REASON_DIR_NOT_EXISTS, FAILURE_REASON_DIR_EMPTY,
            FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION})
    public @interface FAILURE_REASON {
    }

    public static final int FAILURE_REASON_OK = 0;
    public static final int FAILURE_REASON_DIR_NOT_EXISTS = 1;
    public static final int FAILURE_REASON_DIR_EMPTY = 2;
    public static final int FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION = 3;

    private Preferences preferences;
    private MonitorService monitorService;
    private List<CPU> cpus;
    private Map<CPU, OverlayListItem> listItemByCpu;

    public CPUFreqMonitor() {
    }

    @Override
    public int checkSupported(SharedPreferences monitorPreferences) throws MonitorException {
        // Validate preferences
        Preferences preferences = new Preferences(monitorPreferences);

        // Check if dir is present
        File cpusDir = new File("/sys/devices/system/cpu");
        if (!cpusDir.exists()) {
            return FAILURE_REASON_DIR_NOT_EXISTS;
        }

        // Check if dirs for CPU cores are present
        File[] cpuDirs = filterCpus(cpusDir);
        if (cpuDirs == null || cpuDirs.length == 0) {
            return FAILURE_REASON_DIR_EMPTY;
        }

        // Check if scaling_cur_frequency is accessible for at least one core
        // The file might not exist for cores that are offline
        // We assume that at least one core is online
        boolean curFrequencyReadable = false;
        for (File cpuDir : cpuDirs) {
            File curFrequencyFile = new File(CPU.getCurFrequencyFilePath(cpuDir.getAbsolutePath()));
            if (curFrequencyFile.exists() && curFrequencyFile.canRead()) {
                curFrequencyReadable = true;
                break;
            }
        }

        if (!curFrequencyReadable) {
            return FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION;
        }

        return FAILURE_REASON_OK;
    }

    @Override
    public void init(MonitorService monitorService, SharedPreferences monitorPreferences)
            throws MonitorException {
        if (checkSupported(monitorPreferences) != FAILURE_REASON_OK) {
            throw new MonitorException(R.string.monitor_not_supported_with_supplied_preferences);
        }

        this.preferences = new Preferences(monitorPreferences);
        this.monitorService = monitorService;
        this.cpus = getCpus();
        this.listItemByCpu = new HashMap<>();
        for (CPU cpu : cpus) {
            OverlayListItem listItem = new OverlayListItem();
            listItem.setLabel("CPU" + cpu.getId());
            listItemByCpu.put(cpu, listItem);
            monitorService.addListItem(listItem);
        }
    }

    @Override
    public void deinit() {
        for (CPU cpu : cpus) {
            try {
                cpu.deinit();
            } catch (IOException e) {
                Log.e(TAG, "Couldn't deinit CPU", e);
            }
        }
    }

    @Override
    public void run() {
        while (monitorService.isMonitoringRunning()) {
            monitorService.awaitNotPaused();
            Log.v(TAG, "CPUFreqMonitor update");

            updateCpus();

            for (CPU cpu : cpus) {
                OverlayListItem listItem = listItemByCpu.get(cpu);
                assert listItem != null;
                listItem.setValue(buildCpuValueString(cpu));
                monitorService.updateListItem(listItem);
            }

            try {
                Thread.sleep(preferences.interval);
            } catch (InterruptedException e) {
                if (monitorService.isMonitoringRunning()) {
                    // No interrupt should happen except when monitor service quits
                    Log.e(TAG, "Unexcpected Interrupt received", e);
                    return;
                }
            }
        }
    }

    private void updateCpus() {
        for (CPU cpu : cpus) {
            try {
                cpu.update();
            } catch (IOException e) {
                Log.e(TAG, "Error updating CPU '" + cpu.getId() + "'", e);
                return;
            }
        }
    }

    private String buildCpuValueString(CPU cpu) {
        if (cpu.getLastState() == CPU.STATE_OFFLINE) {
            return monitorService.getString(R.string.offline);
        } else {
            return String.format(Locale.getDefault(), "%06.1f MHz",
                    (double) cpu.getLastFrequency() / 1000);
        }
    }

    private static List<CPU> getCpus() {
        File cpusDir = new File("/sys/devices/system/cpu");
        File[] cpuDirs = filterCpus(cpusDir);

        List<CPU> cpus = new ArrayList<>(cpuDirs.length);
        for (File dir : cpuDirs) {
            try {
                cpus.add(new CPU(dir));
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        return cpus;
    }

    private static File[] filterCpus(File cpusDir) {
        return cpusDir.listFiles((dir, name) -> {
            String lcName = name.toLowerCase();
            return lcName.matches("(cpu)[0-9]+");
        });
    }

    private static class Preferences {
        private final int interval;

        private Preferences(SharedPreferences preferences) throws MonitorException {
            this.interval = preferences.getInt(
                    PreferenceConstants.KEY_CPU_FREQ_MONITOR_REFRESH_INTERVAL,
                    PreferenceConstants.DEF_CPU_FREQ_MONITOR_REFRESH_INTERVAL);

            validate();
        }

        private void validate() throws MonitorException {
            if (interval < 500) {
                throw new MonitorException(R.string.interval_must_be_at_least_500ms);
            }
        }
    }
}
