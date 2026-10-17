package com.comp1786.logbook.eventmanager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Reads and writes event dates in the DD/MM/YYYY format used by the brief.
 */
public final class EventDates {

    // Earliest and latest dates accepted, to catch typing mistakes such as the year 0026.
    public static final LocalDate MINIMUM = LocalDate.of(1900, 1, 1);
    public static final LocalDate MAXIMUM = LocalDate.of(2100, 12, 31);

    // STRICT rejects days that do not exist, such as 31/02/2026.
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    // Day names are in English to match the rest of the app.
    private static final DateTimeFormatter OUTPUT_WITH_DAY =
            DateTimeFormatter.ofPattern("EEE, dd/MM/uuuu", Locale.UK);

    private EventDates() {
    }

    /** Returns the date for text such as "5/3/2026", or null if it is not a real date. */
    public static LocalDate parse(String text) {
        try {
            return LocalDate.parse(text, INPUT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Formats a date as DD/MM/YYYY, such as "05/03/2026". */
    public static String format(LocalDate date) {
        return date.format(OUTPUT);
    }

    /** Formats a date with its day of the week, such as "Sat, 24/10/2026". */
    public static String formatWithDay(LocalDate date) {
        return date.format(OUTPUT_WITH_DAY);
    }
}
