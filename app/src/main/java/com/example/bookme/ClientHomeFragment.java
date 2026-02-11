package com.example.bookme;

import android.app.AlertDialog;
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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientHomeFragment extends Fragment {

    private FirebaseFirestore db;

    public ClientHomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // 1. חיבור רכיבי ה-UI
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        TextView tvNextAppointmentDate = view.findViewById(R.id.tvNextAppointmentDate);

        // 2. שליפת הפרטים מה-SharedPreferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("BookMePrefs", Context.MODE_PRIVATE);
        String fullName = sharedPref.getString("user_name", "Guest");
        String userPhone = sharedPref.getString("user_phone", "").trim();

        // בדיקת התראות ביטול מהספר (התוספת החדשה)
        if (!userPhone.isEmpty()) {
            checkForCancellations(userPhone);
        }

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

        // 4. הגדרת הניווט למסך הזמנת תור
        view.findViewById(R.id.cardToBook).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_clientHomeFragment_to_clientBookFragment));
    }

    /**
     * פונקציה לבדיקת הודעות על ביטול תורים
     */
    private void checkForCancellations(String phone) {
        db.collection("notifications")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        String message = doc.getString("message");

                        // יצירת הודעה קופצת (Popup) ללקוח
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Important Update")
                                .setMessage(message)
                                .setCancelable(false) // הלקוח חייב ללחוץ על הכפתור כדי לסגור
                                .setPositiveButton("Got it", (dialog, which) -> {
                                    // מחיקת ההתראה מה-Firebase לאחר שהלקוח קרא אותה
                                    db.collection("notifications").document(doc.getId()).delete();
                                })
                                .show();
                    }
                });
    }

    /**
     * פונקציה לשליפת התור מ-Firestore
     */
    private void fetchNextAppointment(String phone, TextView targetView) {
        db.collection("appointments")
                .whereEqualTo("clientPhone", phone.trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            String dateStr = document.getString("date");
                            String time = document.getString("time");
                            String service = document.getString("type");

                            try {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ENGLISH);
                                java.util.Date date = sdf.parse(dateStr);

                                java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEEE", java.util.Locale.ENGLISH);
                                String dayName = dayFormat.format(date);

                                String formattedText = dayName + ", " + dateStr + " at " + time + "\n" + "for " + service;
                                targetView.setText(formattedText);

                            } catch (Exception e) {
                                targetView.setText(dateStr + " at " + time + "\n" + "for " + service);
                            }
                            break; // מציג רק את התור הראשון שנמצא
                        }
                    } else {
                        targetView.setText("You have no upcoming appointments");
                    }
                });
    }
}