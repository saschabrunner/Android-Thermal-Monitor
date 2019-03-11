package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.main.GlobalPreferences;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorController;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorException;
import com.gitlab.saschabrunner.thermalmonitor.main.monitor.MonitorItem;
import com.gitlab.saschabrunner.thermalmonitor.root.RootAccessException;
import com.gitlab.saschabrunner.thermalmonitor.root.RootIPCSingleton;
import com.gitlab.saschabrunner.thermalmonitor.util.MessageUtils;
import com.gitlab.saschabrunner.thermalmonitor.util.StringUtils;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ThermalZonePickerDialogFragment extends PreferenceDialogFragmentCompat {
    private static final String TAG = "ThermalZonePickerDialog";
    private static final int MIN_TEMPERATURE = 1;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private ThermalZonePickerListAdapter listAdapter;
    private ThermalMonitor monitor;
    private ThermalMonitorController controller;
    private Thread monitoringThread;

    private boolean monitoringRunning = true;
    private final Lock monitoringRunningLock = new ReentrantLock();

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

        try {
            if (!ThermalMonitorValidator.checkMonitoringAvailable(getContext(),
                    ThermalMonitor.Preferences.getPreferencesAllThermalZones(
                            Utils.getGlobalPreferences(getContext())))) {
                dismiss();
                return;
            }
        } catch (MonitorException e) {
            Log.e(TAG, "Illegal configuration for monitor", e);
            MessageUtils.showInfoDialog(getContext(), R.string.thermalMonitoringNotAvailable,
                    R.string.monitorConfigurationInvalid);
            dismiss();
            return;
        }

        controller = new ThermalMonitorController();
        listAdapter = new ThermalZonePickerListAdapter();
        progressBar = view.findViewById(R.id.dialogThermalZonePickerProgressBar);
        initializeRecyclerView(view);

        createThermalMonitor();
        new ThermalMonitorInitializer(this).execute();
    }

    private void initializeRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.dialogThermalZonePickerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(listAdapter);

        // Disable animations (otherwise excessive ViewHolders are used in notifyItemChanged())
        recyclerView.setItemAnimator(null);
    }

    private void createThermalMonitor() {
        if (GlobalPreferences.getInstance().rootEnabled()) {
            try {
                monitor = new ThermalMonitor(
                        RootIPCSingleton.getInstance(getContext()));
            } catch (RootAccessException e) {
                Log.e(TAG, "Could not acquire root access", e);
                MessageUtils.showInfoDialog(getContext(), R.string.rootAccessDenied,
                        R.string.couldNotAcquireRootAccess);
                return;
            }
        } else {
            monitor = new ThermalMonitor();
        }

        monitoringThread = new Thread(monitor);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Set<String> selectedZoneIds = new HashSet<>();
            for (ThermalZonePickerListItem thermalZone : controller.getThermalZones()) {
                if (thermalZone.isSelected()) {
                    selectedZoneIds.add(String.valueOf(thermalZone.getThermalZoneInfo().getId()));
                }
            }

            ThermalZonePickerPreference preference = (ThermalZonePickerPreference) getPreference();
            preference.setValues(selectedZoneIds);
        }

        if (monitor != null) {
            deinitMonitoring();
        }
    }

    private void deinitMonitoring() {
        monitoringRunningLock.lock();

        try {
            monitoringRunning = false;
            monitoringThread.interrupt();
        } finally {
            monitoringRunningLock.unlock();
        }

        try {
            monitoringThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Received unexpected interrupt");
            return;
        }

        monitor.deinit();
    }

    /**
     * Called in separate thread!
     */
    private void initAfterWarmup() {
        applySelection();
        Multimap<String, ThermalZonePickerListItem> thermalZoneGroupByName
                = groupThermalZones(controller.getThermalZones());
        listAdapter.setListContents(thermalZoneGroupByName);
        recyclerView.post(() -> {
            recyclerView.addItemDecoration(
                    new GroupTitleItemDecoration(calculateTitlePositions(thermalZoneGroupByName)));
            progressBar.setVisibility(View.GONE);
        });
    }

    private void applySelection() {
        ThermalZonePickerPreference preference = (ThermalZonePickerPreference) getPreference();
        Set<String> selectedThermalZoneIds = preference.getValues();
        for (ThermalZonePickerListItem thermalZone : controller.getThermalZones()) {
            if (selectedThermalZoneIds.contains(
                    String.valueOf(thermalZone.getThermalZoneInfo().getId()))) {
                thermalZone.setSelected(true);
            }
        }
    }

    private Map<Integer, String> calculateTitlePositions(
            Multimap<String, ThermalZonePickerListItem> thermalZoneGroupByName) {
        // HashMap has better performance than SparseArray here because we will do many
        // lookups for non existing items.
        @SuppressLint("UseSparseArrays")
        Map<Integer, String> titleByRecyclerViewPosition = new HashMap<>();

        int currentPosition = 0;
        String currentTitle = null;

        for (Map.Entry<String, ThermalZonePickerListItem> entry
                : thermalZoneGroupByName.entries()) {
            if (!entry.getKey().equals(currentTitle)) {
                // This item has a new title
                currentTitle = entry.getKey();
                titleByRecyclerViewPosition.put(currentPosition, currentTitle);
            }
            currentPosition++;
        }

        return titleByRecyclerViewPosition;
    }

    private Multimap<String, ThermalZonePickerListItem> groupThermalZones(
            Collection<ThermalZonePickerListItem> thermalZones) {
        List<ThermalZonePickerListItem> recommendedZones = new ArrayList<>();
        List<ThermalZonePickerListItem> otherZones = new ArrayList<>();
        Multimap<String, ThermalZonePickerListItem> groupByPrefix = ArrayListMultimap.create();
        List<ThermalZonePickerListItem> excludedZones = new ArrayList<>();

        List<Pattern> recommendedZoneTypes = StringUtils.createRegexPatterns(
                getResources().getStringArray(R.array.thermalMonitorRecommendedZones));
        List<Pattern> excludedZoneTypes = StringUtils.createRegexPatterns(
                getResources().getStringArray(R.array.thermalMonitorExcludedZones));

        // Anything that ends with a number is considered a potential candidate
        Pattern potentialGroupPattern = Pattern.compile(".*\\d");

        for (ThermalZonePickerListItem thermalZone : thermalZones) {
            boolean groupCandidate = false;
            String groupName = null;

            String type = thermalZone.getThermalZoneInfo().getType();

            // Check if zone type is recommended
            if (StringUtils.matchesAnyPattern(recommendedZoneTypes, type)) {
                thermalZone.setRecommended(true);
            }

            // Check if zone type is excluded
            if (StringUtils.matchesAnyPattern(excludedZoneTypes, type)) {
                thermalZone.setExcluded(true);
            }

            // Check if zone temperature value makes sense (else exclude it)
            if (thermalZone.getCurrentTemperatureCelsius() < MIN_TEMPERATURE) {
                thermalZone.setExcluded(true);
            }

            // Check if zone might be part of a group (eg. tsens_tz_sensorXYZ)
            Matcher matcher = potentialGroupPattern.matcher(type);
            if (matcher.matches()) {
                groupCandidate = true;
                // Group name = name of type without number at end
                groupName = type.replaceAll("\\d*$", "");
            }

            // Step one, add every zone in some collection
            if (thermalZone.isRecommended() && !thermalZone.isExcluded()) {
                recommendedZones.add(thermalZone);
            } else if (groupCandidate) {
                groupByPrefix.put(groupName, thermalZone);
            } else if (thermalZone.isExcluded()) {
                excludedZones.add(thermalZone);
            } else {
                otherZones.add(thermalZone);
            }
        }

        // Step two, cleanup, move zones that don't meet the requirements to count as a group
        List<Collection<ThermalZonePickerListItem>> groupsToMove = new ArrayList<>();
        for (Collection<ThermalZonePickerListItem> group : groupByPrefix.asMap().values()) {
            boolean atLeastOneNotExcluded = false;

            for (ThermalZonePickerListItem thermalZone : group) {
                String type = thermalZone.getThermalZoneInfo().getType();
                if (!StringUtils.matchesAnyPattern(excludedZoneTypes, type)
                        && !thermalZone.isExcluded()) {
                    atLeastOneNotExcluded = true;
                }
            }

            // A group must have at least three zones
            if (group.size() < 3 || !atLeastOneNotExcluded) {
                groupsToMove.add(group);
            }
        }

        for (Collection<ThermalZonePickerListItem> group : groupsToMove) {
            Iterator<ThermalZonePickerListItem> iterator = group.iterator();
            while (iterator.hasNext()) {
                ThermalZonePickerListItem thermalZone = iterator.next();

                if (thermalZone.isExcluded()) {
                    excludedZones.add(thermalZone);
                } else {
                    otherZones.add(thermalZone);
                }

                iterator.remove();
            }
        }

        // Order of groups: Recommended, other, specific groups, excluded
        Multimap<String, ThermalZonePickerListItem> groupByName = ArrayListMultimap.create();
        groupByName.putAll(getString(R.string.recommended), recommendedZones);
        groupByName.putAll(getString(R.string.other), otherZones);
        groupByName.putAll(groupByPrefix);
        groupByName.putAll(getString(R.string.excluded), excludedZones);
        return groupByName;
    }

    private class ThermalMonitorController implements MonitorController {
        private int numIterations = 0;
        private Map<ThermalZoneMonitorItem, ThermalZonePickerListItem> listItemByMonitorItem
                = new LinkedHashMap<>();

        @Override
        public void addItem(MonitorItem item) {
            ThermalZonePickerListItem listItem =
                    new ThermalZonePickerListItem((ThermalZoneMonitorItem) item);
            listItemByMonitorItem.put((ThermalZoneMonitorItem) item, listItem);
        }

        @Override
        public void updateItem(MonitorItem item) {
            ThermalZoneMonitorItem tzItem = (ThermalZoneMonitorItem) item;
            ThermalZonePickerListItem listItem = Objects.requireNonNull(
                    listItemByMonitorItem.get(tzItem));
            listItem.setCurrentTemperatureCelsius(tzItem.getLastTemperature());
            listItem.setCurrentTemperatureUiValue(item.getValue());

            // We should only notify the adapter after it has received the elements
            if (listAdapter.getItemCount() > 0) {
                listAdapter.updateListItem(listItem);
            }
        }

        @Override
        public boolean isRunning() {
            monitoringRunningLock.lock();
            try {
                return monitoringRunning;
            } finally {
                monitoringRunningLock.unlock();
            }
        }

        @Override
        public void awaitNotPaused() {
            // No actual waiting needed, since this controller does not pause

            // We count and check the number of iterations to finish initialization later
            if (numIterations == 1) {
                // We now have temperature values for every thermal zone and can display the list
                initAfterWarmup();
            }
            numIterations++;
        }

        private Collection<ThermalZonePickerListItem> getThermalZones() {
            return listItemByMonitorItem.values();
        }
    }

    private class GroupTitleItemDecoration extends RecyclerView.ItemDecoration {
        private final float textSize;
        private final float groupSpacing;

        private final Paint paint = new Paint();
        private final Map<Integer, String> titleByRecyclerViewPosition;

        private GroupTitleItemDecoration(Map<Integer, String> titleByRecyclerViewPosition) {
            this.titleByRecyclerViewPosition = titleByRecyclerViewPosition;
            this.textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
            this.groupSpacing = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
            paint.setTextSize(textSize);
            paint.setAntiAlias(true);
        }

        @Override
        public void onDrawOver(
                @NonNull Canvas c,
                @NonNull RecyclerView parent,
                @NonNull RecyclerView.State state) {
            // Draw the titles for the groups
            for (int i = 0; i < parent.getChildCount(); i++) {
                View view = parent.getChildAt(i);
                int position = parent.getChildAdapterPosition(view);
                String titleForPosition = titleByRecyclerViewPosition.get(position);
                if (titleForPosition != null) {
                    int a = (int) (view.getTop() - groupSpacing / 2 + textSize / 3);
                    c.drawText(titleForPosition, view.getLeft(), a, paint);
                }
            }
        }

        @Override
        public void getItemOffsets(
                @NonNull Rect outRect,
                @NonNull View view,
                @NonNull RecyclerView parent,
                @NonNull RecyclerView.State state) {
            // Make space if this view is the first item of a new group
            if (titleByRecyclerViewPosition
                    .containsKey(parent.getChildAdapterPosition(view))) {
                outRect.set(0, (int) groupSpacing, 0, 0);
            }
        }
    }

    private static class ThermalMonitorInitializer extends AsyncTask<Void, Void, Void> {
        private final WeakReference<ThermalZonePickerDialogFragment> fragment;
        private final ThermalMonitor monitor;
        private final Thread monitoringThread;
        private final ThermalMonitorController controller;
        private final SharedPreferences preferences;

        private boolean failed;

        private ThermalMonitorInitializer(ThermalZonePickerDialogFragment fragment) {
            this.fragment = new WeakReference<>(fragment);
            this.monitor = fragment.monitor;
            this.monitoringThread = fragment.monitoringThread;
            this.controller = fragment.controller;
            this.preferences = Utils.getGlobalPreferences(fragment.getContext());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                monitor.init(controller, ThermalMonitor.Preferences
                        .getPreferencesAllThermalZones(preferences));
            } catch (MonitorException e) {
                Log.e(TAG, "Monitor exited with exception", e);
                failed = true;
                return null;
            }

            monitoringThread.start();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ThermalZonePickerDialogFragment fragment = this.fragment.get();
            if (failed && fragment != null) {
                Dialog dialog = fragment.getDialog();
                if (dialog != null) {
                    dialog.dismiss();
                }

                MessageUtils.showInfoDialog(fragment.getContext(),
                        R.string.thermalMonitoringNotAvailable,
                        R.string.monitorExitedWithException);
            }
        }
    }
}
