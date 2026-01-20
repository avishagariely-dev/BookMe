package com.example.bookme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class BarberCalenderFragment extends Fragment {

    private final Set<Long> blockedDaysUtc = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_barber_calender, container, false);

        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        Button Button_PickDate = view.findViewById(R.id.Button_PickDate);

        blockedDaysUtc.clear();

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(new BlockedDaysValidator(blockedDaysUtc))
                .build();

        Button_PickDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setCalendarConstraints(constraints)
                    .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                tvSelectedDate.setText("Selected: " + formatUtcDate(selection));
            });

            picker.show(getParentFragmentManager(), "BARBER_DATE_PICKER");
        });

        return view;
    }

    public static class BlockedDaysValidator implements CalendarConstraints.DateValidator {

        private final Set<Long> blockedDaysUtc;

        public BlockedDaysValidator(Set<Long> blockedDaysUtc) {
            this.blockedDaysUtc = blockedDaysUtc;
        }

        @Override
        public boolean isValid(long date) {
            long day = normalizeToUtcMidnight(date);
            return !blockedDaysUtc.contains(day);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(android.os.Parcel dest, int flags) {
            // לא צריך לכתוב פה כלום כרגע
        }
    }

    // =========================
    // Helpers לתאריכים
    // =========================
    private static long toUtcMidnightMillis(int offsetDays) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, offsetDays);
        return cal.getTimeInMillis();
    }

    private static long normalizeToUtcMidnight(long millis) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private static String formatUtcDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(millis);
    }
}
