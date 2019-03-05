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
import com.gitlab.saschabrunner.thermalmonitor.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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

    private Multimap<String, ThermalZonePickerListItem> thermalZoneGroupByName;
    private boolean todoLock = false;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Disable animations (otherwise excessive ViewHolders are used in notifyItemChanged())
        recyclerView.setItemAnimator(null);

        ThermalZonePickerListAdapter listAdapter = new ThermalZonePickerListAdapter();
        recyclerView.setAdapter(listAdapter);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            // TODO

            private int textSize = 50;
            private int groupSpacing = 100;
            private int itemsInGroup = 3;

            private Paint paint = new Paint();

            {
                paint.setTextSize(textSize);
            }

            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View view = parent.getChildAt(i);
                    int position = parent.getChildAdapterPosition(view);
                    if (position % itemsInGroup == 0) {
                        c.drawText("Group " + (position / itemsInGroup + 1), view.getLeft(),
                                view.getTop() - groupSpacing / 2 + textSize / 3, paint);
                    }
                }
            }

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) % itemsInGroup == 0) {
                    outRect.set(0, groupSpacing, 0, 0);
                }
            }
        });

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
        List<ThermalZonePickerListItem> thermalZones = new ArrayList<>();

        try {
            // TODO: Deinit
            monitor.init(new MonitorController() {
                @Override
                public void addItem(MonitorItem item) {
                    ThermalZonePickerListItem listItem = new ThermalZonePickerListItem((ThermalZoneMonitorItem) item);
                    listItemByMonitorItem.put((ThermalZoneMonitorItem) item, listItem);
//                    listAdapter.addListItem(listItem);
                    thermalZones.add(listItem);
                }

                @Override
                public void updateItem(MonitorItem item) {
                    ThermalZonePickerListItem listItem = Objects.requireNonNull(listItemByMonitorItem.get((ThermalZoneMonitorItem) item));
                    listItem.setCurrentTemperature(item.getValue());
                    if (todoLock) {
                        listAdapter.updateListItem(listItem);
                    }
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


        thermalZoneGroupByName = groupThermalZones(thermalZones);
        listAdapter.setThermalZones(thermalZoneGroupByName);
        todoLock = true;

        new Thread(monitor).start();

    }

    private Multimap<String, ThermalZonePickerListItem> groupThermalZones(List<ThermalZonePickerListItem> thermalZones) {
        List<ThermalZonePickerListItem> recommendedZones = new ArrayList<>();
        List<ThermalZonePickerListItem> otherZones = new ArrayList<>();
        Multimap<String, ThermalZonePickerListItem> groupByPrefix = ArrayListMultimap.create();
        List<ThermalZonePickerListItem> excludedZones = new ArrayList<>();

        List<Pattern> recommendedZoneTypes = createRegexPatterns(getResources().getStringArray(
                R.array.thermalMonitorRecommendedZones));
        List<Pattern> excludedZoneTypes = createRegexPatterns(getResources().getStringArray(
                R.array.thermalMonitorExcludedZones));

        // Anything that ends with a number is considered a potential candidate
        Pattern potentialGroupPattern = Pattern.compile(".*\\d");

        for (ThermalZonePickerListItem thermalZone : thermalZones) {
            boolean groupCandidate = false;
            String groupName = null;

            String type = thermalZone.getThermalZoneInfo().getType();

            // Check if zone type is recommended
            if (matchesAnyPattern(recommendedZoneTypes, type)) {
                thermalZone.setRecommended(true);
            }

            // Check if zone type is excluded
            if (matchesAnyPattern(excludedZoneTypes, type)) {
                thermalZone.setExcluded(true);
            }

            // Check if zone temperature value makes sense (else exclude it)
            // TODO: Check temperature value

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
                if (!matchesAnyPattern(excludedZoneTypes, type)) {
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

    private boolean matchesAnyPattern(List<Pattern> patterns, String string) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(string).matches()) {
                return true;
            }
        }
        return false;
    }

    // TODO: Util class
    public List<Pattern> createRegexPatterns(String[] strings) {
        List<Pattern> patterns = new ArrayList<>(strings.length);
        for (String string : strings) {
            patterns.add(Pattern.compile(string));
        }
        return patterns;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
