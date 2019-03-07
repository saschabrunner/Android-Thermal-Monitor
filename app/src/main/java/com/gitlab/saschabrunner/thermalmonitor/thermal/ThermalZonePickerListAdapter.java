package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.gitlab.saschabrunner.thermalmonitor.databinding.DialogThermalZonePickerListItemBinding;
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

        DialogThermalZonePickerListItemBinding binding =
                DialogThermalZonePickerListItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThermalZonePickerListItem listItem = items.get(position);
        holder.rebind(listItem);
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
        private DialogThermalZonePickerListItemBinding binding;

        private ViewHolder(@NonNull DialogThermalZonePickerListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void rebind(ThermalZonePickerListItem data) {
            binding.setData(data);
            binding.executePendingBindings();
        }
    }
}
