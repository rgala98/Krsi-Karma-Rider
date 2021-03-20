package com.krsikarma.app.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.krsikarma.app.Activities.OrderDetailsActivity;
import com.krsikarma.app.Activities.RaiseAComplaintActivity;
import com.krsikarma.app.Models.Order;
import com.krsikarma.app.Models.Service;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.Utils;

import java.util.ArrayList;

public class OrdersRecyclerAdapter extends RecyclerView.Adapter<OrdersRecyclerAdapter.ViewHolder> {
    String TAG = "OrdersRecyclerAdapter";
    Activity mActivity;
    ArrayList<Order> orderArrayList;
    String phoneLanguage;
    Utils utils;

    public OrdersRecyclerAdapter(Activity mActivity, ArrayList<Order> orderArrayList, String phoneLanguage) {
        this.mActivity = mActivity;
        this.orderArrayList = orderArrayList;
        this.phoneLanguage = phoneLanguage;
        utils = new Utils();
    }

    @NonNull
    @Override
    public OrdersRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater= LayoutInflater.from(mActivity.getApplicationContext());
        View view=layoutInflater.inflate(R.layout.row_orders,null);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersRecyclerAdapter.ViewHolder holder, int position) {

        Order order = orderArrayList.get(position);

        Glide.with(mActivity.getApplicationContext())
                .load(order.getService_image_url())
                .into(holder.img_service);
        holder.tv_service_name.setText(order.getService_name());
        holder.tv_final_amount.setText("₹ " +order.getOrder_amount());
        holder.tv_metric_rate.setText(order.getOrder_quantity() + " " + mActivity.getString(R.string.acres) + " x ₹ " + order.getOrder_rate());
        holder.tv_address.setText(order.getOrder_address());
        holder.tv_order_id.setText("#" + order.getOrder_id());
        holder.tv_order_date_time.setText(order.getOrder_date() +" at " + order.getOrder_time());
        holder.tv_status.setText(order.getOrder_status());

        if(phoneLanguage.equals("hi")){
            utils.translateEnglishToHindi(order.getService_name(), holder.tv_service_name);
            utils.translateEnglishToHindi(order.getOrder_address(), holder.tv_address);
            utils.translateEnglishToHindi(order.getOrder_status(), holder.tv_status);

        }

        if(order.getOrder_status().equals(mActivity.getString(R.string.order_status_type_requested))){
            holder.tv_status.setBackgroundColor(mActivity.getColor(R.color.light_orange));
            holder.tv_status.setTextColor(mActivity.getColor(R.color.orange));

        }

        else if(order.getOrder_status().equals(mActivity.getString(R.string.order_status_type_completed))){
            holder.tv_status.setBackgroundColor(mActivity.getColor(R.color.light_green));
            holder.tv_status.setTextColor(mActivity.getColor(R.color.green));

        }

        else if(order.getOrder_status().equals(mActivity.getString(R.string.order_status_type_ongoing))){
            holder.tv_status.setBackgroundColor(mActivity.getColor(R.color.light_yellow));
            holder.tv_status.setTextColor(mActivity.getColor(R.color.yellow));

        }

        else if(order.getOrder_status().equals(mActivity.getString(R.string.order_status_type_cancelled))){
            holder.tv_status.setBackgroundColor(mActivity.getColor(R.color.brand_color_light));
            holder.tv_status.setTextColor(mActivity.getColor(R.color.brand_color));
        }

        else if(order.getOrder_status().equals(mActivity.getString(R.string.order_status_type_payment_pending))){

            holder.tv_status.setBackgroundColor(mActivity.getColor(R.color.light_blue));
            holder.tv_status.setTextColor(mActivity.getColor(R.color.blue));
        }


        holder.btn_raise_complaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent complaint_intent = new Intent(mActivity, RaiseAComplaintActivity.class);
                complaint_intent.putExtra("order_list", orderArrayList);
                complaint_intent.putExtra("pos",position);
                complaint_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(complaint_intent);
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, OrderDetailsActivity.class);
                intent.putExtra("order_id", Long.parseLong(orderArrayList.get(position).getOrder_id()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return orderArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView img_service;
        TextView tv_service_name;
        TextView tv_final_amount;
        TextView tv_metric_rate;
        TextView tv_address;
        TextView tv_order_date_time;
        TextView tv_order_id;
        TextView tv_status;
        Button btn_raise_complaint;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img_service = (ImageView) itemView.findViewById(R.id.img_service);
            tv_service_name = (TextView) itemView.findViewById(R.id.tv_service_name);
            tv_final_amount = (TextView) itemView.findViewById(R.id.tv_final_amount);
            tv_metric_rate = (TextView) itemView.findViewById(R.id.tv_metric_rate);
            tv_address = (TextView) itemView.findViewById(R.id.tv_address);
            tv_order_date_time = (TextView) itemView.findViewById(R.id.tv_order_date_time);
            tv_order_id = (TextView) itemView.findViewById(R.id.tv_order_id);
            btn_raise_complaint = (Button) itemView.findViewById(R.id.btn_raise_complaint);
            tv_status = (TextView) itemView.findViewById(R.id.tv_status);



        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        utils.closeTranslator();
    }
}