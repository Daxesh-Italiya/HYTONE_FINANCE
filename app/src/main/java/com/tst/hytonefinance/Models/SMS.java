package com.tst.hytonefinance.Models;

public class SMS {
    String title,sender,message,date_time;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    @Override
    public String toString() {
        return "SMS{" +
                "title='" + title + '\'' +
                ", sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                ", date_time='" + date_time + '\'' +
                '}';
    }
}
