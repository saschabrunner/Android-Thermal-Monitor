package com.gitlab.saschabrunner.thermalmonitor.cpufreq;

import android.content.Context;
import android.util.Log;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

public class CPUFreqMonitorValidator {
    private static final String TAG = "CPUFreqMonitorValidator";

    public static boolean checkMonitoringAvailable(Context context) {
        boolean success = true;

        CPUFreqMonitor cpuFreqMonitor = new CPUFreqMonitor(context);

        int cpuFreqMonitoringAvailable;
        try {
            cpuFreqMonitoringAvailable = cpuFreqMonitor.checkSupported(CPUFreqMonitor.Preferences
                    .getPreferences(Utils.getGlobalPreferences(context)));
        } catch (MonitorException e) {
            Log.e(TAG, "Illegal configuration for monitor", e);
            MessageUtils.showInfoDialog(context, R.string.cpuFrequencyMonitoringNotAvailable,
                    R.string.monitorConfigurationInvalid);
            return false;
        }

        switch (cpuFreqMonitoringAvailable) {
            case CPUFreqMonitor.FAILURE_REASON_OK:
                break;
            case CPUFreqMonitor.FAILURE_REASON_DIR_NOT_EXISTS:
                MessageUtils.showInfoDialog(context, R.string.cpuFrequencyMonitoringNotAvailable,
                        R.string.sysDevicesSystemCpuDoesNotExist);
                success = false;
                break;
            case CPUFreqMonitor.FAILURE_REASON_DIR_EMPTY:
                MessageUtils.showInfoDialog(context, R.string.cpuFrequencyMonitoringNotAvailable,
                        R.string.canNotFindAnyCpusInSysDevicesSystemCpu);
                success = false;
                break;
            case CPUFreqMonitor.FAILURE_REASON_CUR_FREQUENCY_NO_PERMISSION:
                MessageUtils.showInfoDialog(context, R.string.cpuFrequencyMonitoringNotAvailable,
                        R.string.canNotReadFrequencyOfACpu);
                success = false;
                break;
            case CPUFreqMonitor.FAILURE_REASON_ILLEGAL_CONFIGURATION:
                MessageUtils.showInfoDialog(context, R.string.cpuFrequencyMonitoringNotAvailable,
                        R.string.monitorConfigurationInvalid);
                break;
            default:
                MessageUtils.showInfoDialog(context, R.string.cpuFrequencyMonitoringNotAvailable,
                        R.string.unknownError);
                success = false;
                break;
        }

        return success;
    }
}
