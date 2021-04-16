package com.christianas.tiplearning.Model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("isSubscribed")
    private boolean isSubscribed;
    public String getId() {
        return id;
    }
    @SerializedName("subscriptionCode")
    private String subscriptionCode;

    public String getSubscriptionCode() {
        return subscriptionCode;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public User(String id, String first_name, String last_name, String email, String created_at, String startDate,
                String endDate, String type, String price, String currency, boolean isSubscribed, String subscriptionCode) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.created_at = created_at;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.price = price;
        this.currency = currency;
        this.isSubscribed = isSubscribed;
        this.subscriptionCode = subscriptionCode;
    }

    @SerializedName("first_name")
    private String first_name;

    @SerializedName("last_name")
    private String last_name;

    @SerializedName("email")
    private String email;

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    @SerializedName("created_at")
    private String created_at;
    @SerializedName("startDate")
    private String startDate;
    @SerializedName("endDate")
    private String endDate;
    @SerializedName("type")
    private String type;
    @SerializedName("price")
    private String price;
    @SerializedName("currency")
    private String currency;

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getType() {
        return type;
    }

    public String getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public User() {
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
