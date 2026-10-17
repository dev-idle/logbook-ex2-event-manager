package com.comp1786.logbook.eventmanager;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Shows each event as a card with all five of its details.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        EventType type = event.getType();
        holder.nameText.setText(event.getName());
        holder.typeLabel.setText(type.getLabelRes());
        holder.typeLabel.setBackgroundTintList(ColorStateList.valueOf(
                holder.itemView.getContext().getColor(type.getContainerColorRes())));
        holder.dateText.setText(EventDates.formatWithDay(event.getDate()));
        holder.locationText.setText(event.getLocation());
        holder.organizerText.setText(event.getOrganizer());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /** The views of one event card. */
    public static final class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameText;
        private final TextView typeLabel;
        private final TextView dateText;
        private final TextView locationText;
        private final TextView organizerText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.event_name);
            typeLabel = itemView.findViewById(R.id.type_label);
            dateText = itemView.findViewById(R.id.event_date);
            locationText = itemView.findViewById(R.id.event_location);
            organizerText = itemView.findViewById(R.id.event_organizer);
        }
    }
}
