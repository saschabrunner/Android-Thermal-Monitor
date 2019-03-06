package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ThermalZonePickerListAdapter
        extends RecyclerView.Adapter<ThermalZonePickerListAdapter.ViewHolder> {
    private static final String TAG = "ThermalZonePickerLA";
    private List<RecyclerView> recyclerViews = new ArrayList<>();
    private List<ThermalZonePickerListItem> items = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.v(TAG, "onCreateViewHolder called");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_thermal_zone_picker_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThermalZonePickerListItem listItem = items.get(position);
        holder.getId().setText(String.valueOf(listItem.getThermalZoneInfo().getId()));
        holder.getType().setText(listItem.getThermalZoneInfo().getType());
        holder.getTemperature().setText(listItem.getCurrentTemperature());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerViews.add(recyclerView);
    }

    /**
     * Update the value of an existing list item in the recycler view.
     *
     * @param listItem Existing list item to update.
     */
    public void updateListItem(ThermalZonePickerListItem listItem) {
        if (listItem.getRecyclerViewId() < 0 || listItem.getRecyclerViewId() >= getItemCount()) {
            throw new ArrayIndexOutOfBoundsException("List item " +
                    listItem.getRecyclerViewId() + " does not exist");
        }
        for (RecyclerView recyclerView : recyclerViews) {
            recyclerView.post(() -> notifyItemChanged(listItem.getRecyclerViewId()));
        }
    }

    public void setListContents(Multimap<String, ThermalZonePickerListItem> thermalZoneGroupByName) {
        int i = 0;
        for (ThermalZonePickerListItem thermalZone : thermalZoneGroupByName.values()) {
            thermalZone.setRecyclerViewId(i++);
            items.add(thermalZone);
        }
        for (RecyclerView recyclerView : recyclerViews) {
            recyclerView.post(this::notifyDataSetChanged);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView id;
        private final TextView type;
        private final TextView temperature;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.id = itemView.findViewById(R.id.dialogThermalZonePickerId);
            this.type = itemView.findViewById(R.id.dialogThermalZonePickerType);
            this.temperature = itemView.findViewById(R.id.dialogThermalZonePickerTemperature);
        }

        public TextView getId() {
            return id;
        }

        public TextView getType() {
            return type;
        }

        public TextView getTemperature() {
            return temperature;
        }
    }
}
