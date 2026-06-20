package com.mrdeveloper.mytourplan.models;

public class User {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String profilePic;

    public User() {}

    public User(int id, String name, String email, String phone, String profilePic) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profilePic = profilePic;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getProfilePic() { return profilePic; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
}
