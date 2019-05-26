package com.dating.needtodate.DataModel;

public class Message {
    public String sender;
    public String reciever;

    public String message;

    boolean seen;

    public Message(String sender, String reciever, String message, boolean seen) {
        this.sender = sender;
        this.reciever = reciever;
        this.message = message;
        this.seen = seen;
    }

    public Message(){

    }
}
