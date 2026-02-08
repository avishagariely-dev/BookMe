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

        // 1. שליפת הנתונים מה-Bundle שנשלח מהמסך הקודם
        String date = "N/A";
        String time = "N/A";

        if (getArguments() != null) {
            date = getArguments().getString("date", "N/A");
            time = getArguments().getString("time", "N/A");
        }

        // 2. טיפול בבחירת תוקף הכרטיס (מה שכבר היה לך)
        EditText expiry = view.findViewById(R.id.Expiry);
        expiry.setOnClickListener(v -> showExpiryPicker(expiry));

        // 3. טיפול בכפתור האישור
        Button btnFinish = view.findViewById(R.id.ButtonConfirm);

        // הגדרת משתנים סופיים לשימוש בתוך ה-Lambda
        final String finalDate = date;
        final String finalTime = time;

        if (btnFinish != null) {
            btnFinish.setOnClickListener(v -> {
                // יצירת הודעה מותאמת אישית עם התאריך והשעה האמיתיים
                String message = "Looking forward to seeing you on " + finalDate + " at " + finalTime;

                showConfirmationDialog(message);
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