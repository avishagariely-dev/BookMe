package com.example.bookme;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PaymentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PaymentFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PaymentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PaymentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PaymentFragment newInstance(String param1, String param2) {
        PaymentFragment fragment = new PaymentFragment();
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
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        EditText Expiry = view.findViewById(R.id.Expiry);
        Expiry.setOnClickListener(v -> showExpiryPicker(Expiry));

        return view;
    }

    private void showExpiryPicker(EditText etExpiry) {
        // יוצרים שתי בחירות: חודש ושנה
        android.widget.NumberPicker monthPicker = new android.widget.NumberPicker(requireContext());
        android.widget.NumberPicker yearPicker  = new android.widget.NumberPicker(requireContext());

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setFormatter(value -> String.format("%02d", value));

        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        yearPicker.setMinValue(currentYear);
        yearPicker.setMaxValue(currentYear + 15); // עד 15 שנים קדימה

        // עוטפים ב-Layout אנכי
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        layout.addView(monthPicker);
        layout.addView(yearPicker);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Select expiry")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    int mm = monthPicker.getValue();
                    int yy = yearPicker.getValue() % 100; // שתי ספרות
                    etExpiry.setText(String.format("%02d/%02d", mm, yy));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}