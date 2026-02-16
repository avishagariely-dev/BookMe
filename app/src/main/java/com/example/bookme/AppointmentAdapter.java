package com.example.bookme;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        int orange = ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_orange);
        int darkBrown = ContextCompat.getColor(holder.itemView.getContext(), R.color.dark_brown);
        int secondaryText = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary);

        holder.tvTime.setText(appointment.getTime());
        holder.tvTime.setTextColor(orange);

        boolean blocked = appointment.isBlocked() || "BLOCKED".equals(appointment.getClientName());

        if (blocked) {
            holder.tvClientName.setText("BLOCKED");
            holder.tvClientName.setTextColor(Color.parseColor("#C0392B"));
            holder.tvStatus.setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.cardMainLayout).setBackgroundColor(Color.parseColor("#FFF5F5"));
            holder.btnAction.setText("UNBLOCK");
        } else {
            holder.tvClientName.setText(appointment.getClientName());
            holder.tvClientName.setTextColor(darkBrown);
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText(appointment.getType());
            holder.tvStatus.setTextColor(secondaryText);
            holder.itemView.findViewById(R.id.cardMainLayout).setBackgroundColor(Color.WHITE);
            holder.btnAction.setText("CANCEL");
        }

        holder.btnAction.setOnClickListener(v -> {
            String docId = appointment.getDocId();
            if (docId == null) return;

            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Confirm Action")
                    .setMessage("Are you sure you want to proceed? This will notify the client and remove the slot.")
                    .setPositiveButton("Yes, Proceed", (dialog, which) -> {
                        db.collection("appointments").document(docId).delete()
                                .addOnSuccessListener(unused -> {
                                    int currentPos = holder.getAdapterPosition();
                                    if (currentPos != RecyclerView.NO_POSITION) {
                                        appointmentList.remove(currentPos);
                                        notifyItemRemoved(currentPos);
                                        Toast.makeText(v.getContext(), blocked ? "Unblocked" : "Cancelled", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvClientName, tvStatus;
        Button btnAction;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}