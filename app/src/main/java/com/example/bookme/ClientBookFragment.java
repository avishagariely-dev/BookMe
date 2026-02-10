package com.example.bookme;

import android.app.DatePickerDialog;
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
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ClientBookFragment extends Fragment {

    // משתנים לשמירת הבחירה של המשתמש
    private String selectedDate = "";
    private String selectedTime = "";
    private Spinner timePickerSpinner;

    public ClientBookFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_book, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. אתחול כל הרכיבים פעם אחת (כדי שיהיו נגישים בכל הלמדות)
        timePickerSpinner = view.findViewById(R.id.TimePickerSpinner);
        final EditText datePicker = view.findViewById(R.id.DatePicker);
        final Spinner barberSpinner = view.findViewById(R.id.BarberList);
        final Spinner haircutSpinner = view.findViewById(R.id.HaircutList);
        Button buttonToPay = view.findViewById(R.id.ButtonToPay);

        // 2. טיפול בבחירת תאריך
        datePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(
                    requireContext(),
                    (picker, y, m, d) -> {
                        selectedDate = String.format("%02d/%02d/%04d", d, (m + 1), y);
                        datePicker.setText(selectedDate);
                        datePicker.setError(null); // ניקוי שגיאה אם הייתה

                        // עדכון השעות עבור הספר הנבחר בתאריך החדש
                        String selectedBarber = barberSpinner.getSelectedItem().toString();
                        filterTakenSlots(selectedBarber, selectedDate);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            );
            dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dp.show();
        });

        // 3. כפתור מעבר לתשלום עם ולידציה מלאה
        buttonToPay.setOnClickListener(v -> {
            // א. בדיקה שנבחרה תספורת
            if (haircutSpinner.getSelectedItemPosition() == 0) {
                Toast.makeText(getContext(), "Please select a haircut", Toast.LENGTH_SHORT).show();
                return;
            }

            // ב. בדיקה שנבחר ספר
            if (barberSpinner.getSelectedItemPosition() == 0) {
                Toast.makeText(getContext(), "Please select a barber", Toast.LENGTH_SHORT).show();
                return;
            }

            // ג. בדיקה שנבחר תאריך
            if (selectedDate.isEmpty()) {
                datePicker.setError("Please select a date");
                Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            // ד. בדיקה שנבחרה שעה פנויה מה-Spinner
            if (timePickerSpinner.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Please select an available time", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedTime = timePickerSpinner.getSelectedItem().toString();

            // אם הכל תקין - מעבר למסך תשלום עם הנתונים
            Bundle bundle = new Bundle();
            bundle.putString("date", selectedDate);
            bundle.putString("time", selectedTime);
            bundle.putString("barberId", barberSpinner.getSelectedItem().toString());
            bundle.putString("type", haircutSpinner.getSelectedItem().toString());

            Navigation.findNavController(view).navigate(R.id.action_clientBookFragment_to_paymentFragment, bundle);
        });

        // עדכון שעות אם מחליפים ספר ללא החלפת תאריך
        barberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!selectedDate.isEmpty() && position != 0) {
                    filterTakenSlots(parent.getItemAtPosition(position).toString(), selectedDate);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // לוגיקת שעות העבודה: משך תור שעה, חסימת עבר, ושישי/שבת
    private List<String> getWorkingHours(String selectedDate) {
        List<String> slots = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dateObj = sdf.parse(selectedDate);
            Calendar now = Calendar.getInstance();

            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(dateObj);

            // בדיקה האם התאריך הנבחר הוא היום
            boolean isToday = (now.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR));

            int dayOfWeek = selectedCal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY) return slots; // סגור בשבת

            int endHour = (dayOfWeek == Calendar.FRIDAY) ? 13 : 20; // שישי עד 13:00

            for (int h = 8; h < endHour; h++) {
                // אם זה היום - נוסיף רק שעות שטרם עברו
                if (!isToday || h > now.get(Calendar.HOUR_OF_DAY)) {
                    slots.add(String.format("%02d:00", h));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return slots;
    }

    // סינון שעות שכבר תפוסות ב-Firebase
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
                    // עדכון ה-Spinner עם השעות הפנויות שנשארו
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, availableSlots);
                    timePickerSpinner.setAdapter(adapter);
                });
    }
}