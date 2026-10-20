package com.comp1786.logbook.eventmanager;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the events as cards with all their details, under a heading for each month.
 */
public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MONTH = 0;
    private static final int TYPE_EVENT = 1;

    private final OnEventClickListener listener;
    private List<Row> rows = new ArrayList<>();

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    /** Shows the events, which must be in start order, as they stand at the current time. */
    public void setEvents(List<Event> events) {
        List<Row> newRows = buildRows(events, LocalDateTime.now());
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
        EventViewHolder holder =
                new EventViewHolder(inflater.inflate(R.layout.item_event, parent, false));
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onEventClick(rows.get(position).event);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (row.event == null) {
            ((MonthViewHolder) holder).monthText.setText(EventDates.formatMonth(row.month));
        } else {
            bindEvent((EventViewHolder) holder, row);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private static void bindEvent(EventViewHolder holder, Row row) {
        Context context = holder.itemView.getContext();
        Event event = row.event;
        LocalDate date = event.getDate();
        EventType type = event.getType();
        EventCardState state = row.state;

        holder.card.setDatePanelColor(
                MaterialColors.getColor(holder.itemView, state.getDatePanelColorAttr()));
        int datePanelTextColor =
                MaterialColors.getColor(holder.itemView, state.getDatePanelTextColorAttr());
        holder.monthText.setTextColor(datePanelTextColor);
        holder.dayText.setTextColor(datePanelTextColor);
        holder.dayOfWeekText.setTextColor(datePanelTextColor);

        holder.datePanel.setContentDescription(EventDates.formatLong(date));
        holder.monthText.setText(EventDates.formatShortMonth(date));
        holder.dayText.setText(EventDates.formatDayOfMonth(date));
        holder.dayOfWeekText.setText(EventDates.formatShortDayOfWeek(date));
        holder.nameText.setText(event.getName());
        holder.typeLabel.setText(type.getLabelRes());
        holder.typeLabel.setBackgroundTintList(
                ColorStateList.valueOf(context.getColor(type.getContainerColorRes())));
        state.showCountdown(holder.countdownText, event, row.daysAway);
        bindTimes(holder, event);
        holder.locationText.setText(event.getLocation());
        holder.locationText.setContentDescription(
                context.getString(R.string.description_location, event.getLocation()));
        holder.organizerText.setText(event.getOrganizer());
        holder.organizerText.setContentDescription(
                context.getString(R.string.description_organizer, event.getOrganizer()));
    }

    /** Shows the start, end and length like a boarding pass, read out as one sentence. */
    private static void bindTimes(EventViewHolder holder, Event event) {
        Context context = holder.itemView.getContext();
        String start = EventTimes.format(event.getStartTime());
        String end = EventTimes.format(event.getEndTime());
        String length = EventTimes.formatLength(
                context.getResources(), event.getStartTime(), event.getEndTime());
        holder.startTimeText.setText(start);
        holder.endTimeText.setText(end);
        holder.lengthText.setText(length);
        holder.nextDayText.setVisibility(event.endsNextDay() ? View.VISIBLE : View.GONE);
        holder.timeRow.setContentDescription(context.getString(event.endsNextDay()
                ? R.string.description_time_range_next_day : R.string.description_time_range,
                start, end, length));
    }

    /** Adds month headings and works out the state of each event at the given time. */
    private static List<Row> buildRows(List<Event> events, LocalDateTime now) {
        LocalDateTime nextStart = EventCardState.nextStart(events, now);
        List<Row> rows = new ArrayList<>();
        YearMonth month = null;
        for (Event event : events) {
            YearMonth eventMonth = YearMonth.from(event.getDate());
            if (!eventMonth.equals(month)) {
                month = eventMonth;
                rows.add(new Row(month, null, null, 0));
            }
            EventCardState state = EventCardState.at(event, now, nextStart);
            rows.add(new Row(month, event, state, state.daysAway(event, now.toLocalDate())));
        }
        return rows;
    }

    /** A month heading when event is null, otherwise an event in that month. */
    private static final class Row {

        private final YearMonth month;
        @Nullable
        private final Event event;
        @Nullable
        private final EventCardState state;
        private final long daysAway;

        Row(YearMonth month, @Nullable Event event, @Nullable EventCardState state, long daysAway) {
            this.month = month;
            this.event = event;
            this.state = state;
            this.daysAway = daysAway;
        }
    }

    /** Finds the rows that an update adds, removes or changes, so only those are redrawn. */
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
            Row oldRow = oldRows.get(oldPosition);
            Row newRow = newRows.get(newPosition);
            // States and countdowns change as time passes and as earlier events are added.
            return oldRow.state == newRow.state && oldRow.daysAway == newRow.daysAway;
        }
    }

    /** Called when a card is tapped. */
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    /** The view of one month heading. */
    private static final class MonthViewHolder extends RecyclerView.ViewHolder {

        private final TextView monthText;

        MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            monthText = itemView.findViewById(R.id.month_heading);
        }
    }

    /** The views of one event card. */
    private static final class EventViewHolder extends RecyclerView.ViewHolder {

        private final EventCardDrawable card;
        private final View datePanel;
        private final TextView monthText;
        private final TextView dayText;
        private final TextView dayOfWeekText;
        private final TextView nameText;
        private final TextView typeLabel;
        private final TextView countdownText;
        private final View timeRow;
        private final TextView startTimeText;
        private final TextView endTimeText;
        private final TextView lengthText;
        private final TextView nextDayText;
        private final TextView locationText;
        private final TextView organizerText;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            card = new EventCardDrawable(itemView.getContext());
            int rippleColor = MaterialColors.getColor(
                    itemView, androidx.appcompat.R.attr.colorControlHighlight);
            // Without a mask, the ripple is clipped to the card shape.
            itemView.setBackground(
                    new RippleDrawable(ColorStateList.valueOf(rippleColor), card, null));
            // Screen readers say "Double tap to view details".
            ViewCompat.replaceAccessibilityAction(itemView, AccessibilityActionCompat.ACTION_CLICK,
                    itemView.getContext().getString(R.string.description_view_details), null);
            datePanel = itemView.findViewById(R.id.date_panel);
            monthText = itemView.findViewById(R.id.event_month);
            dayText = itemView.findViewById(R.id.event_day);
            dayOfWeekText = itemView.findViewById(R.id.event_day_of_week);
            nameText = itemView.findViewById(R.id.event_name);
            typeLabel = itemView.findViewById(R.id.type_label);
            countdownText = itemView.findViewById(R.id.event_countdown);
            timeRow = itemView.findViewById(R.id.time_row);
            startTimeText = itemView.findViewById(R.id.start_time);
            endTimeText = itemView.findViewById(R.id.end_time);
            lengthText = itemView.findViewById(R.id.event_length);
            nextDayText = itemView.findViewById(R.id.next_day);
            locationText = itemView.findViewById(R.id.event_location);
            organizerText = itemView.findViewById(R.id.event_organizer);
        }
    }
}
