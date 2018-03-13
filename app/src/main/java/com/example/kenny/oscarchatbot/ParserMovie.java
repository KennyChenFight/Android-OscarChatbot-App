package com.example.kenny.oscarchatbot;

import java.util.ArrayList;

public class ParserMovie {
    private String name;
    private String enName;
    private String date;
    private String content;
    private String link;
    private ArrayList<String> type = new ArrayList<>();

    public ParserMovie() {

    }

    public ParserMovie(String name, String enName, String date, String content, String link, ArrayList<String> type) {
        this.name = name;
        this.enName = enName;
        this.date = date;
        this.content = content;
        this.link = link;
        this.type = type;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getEnName() {
        return enName;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setContent() {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setType(ArrayList<String> type) {
        this.type = type;
    }

    public ArrayList<String> getType() {
        return type;
    }
}
