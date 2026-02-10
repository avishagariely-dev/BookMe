package com.example.bookme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BarberHomeFragment extends Fragment {

    private FirebaseFirestore db;
    private AppointmentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barber_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        TextView tvWelcome = view.findViewById(R.id.tvWelcomeName);
        tvWelcome.setText("Welcome " + Session.barberName);

        RecyclerView rvToday = view.findViewById(R.id.rvTodayAppointments);
        adapter = new AppointmentAdapter();
        rvToday.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvToday.setAdapter(adapter);

        view.findViewById(R.id.Button_BarberCalender).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_barberHomeFragment_to_barberCalenderFragment));

        loadTodayAppointments();
    }

    private void loadTodayAppointments() {
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        db.collection("appointments")
                .whereEqualTo("barberId", Session.barberName)
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> list = querySnapshot.toObjects(Appointment.class);
                    adapter.setAppointments(list);
                });
    }
}