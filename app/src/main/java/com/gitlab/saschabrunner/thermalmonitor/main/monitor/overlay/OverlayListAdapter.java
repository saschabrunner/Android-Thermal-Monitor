package com.gitlab.saschabrunner.thermalmonitor.main.monitor.overlay;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gitlab.saschabrunner.thermalmonitor.R;
import com.gitlab.saschabrunner.thermalmonitor.databinding.OverlayListItemBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OverlayListAdapter extends RecyclerView.Adapter<OverlayListAdapter.ViewHolder> {
    private static final String TAG = "OverlayListAdapter";

    private final OverlayConfig config;
    private final List<OverlayListItem> items;
    private final List<RecyclerView> recyclerViews;

    public OverlayListAdapter(OverlayConfig config) {
        this.config = config;
        this.items = new ArrayList<>();
        this.recyclerViews = new ArrayList<>();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.v(TAG, "onCreateViewHolder called");
        OverlayListItemBinding listItem = OverlayListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        listItem.setConfig(config);

        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OverlayListItem item = items.get(position);
        holder.getLabel().setText(item.getLabel());
        holder.getValue().setText(item.getValue());
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

    @Override
    public long getItemId(int position) {
        // This adapter allows no removal or reordering anyway, so using the position is fine
        return position;
    }

    /**
     * Add a list item to the end of the recycler view.
     *
     * @param listItem List item to add.
     */
    public void addListItem(OverlayListItem listItem) {
        int position = items.size();
        this.items.add(listItem);
        listItem.setId(position);
        notifyItemInserted(position);
    }

    /**
     * Update the value of an existing list item in the recycler view.
     *
     * @param listItem Existing list item to update.
     */
    public void updateListItem(OverlayListItem listItem) {
        if (listItem.getId() < 0 || listItem.getId() >= items.size()) {
            throw new ArrayIndexOutOfBoundsException("List item " +
                    listItem.getId() + " does not exist");
        }
        for (RecyclerView recyclerView : recyclerViews) {
            recyclerView.post(() -> notifyItemChanged(listItem.getId()));
        }
    }

    /**
     * Simple ViewHolder for a row containing a label and a value TextView
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final TextView value;

        public ViewHolder(@NonNull OverlayListItemBinding itemView) {
            super(itemView.getRoot());

            this.label = itemView.getRoot().findViewById(R.id.overlayItemLabel);
            this.value = itemView.getRoot().findViewById(R.id.overlayItemValue);
        }

        public TextView getLabel() {
            return label;
        }

        public TextView getValue() {
            return value;
        }
    }
}
