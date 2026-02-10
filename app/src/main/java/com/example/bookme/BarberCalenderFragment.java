package com.example.bookme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class BarberCalenderFragment extends Fragment {

    private FirebaseFirestore db;
    private AppointmentAdapter adapter;
    private TextView tvSelectedDate;
    private String selectedDateKey = null;
    private String currentBarberId = Session.barberName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barber_calender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        tvSelectedDate = view.findViewById(R.id.tvSelectedDateCalender);
        Button btnPickDate = view.findViewById(R.id.btnPickDate);
        Button btnLoadAppointments = view.findViewById(R.id.btnLoadAppointments);

        RecyclerView rvAppointments = view.findViewById(R.id.rvAppointments);
        adapter = new AppointmentAdapter();
        rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAppointments.setAdapter(adapter);

        btnPickDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build();

            picker.show(getParentFragmentManager(), "DATE_PICKER");

            picker.addOnPositiveButtonClickListener(selection -> {
                selectedDateKey = formatDateDdMmYyyy(selection); // "11/02/2026"
                tvSelectedDate.setText("Selected Date: " + selectedDateKey);
            });
        });

        btnLoadAppointments.setOnClickListener(v -> {
            if (selectedDateKey == null) {
                Toast.makeText(getContext(), "Pick a date first", Toast.LENGTH_SHORT).show();
                return;
            }
            loadAppointments(selectedDateKey);
        });
    }

    private void loadAppointments(String dateKey) {
        db.collection("appointments")
                .whereEqualTo("barberId", currentBarberId)
                .whereEqualTo("date", dateKey)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> list = querySnapshot.toObjects(Appointment.class);
                    adapter.setAppointments(list);
                });
    }

    private String formatDateDdMmYyyy(long millisUtc) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(millisUtc));
    }
}
