package com.muvit.passenger.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muvit.passenger.Models.CarsItem;
import com.muvit.passenger.R;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.View_Holder> {

    private ArrayList<CarsItem> arrayList;
    private Context context;
    private int lastPos = -1;
    private SelectCarCallback mListener;

    public HomeAdapter(Context context, ArrayList<CarsItem> arrayList,SelectCarCallback mListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.mListener = mListener;
    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home_fragment, parent, false);
        View_Holder holder = new View_Holder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(View_Holder holder, final int position) {

        if (!arrayList.get(position).getSelected()) {
//            holder.txtCarName.setTextColor(Color.parseColor("#777777"));
//            holder.imgCar.setColorFilter(ContextCompat.getColor(context, R.color.transparentColor));
        } else {
//            holder.txtCarName.setTextColor(Color.parseColor("#000000"));
//            holder.imgCar.setColorFilter(ContextCompat.getColor(context, R.color.transparentColorBlack));
        }

        /*Picasso.with(context)
                .load(arrayList.get(position).getImage())
                .into(holder.imgCar);*/
        String url = WebServiceUrl.carUrl+ arrayList.get(position).getId()+"/"+arrayList.get(position).getTypeImage();
        Picasso.get().load(url).placeholder(R.drawable.rides).error(R.drawable.rides).into(holder.imgCar);


//        Ion.with(holder.imgCar)
//                .error(R.drawable.rides)
//                .load(WebServiceUrl.carUrl+ arrayList.get(position).getId()+"/"+arrayList.get(position).getTypeImage());
        holder.txtCarName.setText(arrayList.get(position).getTypeName());



        holder.imgCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastPos != position) {
                    arrayList.get(position).setSelected(true);
                    if (lastPos != -1) {
                        arrayList.get(lastPos).setSelected(false);
                    }
                    lastPos = position;
                    notifyDataSetChanged();

                }
                mListener.onCarSelected(arrayList.get(position));
                /*Intent intent = new Intent(context, Step2Activity.class);
                context.startActivity(intent);*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class View_Holder extends RecyclerView.ViewHolder {

        private ImageView imgCar;
        private TextView txtCarName;

        public View_Holder(View itemView) {
            super(itemView);
            imgCar = (ImageView) itemView.findViewById(R.id.imgCar);
            txtCarName = (TextView) itemView.findViewById(R.id.txtCarName);
        }
    }

    public CarsItem getSelectedCar() {
        return arrayList.get(lastPos);
    }

    public interface SelectCarCallback {
        void onCarSelected(CarsItem carsItem);
    }

    public void resetSelection(){
        arrayList.get(lastPos).setSelected(false);
        lastPos = -1;
    }
}
