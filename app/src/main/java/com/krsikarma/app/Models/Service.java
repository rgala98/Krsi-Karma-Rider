package com.krsikarma.app.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Service implements Parcelable {
    String service_id;
    String service_name;
    Double service_rate;
    String service_metric;
    String service_image_url;
    String service_description;
    String service_date_created;

    public Service(String service_id, String service_name, Double service_rate, String service_metric, String service_image_url, String service_description, String service_date_created) {
        this.service_id = service_id;
        this.service_name = service_name;
        this.service_rate = service_rate;
        this.service_metric = service_metric;
        this.service_image_url = service_image_url;
        this.service_description = service_description;
        this.service_date_created = service_date_created;
    }

    protected Service(Parcel in) {
        service_id = in.readString();
        service_name = in.readString();
        if (in.readByte() == 0) {
            service_rate = null;
        } else {
            service_rate = in.readDouble();
        }
        service_metric = in.readString();
        service_image_url = in.readString();
        service_description = in.readString();
        service_date_created = in.readString();
    }

    public static final Creator<Service> CREATOR = new Creator<Service>() {
        @Override
        public Service createFromParcel(Parcel in) {
            return new Service(in);
        }

        @Override
        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

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

    public Double getService_rate() {
        return service_rate;
    }

    public void setService_rate(Double service_rate) {
        this.service_rate = service_rate;
    }

    public String getService_metric() {
        return service_metric;
    }

    public void setService_metric(String service_metric) {
        this.service_metric = service_metric;
    }

    public String getService_image_url() {
        return service_image_url;
    }

    public void setService_image_url(String service_image_url) {
        this.service_image_url = service_image_url;
    }

    public String getService_description() {
        return service_description;
    }

    public void setService_description(String service_description) {
        this.service_description = service_description;
    }

    public String getService_date_created() {
        return service_date_created;
    }

    public void setService_date_created(String service_date_created) {
        this.service_date_created = service_date_created;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(service_id);
        parcel.writeString(service_name);
        if (service_rate == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(service_rate);
        }
        parcel.writeString(service_metric);
        parcel.writeString(service_image_url);
        parcel.writeString(service_description);
        parcel.writeString(service_date_created);
    }
}
