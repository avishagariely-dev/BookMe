package com.example.bookme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;

public class BarberCalenderFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_barber_calender, container, false);

        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDateCalender);
        Button btnPickDate = view.findViewById(R.id.btnPickDate);

        btnPickDate.setOnClickListener(v -> {

            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build();

            picker.show(getParentFragmentManager(), "DATE_PICKER");

            picker.addOnPositiveButtonClickListener(selection -> {
                tvSelectedDate.setText(picker.getHeaderText());
            });

        });

        return view;
    }
}
