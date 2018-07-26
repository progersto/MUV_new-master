package com.muvit.passenger.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muvit.passenger.Activities.RideDetailsActivity;
import com.muvit.passenger.Activities.RideInformationActivity;
import com.muvit.passenger.Activities.StartedRideInformationActivity;
import com.muvit.passenger.Models.Notification;
import com.muvit.passenger.R;

import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.View_Holder> {

    private ArrayList<Notification> arrayList;
    private Context context;

    public NotificationsAdapter(Context context, ArrayList<Notification> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notifications, parent, false);
        View_Holder holder = new View_Holder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(View_Holder holder, int position) {

        final Notification item = arrayList.get(position);

        if (item.getNotifyType().equalsIgnoreCase("s")) {
            holder.imgIcon.setImageResource(R.drawable.success);
            holder.txtStatus.setTextColor(context.getResources().getColor(R.color.notif_success));
            holder.txtStatus.setText("Success");
        } else if (item.getNotifyType().equalsIgnoreCase("d")) {
            holder.imgIcon.setImageResource(R.drawable.cancellations);
            holder.txtStatus.setTextColor(context.getResources().getColor(R.color.notif_error));
            holder.txtStatus.setText("Cancellations");
        } else {
            holder.imgIcon.setImageResource(R.drawable.information);
            holder.txtStatus.setTextColor(context.getResources().getColor(R.color.notif_info));
            holder.txtStatus.setText("Information");
        }
        holder.txtDescription.setText(item.getNotifyMessage());
        holder.txtDate.setText(item.getNotificationDateTime());

        holder.rl_notiMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getStatus() != null && item.getStatus().length() > 0) {
                    if (item.getStatus().equalsIgnoreCase("w")) {
                        Intent intent = new Intent(context, RideInformationActivity.class);
                        Log.e("MyTripsAdapter", "onClick: " + item.getRideId());
                        intent.putExtra("rideId", String.valueOf(item.getRideId()));
                        context.startActivity(intent);
                    } else if (item.getStatus().equalsIgnoreCase("s")) {
                        Intent intent = new Intent(context, StartedRideInformationActivity.class);
                        intent.putExtra("rideId", String.valueOf(item.getRideId()));
                        context.startActivity(intent);
                    } else if (item.getStatus().equalsIgnoreCase("c")) {
                        Intent intent = new Intent(context, RideDetailsActivity.class);
                        intent.putExtra("tripId", item.getRideId());
                        context.startActivity(intent);
                    } else if (item.getStatus().equalsIgnoreCase("r")) {
                        Intent intent = new Intent(context, RideDetailsActivity.class);
                        intent.putExtra("tripId", item.getRideId());
                        context.startActivity(intent);
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public String getLastId() {
        return arrayList.get(arrayList.size() - 1).getNotificationId();
    }

    public class View_Holder extends RecyclerView.ViewHolder {

        private RelativeLayout rl_notiMain;
        private ImageView imgIcon;
        private TextView txtStatus, txtDescription, txtDate;

        public View_Holder(View itemView) {
            super(itemView);
            rl_notiMain = (RelativeLayout) itemView.findViewById(R.id.rl_notiMain);
            imgIcon = (ImageView) itemView.findViewById(R.id.imgIcon);
            txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
            txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
        }
    }
}
