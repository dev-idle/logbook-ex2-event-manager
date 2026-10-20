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
import java.time.LocalDateTime;
import java.time.YearMonth;
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
    private View timeBlock;
    private TextView dayText;
    private TextView dayOfWeekText;
    private TextView monthText;
    private TextView startTimeText;
    private TextView lengthText;
    private TextView endTimeText;
    private TextView nextDayText;
    private TextView endDateText;
    private TextView locationText;
    private TextView organizerText;

    private Event event;
    private List<Event> events;

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
        event = Objects.requireNonNull(getIntent().getSerializableExtra(EXTRA_EVENT, Event.class));
        events = Arrays.asList(Objects.requireNonNull(
                getIntent().getSerializableExtra(EXTRA_EVENTS, Event[].class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Times move on while the app is away, so the countdown is worked out again.
        showEvent();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        nameText = findViewById(R.id.event_name);
        typeLabel = findViewById(R.id.type_label);
        countdownText = findViewById(R.id.event_countdown);
        timeBlock = findViewById(R.id.time_block);
        dayText = findViewById(R.id.event_day);
        dayOfWeekText = findViewById(R.id.event_day_of_week);
        monthText = findViewById(R.id.event_month);
        startTimeText = findViewById(R.id.start_time);
        lengthText = findViewById(R.id.event_length);
        endTimeText = findViewById(R.id.end_time);
        nextDayText = findViewById(R.id.next_day);
        endDateText = findViewById(R.id.end_date);
        locationText = findViewById(R.id.event_location);
        organizerText = findViewById(R.id.event_organizer);
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
        showTimes(state);
        locationText.setText(event.getLocation());
        organizerText.setText(event.getOrganizer());
    }

    /** Shows the date and a timeline from start to end, with the end date if it is the next day. */
    private void showTimes(EventCardState state) {
        LocalDate startDate = event.getDate();
        LocalDate endDate = event.getEnd().toLocalDate();
        String start = EventTimes.format(event.getStartTime());
        String end = EventTimes.format(event.getEndTime());
        String length = EventTimes.formatLength(
                getResources(), event.getStartTime(), event.getEndTime());
        dayText.setText(EventDates.formatDayOfMonth(startDate));
        // Blue like a colored date panel, or gray like a past one.
        dayText.setTextColor(MaterialColors.getColor(dayText, state == EventCardState.PAST
                ? com.google.android.material.R.attr.colorOnSurfaceVariant
                : androidx.appcompat.R.attr.colorPrimary));
        dayOfWeekText.setText(EventDates.formatDayOfWeek(startDate));
        monthText.setText(EventDates.formatMonth(YearMonth.from(startDate)));
        startTimeText.setText(start);
        lengthText.setText(length);
        endTimeText.setText(end);
        nextDayText.setVisibility(event.endsNextDay() ? View.VISIBLE : View.GONE);
        endDateText.setVisibility(event.endsNextDay() ? View.VISIBLE : View.GONE);
        endDateText.setText(EventDates.formatLong(endDate));
        timeBlock.setContentDescription(getString(R.string.description_starts_ends,
                start, EventDates.formatLong(startDate), end, EventDates.formatLong(endDate),
                length));
    }
}
