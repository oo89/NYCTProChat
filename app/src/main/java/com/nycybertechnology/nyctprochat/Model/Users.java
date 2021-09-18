package com.nycybertechnology.nyctprochat.Model;

public class Users {

    private String id;
    private String username;
    private String imageURL;


    // Constructors;
    public Users() {
    }
    public Users(String id, String username, String imageURL)
    {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
    }
    // Getters and Setters


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // me quede por el minuto 1:04:09 video
}
