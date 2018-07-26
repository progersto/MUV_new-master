package com.muvit.passenger.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muvit.passenger.Activities.RideDetailsActivity;
import com.muvit.passenger.Activities.RideInformationActivity;
import com.muvit.passenger.Activities.StartedRideInformationActivity;
import com.muvit.passenger.Models.RidesItem;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ImgUtils;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;

import java.util.ArrayList;

public class MyTripsAdapter extends RecyclerView.Adapter<MyTripsAdapter.View_Holder> {

    private ArrayList<RidesItem> arrayList;
    private Context context;

    public MyTripsAdapter(Context context, ArrayList<RidesItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_trip, parent, false);
        View_Holder holder = new View_Holder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(View_Holder holder, final int position) {

        //holder.imgProfile.setImageResource(arrayList.get(position).getImage());
//        holder.txtName.setText(arrayList.get(position).getDriverFirstName() + " " + arrayList.get(position).getDriverLastName());
        holder.txtDate.setText(arrayList.get(position).getCreatedDateTime());
//        holder.txtCarName.setText(arrayList.get(position).getBrandName() + " " + arrayList.get(position).getCarName());
        holder.txtPickUp.setText(arrayList.get(position).getPickUpLocation());
        holder.txtDropOff.setText(arrayList.get(position).getDropOffLocation());

       /* holder.txtViewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RideDetailsActivity.class);
                intent.putExtra("tripId",arrayList.get(position).getRideId());
                context.startActivity(intent);
            }
        });*/

//        Ion.with(holder.imgProfile)
//                .transform(new Transform() {
//                    @Override
//                    public Bitmap transform(Bitmap b) {
//                        return ImgUtils.createCircleBitmap(b);
//                    }
//
//                    @Override
//                    public String key() {
//                        return null;
//                    }
//                })
//                .error(R.drawable.no_image)
//                .load(WebServiceUrl.profileUrl + arrayList.get(position).getDriverProfileImage());
//        Ion.with(holder.imgCarType)
//                .error(R.drawable.rides)
//                .load(WebServiceUrl.carUrl + arrayList.get(position).getTypeImage());

        holder.txtStatus.setTextColor(ContextCompat.getColor(context,R.color.notif_success));
        holder.ic_status.setImageResource(R.drawable.ic_tick);

        if (arrayList.get(position).getStatus().equalsIgnoreCase("p")) {
            holder.txtStatus.setText("Pending");
            if (arrayList.get(position).getRejectedBy().equalsIgnoreCase("a")) {
                holder.txtStatus.setText("Expired");
                holder.txtViewDetails.setVisibility(View.INVISIBLE);
            }else {
                holder.txtViewDetails.setVisibility(View.INVISIBLE);
            }
            holder.txtViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RideDetailsActivity.class);
                    intent.putExtra("tripId", arrayList.get(position).getRideId());
                    context.startActivity(intent);
                }
            });
        } else if (arrayList.get(position).getStatus().equalsIgnoreCase("w")) {
            holder.txtStatus.setText("Waiting");
            holder.txtViewDetails.setVisibility(View.VISIBLE);
            holder.txtViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RideInformationActivity.class);
                    Log.e("MyTripsAdapter", "onClick: " + arrayList.get(position).getRideId());
                    intent.putExtra("rideId", String.valueOf(arrayList.get(position).getRideId()));
                    context.startActivity(intent);
                }
            });
        } else if (arrayList.get(position).getStatus().equalsIgnoreCase("s")) {
            holder.txtStatus.setText("Started");
            holder.txtViewDetails.setVisibility(View.VISIBLE);
            holder.txtViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, StartedRideInformationActivity.class);
                    intent.putExtra("rideId", String.valueOf(arrayList.get(position).getRideId()));
                    context.startActivity(intent);
                }
            });
        } else if (arrayList.get(position).getStatus().equalsIgnoreCase("c")) {
            holder.txtStatus.setText("Completed");
            holder.txtViewDetails.setVisibility(View.VISIBLE);
            holder.txtViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RideDetailsActivity.class);
                    intent.putExtra("tripId", arrayList.get(position).getRideId());
                    context.startActivity(intent);
                }
            });
        } else if (arrayList.get(position).getStatus().equalsIgnoreCase("r")) {
            holder.txtViewDetails.setVisibility(View.GONE);
            holder.txtStatus.setText("Rejected");
            if (arrayList.get(position).getRejectedBy().equalsIgnoreCase("a")) {
                holder.txtStatus.setText("Expired");
                holder.txtViewDetails.setVisibility(View.INVISIBLE);
            }else {
                holder.txtStatus.setTextColor(ContextCompat.getColor(context,R.color.red_fn));
                holder.ic_status.setImageResource(R.drawable.ic_cancel);
//                holder.txtViewDetails.setVisibility(View.VISIBLE);
            }

            holder.txtViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RideDetailsActivity.class);
                    intent.putExtra("tripId", arrayList.get(position).getRideId());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class View_Holder extends RecyclerView.ViewHolder {

        private ImageView imgProfile, imgCarType,ic_status;
        ;
        private TextView txtName, txtDate, txtCarName, txtPickUp, txtDropOff, txtViewDetails, txtStatus;

        public View_Holder(View itemView) {
            super(itemView);

            //imgProfile = (ImageView) itemView.findViewById(R.id.imgProfile);
            //imgCarType = (ImageView) itemView.findViewById(R.id.imgCarType);
            //txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
           // txtCarName = (TextView) itemView.findViewById(R.id.txtCarName);
            txtPickUp = (TextView) itemView.findViewById(R.id.txtPickUp);
            txtDropOff = (TextView) itemView.findViewById(R.id.txtDropOff);
            txtViewDetails = (TextView) itemView.findViewById(R.id.txtViewDetails);
            txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
            ic_status =     (ImageView) itemView.findViewById(R.id.ic_status);

    }
    }
}
