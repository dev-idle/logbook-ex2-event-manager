package com.comp1786.logbook.eventmanager;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.AttrRes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * Where an event is in time, with the colors and countdown that show it.
 */
public enum TicketState {

    LIVE(androidx.appcompat.R.attr.colorPrimary,
            com.google.android.material.R.attr.colorOnPrimary,
            androidx.appcompat.R.attr.colorPrimary),
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

    TicketState(@AttrRes int stubColorAttr, @AttrRes int stubTextColorAttr,
            @AttrRes int countdownColorAttr) {
        this.stubColorAttr = stubColorAttr;
        this.stubTextColorAttr = stubTextColorAttr;
        this.countdownColorAttr = countdownColorAttr;
    }

    @AttrRes
    public int getStubColorAttr() {
        return stubColorAttr;
    }

    @AttrRes
    public int getStubTextColorAttr() {
        return stubTextColorAttr;
    }

    @AttrRes
    public int getCountdownColorAttr() {
        return countdownColorAttr;
    }

    /** Returns the state at the given time. EventAdapter marks the next events itself. */
    public static TicketState at(Event event, LocalDateTime now) {
        if (!event.getEnd().isAfter(now)) {
            return PAST;
        }
        return event.getStart().isAfter(now) ? UPCOMING : LIVE;
    }

    /** Returns the days from today to the day the countdown counts to, negative once past. */
    public long daysAway(Event event, LocalDate today) {
        return ChronoUnit.DAYS.between(today, countdownDate(event));
    }

    /** Describes when the event is, such as "Happening now", "In 6 days" or "2 months ago". */
    public String countdown(Context context, Event event, long daysAway) {
        if (this == LIVE) {
            return context.getString(R.string.countdown_live);
        }
        LocalDate date = countdownDate(event);
        LocalDate today = date.minusDays(daysAway);
        if (daysAway == 0) {
            return context.getString(
                    this == PAST ? R.string.countdown_ended_today : R.string.countdown_today);
        } else if (daysAway == 1) {
            return context.getString(R.string.countdown_tomorrow);
        } else if (daysAway == -1) {
            return context.getString(R.string.countdown_yesterday);
        }
        Period period = daysAway > 0 ? Period.between(today, date) : Period.between(date, today);
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
        return context.getString(daysAway > 0 ? R.string.countdown_in : R.string.countdown_ago,
                duration);
    }

    /** Returns the day a countdown counts to: the end for past events, otherwise the start. */
    private LocalDate countdownDate(Event event) {
        return this == PAST ? event.getEnd().toLocalDate() : event.getDate();
    }
}
