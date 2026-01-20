package com.example.bookme;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.DatePickerDialog;
import java.util.Calendar;
import android.widget.EditText;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClientBookFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClientBookFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ClientBookFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClientBookFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClientBookFragment newInstance(String param1, String param2) {
        ClientBookFragment fragment = new ClientBookFragment();
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

        View view = inflater.inflate(R.layout.fragment_client_book, container, false);

        EditText datePicker = view.findViewById(R.id.DatePicker);

        datePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(
                    requireContext(),
                    (picker, y, m, d) -> {
                        String chosenDate =
                                String.format("%02d/%02d/%04d", d, (m + 1), y);
                        datePicker.setText(chosenDate);
                    },
                    year, month, day
            );

            dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dp.show();
        });

        EditText timePicker = view.findViewById(R.id.TimePicker);

        timePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog tp = new TimePickerDialog(
                    requireContext(),
                    (picker, h, m) -> {
                        String chosenTime =
                                String.format("%02d:%02d", h, m);
                        timePicker.setText(chosenTime);
                    },
                    hour, minute,
                    true
            );

            tp.show();
        });


        return view;
    }

}