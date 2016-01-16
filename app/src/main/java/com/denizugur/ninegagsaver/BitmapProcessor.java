package com.denizugur.ninegagsaver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;

import com.denizugur.core.TouchImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;

public class BitmapProcessor {

    private Context context;
    private Bitmap image;
    private Bitmap.Config config;
    private DisplayReceivedImage dis;
    private Bitmap resultBitmap;

    public BitmapProcessor(Context context, DisplayReceivedImage dis) {
        this.context = context;
        this.dis = dis;
    }

    @Deprecated
    public void cache(Uri photoURI, TouchImageView iV, Boolean customSize, Boolean process) {

        ImageLoaderConfiguration iLConfig = new ImageLoaderConfiguration.Builder(context)
                .denyCacheImageMultipleSizesInMemory()
                .writeDebugLogs()
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(iLConfig);

        File f = new File(photoURI.getPath());
        image = imageLoader.loadImageSync("file://" + f.getAbsolutePath());
        config = image.getConfig();
        iV.setImageBitmap(image);
        if (process) dis.process(customSize);
    }

    public void cache(String extension, TouchImageView iV, Boolean customSize, Boolean process) {
        File directory_download = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "downloads");
        File destinationFile = new File(directory_download + "/GAG" + extension);

        ImageLoaderConfiguration iLConfig = new ImageLoaderConfiguration.Builder(context)
                .denyCacheImageMultipleSizesInMemory()
                .writeDebugLogs()
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(iLConfig);

        image = imageLoader.loadImageSync(Uri.fromFile(destinationFile).toString());
        config = image.getConfig();
        iV.setImageBitmap(image);
        if (process) dis.process(customSize);
    }

    public Bitmap getBitmap(Boolean result) {
        if (result) {
            return resultBitmap;
        } else {
            return image;
        }
    }

    public Canvas createCanvas() {
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }

        resultBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), config);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(image, 0, 0, null);

        return canvas;
    }

    public void cleanUp() {
        ImageLoader.getInstance().destroy();
        image.recycle();
    }

}