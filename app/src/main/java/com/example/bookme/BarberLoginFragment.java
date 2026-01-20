package com.example.bookme;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BarberLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarberLoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BarberLoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BarberLoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BarberLoginFragment newInstance(String param1, String param2) {
        BarberLoginFragment fragment = new BarberLoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barber_login, container, false);

        EditText emailEt = view.findViewById(R.id.BarberEmail);
        EditText passwordEt = view.findViewById(R.id.BarberPassword);
        Button loginBtn = view.findViewById(R.id.Button_BarberLogin);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> {

            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(),
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
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
}