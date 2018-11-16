package com.example.admin.somedemo.databingdemo;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.example.admin.somedemo.BR;

/**
 * Author liang
 * Date 2018/11/12
 * Dsc:
 */
public class Goods extends BaseObservable {

    private String name;

    private String details;

    private float price;

    public Goods(String name, String details, float price) {
        this.name = name;
        this.details = details;
        this.price = price;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
        notifyChange();
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
