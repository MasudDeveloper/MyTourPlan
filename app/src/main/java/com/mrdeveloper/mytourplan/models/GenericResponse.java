package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class GenericResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    public String getMessage() { return message; }
    public String getError() { return error; }
}
