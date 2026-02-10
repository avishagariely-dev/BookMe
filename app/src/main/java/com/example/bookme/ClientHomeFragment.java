package com.example.bookme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

// ה-Imports החדשים עבור Firebase
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ClientHomeFragment extends Fragment {

    public ClientHomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. חיבור רכיבי ה-UI
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        TextView tvNextAppointmentDate = view.findViewById(R.id.tvNextAppointmentDate);

        // 2. שליפת הפרטים מה-SharedPreferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("BookMePrefs", Context.MODE_PRIVATE);
        String fullName = sharedPref.getString("user_name", "Guest");
        String userPhone = sharedPref.getString("user_phone", "").trim();

        // עדכון ברכת שלום
        if (tvWelcome != null) {
            tvWelcome.setText("Hello, " + fullName);
        }

        // 3. לוגיקת בדיקת תורים ב-Firebase
        if (tvNextAppointmentDate != null) {
            if (userPhone.isEmpty()) {
                tvNextAppointmentDate.setText("אין לך תורים קרובים");
            } else {
                fetchNextAppointment(userPhone, tvNextAppointmentDate);
            }
        }

        // 4. הגדרת הניווט
        view.findViewById(R.id.cardToBook).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_clientHomeFragment_to_clientBookFragment));
    }

    /**
     * פונקציה לשליפת התור מ-Firestore
     */
    private void fetchNextAppointment(String phone, TextView targetView) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        db.collection("appointments") // אוסף appointments כפי שמופיע ב-Firebase
                .whereEqualTo("clientPhone", phone.trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            String dateStr = document.getString("date"); // למשל "12/02/2026"
                            String time = document.getString("time");   // למשל "15:00"
                            String service = document.getString("serviceType"); // למשל "Highlights"

                            try {
                                // 1. הפיכת מחרוזת התאריך לאובייקט Date
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ENGLISH);
                                java.util.Date date = sdf.parse(dateStr);

                                // 2. חילוץ שם היום (למשל Monday)
                                java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEEE", java.util.Locale.ENGLISH);
                                String dayName = dayFormat.format(date);

                                // 3. בניית המחרוזת בשתי שורות לפי התבנית שביקשת (\n יורד שורה)
                                String formattedText = dayName + ", " + dateStr + " at " + time + "\n" + "for " + service;

                                targetView.setText(formattedText);

                            } catch (Exception e) {
                                // במקרה של שגיאה בפורמט התאריך, נציג ללא היום
                                targetView.setText(dateStr + " at " + time + "\n" + "for " + service);
                            }
                            break;
                        }
                    } else {
                        targetView.setText("You have no upcoming appointments");
                    }
                });
    }
}