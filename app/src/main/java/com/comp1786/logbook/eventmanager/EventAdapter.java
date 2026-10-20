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
 * Shows the events as tickets with all their details, under a heading for each month.
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
                Row row = rows.get(position);
                listener.onEventClick(row.event, row.state);
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
        TicketState state = row.state;

        holder.ticket.setStubColor(
                MaterialColors.getColor(holder.itemView, state.getStubColorAttr()));
        int stubTextColor = MaterialColors.getColor(holder.itemView, state.getStubTextColorAttr());
        holder.monthText.setTextColor(stubTextColor);
        holder.dayText.setTextColor(stubTextColor);
        holder.dayOfWeekText.setTextColor(stubTextColor);
        holder.countdownText.setTextColor(
                MaterialColors.getColor(holder.itemView, state.getCountdownColorAttr()));
        holder.countdownText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                state == TicketState.LIVE ? R.drawable.ic_live : 0, 0, 0, 0);

        holder.dateStub.setContentDescription(EventDates.formatLong(date));
        holder.monthText.setText(EventDates.formatShortMonth(date));
        holder.dayText.setText(EventDates.formatDayOfMonth(date));
        holder.dayOfWeekText.setText(EventDates.formatShortDayOfWeek(date));
        holder.nameText.setText(event.getName());
        holder.typeLabel.setText(type.getLabelRes());
        holder.typeLabel.setBackgroundTintList(
                ColorStateList.valueOf(context.getColor(type.getContainerColorRes())));
        holder.countdownText.setText(state.countdown(context, event, row.daysAway));
        bindTimes(holder, event);
        holder.locationText.setText(event.getLocation());
        holder.organizerText.setText(event.getOrganizer());
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
        // Events happening now stand out; when none is, the next ones to start do.
        boolean anyLive = false;
        LocalDateTime nextStart = null;
        for (Event event : events) {
            TicketState state = TicketState.at(event, now);
            if (state == TicketState.LIVE) {
                anyLive = true;
            } else if (state == TicketState.UPCOMING && nextStart == null) {
                nextStart = event.getStart();
            }
        }
        List<Row> rows = new ArrayList<>();
        YearMonth month = null;
        for (Event event : events) {
            YearMonth eventMonth = YearMonth.from(event.getDate());
            if (!eventMonth.equals(month)) {
                month = eventMonth;
                rows.add(new Row(month, null, null, 0));
            }
            TicketState state = TicketState.at(event, now);
            if (state == TicketState.UPCOMING && !anyLive && event.getStart().equals(nextStart)) {
                state = TicketState.NEXT;
            }
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
        private final TicketState state;
        private final long daysAway;

        Row(YearMonth month, @Nullable Event event, @Nullable TicketState state, long daysAway) {
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

    /** Called when a ticket is tapped, with the state its colors show. */
    public interface OnEventClickListener {
        void onEventClick(Event event, TicketState state);
    }

    /** The view of one month heading. */
    public static final class MonthViewHolder extends RecyclerView.ViewHolder {

        private final TextView monthText;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            monthText = itemView.findViewById(R.id.month_heading);
        }
    }

    /** The views of one event ticket. */
    public static final class EventViewHolder extends RecyclerView.ViewHolder {

        private final TicketDrawable ticket;
        private final View dateStub;
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

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ticket = new TicketDrawable(itemView.getContext());
            int rippleColor = MaterialColors.getColor(
                    itemView, androidx.appcompat.R.attr.colorControlHighlight);
            // Without a mask, the ripple is clipped to the ticket shape.
            itemView.setBackground(
                    new RippleDrawable(ColorStateList.valueOf(rippleColor), ticket, null));
            // Screen readers say "Double tap to view details".
            ViewCompat.replaceAccessibilityAction(itemView, AccessibilityActionCompat.ACTION_CLICK,
                    itemView.getContext().getString(R.string.description_view_details), null);
            dateStub = itemView.findViewById(R.id.date_stub);
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
