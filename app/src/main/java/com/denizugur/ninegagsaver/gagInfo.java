package com.denizugur.ninegagsaver;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class gagInfo implements Comparable<gagInfo> {
    protected String photoId;
    protected String title;
    protected String saved_date;
    protected String likes;
    protected String comments;
    protected String file_path;

    public void setPhotoId(String str) {
        this.photoId = str;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public void setSaved_Date(String str) {
        this.saved_date = str;
    }

    public void setLikes(String str) {
        this.likes = str;
    }

    public void setComments(String str) {
        this.comments = str;
    }

    public void setFile_Path(String str) {
        this.file_path = str;
    }

    public gagInfo setEmpty() {
        this.title = "";
        this.likes = "";
        this.comments = "";
        this.saved_date = "";
        this.file_path = "";
        this.photoId = "";

        return this;
    }

    public Date getDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date d = null;
        try {
            d = formatter.parse(saved_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    @Override
    public int compareTo(@NonNull gagInfo gi) {
        return gi.getDateTime().compareTo(getDateTime());
    }

}