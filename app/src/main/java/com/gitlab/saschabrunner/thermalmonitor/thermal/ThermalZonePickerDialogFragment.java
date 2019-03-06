package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
import com.gitlab.saschabrunner.thermalmonitor.util.StringUtils;
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ThermalZonePickerDialogFragment extends PreferenceDialogFragmentCompat {
    private static final String TAG = "ThermalZonePickerDialog";
    private static final int MIN_TEMPERATURE = 1;

    private ThermalZonePickerListAdapter listAdapter;
    private Multimap<String, ThermalZonePickerListItem> thermalZoneGroupByName;
    private Map<Integer, String> titleByRecyclerViewPosition = new HashMap<>();
    private ThermalMonitor monitor;
    private ThermalMonitorController controller;
    private Thread monitoringThread;

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

        controller = new ThermalMonitorController();
        listAdapter = new ThermalZonePickerListAdapter();
        initializeRecyclerView(view);

        initializeThermalMonitor();
        startMonitoringThread();
    }

    private void initAfterWarmup() {
        thermalZoneGroupByName = groupThermalZones(controller.getThermalZones());
        calculateTitlePositions();
        listAdapter.setListContents(thermalZoneGroupByName);
    }

    private void startMonitoringThread() {
        monitoringThread = new Thread(monitor);
        monitoringThread.start();
    }

    private void initializeRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.dialogThermalZonePickerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new GroupTitleItemDecoration());
        recyclerView.setAdapter(listAdapter);

        // Disable animations (otherwise excessive ViewHolders are used in notifyItemChanged())
        recyclerView.setItemAnimator(null);
    }

    private void initializeThermalMonitor() {
        if (GlobalPreferences.getInstance().rootEnabled()) {
            try {
                monitor = new ThermalMonitor(
                        RootIPCSingleton.getInstance(getContext()));
            } catch (RootAccessException e) {
                Log.e(TAG, "Could not acquire root access", e);
//                pref.setDialogMessage(R.string.couldNotAcquireRootAccess);
                // TODO: Error handling
                return;
            }
        } else {
            monitor = new ThermalMonitor();
        }


        try {
            // TODO: Deinit
            monitor.init(controller, ThermalMonitor.Preferences
                    .getPreferencesAllThermalZones(Utils.getGlobalPreferences(getContext())));
        } catch (MonitorException e) {
            // TODO: Error handling
        }
    }

    private void calculateTitlePositions() {
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
    }

    private Multimap<String, ThermalZonePickerListItem> groupThermalZones(Collection<ThermalZonePickerListItem> thermalZones) {
        List<ThermalZonePickerListItem> recommendedZones = new ArrayList<>();
        List<ThermalZonePickerListItem> otherZones = new ArrayList<>();
        Multimap<String, ThermalZonePickerListItem> groupByPrefix = ArrayListMultimap.create();
        List<ThermalZonePickerListItem> excludedZones = new ArrayList<>();

        List<Pattern> recommendedZoneTypes = StringUtils.createRegexPatterns(getResources().getStringArray(
                R.array.thermalMonitorRecommendedZones));
        List<Pattern> excludedZoneTypes = StringUtils.createRegexPatterns(getResources().getStringArray(
                R.array.thermalMonitorExcludedZones));

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
            if (Integer.parseInt(thermalZone.getCurrentTemperature()) < MIN_TEMPERATURE) {
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

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // TODO: Stop monitoring thread

        if (monitor != null) {
            monitor.deinit();
        }
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
            ThermalZonePickerListItem listItem = Objects.requireNonNull(
                    listItemByMonitorItem.get((ThermalZoneMonitorItem) item));
            listItem.setCurrentTemperature(item.getValue());

            // We should only notify the adapter after it has received the elements
            if (listAdapter.getItemCount() > 0) {
                listAdapter.updateListItem(listItem);
            }
        }

        @Override
        public boolean isRunning() {
            return true;
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
        private int textSize = 50;
        private int groupSpacing = 100;

        private Paint paint = new Paint();

        public GroupTitleItemDecoration() {
            paint.setTextSize(textSize);
        }

        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            // Draw the titles for the groups
            for (int i = 0; i < parent.getChildCount(); i++) {
                View view = parent.getChildAt(i);
                int position = parent.getChildAdapterPosition(view);
                String titleForPosition = titleByRecyclerViewPosition.get(position);
                if (titleForPosition != null) {
                    c.drawText(titleForPosition, view.getLeft(),
                            view.getTop() - groupSpacing / 2 + textSize / 3, paint);
                }
            }
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            // Make space if this view is the first item of a new group
            String titleForPosition = titleByRecyclerViewPosition.get(parent.getChildAdapterPosition(view));
            if (titleByRecyclerViewPosition
                    .containsKey(parent.getChildAdapterPosition(view))) {
                outRect.set(0, groupSpacing, 0, 0);
            }
        }
    }
}
