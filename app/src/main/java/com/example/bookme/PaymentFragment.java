package com.example.bookme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PaymentFragment extends Fragment {

    private FirebaseFirestore db;
    private String date, time, barberId, type;

    public PaymentFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            date = getArguments().getString("date");
            time = getArguments().getString("time");
            barberId = getArguments().getString("barberId");
            type = getArguments().getString("type");
        }

        TextView tvSummary = view.findViewById(R.id.tvAppointmentSummary);
        EditText etName = view.findViewById(R.id.etFullName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etCard = view.findViewById(R.id.etCardNumber);
        EditText etExpiry = view.findViewById(R.id.etExpiry);
        EditText etCVC = view.findViewById(R.id.etCVC);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        tvSummary.setText("Service: " + type + "\nBarber: " + barberId + "\nDate: " + date + "\nTime: " + time);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("BookMePrefs", Context.MODE_PRIVATE);
        etName.setText(sharedPref.getString("user_name", ""));
        etPhone.setText(sharedPref.getString("user_phone", ""));

        // עיצוב אוטומטי MM/YY
        etExpiry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (count == 1 && input.length() == 2) {
                    etExpiry.setText(input + "/");
                    etExpiry.setSelection(etExpiry.getText().length());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String card = etCard.getText().toString().trim();
            String expiry = etExpiry.getText().toString().trim();
            String cvc = etCVC.getText().toString().trim();

            // 1. ולידציה למספר טלפון (10 ספרות)
            if (phone.length() != 10) {
                etPhone.setError("Phone number must be 10 digits");
                return;
            }

            // 2. ולידציה לתוקף הכרטיס (MM/YY)
            if (expiry.length() == 5 && expiry.contains("/")) {
                try {
                    String[] parts = expiry.split("/");
                    int expMonth = Integer.parseInt(parts[0]);
                    int expYear = Integer.parseInt(parts[1]);

                    Calendar now = Calendar.getInstance();
                    int currentMonth = now.get(Calendar.MONTH) + 1; // ינואר הוא 0
                    int currentYear = now.get(Calendar.YEAR) % 100; // מקבלים 26 עבור 2026

                    if (expMonth < 1 || expMonth > 12) {
                        etExpiry.setError("Invalid month (01-12)");
                        return;
                    }

                    // בדיקה אם השנה עברה או אם זו השנה הנוכחית והחודש עבר
                    if (expYear < currentYear || (expYear == currentYear && expMonth < currentMonth)) {
                        etExpiry.setError("Card has expired");
                        return;
                    }
                } catch (Exception e) {
                    etExpiry.setError("Invalid format");
                    return;
                }
            } else {
                etExpiry.setError("Enter expiry as MM/YY");
                return;
            }

            // 3. שאר הבדיקות (כרטיס ו-CVC)
            if (card.length() < 16 || cvc.length() < 3) {
                Toast.makeText(getContext(), "Check card and CVC details", Toast.LENGTH_SHORT).show();
                return;
            }

            saveAppointmentToFirebase(name, phone, card);
        });
    }

    private void saveAppointmentToFirebase(String name, String phone, String card) {
        Map<String, Object> appointment = new HashMap<>();
        appointment.put("clientName", name);
        appointment.put("clientPhone", phone);
        appointment.put("date", date);
        appointment.put("time", time);
        appointment.put("barberId", barberId);
        appointment.put("type", type);
        appointment.put("status", "BOOKED");
        appointment.put("paymentInfo", "Card ending in " + card.substring(card.length() - 4));

        db.collection("appointments").add(appointment)
                .addOnSuccessListener(documentReference -> showConfirmationDialog(date + " at " + time))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showConfirmationDialog(String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.confirmation, null); //
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvConfirmationTitle);
        TextView tvDetails = dialogView.findViewById(R.id.tvConfirmationDetails);
        Button btnClose = dialogView.findViewById(R.id.btnConfirmClose);

        if (tvTitle != null) tvTitle.setText("Booking Confirmed!");
        if (tvDetails != null) tvDetails.setText("Your session for " + info + " is ready.");

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                alertDialog.dismiss();
                Navigation.findNavController(requireView()).navigate(R.id.action_paymentFragment_to_clientHomeFragment);
            });
        }
        alertDialog.show();
    }
}