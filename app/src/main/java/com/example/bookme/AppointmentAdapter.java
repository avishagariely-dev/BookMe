package com.example.bookme;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        holder.tvTime.setText(appointment.getTime());
        holder.tvStatus.setText(appointment.getType()); // סוג תור

        boolean blocked = appointment.isBlocked() || "BLOCKED".equals(appointment.getClientName());

        if (blocked) {
            holder.tvClientName.setText("BLOCKED");
            holder.tvClientName.setTextSize(14f);
            holder.itemView.setBackgroundColor(Color.parseColor("#FFEBEE")); // אדמדם
            holder.tvClientName.setTextColor(Color.RED);
            holder.btnAction.setText("Unblock");
        } else {
            holder.tvClientName.setText(appointment.getClientName());
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvClientName.setTextColor(Color.BLACK);
            holder.btnAction.setText("Cancel");
        }

        holder.btnAction.setOnClickListener(v -> {
            String docId = appointment.getDocId();
            String clientPhone = appointment.getClientPhone();
            String date = appointment.getDate();

            if (docId == null) return;

            // 1. אם זה תור של לקוח (לא חסימה), ניצור התראה לפני המחיקה
            if (!appointment.isBlocked() && clientPhone != null) {
                java.util.Map<String, Object> notification = new java.util.HashMap<>();
                notification.put("phone", clientPhone);
                notification.put("message", "Your appointment on " + date + " was cancelled by the barber.");
                notification.put("timestamp", System.currentTimeMillis());

                db.collection("notifications").add(notification);
            }

            // 2. עכשיו מוחקים את התור
            db.collection("appointments").document(docId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(v.getContext(), appointment.isBlocked() ? "Unblocked" : "Cancelled & Notified", Toast.LENGTH_SHORT).show();
                        appointmentList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    });
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
