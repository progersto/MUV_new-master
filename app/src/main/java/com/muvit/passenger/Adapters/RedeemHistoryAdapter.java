package com.muvit.passenger.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.muvit.passenger.Models.RedeemHistoryItem;
import com.muvit.passenger.R;

import java.util.ArrayList;

public class RedeemHistoryAdapter extends RecyclerView.Adapter<RedeemHistoryAdapter.View_Holder> {

    private ArrayList<RedeemHistoryItem> arrayList;
    private Context context;

    public RedeemHistoryAdapter(Context context, ArrayList<RedeemHistoryItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_redeem_history, parent, false);
        View_Holder holder = new View_Holder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(View_Holder holder, final int position) {

        holder.txtDate.setText(arrayList.get(position).getCreatedDateTime());
        holder.txtAmount.setText(arrayList.get(position).getAmount());
        holder.txtDescription.setText(arrayList.get(position).getDescription());
        holder.txtEmail.setText(arrayList.get(position).getEmailAddress());


        if (arrayList.get(position).getStatus().equalsIgnoreCase("c")) {
            holder.txtStatus.setText("Completed");
        }else if (arrayList.get(position).getStatus().equalsIgnoreCase("r")) {
            holder.txtStatus.setText("Rejected");
        }else if (arrayList.get(position).getStatus().equalsIgnoreCase("p")) {
            holder.txtStatus.setText("Pending");
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class View_Holder extends RecyclerView.ViewHolder {

        private TextView txtDate, txtAmount, txtDescription, txtEmail,txtStatus;

        public View_Holder(View itemView) {
            super(itemView);

            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
            txtAmount = (TextView) itemView.findViewById(R.id.txtAmount);
            txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
            txtEmail = (TextView) itemView.findViewById(R.id.txtEmail);
            txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
        }
    }
}
