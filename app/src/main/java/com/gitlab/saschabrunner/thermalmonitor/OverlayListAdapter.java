package com.gitlab.saschabrunner.thermalmonitor;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OverlayListAdapter extends RecyclerView.Adapter<OverlayListAdapter.ViewHolder> {
    private static final String TAG = "OverlayListAdapter";

    private List<OverlayListItem> items;
    private List<RecyclerView> recyclerViews;

    public OverlayListAdapter() {
        this.items = new ArrayList<>();
        this.recyclerViews = new ArrayList<>();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.v(TAG, "onCreateViewHolder called");
        LinearLayout listItem = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.overlay_list_item, parent, false);

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

        public ViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);

            this.label = itemView.findViewById(R.id.overlayItemLabel);
            this.value = itemView.findViewById(R.id.overlayItemValue);
        }

        public TextView getLabel() {
            return label;
        }

        public TextView getValue() {
            return value;
        }
    }
}
