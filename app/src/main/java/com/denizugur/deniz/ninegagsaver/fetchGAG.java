package com.denizugur.deniz.ninegagsaver;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by deniz on 19/08/15.
 */
public class fetchGAG {

    private String Title = null;
    private String photoURL = null;
    private String url;
    private int Height;

    public void setURL(String string) {
        this.url = string;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getTitle() {
        return Title;
    }

    public int getHeightImage() {
        return Height;
    }

    public void fetch() {
        if (url != null) {
            fetchRunnable fr = new fetchRunnable();
            Thread t = new Thread(fr);
            Log.d(t.getName(), "Fetch started");
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
                Document doc = Jsoup.connect(url).timeout(0).get();
                Elements elementImage = doc.select("meta[property=og:image]");
                Elements elementTitle = doc.select("meta[property=og:title]");

                Elements elementGif = doc.select("div[data-mp4]");
                if (!elementGif.isEmpty()) {
                    Height = 0;
                    return;
                }

                photoURL = elementImage.attr("content");
                Title = elementTitle.attr("content");

                URL url = new URL(photoURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                Height = myBitmap.getHeight();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}