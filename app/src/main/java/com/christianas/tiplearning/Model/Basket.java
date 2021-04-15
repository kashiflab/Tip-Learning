package com.christianas.tiplearning.Model;

import com.google.gson.annotations.SerializedName;

public class Basket {
    @SerializedName("id")
    private String id;
    @SerializedName("course_id")
    private String course_id;

    public Basket(String id, String course_id) {
        this.id = id;
        this.course_id = course_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }
}
