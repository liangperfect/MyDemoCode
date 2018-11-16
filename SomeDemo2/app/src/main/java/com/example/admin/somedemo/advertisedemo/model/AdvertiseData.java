package com.example.admin.somedemo.advertisedemo.model;

/**
 * Author liang
 * Date
 */
public class AdvertiseData {
    private String status;
    private int error_code;
    private Data data;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public int getError_code() {
        return error_code;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }
}
