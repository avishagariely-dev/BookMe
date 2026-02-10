package com.example.bookme;

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

    // עדכון הרשימה בכל פעם שנוספים נתונים חדשים מ-Firebase
    public void setAppointments(List<Appointment> appointments) {
        this.appointmentList = appointments;
        notifyDataSetChanged(); // מעדכן את ה-UI שהנתונים השתנו
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // מחבר את הקוד לקובץ ה-XML שיצרנו בשלב 5.2
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        // לוקח את הנתונים מהאובייקט ומציג אותם בתוך ה-TextViews
        Appointment appointment = appointmentList.get(position);
        holder.tvTime.setText(appointment.getTime());
        holder.tvClientName.setText(appointment.getClientName());
        holder.tvStatus.setText(appointment.getType());
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    // מחלקת עזר שמחזיקה את הרכיבים הויזואליים של כל שורה
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