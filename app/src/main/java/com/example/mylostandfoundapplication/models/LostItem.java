package com.example.mylostandfoundapplication.models;

public class LostItem {
    private String id;
    private String title;
    private String description;
    private String location;
    private String date;
    private String userId;
    private String imageBase64;
    private String contactInfo;

    public LostItem() {
        // Required empty constructor for Firebase
    }

    public LostItem(String title, String description, String location, String date, String userId, String imageBase64, String contactInfo) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.userId = userId;
        this.imageBase64 = imageBase64;
        this.contactInfo = contactInfo;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
} 