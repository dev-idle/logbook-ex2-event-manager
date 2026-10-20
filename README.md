# Event Manager

Android app for COMP1786 Logbook Exercise 2. Users can view all events and add
new ones. Each event has a name, date, start and end time, location, organizer
and type. Events are kept in memory only, as the exercise does not need
persistence.

## Features

- Events listed in start order under month headings, each with all its details.
- Tap a ticket to see the event in full, including any long name, location or
  organizer the ticket cuts short. The end date shows in color when the event
  ends on the next day.
- Add event form with a date picker, start and end time lists in 15-minute
  steps, and the five event types from the brief.
- Validation that shows every error at once and moves focus to the first one.
- Ticket colors and a countdown, such as "Happening now" or "In 5 days", for
  live, next, upcoming and past events.
- A prompt before a form with input is discarded.
- Material 3 light and dark themes with colors generated from seed #2E6FD8 [1].
- Two columns in landscape, and events and form input kept after rotation.
- Screen reader text for the full date and time range of each event, and
  labels for its location and organizer.

## Validation

- Name: required, and not already used on the same date, ignoring case.
- Date: required, DD/MM/YYYY, a real date, and from 01/01/1900 to 31/12/2100,
  the range of Android's date picker [2].
- Start and end time: required. The end must differ from the start.
- Location, organizer and type: required.

Error messages follow the GOV.UK Design System guidance [3].

## Event times

Start and end times were added to the brief's fields at the lecturer's request.

- The start list opens at 06:00. Choosing a start fills in the end an hour
  later, and changing the start keeps the length.
- An end at or before the start is on the next day, so an event can run past
  midnight. The ticket marks that end time with +1.

## Module guide

The app uses the patterns from the module's Android Studio brief: explicit
intents that pass a Serializable event, an ActivityResultLauncher for the
result, a Toast after saving, and a DatePickerDialog in a DialogFragment [4].

- RecyclerView [5] replaces ListView, for the month headings and the landscape
  grid.
- Material exposed dropdown menus [6] replace Spinners, so the type and time
  fields look and show errors like the other fields.

## Build and run

- Android Studio with its bundled JDK, AGP 9.4.1 and Gradle 9.6.0.
- minSdk 33, targetSdk 37, Java 11.
- Tested on a Pixel 7 Pro emulator running API 33.

Open the project in Android Studio and run the `app` configuration, or build
from the command line:

```
./gradlew assembleDebug
```

## Project structure

| File                        | Purpose                                          |
| --------------------------- | ------------------------------------------------ |
| `MainActivity.java`         | Event list, empty state and the Add event button |
| `AddEventActivity.java`     | Add event form, time lists and error messages    |
| `EventDetailsActivity.java` | One event in full, with its start and end        |
| `EventAdapter.java`         | Month headings and tickets                       |
| `TicketState.java`          | Ticket states, colors and countdowns             |
| `TicketDrawable.java`       | Ticket shape with the date stub and perforation  |
| `Event.java`                | Event model                                      |
| `EventType.java`            | Event types and their label colors               |
| `EventValidator.java`       | Input validation and the duplicate check         |
| `EventDates.java`           | Date parsing and formatting                      |
| `EventTimes.java`           | Time slots, lengths and formatting               |
| `DropdownField.java`        | Menu field that opens on the chosen item         |
| `DatePickerFragment.java`   | Date picker dialog hosted in a DialogFragment    |

## Sources

1. Material Theme Builder.
   https://material-foundation.github.io/material-theme-builder/
2. Android Open Source Project. DatePickerCalendarDelegate.java,
   DEFAULT_START_YEAR and DEFAULT_END_YEAR.
   https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/widget/DatePickerCalendarDelegate.java
3. GOV.UK Design System. Error message.
   https://design-system.service.gov.uk/components/error-message/
4. Android Developers. Add pickers to your app.
   https://developer.android.com/develop/ui/views/components/pickers
5. Android Developers. Create dynamic lists with RecyclerView.
   https://developer.android.com/develop/ui/views/layout/recyclerview
6. Material Design 3. Menus. https://m3.material.io/components/menus/overview
