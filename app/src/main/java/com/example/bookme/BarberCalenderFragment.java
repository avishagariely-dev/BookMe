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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BarberCalenderFragment extends Fragment {

    // 1. הגדרת המשתנים שיופיעו בכל רחבי המחלקה
    private AppointmentAdapter adapter;
    private FirebaseFirestore db;
    private String currentBarberUid;

    public BarberCalenderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ניפוח ה-Layout
        return inflater.inflate(R.layout.fragment_barber_calender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. אתחול רכיבי ה-UI
        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDateCalender);
        Button btnPickDate = view.findViewById(R.id.btnPickDate);
        RecyclerView rvAppointments = view.findViewById(R.id.rvAppointments);

        // 3. הגדרת ה-RecyclerView וה-Adapter
        db = FirebaseFirestore.getInstance();
        adapter = new AppointmentAdapter();
        rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAppointments.setAdapter(adapter);

        // שליפת ה-ID של הספר המחובר (כדי שיראה רק את שלו)
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentBarberUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // 4. לוגיקה לבחירת תאריך והצגת נתונים
        btnPickDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build();

            picker.show(getParentFragmentManager(), "DATE_PICKER");

            picker.addOnPositiveButtonClickListener(selection -> {
                // המרת הבחירה לפורמט טקסט קריא וגם לפורמט שתואם ל-Firebase
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dateForFirebase = sdf.format(new Date(selection));

                tvSelectedDate.setText("Appointments for: " + dateForFirebase);

                // קריאה לפונקציית השליפה מ-Firebase
                loadAppointments(dateForFirebase);
            });
        });
    }

    // 5. הפונקציה שמאזינה לשינויים ב-Firebase בזמן אמת
    private void loadAppointments(String selectedDate) {
        if (currentBarberUid == null) return;

        db.collection("appointments")
                .whereEqualTo("barberId", currentBarberUid) // סינון לפי הספר
                .whereEqualTo("date", selectedDate)        // סינון לפי התאריך
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        // המרת הנתונים לאובייקטים של Java ועדכון ה-Adapter
                        List<Appointment> list = value.toObjects(Appointment.class);
                        adapter.setAppointments(list);
                    }
                });
    }
}