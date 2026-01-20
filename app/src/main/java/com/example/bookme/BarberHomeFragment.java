package com.example.bookme;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BarberHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarberHomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BarberHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BarberHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BarberHomeFragment newInstance(String param1, String param2) {
        BarberHomeFragment fragment = new BarberHomeFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_barber_home, container, false);

        Button calendarBtn = view.findViewById(R.id.Button_BarberCalender);
        Button blockBtn = view.findViewById(R.id.Button_Block);
        Button logoutBtn = view.findViewById(R.id.Button_Logout);

        calendarBtn.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_barberHomeFragment_to_barberCalenderFragment));

        blockBtn.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_barberHomeFragment_to_blockTimeFragment));

        logoutBtn.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_barberHomeFragment_to_barberLoginFragment));

        return view;

    }


}