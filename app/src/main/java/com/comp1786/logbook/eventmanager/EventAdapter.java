package com.comp1786.logbook.eventmanager;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the events as cards with all five details, under a heading for each month.
 */
public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MONTH = 0;
    private static final int TYPE_EVENT = 1;

    private List<Row> rows = new ArrayList<>();

    /** Shows the events, which must be in date order. */
    public void setEvents(List<Event> events) {
        List<Row> newRows = buildRows(events);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new RowDiff(rows, newRows));
        rows = newRows;
        diff.dispatchUpdatesTo(this);
    }

    /** Returns the position of the event in the list, or NO_POSITION if it is not shown. */
    public int positionOf(Event event) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).event == event) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    public boolean isMonthHeading(int position) {
        return rows.get(position).event == null;
    }

    @Override
    public int getItemViewType(int position) {
        return isMonthHeading(position) ? TYPE_MONTH : TYPE_EVENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_MONTH) {
            return new MonthViewHolder(inflater.inflate(R.layout.item_month, parent, false));
        }
        return new EventViewHolder(inflater.inflate(R.layout.item_event, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (row.event == null) {
            ((MonthViewHolder) holder).monthText.setText(EventDates.formatMonth(row.month));
            return;
        }
        EventViewHolder eventHolder = (EventViewHolder) holder;
        Event event = row.event;
        EventType type = event.getType();
        eventHolder.nameText.setText(event.getName());
        eventHolder.typeLabel.setText(type.getLabelRes());
        eventHolder.typeLabel.setBackgroundTintList(ColorStateList.valueOf(
                holder.itemView.getContext().getColor(type.getContainerColorRes())));
        eventHolder.dateText.setText(EventDates.formatWithDay(event.getDate()));
        eventHolder.locationText.setText(event.getLocation());
        eventHolder.organizerText.setText(event.getOrganizer());
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    /** Puts a heading before the first event of each month. */
    private static List<Row> buildRows(List<Event> events) {
        List<Row> rows = new ArrayList<>();
        YearMonth month = null;
        for (Event event : events) {
            YearMonth eventMonth = YearMonth.from(event.getDate());
            if (!eventMonth.equals(month)) {
                month = eventMonth;
                rows.add(new Row(month, null));
            }
            rows.add(new Row(month, event));
        }
        return rows;
    }

    /** A month heading when event is null, otherwise an event in that month. */
    private static final class Row {

        private final YearMonth month;
        @Nullable
        private final Event event;

        Row(YearMonth month, @Nullable Event event) {
            this.month = month;
            this.event = event;
        }
    }

    /** Finds the rows that an update adds or removes, so only those are redrawn. */
    private static final class RowDiff extends DiffUtil.Callback {

        private final List<Row> oldRows;
        private final List<Row> newRows;

        RowDiff(List<Row> oldRows, List<Row> newRows) {
            this.oldRows = oldRows;
            this.newRows = newRows;
        }

        @Override
        public int getOldListSize() {
            return oldRows.size();
        }

        @Override
        public int getNewListSize() {
            return newRows.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPosition, int newPosition) {
            Row oldRow = oldRows.get(oldPosition);
            Row newRow = newRows.get(newPosition);
            // Events cannot be edited, so an event keeps the same object while it is listed.
            return oldRow.event == newRow.event && oldRow.month.equals(newRow.month);
        }

        @Override
        public boolean areContentsTheSame(int oldPosition, int newPosition) {
            return true;
        }
    }

    /** The view of one month heading. */
    public static final class MonthViewHolder extends RecyclerView.ViewHolder {

        private final TextView monthText;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            monthText = itemView.findViewById(R.id.month_heading);
        }
    }

    /** The views of one event card. */
    public static final class EventViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameText;
        private final TextView typeLabel;
        private final TextView dateText;
        private final TextView locationText;
        private final TextView organizerText;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.event_name);
            typeLabel = itemView.findViewById(R.id.type_label);
            dateText = itemView.findViewById(R.id.event_date);
            locationText = itemView.findViewById(R.id.event_location);
            organizerText = itemView.findViewById(R.id.event_organizer);
        }
    }
}
