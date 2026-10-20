package com.comp1786.logbook.eventmanager;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Shows one event in full, including the text a ticket cuts short.
 */
public class EventDetailsActivity extends AppCompatActivity {

    /** Extra with the event to show. */
    public static final String EXTRA_EVENT = "event";
    /** Extra with the state of the event's ticket, so the countdown has the same color. */
    public static final String EXTRA_STATE = "state";

    private MaterialToolbar toolbar;
    private TextView nameText;
    private TextView typeLabel;
    private TextView countdownText;
    private View timeBlock;
    private TextView startTimeText;
    private TextView startDateText;
    private TextView lengthText;
    private TextView endTimeText;
    private TextView endDateText;
    private TextView locationText;
    private TextView organizerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        Event event = getIntent().getSerializableExtra(EXTRA_EVENT, Event.class);
        TicketState state = getIntent().getSerializableExtra(EXTRA_STATE, TicketState.class);
        showEvent(Objects.requireNonNull(event), Objects.requireNonNull(state));
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        nameText = findViewById(R.id.event_name);
        typeLabel = findViewById(R.id.type_label);
        countdownText = findViewById(R.id.event_countdown);
        timeBlock = findViewById(R.id.time_block);
        startTimeText = findViewById(R.id.start_time);
        startDateText = findViewById(R.id.start_date);
        lengthText = findViewById(R.id.event_length);
        endTimeText = findViewById(R.id.end_time);
        endDateText = findViewById(R.id.end_date);
        locationText = findViewById(R.id.event_location);
        organizerText = findViewById(R.id.event_organizer);
    }

    private void showEvent(Event event, TicketState state) {
        EventType type = event.getType();
        nameText.setText(event.getName());
        typeLabel.setText(type.getLabelRes());
        typeLabel.setBackgroundTintList(
                ColorStateList.valueOf(getColor(type.getContainerColorRes())));
        countdownText.setText(
                state.countdown(this, event, state.daysAway(event, LocalDate.now())));
        countdownText.setTextColor(
                MaterialColors.getColor(countdownText, state.getCountdownColorAttr()));
        countdownText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                state == TicketState.LIVE ? R.drawable.ic_live : 0, 0, 0, 0);
        showTimes(event);
        locationText.setText(event.getLocation());
        organizerText.setText(event.getOrganizer());
    }

    /** Shows when the event starts and ends, with the end date in color if it is the next day. */
    private void showTimes(Event event) {
        LocalDate startDate = event.getDate();
        LocalDate endDate = event.getEnd().toLocalDate();
        String start = EventTimes.format(event.getStartTime());
        String end = EventTimes.format(event.getEndTime());
        String length = EventTimes.formatLength(
                getResources(), event.getStartTime(), event.getEndTime());
        startTimeText.setText(start);
        startDateText.setText(EventDates.formatMedium(startDate));
        lengthText.setText(length);
        endTimeText.setText(end);
        endDateText.setText(EventDates.formatMedium(endDate));
        // The same color as the +1 on the ticket.
        endDateText.setTextColor(MaterialColors.getColor(endDateText, event.endsNextDay()
                ? androidx.appcompat.R.attr.colorPrimary
                : com.google.android.material.R.attr.colorOnSurfaceVariant));
        timeBlock.setContentDescription(getString(R.string.description_starts_ends,
                start, EventDates.formatLong(startDate), end, EventDates.formatLong(endDate),
                length));
    }
}
