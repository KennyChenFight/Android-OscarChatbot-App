package com.example.kenny.oscarchatbot;

public class OscarMovie {
    private String actor;
    private String actress;
    private String content;
    private String director;
    private int grade;
    private String length;
    private String link;
    private String name;
    private String prize;
    private double score;
    private String type;

    public OscarMovie() {

    }

    public OscarMovie(String actor, String actress, String content, String director,
                      int grade, String length, String link, String name, String prize,
                      double score, String type) {
        this.actor = actor;
        this.actress = actress;
        this.content = content;
        this.director = director;
        this.grade = grade;
        this.length = length;
        this.link = link;
        this.name = name;
        this.prize = prize;
        this.score = score;
        this.type = type;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getActress() {
        return actress;
    }

    public void setActress(String actress) {
        this.actress = actress;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
