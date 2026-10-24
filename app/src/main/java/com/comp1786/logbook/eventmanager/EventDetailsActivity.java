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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Shows one event in full, including the text a card cuts short.
 */
public class EventDetailsActivity extends AppCompatActivity {

    /** Extra with the event to show. */
    public static final String EXTRA_EVENT = "event";
    /** Extra with every listed event, which decides whether this one is the next. */
    public static final String EXTRA_EVENTS = "events";

    private MaterialToolbar toolbar;
    private TextView nameText;
    private TextView typeLabel;
    private TextView countdownText;
    private TextView dateText;
    private View timeRow;
    private TextView timeText;
    private TextView nextDayText;
    private TextView lengthText;
    private TextView locationText;
    private TextView organizerText;
    private View content;

    private Event event;
    private List<Event> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            // The card scrolls under the navigation bar and ends clear of it.
            v.setPadding(bars.left, bars.top, bars.right, 0);
            content.setPadding(content.getPaddingLeft(), content.getPaddingTop(),
                    content.getPaddingRight(),
                    getResources().getDimensionPixelSize(R.dimen.screen_padding) + bars.bottom);
            return insets;
        });

        bindViews();
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        event = Objects.requireNonNull(getIntent().getSerializableExtra(EXTRA_EVENT, Event.class));
        events = Arrays.asList(Objects.requireNonNull(
                getIntent().getSerializableExtra(EXTRA_EVENTS, Event[].class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The countdown depends on the current time, so it is worked out on each start.
        showEvent();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        nameText = findViewById(R.id.event_name);
        typeLabel = findViewById(R.id.type_label);
        countdownText = findViewById(R.id.event_countdown);
        dateText = findViewById(R.id.event_date);
        timeRow = findViewById(R.id.time_row);
        timeText = findViewById(R.id.event_time);
        nextDayText = findViewById(R.id.next_day);
        lengthText = findViewById(R.id.event_length);
        locationText = findViewById(R.id.event_location);
        organizerText = findViewById(R.id.event_organizer);
        content = findViewById(R.id.content);
    }

    /** Shows the event with the same countdown and colors as its card. */
    private void showEvent() {
        LocalDateTime now = LocalDateTime.now();
        EventCardState state = EventCardState.at(event, now, EventCardState.nextStart(events, now));
        EventType type = event.getType();
        nameText.setText(event.getName());
        typeLabel.setText(type.getLabelRes());
        typeLabel.setBackgroundTintList(
                ColorStateList.valueOf(getColor(type.getContainerColorRes())));
        state.showCountdown(countdownText, event, state.daysAway(event, now.toLocalDate()));
        showTimes();
        // Without labels on screen, screen readers hear what each row is.
        locationText.setText(event.getLocation());
        locationText.setContentDescription(
                getString(R.string.description_location, event.getLocation()));
        organizerText.setText(event.getOrganizer());
        organizerText.setContentDescription(
                getString(R.string.description_organizer, event.getOrganizer()));
    }

    /** Shows the date, then the times and length, with a +1 when the event ends the next day. */
    private void showTimes() {
        String start = EventTimes.format(event.getStartTime());
        String end = EventTimes.format(event.getEndTime());
        String length = EventTimes.formatLength(
                getResources(), event.getStartTime(), event.getEndTime());
        dateText.setText(EventDates.formatLong(event.getDate()));
        timeText.setText(getString(R.string.time_range, start, end));
        lengthText.setText(length);
        nextDayText.setVisibility(event.endsNextDay() ? View.VISIBLE : View.GONE);
        timeRow.setContentDescription(getString(event.endsNextDay()
                ? R.string.description_time_range_next_day : R.string.description_time_range,
                start, end, length));
    }
}
