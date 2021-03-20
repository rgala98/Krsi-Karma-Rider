package com.krsikarma.app.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Order implements Parcelable {
    String service_id;
    String service_name;
    String service_image_url;
    String order_id;
    String user_id;
    String partner_id;
    String order_date;
    String order_time;
    String order_status;
    String order_address;
    String order_quantity;
    String order_rate;
    String order_metric;
    String order_amount;
    String date_created;
    String order_document_id;


    public Order(String service_id, String service_name, String service_image_url, String order_id, String user_id, String partner_id, String order_date, String order_time, String order_status, String order_address, String order_quantity, String order_rate, String order_metric, String order_amount, String date_created, String order_document_id) {
        this.service_id = service_id;
        this.service_name = service_name;
        this.service_image_url = service_image_url;
        this.order_id = order_id;
        this.user_id = user_id;
        this.partner_id = partner_id;
        this.order_date = order_date;
        this.order_time = order_time;
        this.order_status = order_status;
        this.order_address = order_address;
        this.order_quantity = order_quantity;
        this.order_rate = order_rate;
        this.order_metric = order_metric;
        this.order_amount = order_amount;
        this.date_created = date_created;
        this.order_document_id = order_document_id;
    }



    public static Creator<Order> getCREATOR() {
        return CREATOR;
    }

    protected Order(Parcel in) {
        service_id = in.readString();
        service_name = in.readString();
        service_image_url = in.readString();
        order_id = in.readString();
        user_id = in.readString();
        partner_id = in.readString();
        order_date = in.readString();
        order_time = in.readString();
        order_status = in.readString();
        order_address = in.readString();
        order_quantity = in.readString();
        order_rate = in.readString();
        order_metric = in.readString();
        order_amount = in.readString();
        date_created = in.readString();
        order_document_id = in.readString();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public String getOrder_document_id() {
        return order_document_id;
    }

    public void setOrder_document_id(String order_document_id) {
        this.order_document_id = order_document_id;
    }
    public String getOrder_amount() {
        return order_amount;
    }

    public void setOrder_amount(String order_amount) {
        this.order_amount = order_amount;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPartner_id() {
        return partner_id;
    }

    public void setPartner_id(String partner_id) {
        this.partner_id = partner_id;
    }

    public String getOrder_date() {
        return order_date;
    }

    public void setOrder_date(String order_date) {
        this.order_date = order_date;
    }

    public String getOrder_time() {
        return order_time;
    }

    public void setOrder_time(String order_time) {
        this.order_time = order_time;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getOrder_address() {
        return order_address;
    }

    public void setOrder_address(String order_address) {
        this.order_address = order_address;
    }

    public String getOrder_quantity() {
        return order_quantity;
    }

    public void setOrder_quantity(String order_quantity) {
        this.order_quantity = order_quantity;
    }

    public String getOrder_rate() {
        return order_rate;
    }

    public void setOrder_rate(String order_rate) {
        this.order_rate = order_rate;
    }

    public String getOrder_metric() {
        return order_metric;
    }

    public void setOrder_metric(String order_metric) {
        this.order_metric = order_metric;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getService_image_url() {
        return service_image_url;
    }

    public void setService_image_url(String service_image_url) {
        this.service_image_url = service_image_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(service_id);
        parcel.writeString(service_name);
        parcel.writeString(service_image_url);
        parcel.writeString(order_id);
        parcel.writeString(user_id);
        parcel.writeString(partner_id);
        parcel.writeString(order_date);
        parcel.writeString(order_time);
        parcel.writeString(order_status);
        parcel.writeString(order_address);
        parcel.writeString(order_quantity);
        parcel.writeString(order_rate);
        parcel.writeString(order_metric);
        parcel.writeString(order_amount);
        parcel.writeString(date_created);
        parcel.writeString(order_document_id);
    }
}
