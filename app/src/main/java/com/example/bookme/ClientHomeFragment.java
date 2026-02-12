package com.example.bookme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ClientHomeFragment extends Fragment {

    private FirebaseFirestore db;
    private String currentAppointmentId = null; // משתנה לשמירת ה-ID של התור הנוכחי

    public ClientHomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        TextView tvNextAppointmentDate = view.findViewById(R.id.tvNextAppointmentDate);
        Button btnCancel = view.findViewById(R.id.btnCancelAppointmentHome); // זיהוי הכפתור מה-XML

        SharedPreferences sharedPref = getActivity().getSharedPreferences("BookMePrefs", Context.MODE_PRIVATE);
        String fullName = sharedPref.getString("user_name", "Guest");
        String userPhone = sharedPref.getString("user_phone", "").trim();

        if (!userPhone.isEmpty()) {
            checkForCancellations(userPhone);
        }

        if (tvWelcome != null) {
            tvWelcome.setText("Hello, " + fullName);
        }

        if (tvNextAppointmentDate != null) {
            if (userPhone.isEmpty()) {
                tvNextAppointmentDate.setText("אין לך תורים קרובים");
            } else {
                fetchNextAppointment(userPhone, tvNextAppointmentDate, btnCancel);
            }
        }

        view.findViewById(R.id.cardToBook).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_clientHomeFragment_to_clientBookFragment));
    }

    private void fetchNextAppointment(String phone, TextView targetView, Button btnCancel) {
        db.collection("appointments")
                .whereEqualTo("clientPhone", phone.trim())
                .whereEqualTo("status", "BOOKED") // מוודא שזה תור פעיל
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            currentAppointmentId = document.getId();

                            String dateStr = document.getString("date");
                            String time = document.getString("time");
                            String service = document.getString("type");

                            targetView.setText(dateStr + " at " + time + "\nfor " + service);

                            // הצגת כפתור הביטול והגדרת הלחיצה עליו
                            if (btnCancel != null) {
                                btnCancel.setVisibility(View.VISIBLE);
                                btnCancel.setOnClickListener(v -> confirmAndCancel(currentAppointmentId, targetView, btnCancel));
                            }
                            break;
                        }
                    } else {
                        targetView.setText("You have no upcoming appointments");
                        if (btnCancel != null) btnCancel.setVisibility(View.GONE);
                    }
                });
    }

    private void confirmAndCancel(String appointmentId, TextView targetView, Button btnCancel) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> deleteAppointment(appointmentId, targetView, btnCancel))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAppointment(String appointmentId, TextView targetView, Button btnCancel) {
        db.collection("appointments").document(appointmentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    targetView.setText("You have no upcoming appointments");
                    btnCancel.setVisibility(View.GONE);
                    currentAppointmentId = null;
                    showSuccessDialog(); // הצגת החלונית המעוצבת שהכנו
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.confirmation, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        // הגדרת רקע שקוף לאחידות עיצובית
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvConfirmationTitle);
        TextView tvDetails = dialogView.findViewById(R.id.tvConfirmationDetails);
        Button btnClose = dialogView.findViewById(R.id.btnConfirmClose);

        // הטקסט שמתאים לביטול תור
        if (tvTitle != null) tvTitle.setText("Appointment Cancelled");
        if (tvDetails != null) tvDetails.setText("Your appointment has been successfully removed.");

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> alertDialog.dismiss());
        }
        alertDialog.show();
    }

    private void checkForCancellations(String phone) {
        db.collection("notifications")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        String message = doc.getString("message");
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Important Update")
                                .setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton("Got it", (dialog, which) -> db.collection("notifications").document(doc.getId()).delete())
                                .show();
                    }
                });
    }
}