package com.example.bookme;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.auth.FirebaseAuth;

public class BarberLoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barber_login, container, false);

        EditText emailEt = view.findViewById(R.id.BarberEmail);
        EditText passwordEt = view.findViewById(R.id.BarberPassword);
        Button loginBtn = view.findViewById(R.id.Button_BarberLogin);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> {

            String email = emailEt.getText().toString().trim().toLowerCase();
            String password = passwordEt.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(),
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {

                        // ממירים אימייל -> שם ספר בדיוק כמו ב-Firestore
                        Session.barberName = mapEmailToBarberName(email);

                        if (Session.barberName == null) {
                            Toast.makeText(getContext(),
                                    "This user is not defined as a barber",
                                    Toast.LENGTH_LONG).show();
                            auth.signOut();
                            return;
                        }

                        NavHostFragment.findNavController(BarberLoginFragment.this)
                                .navigate(R.id.action_barberLoginFragment_to_barberHomeFragment);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Login failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }

    private String mapEmailToBarberName(String email) {
        if (email.equals("netanel@gmail.com")) return "Netanel";
        if (email.equals("david@gmail.com")) return "David";
        if (email.equals("or@gmail.com")) return "Or";
        if (email.equals("daniella@gmail.com")) return "Daniella";
        return null;
    }
}
