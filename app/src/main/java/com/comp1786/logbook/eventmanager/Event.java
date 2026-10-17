package com.comp1786.logbook.eventmanager;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * An event entry. Serializable so it can be passed between activities in an Intent.
 */
public final class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final LocalDate date;
    private final String location;
    private final String organizer;
    private final EventType type;

    public Event(String name, LocalDate date, String location, String organizer, EventType type) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.organizer = organizer;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getOrganizer() {
        return organizer;
    }

    public EventType getType() {
        return type;
    }
}
