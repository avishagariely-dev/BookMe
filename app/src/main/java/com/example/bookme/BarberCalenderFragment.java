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
    private String selectedDateKey = null, startTime = "09:00", endTime = "10:00";
    private boolean isAllDay = false;

    private TextView tvSelectedDate, tvStartTime, tvEndTime, tvEmptyMessage;
    private View layoutBlockingUI;
    private LinearLayout layoutActions, layoutTimePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barber_calender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // קישור רכיבים
        tvSelectedDate = view.findViewById(R.id.tvSelectedDateCalender);
        tvStartTime = view.findViewById(R.id.tvStartTime);
        tvEndTime = view.findViewById(R.id.tvEndTime);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        layoutActions = view.findViewById(R.id.layoutActions);
        layoutBlockingUI = view.findViewById(R.id.layoutBlockingUI);
        layoutTimePicker = view.findViewById(R.id.layoutTimePicker);

        RecyclerView rv = view.findViewById(R.id.rvAppointments);
        adapter = new AppointmentAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // כפתורים
        view.findViewById(R.id.btnPickDate).setOnClickListener(v -> pickDate());
        view.findViewById(R.id.btnViewAppointments).setOnClickListener(v -> loadAppointments());
        view.findViewById(R.id.btnOpenBlockUI).setOnClickListener(v -> toggleBlockUI());

        setupBlockUI(view);
    }

    private void pickDate() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.show(getParentFragmentManager(), "DATE");
        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDateKey = formatDate(selection);
            tvSelectedDate.setText(selectedDateKey);

            // מציג את הכפתורים (VIEW LOG / BLOCK TIME)
            layoutActions.setVisibility(View.VISIBLE);

            // איפוס תצוגה - לא טוען את התורים אוטומטית!
            layoutBlockingUI.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.GONE);
            adapter.setAppointments(new ArrayList<>()); // מנקה את הרשימה הקודמת
        });
    }

    private void toggleBlockUI() {
        if (selectedDateKey == null) return;
        int visibility = (layoutBlockingUI.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;
        layoutBlockingUI.setVisibility(visibility);
    }

    private void setupBlockUI(View view) {
        SwitchMaterial switchAllDay = view.findViewById(R.id.switchAllDay);
        switchAllDay.setOnCheckedChangeListener((b, checked) -> {
            isAllDay = checked;
            layoutTimePicker.setVisibility(checked ? View.GONE : View.VISIBLE);
        });

        view.findViewById(R.id.btnPickStart).setOnClickListener(v -> showTimePicker(true));
        view.findViewById(R.id.btnPickEnd).setOnClickListener(v -> showTimePicker(false));
        view.findViewById(R.id.btnSubmitBlock).setOnClickListener(v -> saveBlock());
    }

    private void showTimePicker(boolean isStart) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build();
        picker.show(getParentFragmentManager(), "TIME");
        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute());
            if (isStart) {
                startTime = time;
                tvStartTime.setText(time);
            } else {
                endTime = time;
                tvEndTime.setText(time);
            }
        });
    }

    private void loadAppointments() {
        if (selectedDateKey == null) return;
        layoutBlockingUI.setVisibility(View.GONE);
        db.collection("appointments")
                .whereEqualTo("barberId", Session.barberName)
                .whereEqualTo("date", selectedDateKey)
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
                    tvEmptyMessage.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void saveBlock() {
        if (selectedDateKey == null) return;
        String timeToBlock = isAllDay ? "All Day" : startTime + " - " + endTime;
        Map<String, Object> block = new HashMap<>();
        block.put("barberId", Session.barberName);
        block.put("date", selectedDateKey);
        block.put("isBlocked", true);
        block.put("time", timeToBlock);
        block.put("clientName", "BLOCKED");
        block.put("type", "Time Off");

        db.collection("appointments").add(block).addOnSuccessListener(d -> {
            Toast.makeText(getContext(), "Time Blocked!", Toast.LENGTH_SHORT).show();
            layoutBlockingUI.setVisibility(View.GONE);
            loadAppointments();
        });
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(millis));
    }
}