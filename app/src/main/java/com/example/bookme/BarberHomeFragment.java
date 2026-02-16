package com.example.bookme;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BarberHomeFragment extends Fragment {

    private FirebaseFirestore db;
    private AppointmentAdapter adapter;
    private TextView tvEmptyMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barber_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        TextView tvWelcome = view.findViewById(R.id.tvWelcomeBarber);
        if (Session.barberName != null) {
            tvWelcome.setText("Welcome, " + Session.barberName);
        }

        //  הגדרת ה-RecyclerView וה-Empty State
        RecyclerView rvToday = view.findViewById(R.id.rvTodayAppointments);
        tvEmptyMessage = view.findViewById(R.id.tvNoAppointments);

        adapter = new AppointmentAdapter();
        rvToday.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvToday.setAdapter(adapter);

        //  כפתור מעבר ליומן
        view.findViewById(R.id.btnOpenCalendar).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_barberHomeFragment_to_barberCalenderFragment);
        });

        //  טעינת התורים להיום
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String today = sdf.format(new Date());
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

                    if (list.isEmpty()) {
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        Log.d("FIRESTORE", "No appointments for " + Session.barberName);
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Error: " + e.getMessage());
                    Toast.makeText(getContext(), "Error loading: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}