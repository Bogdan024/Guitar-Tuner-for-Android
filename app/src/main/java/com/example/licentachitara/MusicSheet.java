package com.example.licentachitara;

import java.util.Date;

public class MusicSheet {
    private String title;
    private String sheetDescription;
    private String sheetMusicUrl;
    private Date timestamp;
    private String username;
    private String userId;

    public MusicSheet() {

    }

    public MusicSheet(String title, String sheetDescription, String sheetMusicUrl, Date timestamp, String username, String userId) {
        this.title = title;
        this.sheetDescription = sheetDescription;
        this.sheetMusicUrl = sheetMusicUrl;
        this.timestamp = timestamp;
        this.username = username;
        this.userId = userId;
    }

    public String getTitle() { return title; }
    public String getSheetDescription() { return sheetDescription; }
    public String getSheetMusicUrl() { return sheetMusicUrl; }
    public Date getTimestamp() { return timestamp; }
    public String getUsername() { return username; }

}
