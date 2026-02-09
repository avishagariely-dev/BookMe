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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import java.util.Calendar;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ClientBookFragment extends Fragment {

    // 1. משתנים לשמירת הבחירה של המשתמש
    private String selectedDate = "";
    private String selectedTime = "";
    private Spinner timePickerSpinner; // הגדרת המשתנה

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

        // 1. אתחול הרכיבים - שימי לב ש-timePickerSpinner הוא Spinner!
        timePickerSpinner = view.findViewById(R.id.TimePickerSpinner);
        EditText datePicker = view.findViewById(R.id.DatePicker);
        Spinner barberSpinner = view.findViewById(R.id.BarberList); // הוספנו את רשימת הספרים
        Button buttonToPay = view.findViewById(R.id.ButtonToPay);

        // 2. טיפול בבחירת תאריך
        datePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(
                    requireContext(),
                    (picker, y, m, d) -> {
                        selectedDate = String.format("%02d/%02d/%04d", d, (m + 1), y);
                        datePicker.setText(selectedDate);

                        // ברגע שנבחר תאריך -> מפעילים את הסינון (שלב 3)
                        String selectedBarber = barberSpinner.getSelectedItem().toString();
                        filterTakenSlots(selectedBarber, selectedDate);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            );
            dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dp.show();
        });

        // 3. כפתור מעבר לתשלום
        buttonToPay.setOnClickListener(v -> {
            // שליפת השעה שנבחרה מה-Spinner
            if (timePickerSpinner.getSelectedItem() != null) {
                selectedTime = timePickerSpinner.getSelectedItem().toString();
            }

            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(getContext(), "Please select date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("date", selectedDate);
            bundle.putString("time", selectedTime);
            bundle.putString("barberId", barberSpinner.getSelectedItem().toString()); // העברת ה-ID של הספר

            Navigation.findNavController(view).navigate(R.id.action_clientBookFragment_to_paymentFragment, bundle);
        });

        barberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // אם כבר נבחר תאריך, נעדכן את השעות עבור הספר החדש
                if (!selectedDate.isEmpty()) {
                    filterTakenSlots(parent.getItemAtPosition(position).toString(), selectedDate);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private List<String> getWorkingHours(String selectedDate) {
        List<String> slots = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(selectedDate));
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            if (dayOfWeek == Calendar.SATURDAY) return slots; // סגור בשבת

            int endHour = (dayOfWeek == Calendar.FRIDAY) ? 13 : 20; // שישי עד 13:00, שאר הימים עד 20:00
            for (int h = 8; h < endHour; h++) {
                slots.add(String.format("%02d:00", h));
            }
            slots.add(endHour + ":00");
        } catch (Exception e) { e.printStackTrace(); }
        return slots;
    }

    private void filterTakenSlots(String barberId, String date) {
        List<String> availableSlots = getWorkingHours(date);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("appointments")
                .whereEqualTo("barberId", barberId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        availableSlots.remove(doc.getString("time"));
                    }
                    // עדכון ה-Spinner עם מה שנשאר
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, availableSlots);
                    timePickerSpinner.setAdapter(adapter);
                });
    }
}