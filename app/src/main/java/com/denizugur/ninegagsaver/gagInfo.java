package com.denizugur.ninegagsaver;

/**
 * Created by deniz on 28/08/15.
 */

public class gagInfo {
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
}