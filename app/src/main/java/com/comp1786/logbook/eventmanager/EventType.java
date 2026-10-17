package com.comp1786.logbook.eventmanager;

import androidx.annotation.StringRes;

/**
 * Event types listed in the logbook brief.
 */
public enum EventType {

    CONFERENCE(R.string.type_conference),
    WORKSHOP(R.string.type_workshop),
    SEMINAR(R.string.type_seminar),
    SOCIAL(R.string.type_social),
    OTHER(R.string.type_other);

    @StringRes
    private final int labelRes;

    EventType(@StringRes int labelRes) {
        this.labelRes = labelRes;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }
}
