package com.christianas.tiplearning.Model;

import com.google.gson.annotations.SerializedName;

public class PurchasedCourse {
    @SerializedName("id")
    private String id;
    @SerializedName("created_at")
    private String created_at;
    @SerializedName("course_id")
    private String course_id;
    @SerializedName("purchased_id")
    private String purchased_id;
    @SerializedName("state")
    private String state;
    @SerializedName("status")
    private String status;
    @SerializedName("code")
    private String code;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }

    public String getPurchased_id() {
        return purchased_id;
    }

    public void setPurchased_id(String purchased_id) {
        this.purchased_id = purchased_id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public PurchasedCourse(String id, String created_at, String course_id, String purchased_id, String state, String status,
                           String code) {
        this.id = id;
        this.created_at = created_at;
        this.course_id = course_id;
        this.purchased_id = purchased_id;
        this.state = state;
        this.status = status;
        this.code = code;
    }
}
