package com.christianas.tiplearning.Model;

import com.google.gson.annotations.SerializedName;

public class Course {
    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("price")
    private String price;
    @SerializedName("img")
    private String img;
    @SerializedName("short_desc")
    private String short_desc;
    @SerializedName("long_desc")
    private String long_desc;
    @SerializedName("created_at")
    private String created_at;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @SerializedName("currency")
    private String currency;

    public Course(){}

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Course(String id, String title, String price, String img, String short_desc, String long_desc, String created_at,
                  String currency) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.img = img;
        this.short_desc = short_desc;
        this.long_desc = long_desc;
        this.created_at = created_at;
        this.currency = currency;
    }

    public String getShort_desc() {
        return short_desc;
    }

    public void setShort_desc(String short_desc) {
        this.short_desc = short_desc;
    }

    public String getLong_desc() {
        return long_desc;
    }

    public void setLong_desc(String long_desc) {
        this.long_desc = long_desc;
    }
}
