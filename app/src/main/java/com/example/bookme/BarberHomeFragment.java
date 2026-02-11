package com.example.bookme;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BarberHomeFragment extends Fragment {

    private FirebaseFirestore db;
    private AppointmentAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barber_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // הגדרת ה-RecyclerView וה-Adapter
        RecyclerView rvToday = view.findViewById(R.id.rvTodayAppointments);
        adapter = new AppointmentAdapter();
        rvToday.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvToday.setAdapter(adapter);

        // כפתור מעבר ליומן/חסימת תורים
        view.findViewById(R.id.Button_BarberCalender).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_barberHomeFragment_to_barberCalenderFragment);
        });

        // --- התיקון המרכזי כאן ---
        // 1. יצירת פורמט לתאריך של היום
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String today = sdf.format(new Date());

        // 2. שליחת התאריך לפונקציה (כאן הייתה חסרה הארגומנט שגרם לשגיאה האדומה)
        loadTodayAppointments(today);
    }

    private void loadTodayAppointments(String date) {
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

                    if (qs.isEmpty()) {
                        Log.d("FIRESTORE", "No appointments found for " + Session.barberName + " on " + date);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Error: " + e.getMessage());
                    Toast.makeText(getContext(), "Error loading: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}