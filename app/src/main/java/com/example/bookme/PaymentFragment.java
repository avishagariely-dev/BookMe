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

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.widget.Toast;

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

        // 1. שליפת הנתונים מה-Bundle (הוספנו גם את barberId)
        String date = "N/A";
        String time = "N/A";
        String barberId = "N/A";

        if (getArguments() != null) {
            date = getArguments().getString("date", "N/A");
            time = getArguments().getString("time", "N/A");
            barberId = getArguments().getString("barberId", "N/A");
        }

        EditText expiry = view.findViewById(R.id.Expiry);
        expiry.setOnClickListener(v -> showExpiryPicker(expiry));

        Button btnFinish = view.findViewById(R.id.ButtonConfirm);

        final String finalDate = date;
        final String finalTime = time;
        final String finalBarberId = barberId;

        if (btnFinish != null) {
            btnFinish.setOnClickListener(v -> {

                // --- תחילת שלב 4: שמירה ל-Firebase ---
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // יצירת מפת הנתונים לפי המבנה שראינו ב-Console שלך
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("barberId", finalBarberId);
                appointment.put("date", finalDate);
                appointment.put("time", finalTime);
                appointment.put("status", "BOOKED");
                appointment.put("clientName", "Guest Client"); // תוכלי להחליף ב-EditText אם יש
                appointment.put("clientPhone", "0500000000"); // כנ"ל

                // הוספת המסמך לאוסף appointments
                db.collection("appointments").add(appointment)
                        .addOnSuccessListener(documentReference -> {
                            // הצלחה: רק עכשיו מציגים את דיאלוג האישור
                            String message = "Looking forward to seeing you on " + finalDate + " at " + finalTime;
                            showConfirmationDialog(message);
                        })
                        .addOnFailureListener(e -> {
                            // שגיאה: למשל אם אין אינטרנט או בעיית הרשאות
                            Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                // --- סוף שלב 4 ---

            });
        }
    }

    // ... (שאר הפונקציות: showExpiryPicker ו-showConfirmationDialog נשארות אותו דבר) ...

    private void showExpiryPicker(EditText etExpiry) {
        // (הקוד הקודם שלך נשאר כאן ללא שינוי)
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