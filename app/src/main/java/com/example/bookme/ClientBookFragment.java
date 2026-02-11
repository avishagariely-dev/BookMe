package com.example.bookme;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.QuerySnapshot;

public class ClientBookFragment extends Fragment {

    private String selectedDate = "";
    private String selectedTime = "";
    private Spinner timePickerSpinner;
    private FirebaseFirestore db;

    public ClientBookFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_book, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // בדיקת התראות על תורים שבוטלו ע"י הספר ברגע שהמסך עולה
        checkForCancellations();

        timePickerSpinner = view.findViewById(R.id.TimePickerSpinner);
        final EditText datePicker = view.findViewById(R.id.DatePicker);
        final Spinner barberSpinner = view.findViewById(R.id.BarberList);
        final Spinner haircutSpinner = view.findViewById(R.id.HaircutList);
        Button buttonToPay = view.findViewById(R.id.ButtonToPay);

        datePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(requireContext(), (picker, y, m, d) -> {
                selectedDate = String.format("%02d/%02d/%04d", d, (m + 1), y);
                datePicker.setText(selectedDate);

                if (barberSpinner.getSelectedItemPosition() > 0) {
                    filterTakenSlots(barberSpinner.getSelectedItem().toString(), selectedDate);
                }
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dp.show();
        });

        buttonToPay.setOnClickListener(v -> {
            if (haircutSpinner.getSelectedItemPosition() == 0 || barberSpinner.getSelectedItemPosition() == 0 || selectedDate.isEmpty() || timePickerSpinner.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedTime = timePickerSpinner.getSelectedItem().toString();

            Bundle bundle = new Bundle();
            bundle.putString("date", selectedDate);
            bundle.putString("time", selectedTime);
            bundle.putString("barberId", barberSpinner.getSelectedItem().toString());
            bundle.putString("type", haircutSpinner.getSelectedItem().toString());

            Navigation.findNavController(view).navigate(R.id.action_clientBookFragment_to_paymentFragment, bundle);
        });

        barberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!selectedDate.isEmpty() && position > 0) {
                    filterTakenSlots(parent.getItemAtPosition(position).toString(), selectedDate);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // סינון שעות: מסיר תורים קיימים וגם חסימות של הספר
    private void filterTakenSlots(String barberId, String date) {
        List<String> availableSlots = getWorkingHours(date);

        db.collection("appointments")
                .whereEqualTo("barberId", barberId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String time = doc.getString("time");
                        if (time == null) continue;

                        if (time.equals("All Day")) {
                            availableSlots.clear();
                            break;
                        }

                        // תיקון הטווח: פירוק המחרוזת "13:00 - 16:00" לזמן התחלה וסיום
                        if (time.contains("-")) {
                            String[] parts = time.split(" - ");
                            if (parts.length == 2) {
                                String startTime = parts[0]; // "13:00"
                                String endTime = parts[1];   // "16:00"

                                // מסיר כל שעה שגדולה או שווה להתחלה וקטנה או שווה לסוף
                                availableSlots.removeIf(slot ->
                                        slot.compareTo(startTime) >= 0 && slot.compareTo(endTime) <= 0);
                            }
                        } else {
                            availableSlots.remove(time);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, availableSlots);
                    timePickerSpinner.setAdapter(adapter);
                });
    }

    private List<String> getWorkingHours(String selectedDate) {
        List<String> slots = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(sdf.parse(selectedDate));
            Calendar now = Calendar.getInstance();

            int day = selectedCal.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY) return slots;

            int endHour = (day == Calendar.FRIDAY) ? 13 : 20;
            boolean isToday = (now.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR));

            for (int h = 8; h < endHour; h++) {
                if (!isToday || h > now.get(Calendar.HOUR_OF_DAY)) {
                    slots.add(String.format("%02d:00", h));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return slots;
    }

    // פונקציה שבודקת אם הספר מחק תור ללקוח ומציגה הודעה
    private void checkForCancellations() {
        SharedPreferences prefs = getActivity().getSharedPreferences("BookMePrefs", Context.MODE_PRIVATE);
        String userPhone = prefs.getString("user_phone", "");

        if (userPhone.isEmpty()) return;

        db.collection("notifications")
                .whereEqualTo("phone", userPhone)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String message = doc.getString("message");

                        // הקפצת הודעה ללקוח
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Appointment Update")
                                .setMessage(message)
                                .setPositiveButton("OK", (dialog, which) -> {
                                    // מחיקת ההתראה אחרי שהלקוח ראה אותה
                                    db.collection("notifications").document(doc.getId()).delete();
                                })
                                .show();
                    }
                });
    }
}