package com.comp1786.logbook.eventmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_EVENTS = "events";

    private MaterialToolbar toolbar;
    private RecyclerView eventList;
    private View emptyView;

    private final ArrayList<Event> events = new ArrayList<>();
    private EventAdapter adapter;

    private final ActivityResultLauncher<Intent> addEventLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() == RESULT_OK && data != null) {
                    addEvent(data.getSerializableExtra(AddEventActivity.EXTRA_EVENT, Event.class));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        setSupportActionBar(toolbar);
        adapter = new EventAdapter();
        GridLayoutManager layoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.event_list_columns));
        // Month headings span every column.
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isMonthHeading(position) ? layoutManager.getSpanCount() : 1;
            }
        });
        eventList.setLayoutManager(layoutManager);
        eventList.setAdapter(adapter);
        showEvents();

        findViewById(R.id.add_button).setOnClickListener(v -> openAddEvent());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_EVENTS, events.toArray(new Event[0]));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Event[] saved = savedInstanceState.getSerializable(STATE_EVENTS, Event[].class);
        if (saved != null) {
            events.addAll(Arrays.asList(saved));
            showEvents();
        }
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        eventList = findViewById(R.id.event_list);
        emptyView = findViewById(R.id.empty_view);
    }

    private void openAddEvent() {
        Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
        intent.putExtra(AddEventActivity.EXTRA_EVENTS, events.toArray(new Event[0]));
        addEventLauncher.launch(intent);
    }

    /** Adds an event in date order, after any events on the same day. */
    private void addEvent(Event event) {
        int position = 0;
        while (position < events.size()
                && !events.get(position).getDate().isAfter(event.getDate())) {
            position++;
        }
        events.add(position, event);
        showEvents();
        eventList.scrollToPosition(adapter.positionOf(event));
        Toast.makeText(this, R.string.message_event_added, Toast.LENGTH_SHORT).show();
    }

    /** Shows the events and their number, or the empty state when there are none. */
    private void showEvents() {
        adapter.setEvents(events);
        boolean empty = events.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        toolbar.setSubtitle(empty ? null : getResources().getQuantityString(
                R.plurals.event_count, events.size(), events.size()));
    }
}
