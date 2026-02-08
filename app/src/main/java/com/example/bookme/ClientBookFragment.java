package com.example.bookme;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import java.util.Calendar;

public class ClientBookFragment extends Fragment {

    // 1. משתנים לשמירת הבחירה של המשתמש
    private String selectedDate = "";
    private String selectedTime = "";

    public ClientBookFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ניפוח ה-Layout בלבד
        return inflater.inflate(R.layout.fragment_client_book, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- טיפול בבחירת תאריך ---
        EditText datePicker = view.findViewById(R.id.DatePicker);
        datePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(
                    requireContext(),
                    (picker, y, m, d) -> {
                        selectedDate = String.format("%02d/%02d/%04d", d, (m + 1), y);
                        datePicker.setText(selectedDate);
                    },
                    year, month, day
            );
            dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dp.show();
        });

        // --- טיפול בבחירת שעה ---
        EditText timePicker = view.findViewById(R.id.TimePicker);
        timePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog tp = new TimePickerDialog(
                    requireContext(),
                    (picker, h, m) -> {
                        selectedTime = String.format("%02d:%02d", h, m);
                        timePicker.setText(selectedTime);
                    },
                    hour, minute,
                    true
            );
            tp.show();
        });

        // --- כפתור מעבר לתשלום עם העברת נתונים ---
        Button buttonToPay = view.findViewById(R.id.ButtonToPay);
        buttonToPay.setOnClickListener(v -> {
            // יצירת Bundle ואריזת הנתונים
            Bundle bundle = new Bundle();
            bundle.putString("date", selectedDate);
            bundle.putString("time", selectedTime);

            // ניווט עם ה-Bundle (ודאי שה-ID של ה-action תואם ל-nav_graph שלך)
            Navigation.findNavController(view).navigate(R.id.action_clientBookFragment_to_paymentFragment, bundle);
        });
    }
}