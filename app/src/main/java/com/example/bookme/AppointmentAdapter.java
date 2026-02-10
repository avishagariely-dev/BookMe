package com.example.bookme;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentList = new ArrayList<>();

    public void setAppointments(List<Appointment> appointments) {
        this.appointmentList = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        holder.tvTime.setText(appointment.getTime());
        holder.tvStatus.setText(appointment.getType());

        if (appointment.isBlocked()) {
            holder.tvClientName.setText("BLOCKED");
            holder.itemView.setBackgroundColor(Color.parseColor("#FFEBEE")); // Light red background
            holder.tvClientName.setTextColor(Color.RED);
        } else {
            holder.tvClientName.setText(appointment.getClientName());
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvClientName.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() { return appointmentList.size(); }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvClientName, tvStatus;
        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}