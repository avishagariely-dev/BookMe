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
    private int price; // *** PRICE ADDITION ***

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
            price = getArguments().getInt("price", 0); // *** PRICE ADDITION ***
        }

        TextView tvSummary = view.findViewById(R.id.tvAppointmentSummary);
        TextView tvPrice = view.findViewById(R.id.tvTotalPrice); // *** PRICE ADDITION ***
        EditText etName = view.findViewById(R.id.etFullName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etCard = view.findViewById(R.id.etCardNumber);
        EditText etExpiry = view.findViewById(R.id.etExpiry);
        EditText etCVC = view.findViewById(R.id.etCVC);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        tvSummary.setText("Service: " + type + "\nBarber: " + barberId + "\nDate: " + date + "\nTime: " + time);
        if (tvPrice != null) tvPrice.setText("₪" + price); // *** PRICE ADDITION ***

        SharedPreferences sharedPref = getActivity().getSharedPreferences("BookMePrefs", Context.MODE_PRIVATE);
        etName.setText(sharedPref.getString("user_name", ""));
        etPhone.setText(sharedPref.getString("user_phone", ""));

        etExpiry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1 && s.toString().length() == 2) {
                    etExpiry.setText(s.toString() + "/");
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

            if (phone.length() != 10) { etPhone.setError("10 digits required"); return; }
            if (expiry.length() != 5 || !expiry.contains("/")) { etExpiry.setError("Use MM/YY"); return; }
            if (card.length() < 16 || cvc.length() < 3) { Toast.makeText(getContext(), "Check card details", Toast.LENGTH_SHORT).show(); return; }

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
        appointment.put("price", price); // *** PRICE ADDITION ***
        appointment.put("status", "BOOKED");
        appointment.put("paymentInfo", "Ending in " + card.substring(card.length() - 4));

        db.collection("appointments").add(appointment)
                .addOnSuccessListener(d -> showConfirmationDialog(date + " at " + time))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showConfirmationDialog(String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.confirmation, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ((TextView)dialogView.findViewById(R.id.tvConfirmationDetails)).setText("Your session for " + info + " is ready.");
        dialogView.findViewById(R.id.btnConfirmClose).setOnClickListener(v -> {
            alertDialog.dismiss();
            Navigation.findNavController(requireView()).navigate(R.id.action_paymentFragment_to_clientHomeFragment);
        });
        alertDialog.show();
    }
}