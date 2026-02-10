package com.example.bookme;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class PaymentFragment extends Fragment {

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. אתחול רכיבים כ-final כדי שיהיו נגישים בתוך הטרנזקציה
        final EditText etName = view.findViewById(R.id.Name);
        final EditText etPhone = view.findViewById(R.id.editTextPhone);
        final EditText etCard = view.findViewById(R.id.CardNumber);
        final EditText etCvc = view.findViewById(R.id.CVC);
        final EditText etExpiry = view.findViewById(R.id.Expiry);
        final Button btnFinish = view.findViewById(R.id.ButtonConfirm);

        // 2. שליפת הנתונים מה-Bundle
        String date = "N/A";
        String time = "N/A";
        String barberId = "N/A";

        if (getArguments() != null) {
            date = getArguments().getString("date", "N/A");
            time = getArguments().getString("time", "N/A");
            barberId = getArguments().getString("barberId", "N/A");
        }

        final String finalDate = date;
        final String finalTime = time;
        final String finalBarberId = barberId;

        // הגדרת בחירת תוקף
        etExpiry.setOnClickListener(v -> showExpiryPicker(etExpiry));

        if (btnFinish != null) {
            btnFinish.setOnClickListener(v -> {
                // בדיקת תקינות קלט
                if (!validateInputs(etName, etPhone, etCard, etCvc, etExpiry)) return;

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // יצירת מזהה ייחודי לתור למניעת כפל תורים
                String slotId = finalBarberId + "_" + finalDate.replace("/", "-") + "_" + finalTime.replace(":", "-");
                final DocumentReference appointmentRef = db.collection("appointments").document(slotId);

                // ביצוע הטרנזקציה
                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(appointmentRef);

                    // בדיקה אם התור כבר נתפס
                    if (snapshot.exists()) {
                        throw new FirebaseFirestoreException("This slot is already taken!",
                                FirebaseFirestoreException.Code.ABORTED);
                    }

                    // הכנת הנתונים לשמירה
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("barberId", finalBarberId);
                    appointment.put("date", finalDate);
                    appointment.put("time", finalTime);
                    appointment.put("clientName", etName.getText().toString().trim());
                    appointment.put("clientPhone", etPhone.getText().toString().trim());
                    appointment.put("status", "BOOKED");

                    // שמירה בתוך הטרנזקציה
                    transaction.set(appointmentRef, appointment);
                    return null;
                }).addOnSuccessListener(aVoid -> {
                    showConfirmationDialog("Appointment confirmed for " + finalDate + " at " + finalTime);
                }).addOnFailureListener(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("already taken")) {
                        Toast.makeText(getContext(), "Oops! Someone just took this slot.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    // ולידציה על שדות הקלט
    private boolean validateInputs(EditText etName, EditText etPhone, EditText etCard, EditText etCvc, EditText etExpiry) {
        boolean isValid = true;

        // בדיקת שם מלא (לפחות שני שמות)
        String fullName = etName.getText().toString().trim();
        if (fullName.isEmpty()) {
            etName.setError("Full name is required");
            isValid = false;
        } else if (!fullName.contains(" ")) {
            // בדיקה שיש לפחות רווח אחד בין השמות
            etName.setError("Please enter your full name (First and Last name)");
            isValid = false;
        }

        String phone = etPhone.getText().toString().trim();
        if (phone.length() != 10) {
            etPhone.setError("Phone must be 10 digits");
            isValid = false;
        }

        if (etCard.getText().toString().trim().length() < 16) {
            etCard.setError("Invalid card number");
            isValid = false;
        }

        if (etCvc.getText().toString().trim().length() != 3) {
            etCvc.setError("CVC must be 3 digits");
            isValid = false;
        }

        if (etExpiry.getText().toString().trim().isEmpty()) {
            etExpiry.setError("Select expiry date");
            isValid = false;
        }

        return isValid;
    }

    private void showExpiryPicker(EditText etExpiry) {
        android.widget.NumberPicker monthPicker = new android.widget.NumberPicker(requireContext());
        android.widget.NumberPicker yearPicker  = new android.widget.NumberPicker(requireContext());
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setFormatter(value -> String.format("%02d", value));
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        yearPicker.setMinValue(currentYear);
        yearPicker.setMaxValue(currentYear + 15);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        layout.addView(monthPicker);
        layout.addView(yearPicker);
        new AlertDialog.Builder(requireContext())
                .setTitle("Select expiry")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    int mm = monthPicker.getValue();
                    int yy = yearPicker.getValue() % 100;
                    etExpiry.setText(String.format("%02d/%02d", mm, yy));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showConfirmationDialog(String appointmentInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.confirmation, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();
        TextView tvDetails = dialogView.findViewById(R.id.tvConfirmationDetails);
        Button btnClose = dialogView.findViewById(R.id.btnConfirmClose);

        if (tvDetails != null) tvDetails.setText(appointmentInfo);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                alertDialog.dismiss();
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_paymentFragment_to_clientHomeFragment);
            });
        }
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        alertDialog.show();
    }
}