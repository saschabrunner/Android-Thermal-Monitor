package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorController;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorItem;
import com.gitlab.saschabrunner.thermalmonitor.root.RootAccessException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootIPCSingleton;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class ThermalZonePickerDialogFragment extends PreferenceDialogFragmentCompat {
    private static final String TAG = "ThermalZonePickerDialog";

    public static ThermalZonePickerDialogFragment newInstance(String key) {
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);

        ThermalZonePickerDialogFragment fragment = new ThermalZonePickerDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        RecyclerView recyclerView = view.findViewById(R.id.dialogThermalZonePickerRecyclerView);
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);

        ThermalZonePickerListAdapter listAdapter = new ThermalZonePickerListAdapter();
        recyclerView.setAdapter(listAdapter);


        ThermalMonitor monitor;
        if (GlobalPreferences.getInstance().rootEnabled()) {
            try {
                monitor = new ThermalMonitor(
                        RootIPCSingleton.getInstance(getContext()));
            } catch (RootAccessException e) {
                Log.e(TAG, "Could not acquire root access", e);
//                pref.setDialogMessage(R.string.couldNotAcquireRootAccess);
                return;
            }
        } else {
            monitor = new ThermalMonitor();
        }

        Map<ThermalZoneMonitorItem, ThermalZonePickerListItem> listItemByMonitorItem = new HashMap<>();

        try {
            // TODO: Deinit
            monitor.init(new MonitorController() {
                @Override
                public void addItem(MonitorItem item) {
                    ThermalZonePickerListItem listItem = new ThermalZonePickerListItem((ThermalZoneMonitorItem) item);
                    listItemByMonitorItem.put((ThermalZoneMonitorItem) item, listItem);
                    listAdapter.addListItem(listItem);
                }

                @Override
                public void updateItem(MonitorItem item) {
                    ThermalZonePickerListItem listItem = Objects.requireNonNull(listItemByMonitorItem.get((ThermalZoneMonitorItem) item));
                    listItem.setCurrentTemperature(item.getValue());
                    listAdapter.updateListItem(listItem);
                }

                @Override
                public boolean isRunning() {
                    return true;
                }

                @Override
                public void awaitNotPaused() {
                    // Not needed
                }
            }, ThermalMonitor.Preferences.getPreferencesAllThermalZones(Utils.getGlobalPreferences(getContext())));
        } catch (MonitorException e) {
            // TODO
            return;
        }

        new Thread(monitor).start();

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
