package com.muvit.passenger.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.muvit.passenger.Activities.InfoInDetailActivity;
import com.muvit.passenger.Models.InfoItem;
import com.muvit.passenger.R;

import java.util.ArrayList;

/**
 * Created by User122 on 02-09-2015.
 */
public class InformationAdapter extends RecyclerView.Adapter<InformationAdapter.ViewHolder> {

    private ArrayList<InfoItem> arrayList;
    private Context context;

    public InformationAdapter(Context context, ArrayList<InfoItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_information, parent, false);

        //Create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    //Replace the content of the view
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        //get data from the item data
        viewHolder.txtTitle.setText(arrayList.get(position).getConstant());
        viewHolder.txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InfoInDetailActivity.class);
                intent.putExtra("pId", arrayList.get(position).getId());
                intent.putExtra("pageTitle", arrayList.get(position).getConstant());
                context.startActivity(intent);
            }
        });
    }

    //Inner class to hold a reference to each item of RecylcerView
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTitle;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txtTitle = (TextView) itemLayoutView.findViewById(R.id.txtTitle);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}