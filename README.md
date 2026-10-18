# Event Manager

Android app for COMP1786 Logbook Exercise 2. It lists events and adds new ones.
Each event has a name, date, start and end time, location, organizer and type.
Events are kept in memory while the app runs, as the exercise needs no
persistence.

## Features

- Event list shown as tickets under month headings. Each ticket shows the date,
  name, type, start and end time with the length, location and organizer.
- Ticket colors and a countdown, such as "In 5 days" or "Ended today", show
  whether an event is happening now, next, upcoming or past. They are worked
  out again whenever the app returns to the screen.
- Add event form with a date picker [1], start and end time lists in 15-minute
  steps, and a list of the five event types: Conference, Workshop, Seminar,
  Social and Other.
- Validation that shows every error at once and moves focus to the first one.
- A prompt before a form with input is discarded.
- Material 3 theme with a color scheme generated from seed #2E6FD8 [2]. Ticket
  states use the Material color roles [3].
- Medium collapsing app bar with the event count [4], and an Add event button
  that shrinks to its icon while the list scrolls down [5].
- Light and dark themes, two columns in landscape, and events and form input
  kept after rotation.
- Screen readers read each ticket as one item, with the full date and the time
  range.

## Validation

- Event name: required, and not already used on the same date, ignoring case.
- Event date: required, in the format DD/MM/YYYY, a real date, and from
  01/01/1900 to 31/12/2100, the range of Android's date picker [6]. The field
  accepts only digits and slashes.
- Start and end time: required and chosen from the lists. The end must differ
  from the start.
- Location and organizer: required.
- Event type: required and chosen from the list.

Error messages follow the GOV.UK Design System guidance [7], such as "Event date
must be a real date".

## Event times

Start and end times are not in the brief's list of fields. They were added at
the lecturer's request.

- Times use the 24-hour HH:MM format. The start list opens at 06:00 while it is
  empty.
- Choosing a start time fills in the end one hour later. Changing the start
  later moves the end so the length stays the same.
- An end at or before the start is on the next day, so an event can run past
  midnight for up to 23 hr 45 min. The form shows the length under the end
  time, such as "Next day (3 hr)", and the ticket marks the end time with +1.
- The list is sorted by start date and time. An event is happening now from its
  start until its end.

## Module guide

The app follows the patterns in the module's Android Studio brief: explicit
intents that pass a Serializable event, an ActivityResultLauncher for the
result, a Toast after saving, and a DatePickerDialog hosted in a DialogFragment
[1]. Two controls go further than the guide:

- A RecyclerView [8] instead of a ListView, for month headings, two columns in
  landscape and updates that redraw only the rows that changed.
- Material exposed dropdown menus [9] instead of Spinners, so the type and time
  fields look and show errors like the other text fields.

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

| File                      | Purpose                                          |
| ------------------------- | ------------------------------------------------ |
| `MainActivity.java`       | Event list, empty state and the Add event button |
| `AddEventActivity.java`   | Add event form, time lists and error messages    |
| `EventAdapter.java`       | Month headings, ticket states and countdowns     |
| `TicketDrawable.java`     | Ticket shape with the date stub and perforation  |
| `Event.java`              | Event model                                      |
| `EventType.java`          | Event types and their label colors               |
| `EventValidator.java`     | Input validation and the duplicate check         |
| `EventDates.java`         | Date parsing and formatting                      |
| `EventTimes.java`         | Time slots, lengths and formatting               |
| `DropdownField.java`      | Menu field that opens on the chosen item         |
| `DatePickerFragment.java` | Date picker dialog hosted in a DialogFragment    |

## Sources

1. Android Developers. Add pickers to your app.
   https://developer.android.com/develop/ui/views/components/pickers
2. Material Theme Builder.
   https://material-foundation.github.io/material-theme-builder/
3. Material Design 3. Color roles. https://m3.material.io/styles/color/roles
4. Material Design 3. Top app bar.
   https://m3.material.io/components/app-bars/overview
5. Material Design 3. Extended FAB.
   https://m3.material.io/components/extended-fab/overview
6. Android Open Source Project. DatePickerCalendarDelegate.java,
   DEFAULT_START_YEAR and DEFAULT_END_YEAR.
   https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/widget/DatePickerCalendarDelegate.java
7. GOV.UK Design System. Error message.
   https://design-system.service.gov.uk/components/error-message/
8. Android Developers. Create dynamic lists with RecyclerView.
   https://developer.android.com/develop/ui/views/layout/recyclerview
9. Material Design 3. Menus. https://m3.material.io/components/menus/overview
