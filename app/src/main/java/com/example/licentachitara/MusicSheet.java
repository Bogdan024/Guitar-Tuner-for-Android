package com.example.licentachitara;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class MusicSheet implements Parcelable {
    private String title;
    private String author;
    private String sheetDescription;
    private String sheetMusicUrl;
    private String pictureUrl;
    private Date timestamp;
    private String username;
    private String userId;

    public MusicSheet() {
    }

    public MusicSheet(String title, String author, String sheetDescription, String sheetMusicUrl, String pictureUrl, Date timestamp, String username, String userId) {
        this.title = title;
        this.author = author;
        this.sheetDescription = sheetDescription;
        this.sheetMusicUrl = sheetMusicUrl;
        this.pictureUrl = pictureUrl;
        this.timestamp = timestamp;
        this.username = username;
        this.userId = userId;
    }

    protected MusicSheet(Parcel in) {
        title = in.readString();
        author = in.readString();
        sheetDescription = in.readString();
        sheetMusicUrl = in.readString();
        pictureUrl = in.readString();
        timestamp = new Date(in.readLong());
        username = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(sheetDescription);
        dest.writeString(sheetMusicUrl);
        dest.writeString(pictureUrl);
        dest.writeLong(timestamp != null ? timestamp.getTime() : -1);
        dest.writeString(username);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MusicSheet> CREATOR = new Creator<MusicSheet>() {
        @Override
        public MusicSheet createFromParcel(Parcel in) {
            return new MusicSheet(in);
        }

        @Override
        public MusicSheet[] newArray(int size) {
            return new MusicSheet[size];
        }
    };

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getSheetDescription() {
        return sheetDescription;
    }

    public String getSheetMusicUrl() {
        return sheetMusicUrl;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUsername() {
        return username;
    }
}
