package com.example.classsync;

public class UserProfile {
    public String username, userUID;

    public UserProfile(String username, String userUID) {
        this.username = username;
        this.userUID = userUID;
    }

    public UserProfile() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }
}
