package com.comp1786.logbook.eventmanager;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the events as tickets with all five details, under a heading for each month.
 */
public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MONTH = 0;
    private static final int TYPE_EVENT = 1;

    private List<Row> rows = new ArrayList<>();

    /** Shows the events, which must be in date order. */
    public void setEvents(List<Event> events) {
        List<Row> newRows = buildRows(events, LocalDate.now());
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
        } else {
            bindEvent((EventViewHolder) holder, row.event, row.next);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private static void bindEvent(EventViewHolder holder, Event event, boolean next) {
        Context context = holder.itemView.getContext();
        LocalDate date = event.getDate();
        LocalDate today = LocalDate.now();
        EventType type = event.getType();

        TicketStyle style;
        if (next) {
            style = TicketStyle.NEXT;
        } else if (date.isBefore(today)) {
            style = TicketStyle.PAST;
        } else {
            style = TicketStyle.UPCOMING;
        }
        holder.ticket.setStubColor(MaterialColors.getColor(holder.itemView, style.stubColorAttr));
        int stubTextColor = MaterialColors.getColor(holder.itemView, style.stubTextColorAttr);
        holder.monthText.setTextColor(stubTextColor);
        holder.dayText.setTextColor(stubTextColor);
        holder.dayOfWeekText.setTextColor(stubTextColor);
        holder.countdownText.setTextColor(
                MaterialColors.getColor(holder.itemView, style.countdownColorAttr));

        holder.dateStub.setContentDescription(EventDates.formatLong(date));
        holder.monthText.setText(EventDates.formatShortMonth(date));
        holder.dayText.setText(EventDates.formatDayOfMonth(date));
        holder.dayOfWeekText.setText(EventDates.formatShortDayOfWeek(date));
        holder.nameText.setText(event.getName());
        holder.typeLabel.setText(type.getLabelRes());
        holder.typeLabel.setBackgroundTintList(
                ColorStateList.valueOf(context.getColor(type.getContainerColorRes())));
        holder.countdownText.setText(countdown(context, date, today));
        holder.locationText.setText(event.getLocation());
        holder.organizerText.setText(event.getOrganizer());
    }

    /** Describes the distance from today in whole days, months or years, such as "In 6 days". */
    private static String countdown(Context context, LocalDate date, LocalDate today) {
        long days = ChronoUnit.DAYS.between(today, date);
        if (days == 0) {
            return context.getString(R.string.countdown_today);
        } else if (days == 1) {
            return context.getString(R.string.countdown_tomorrow);
        } else if (days == -1) {
            return context.getString(R.string.countdown_yesterday);
        }
        Period period = days > 0 ? Period.between(today, date) : Period.between(date, today);
        Resources res = context.getResources();
        String duration;
        if (period.getYears() > 0) {
            duration = res.getQuantityString(
                    R.plurals.duration_years, period.getYears(), period.getYears());
        } else if (period.getMonths() > 0) {
            duration = res.getQuantityString(
                    R.plurals.duration_months, period.getMonths(), period.getMonths());
        } else {
            duration = res.getQuantityString(
                    R.plurals.duration_days, period.getDays(), period.getDays());
        }
        return context.getString(days > 0 ? R.string.countdown_in : R.string.countdown_ago,
                duration);
    }

    /** Adds month headings and marks the events on the first date from today as next. */
    private static List<Row> buildRows(List<Event> events, LocalDate today) {
        List<Row> rows = new ArrayList<>();
        YearMonth month = null;
        LocalDate nextDate = null;
        for (Event event : events) {
            LocalDate date = event.getDate();
            YearMonth eventMonth = YearMonth.from(date);
            if (!eventMonth.equals(month)) {
                month = eventMonth;
                rows.add(new Row(month, null, false));
            }
            if (nextDate == null && !date.isBefore(today)) {
                nextDate = date;
            }
            rows.add(new Row(month, event, date.equals(nextDate)));
        }
        return rows;
    }

    /** Ticket colors that set the next event apart and gray out past events. */
    private enum TicketStyle {

        NEXT(androidx.appcompat.R.attr.colorPrimary,
                com.google.android.material.R.attr.colorOnPrimary,
                androidx.appcompat.R.attr.colorPrimary),
        UPCOMING(com.google.android.material.R.attr.colorPrimaryContainer,
                com.google.android.material.R.attr.colorOnPrimaryContainer,
                com.google.android.material.R.attr.colorOnSurfaceVariant),
        PAST(com.google.android.material.R.attr.colorSurfaceContainerHighest,
                com.google.android.material.R.attr.colorOnSurfaceVariant,
                com.google.android.material.R.attr.colorOnSurfaceVariant);

        @AttrRes
        private final int stubColorAttr;
        @AttrRes
        private final int stubTextColorAttr;
        @AttrRes
        private final int countdownColorAttr;

        TicketStyle(@AttrRes int stubColorAttr, @AttrRes int stubTextColorAttr,
                @AttrRes int countdownColorAttr) {
            this.stubColorAttr = stubColorAttr;
            this.stubTextColorAttr = stubTextColorAttr;
            this.countdownColorAttr = countdownColorAttr;
        }
    }

    /** A month heading when event is null, otherwise an event in that month. */
    private static final class Row {

        private final YearMonth month;
        @Nullable
        private final Event event;
        private final boolean next;

        Row(YearMonth month, @Nullable Event event, boolean next) {
            this.month = month;
            this.event = event;
            this.next = next;
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
            // An earlier event added later takes over the next-event highlight.
            return oldRows.get(oldPosition).next == newRows.get(newPosition).next;
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
        private final TextView locationText;
        private final TextView organizerText;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ticket = new TicketDrawable(itemView.getContext());
            itemView.setBackground(ticket);
            dateStub = itemView.findViewById(R.id.date_stub);
            monthText = itemView.findViewById(R.id.event_month);
            dayText = itemView.findViewById(R.id.event_day);
            dayOfWeekText = itemView.findViewById(R.id.event_day_of_week);
            nameText = itemView.findViewById(R.id.event_name);
            typeLabel = itemView.findViewById(R.id.type_label);
            countdownText = itemView.findViewById(R.id.event_countdown);
            locationText = itemView.findViewById(R.id.event_location);
            organizerText = itemView.findViewById(R.id.event_organizer);
        }
    }
}
