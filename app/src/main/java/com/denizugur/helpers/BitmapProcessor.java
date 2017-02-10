package com.denizugur.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.facebook.imagepipeline.request.BaseRepeatedPostProcessor;

public class BitmapProcessor extends BaseRepeatedPostProcessor {

    private Context context;
    private Integer size;
    private String title;
    private Boolean draw = true;

    public Bitmap originalBMP;
    public Bitmap finalBMP;

    public BitmapProcessor(Context context) {
        this.context = context;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void isEmpty(Boolean boo) {
        this.draw = !boo;
    }

    @Override
    public String getName() {
        return "BitmapProcessor";
    }

    @Override
    public void process(Bitmap bitmap) {
        originalBMP = bitmap;
        if (draw) {
            int[] dimen = new int[2];
            dimen[0] = bitmap.getWidth();
            dimen[1] = bitmap.getHeight();

            Canvas canvas = new Canvas(bitmap);

            drawText(canvas, dimen, size, title.length() <= 15);
            canvas.drawBitmap(bitmap, 0, 0, null);
            finalBMP = bitmap;
        }
    }

    private void drawText(Canvas canvas, int[] dimen, Integer percent, Boolean length) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean isGradient = prefs.getBoolean("gradientKey", true);

        int size = 0;
        int smallDummyHeight = 0;
        while (smallDummyHeight < ((dimen[1] / 100) * percent)) {
            TextPaint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            size = size + 1;
            paintText.setTextSize(size);
            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
            paintText.setShadowLayer(3f, 3f, 3f, Color.BLACK);

            Rect smallDummyRect = new Rect();
            paintText.getTextBounds(title, 0, title.length(), smallDummyRect);

            smallDummyHeight = smallDummyRect.height();
        }

        int pL = dimen[0] / 100;
        int pT = dimen[1] / 100;

        if (length) {

            TextPaint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintText.setColor(Color.WHITE);
            paintText.setTextSize(size);
            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
            paintText.setShadowLayer(3f, 3f, 3f, Color.BLACK);

            Rect rectText = new Rect();
            paintText.getTextBounds(title, 0, title.length(), rectText);

            if (isGradient) {
                int GRADIENT_HEIGHT = rectText.height();

                Paint paint = new Paint();
                LinearGradient shader = new LinearGradient(0, 0, 0, GRADIENT_HEIGHT, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
                paint.setShader(shader);
                canvas.drawRect(0, 0, dimen[0], GRADIENT_HEIGHT, paint);
            }

            canvas.drawText(title, pL, rectText.height() + (3 * pT) / 2, paintText);
        } else {

            TextPaint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintText.setColor(Color.WHITE);
            paintText.setTextSize(size);
            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
            paintText.setShadowLayer(3f, 3f, 3f, Color.BLACK);

            StaticLayout mTextLayout = new StaticLayout(
                    title,
                    paintText,
                    canvas.getWidth(),
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0.0f,
                    false);

            if (isGradient) {
                int GRADIENT_HEIGHT = mTextLayout.getHeight();

                Paint paint = new Paint();
                LinearGradient shader = new LinearGradient(0, 0, 0, GRADIENT_HEIGHT, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
                paint.setShader(shader);
                canvas.drawRect(0, 0, dimen[0], GRADIENT_HEIGHT, paint);
            }

            canvas.save();
            canvas.translate(pL, pT);
            mTextLayout.draw(canvas);
            canvas.restore();
        }
    }
}