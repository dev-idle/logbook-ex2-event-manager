package com.comp1786.logbook.eventmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The form for adding an event. The new event is returned to MainActivity as a result.
 */
public class AddEventActivity extends AppCompatActivity {

    /** Result extra holding the new Event. */
    public static final String EXTRA_EVENT = "event";
    /** Intent extra holding the existing events, used to find duplicates. */
    public static final String EXTRA_EVENTS = "events";

    private static final String STATE_TYPE = "type";
    private static final String STATE_DISCARD_SHOWN = "discard_shown";

    private MaterialToolbar toolbar;
    private TextInputLayout nameLayout;
    private TextInputLayout dateLayout;
    private TextInputLayout locationLayout;
    private TextInputLayout organizerLayout;
    private TextInputLayout typeLayout;
    private EditText nameInput;
    private EditText dateInput;
    private EditText locationInput;
    private EditText organizerInput;
    private MaterialAutoCompleteTextView typeInput;

    private List<Event> existingEvents;
    private EventType selectedType;
    private AlertDialog discardDialog;

    // Enabled only once something is entered, so an empty form keeps the system back animation.
    private final OnBackPressedCallback discardCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            showDiscardDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        getOnBackPressedDispatcher().addCallback(this, discardCallback);
        Event[] events = getIntent().getSerializableExtra(EXTRA_EVENTS, Event[].class);
        existingEvents = events != null ? Arrays.asList(events) : Collections.emptyList();
        buildTypeMenu();

        dateLayout.setEndIconOnClickListener(v -> showDatePicker());
        getSupportFragmentManager().setFragmentResultListener(DatePickerFragment.REQUEST_KEY,
                this, (key, result) -> setTextAtEnd(dateInput, EventDates.format(
                        result.getSerializable(DatePickerFragment.RESULT_DATE, LocalDate.class))));
        findViewById(R.id.save_button).setOnClickListener(v -> save());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Added after the views restore their text, so restoring is not treated as an edit.
        for (TextInputLayout layout : allLayouts()) {
            layout.getEditText().addTextChangedListener(new InputWatcher(layout));
        }
        discardCallback.setEnabled(hasInput());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedType != null) {
            outState.putString(STATE_TYPE, selectedType.name());
        }
        outState.putBoolean(STATE_DISCARD_SHOWN,
                discardDialog != null && discardDialog.isShowing());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String type = savedInstanceState.getString(STATE_TYPE);
        if (type != null) {
            selectedType = EventType.valueOf(type);
        }
        if (savedInstanceState.getBoolean(STATE_DISCARD_SHOWN)) {
            showDiscardDialog();
        }
    }

    @Override
    protected void onDestroy() {
        if (discardDialog != null) {
            discardDialog.dismiss();
        }
        super.onDestroy();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        nameLayout = findViewById(R.id.name_layout);
        dateLayout = findViewById(R.id.date_layout);
        locationLayout = findViewById(R.id.location_layout);
        organizerLayout = findViewById(R.id.organizer_layout);
        typeLayout = findViewById(R.id.type_layout);
        nameInput = findViewById(R.id.name_input);
        dateInput = findViewById(R.id.date_input);
        locationInput = findViewById(R.id.location_input);
        organizerInput = findViewById(R.id.organizer_input);
        typeInput = findViewById(R.id.type_input);
    }

    private TextInputLayout[] allLayouts() {
        return new TextInputLayout[] {
                nameLayout, dateLayout, locationLayout, organizerLayout, typeLayout};
    }

    private void buildTypeMenu() {
        EventType[] types = EventType.values();
        String[] labels = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            labels[i] = getString(types[i].getLabelRes());
        }
        // Simple items keep the full list, even when the field already shows a type.
        typeInput.setSimpleItems(labels);
        typeInput.setOnItemClickListener((parent, view, position, id) ->
                selectedType = types[position]);
    }

    /** Opens the picker on the date typed so far, or on today if there is none. */
    private void showDatePicker() {
        EventValidator.Result<LocalDate> typed = EventValidator.validateDate(textOf(dateInput));
        LocalDate date = typed.isValid() ? typed.getValue() : LocalDate.now();
        DatePickerFragment.newInstance(date)
                .show(getSupportFragmentManager(), DatePickerFragment.REQUEST_KEY);
    }

    private boolean hasInput() {
        for (TextInputLayout layout : allLayouts()) {
            if (!textOf(layout.getEditText()).trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void showDiscardDialog() {
        discardDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.discard_title)
                .setMessage(R.string.discard_message)
                .setNegativeButton(R.string.button_keep_editing, null)
                .setPositiveButton(R.string.button_discard, (dialog, which) -> finish())
                .show();
    }

    private void save() {
        EventValidator.Result<String> name = EventValidator.validateText(textOf(nameInput));
        EventValidator.Result<LocalDate> date = EventValidator.validateDate(textOf(dateInput));
        EventValidator.Result<String> location =
                EventValidator.validateText(textOf(locationInput));
        EventValidator.Result<String> organizer =
                EventValidator.validateText(textOf(organizerInput));
        EventValidator.Result<EventType> type = EventValidator.validateType(selectedType);

        showError(nameLayout, name.isValid() ? null : getString(R.string.error_name_required));
        showError(dateLayout, date.isValid() ? null : dateError(date.getError()));
        showError(locationLayout,
                location.isValid() ? null : getString(R.string.error_location_required));
        showError(organizerLayout,
                organizer.isValid() ? null : getString(R.string.error_organizer_required));
        showError(typeLayout, type.isValid() ? null : getString(R.string.error_type_required));
        if (name.isValid() && date.isValid()
                && EventValidator.isDuplicate(name.getValue(), date.getValue(), existingEvents)) {
            showError(nameLayout, getString(R.string.error_name_duplicate));
        }

        for (TextInputLayout layout : allLayouts()) {
            if (layout.getError() != null) {
                layout.getEditText().requestFocus();
                return;
            }
        }
        Event event = new Event(name.getValue(), date.getValue(), location.getValue(),
                organizer.getValue(), type.getValue());
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_EVENT, event));
        finish();
    }

    private String dateError(EventValidator.Error error) {
        switch (error) {
            case WRONG_DATE_FORMAT:
                return getString(R.string.error_date_format);
            case NOT_A_REAL_DATE:
                return getString(R.string.error_date_not_real);
            case DATE_OUT_OF_RANGE:
                return getString(R.string.error_date_out_of_range,
                        EventDates.format(EventDates.MINIMUM),
                        EventDates.format(EventDates.MAXIMUM));
            case EMPTY:
            default:
                return getString(R.string.error_date_required);
        }
    }

    private static String textOf(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private static void setTextAtEnd(EditText input, CharSequence text) {
        input.setText(text);
        input.setSelection(input.length());
    }

    /** Shows the message under the field, or clears the error when it is null. */
    private static void showError(TextInputLayout layout, @Nullable String message) {
        if (message == null) {
            clearError(layout);
        } else {
            layout.setError(message);
        }
    }

    private static void clearError(TextInputLayout layout) {
        layout.setError(null);
        layout.setErrorEnabled(false);
    }

    /** Clears a field's error once the user edits the field. */
    private final class InputWatcher implements TextWatcher {

        private final TextInputLayout layout;
        private String textBefore = "";

        InputWatcher(TextInputLayout layout) {
            this.layout = layout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            textBefore = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Some keyboards report changes that leave the text as it was.
            if (s.toString().equals(textBefore)) {
                return;
            }
            clearError(layout);
            discardCallback.setEnabled(hasInput());
        }
    }
}
