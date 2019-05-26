package com.dating.needtodate.DataModel;

public class User {


    public String displayName;

    public String profileImage;

    public String id;

    public boolean onCall;

    public String status;

    public String current_time;


    public User(String displayName, String profileImage, String id, boolean onCall) {
        this.displayName = displayName;
        this.profileImage = profileImage;
        this.id = id;
        this.onCall =onCall;
    }

    public User(String displayName,  String id, boolean onCall,String current_time,String status) {
        this.displayName = displayName;
        this.profileImage = profileImage;
        this.id = id;
        this.onCall =onCall;
        this.status = status;
        this.current_time = current_time;
    }

    public User(){

    }



    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isOnCall() {
        return onCall;
    }

    public void setOnCall(boolean online) {
        onCall = online;
    }
}
