package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;

    @SerializedName("error")
    private String error;

    public String getMessage() { return message; }
    public String getToken() { return token; }
    public User getUser() { return user; }
    public String getError() { return error; }
}
