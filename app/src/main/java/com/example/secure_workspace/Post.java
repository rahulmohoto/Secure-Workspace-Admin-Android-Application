package com.example.secure_workspace;

public class Post {

    public String name;
    public String address;

    Post(){
        name = null;
        address = null;
    }

    Post(String name, String address) {
        this.name = name;
        this.address = address;
    }

}

