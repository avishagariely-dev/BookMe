package com.example.bookme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ClientLoginFragment extends Fragment {

    public ClientLoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. ניפוח ה-Layout (ודאי שה-ID של ה-XML נכון)
        View view = inflater.inflate(R.layout.fragment_client_login, container, false);

        // 2. חיבור רכיבי ה-UI (ודאי שה-IDs תואמים ל-XML שלך)
        EditText etName = view.findViewById(R.id.etClientName);
        EditText etPhone = view.findViewById(R.id.etClientPhone);
        Button btnLogin = view.findViewById(R.id.btnSubmitLogin);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. ולידציה מול Firestore: בדיקה אם הטלפון תואם לשם
            db.collection("clients").document(phone).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // המקרה שתיארת: הטלפון קיים. נוודא שהשם תואם בדיוק
                        String savedName = document.getString("fullName");

                        if (savedName != null && savedName.equalsIgnoreCase(name)) {
                            // התאמה מלאה - אפשר להיכנס
                            saveUserAndNavigate(name, phone, v);
                        } else {
                            // הטלפון קיים אך השם שגוי - חסימת כניסה
                            etName.setError("This phone number is registered to a different name");
                        }
                    } else {
                        // לקוח חדש: ניצור לו רשומה באוסף הלקוחות
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("fullName", name);
                        userMap.put("phone", phone);

                        db.collection("clients").document(phone).set(userMap)
                                .addOnSuccessListener(aVoid -> saveUserAndNavigate(name, phone, v));
                    }
                } else {
                    Toast.makeText(getContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    // פונקציית עזר לשמירת הנתונים ומעבר לדף הבית
    private void saveUserAndNavigate(String name, String phone, View view) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("BookMePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user_name", name);
        editor.putString("user_phone", phone);
        editor.apply();

        Navigation.findNavController(view).navigate(R.id.action_clientLoginFragment_to_clientHomeFragment);
    }
}