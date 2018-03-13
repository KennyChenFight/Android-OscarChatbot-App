package com.example.kenny.oscarchatbot;

public class OscarModel {
    private String actor;
    private String actress;
    private String adapted_screenplay;
    private String director;
    private String foeign_language_film;
    private int grade;
    private String original_script;
    private String original_song;
    private String song_link;
    private String supporting_actor;
    private String supprting_actress;
    private String video;

    public OscarModel() {

    }

    public OscarModel(String actor, String actress, String adapted_screenplay, String director,
                      String foeign_language_film, int grade, String original_script, String original_song,
                      String song_link, String supporting_actor, String supprting_actress, String video) {
        this.actor = actor;
        this.actress = actress;
        this.adapted_screenplay = adapted_screenplay;
        this.director = director;
        this.foeign_language_film = foeign_language_film;
        this.grade = grade;
        this.original_script = original_script;
        this.original_song = original_song;
        this.song_link = song_link;
        this.supporting_actor = supporting_actor;
        this.supprting_actress = supprting_actress;
        this.video = video;
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

    public String getAdapted_screenplay() {
        return adapted_screenplay;
    }

    public void setAdapted_screenplay(String adapted_screenplay) {
        this.adapted_screenplay = adapted_screenplay;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getFoeign_language_film() {
        return foeign_language_film;
    }

    public void setFoeign_language_film(String foeign_language_film) {
        this.foeign_language_film = foeign_language_film;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getOriginal_script() {
        return original_script;
    }

    public void setOriginal_script(String original_script) {
        this.original_script = original_script;
    }

    public String getOriginal_song() {
        return original_song;
    }

    public void setOriginal_song(String original_song) {
        this.original_song = original_song;
    }

    public String getSong_link() {
        return song_link;
    }

    public void setSong_link(String song_link) {
        this.song_link = song_link;
    }

    public String getSupporting_actor() {
        return supporting_actor;
    }

    public void setSupporting_actor(String supporting_actor) {
        this.supporting_actor = supporting_actor;
    }

    public String getSupprting_actress() {
        return supprting_actress;
    }

    public void setSupprting_actress(String supprting_actress) {
        this.supprting_actress = supprting_actress;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}
