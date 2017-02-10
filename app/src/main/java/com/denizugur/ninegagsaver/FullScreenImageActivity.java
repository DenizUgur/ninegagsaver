package com.denizugur.ninegagsaver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.samples.zoomable.ZoomableDraweeView;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        Intent intent = getIntent();

        Uri uri = Uri.parse(intent.getStringExtra("BitmapImage"));

        ZoomableDraweeView zdv = (ZoomableDraweeView) findViewById(R.id.imgFullScreen);

        ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
                .setAutoRotateEnabled(true);

        PipelineDraweeController controller = (PipelineDraweeController) //TODO: Fail after second attempt
                Fresco.newDraweeControllerBuilder()
                        .setImageRequest(imageRequestBuilder.build())
                        .setOldController(zdv.getController())
                        .setTapToRetryEnabled(true)
                        .build();

        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setProgressBarImage(new ProgressBarDrawable())
                .build();

        zdv.setController(controller);
        zdv.setHierarchy(hierarchy);
    }
}