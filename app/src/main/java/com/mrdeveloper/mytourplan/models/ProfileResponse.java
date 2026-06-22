package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("profile_pic")
    private String profilePic;

    @SerializedName("error")
    private String error;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getProfilePic() { return profilePic; }
    public String getError() { return error; }
}
