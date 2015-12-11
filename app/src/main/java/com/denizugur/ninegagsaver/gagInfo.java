package com.denizugur.ninegagsaver;

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

    public gagInfo setEmpty() {
        this.title = "";
        this.likes = "";
        this.comments = "";
        this.saved_date = "";
        this.file_path = "";
        this.photoId = "";

        return this;
    }
}