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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientCancelFragment extends Fragment {

    private String foundAppointmentId = "";

    public ClientCancelFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_cancel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. אתחול רכיבים
        final EditText etName = view.findViewById(R.id.Name);
        final EditText etPhone = view.findViewById(R.id.editTextPhone);
        final Button btnFind = view.findViewById(R.id.btnFindAppointment);
        final LinearLayout detailsLayout = view.findViewById(R.id.layoutAppointmentDetails);
        final TextView tvDetails = view.findViewById(R.id.tvFoundDetails);
        final Button btnConfirmCancel = view.findViewById(R.id.btnConfirmCancel);
        final ProgressBar progressBar = view.findViewById(R.id.progressBar);

        // 2. לוגיקה לכפתור החיפוש
        btnFind.setOnClickListener(v -> {
            String fullName = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            // 1. ולידציה (הקוד הקיים שלך)
            if (fullName.isEmpty() || !fullName.contains(" ")) {
                etName.setError("Please enter your full name (First and Last name)");
                return;
            }

            if (phone.length() != 10) {
                etPhone.setError("Phone must be 10 digits");
                return;
            }

            // 2. הפעלת חיווי טעינה ונטרול הכפתור
            progressBar.setVisibility(View.VISIBLE);
            btnFind.setEnabled(false);

            FirebaseFirestore db = FirebaseFirestore.getInstance(); //

            db.collection("appointments")
                    .whereEqualTo("clientPhone", phone)
                    .whereEqualTo("status", "BOOKED")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // 3. עצירת חיווי טעינה ושחרור הכפתור
                        progressBar.setVisibility(View.GONE);
                        btnFind.setEnabled(true);

                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            foundAppointmentId = doc.getId();

                            String date = doc.getString("date");
                            String time = doc.getString("time");
                            String barber = doc.getString("barberId");

                            String message = "We found your appointment:\n" +
                                    "Date: " + date + "\n" +
                                    "Time: " + time + "\n" +
                                    "Barber: " + barber;

                            tvDetails.setText(message);
                            detailsLayout.setVisibility(View.VISIBLE);
                        } else {
                            detailsLayout.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "No active appointment found for this phone.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 4. טיפול בשגיאה - עצירת טעינה ושחרור כפתור
                        progressBar.setVisibility(View.GONE);
                        btnFind.setEnabled(true);
                        Toast.makeText(getContext(), "Error searching: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // 3. לוגיקה לכפתור הביטול הסופי
        btnConfirmCancel.setOnClickListener(v -> {
            if (foundAppointmentId.isEmpty()) return;

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // מחיקה לפי ה-ID הייחודי (slotId) משחררת את התור מיד
            db.collection("appointments").document(foundAppointmentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        showSuccessDialog();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to cancel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void showSuccessDialog() {
        // יצירת בנאי לדיאלוג
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        // ניפוח העיצוב המותאם אישית מתוך confirmation.xml
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.confirmation, null);
        builder.setView(dialogView);

        // יצירת הדיאלוג והגדרת הרקע לשקוף (חשוב למראה המעוגל)
        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // מציאת הרכיבים בתוך הדיאלוג המותאם
        TextView tvTitle = dialogView.findViewById(R.id.tvConfirmationTitle);
        TextView tvDetails = dialogView.findViewById(R.id.tvConfirmationDetails);
        Button btnClose = dialogView.findViewById(R.id.btnConfirmClose);

        // עדכון הטקסטים שיתאימו לביטול תור
        if (tvTitle != null) {
            tvTitle.setText("Appointment Cancelled");
        }
        if (tvDetails != null) {
            tvDetails.setText("Your appointment has been successfully canceled.");
        }

        // הגדרת פעולת כפתור הסגירה
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                alertDialog.dismiss();
                // חזרה למסך הבית
                Navigation.findNavController(requireView()).navigate(R.id.action_clientCancelFragment_to_clientHomeFragment);
            });
        }

        // הצגת הדיאלוג
        alertDialog.show();
    }
}