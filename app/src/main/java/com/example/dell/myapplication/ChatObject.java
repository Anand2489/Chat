package com.example.dell.myapplication;


public class ChatObject {

    String message;
    String type;
    public String getType() {
        return type;
    }



    public ChatObject(String message,String type) {
        this.message = message;
        this.type   = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
