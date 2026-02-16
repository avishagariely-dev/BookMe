package com.example.bookme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

        checkForCancellations();

        timePickerSpinner = view.findViewById(R.id.TimePickerSpinner);
        final EditText datePicker = view.findViewById(R.id.DatePicker);
        final Spinner barberSpinner = view.findViewById(R.id.BarberList);
        final Spinner haircutSpinner = view.findViewById(R.id.HaircutList);

        setupSpinner(haircutSpinner, R.array.HaircutList);
        setupSpinner(barberSpinner, R.array.BarberList);
        timePickerSpinner.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        Button buttonToPay = view.findViewById(R.id.ButtonToPay);

        datePicker.setOnClickListener(v -> {
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(DateValidatorPointForward.now());

            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Appointment Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setTheme(R.style.CustomMaterialCalendar)
                    .build();

            materialDatePicker.show(getParentFragmentManager(), "DATE_PICKER");

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                selectedDate = sdf.format(new Date(selection));
                datePicker.setText(selectedDate);

                if (barberSpinner.getSelectedItemPosition() > 0) {
                    filterTakenSlots(barberSpinner.getSelectedItem().toString(), selectedDate);
                }
            });
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

            String fullType = haircutSpinner.getSelectedItem().toString();
            String cleanType = fullType.split("  -  ")[0]; // מסיר את המחיר מהטקסט אם הוא כבר קיים
            int price = 0;
            String typeLower = cleanType.toLowerCase();

            if (typeLower.contains("men") && typeLower.contains("beard")) {
                price = 100;
            } else if (typeLower.contains("men")) {
                price = 80;
            } else if (typeLower.contains("beard")) {
                price = 30;
            } else if (typeLower.contains("women")) {
                price = 150;
            } else if (typeLower.contains("highlights") || typeLower.contains("straightening")) {
                price = 500;
            } else {
                price = 100;
            }

            bundle.putString("type", cleanType);
            bundle.putInt("price", price);

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
                        if (time.equals("All Day")) { availableSlots.clear(); break; }
                        if (time.contains("-")) {
                            String[] parts = time.split(" - ");
                            if (parts.length == 2) {
                                String start = parts[0], end = parts[1];
                                availableSlots.removeIf(slot -> slot.compareTo(start) >= 0 && slot.compareTo(end) <= 0);
                            }
                        } else { availableSlots.remove(time); }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, availableSlots);
                    adapter.setDropDownViewResource(R.layout.spinner_item);
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
                if (!isToday || h > now.get(Calendar.HOUR_OF_DAY)) { slots.add(String.format("%02d:00", h)); }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return slots;
    }

    private void checkForCancellations() {
        SharedPreferences prefs = getActivity().getSharedPreferences("BookMePrefs", Context.MODE_PRIVATE);
        String userPhone = prefs.getString("user_phone", "");
        if (userPhone.isEmpty()) return;
        db.collection("notifications").whereEqualTo("phone", userPhone).get().addOnSuccessListener(qs -> {
            for (DocumentSnapshot doc : qs) {
                new AlertDialog.Builder(requireContext()).setTitle("Appointment Update")
                        .setMessage(doc.getString("message")).setPositiveButton("OK", (d, w) -> db.collection("notifications").document(doc.getId()).delete()).show();
            }
        });
    }

    private void setupSpinner(Spinner spinner, int arrayResourceId) {
        String[] items = getResources().getStringArray(arrayResourceId);
        if (arrayResourceId == R.array.HaircutList) {
            for (int i = 1; i < items.length; i++) {
                int price = 0;
                if (items[i].toLowerCase().contains("haircut")) price = 60;
                else if (items[i].toLowerCase().contains("beard")) price = 40;
                else if (items[i].toLowerCase().contains("straightening")) price = 150;
                else price = 100;
                items[i] = items[i] + "  -  ₪" + price;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, items);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
    }
}