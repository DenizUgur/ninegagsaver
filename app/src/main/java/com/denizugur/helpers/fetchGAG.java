package com.denizugur.helpers;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;

public class fetchGAG {

    private static String TAG = "fetchGAG";
    private String Title = null;
    private String photoURL = null;
    private String url;
    private String ID;
    private String Likes;
    private String Comments;
    public Boolean isGIF = false;

    public String getURL() {
        return url;
    }

    public void setURL(String string) {
        this.url = string;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getTitle() {
        return Title;
    }

    public String getLikes() {
        return Likes;
    }

    public String getComments() {
        return Comments;
    }

    public String getID() {
        return ID;
    }

    public void fetch() {
        if (url != null) {
            fetchRunnable fr = new fetchRunnable();
            Thread t = new Thread(fr);
            Log.d(TAG, "Fetch started");
            try {
                t.start();
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class fetchRunnable implements Runnable {

        public void run() {

            try {
                Document doc = Jsoup.connect(url)
                        .timeout(0)
                        .userAgent("Mozilla/5.0")
                        .maxBodySize(0)
                        .get();

                Elements elementTitle = doc.select("meta[property=og:title]");
                Elements elementComments = doc.select("span[class=badge-item-comment-count]");
                Elements elementLikes = doc.select("span[class=badge-item-love-count]");

                Elements elementGIF = doc.select("div.badge-animated-container-animated");
                Elements elementImage = doc.select("img.badge-item-img");
                if (!elementGIF.isEmpty()) {
                    photoURL = elementGIF.get(0).attr("data-image");
                    isGIF = true;
                } else {
                    photoURL = elementImage.get(0).attr("src"); //TODO: Array out of bounds. test with ae65Wdm
                }

                Title = elementTitle.attr("content");
                URL urlID = new URL(url);
                String path = urlID.getPath();
                ID = path.substring(path.lastIndexOf('/') + 1);
                Comments = elementComments.html();
                Likes = elementLikes.html();

                Log.d("GAG", ID + " " + Likes + " " + Comments);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}