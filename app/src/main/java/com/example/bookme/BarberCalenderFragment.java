package com.example.bookme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class BarberCalenderFragment extends Fragment {

    private FirebaseFirestore db;
    private AppointmentAdapter adapter;

    private String selectedDateKey = null;
    private String startTime = "09:00", endTime = "10:00";
    private boolean isAllDay = false;

    private TextView tvEmptyMessage;
    private View layoutBlockingUI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barber_calender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        Button btnPickDate = view.findViewById(R.id.btnPickDate);
        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDateCalender);

        LinearLayout layoutActions = view.findViewById(R.id.layoutActions);
        Button btnView = view.findViewById(R.id.btnViewAppointments);
        Button btnOpenBlock = view.findViewById(R.id.btnOpenBlockUI);

        layoutBlockingUI = view.findViewById(R.id.layoutBlockingUI);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        RecyclerView rv = view.findViewById(R.id.rvAppointments);
        adapter = new AppointmentAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // --- בחירת תאריך ---
        btnPickDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
            picker.show(getParentFragmentManager(), "DATE");

            picker.addOnPositiveButtonClickListener(selection -> {
                selectedDateKey = formatDate(selection);

                tvSelectedDate.setText("Selected Date: " + selectedDateKey);
                layoutActions.setVisibility(View.VISIBLE);

                // ניקוי תצוגה
                layoutBlockingUI.setVisibility(View.GONE);
                tvEmptyMessage.setVisibility(View.GONE);
                adapter.setAppointments(new ArrayList<>());
            });
        });

        // --- הצגת תורים ---
        btnView.setOnClickListener(v -> {
            if (selectedDateKey == null) {
                Toast.makeText(getContext(), "Please pick a date first!", Toast.LENGTH_SHORT).show();
                return;
            }

            layoutBlockingUI.setVisibility(View.GONE);
            loadAppointments(selectedDateKey);
        });

        // --- פתיחת UI של חסימה ---
        btnOpenBlock.setOnClickListener(v -> {
            if (selectedDateKey == null) {
                Toast.makeText(getContext(), "Please pick a date first!", Toast.LENGTH_SHORT).show();
                return;
            }
            layoutBlockingUI.setVisibility(View.VISIBLE);
        });

        setupBlockUI(view);
    }

    private void setupBlockUI(View view) {
        SwitchMaterial switchAllDay = view.findViewById(R.id.switchAllDay);
        LinearLayout layoutTimePicker = view.findViewById(R.id.layoutTimePicker);
        Button btnStart = view.findViewById(R.id.btnStartTime);
        Button btnEnd = view.findViewById(R.id.btnEndTime);
        Button btnSubmit = view.findViewById(R.id.btnSubmitBlock);

        switchAllDay.setOnCheckedChangeListener((b, checked) -> {
            isAllDay = checked;
            layoutTimePicker.setVisibility(checked ? View.GONE : View.VISIBLE);
        });

        btnStart.setOnClickListener(v -> showTimePicker(true, btnStart));
        btnEnd.setOnClickListener(v -> showTimePicker(false, btnEnd));
        btnSubmit.setOnClickListener(v -> saveBlock());
    }

    private void showTimePicker(boolean isStart, Button btn) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build();

        picker.show(getParentFragmentManager(), "TIME");

        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d",
                    picker.getHour(), picker.getMinute());

            if (isStart) startTime = time;
            else endTime = time;

            btn.setText(time);
        });
    }

    private void saveBlock() {
        if (selectedDateKey == null) {
            Toast.makeText(getContext(), "Please pick a date first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeToBlock = isAllDay ? "All Day" : startTime + " - " + endTime;

        // 1. חיפוש תורים קיימים שמתנגשים עם החסימה
        db.collection("appointments")
                .whereEqualTo("barberId", Session.barberName)
                .whereEqualTo("date", selectedDateKey)
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        String existingTime = doc.getString("time");
                        String clientPhone = doc.getString("clientPhone");

                        // אם חוסמים יום שלם או שהשעה מתנגשת
                        if (isAllDay || (existingTime != null && existingTime.contains(startTime))) {
                            // מחיקת התור הקיים
                            db.collection("appointments").document(doc.getId()).delete();

                            // שליחת התראה ללקוח (יצירת מסמך חדש באוסף התראות)
                            if (clientPhone != null) {
                                Map<String, Object> notification = new HashMap<>();
                                notification.put("phone", clientPhone);
                                notification.put("message", "Your appointment on " + selectedDateKey + " was cancelled by the barber.");
                                notification.put("timestamp", System.currentTimeMillis());
                                db.collection("notifications").add(notification);
                            }
                        }
                    }

                    // 2. הוספת החסימה עצמה
                    Map<String, Object> block = new HashMap<>();
                    block.put("barberId", Session.barberName);
                    block.put("date", selectedDateKey);
                    block.put("isBlocked", true); // שדה בוליאני
                    block.put("time", timeToBlock);
                    block.put("clientName", "BLOCKED");
                    block.put("type", "Closed");

                    db.collection("appointments").add(block).addOnSuccessListener(d -> {
                        Toast.makeText(getContext(), "Time Blocked & Clients Notified", Toast.LENGTH_SHORT).show();
                        loadAppointments(selectedDateKey);
                    });
                });
    }

    private void loadAppointments(String date) {
        db.collection("appointments")
                .whereEqualTo("barberId", Session.barberName)
                .whereEqualTo("date", date)
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(qs -> {

                    List<Appointment> list = new ArrayList<>();

                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Appointment a = doc.toObject(Appointment.class);
                        if (a != null) {
                            a.setDocId(doc.getId());
                            list.add(a);
                        }
                    }

                    adapter.setAppointments(list);

                    if (list.isEmpty()) {
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(millis));
    }
}
