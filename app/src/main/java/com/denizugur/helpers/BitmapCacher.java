package com.denizugur.helpers;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.samples.zoomable.ZoomableDraweeView;

public class BitmapCacher {

    private Context context;
    private ZoomableDraweeView zdv;
    private Uri fileUri;
    private BitmapProcessor bp;

    public BitmapCacher(Context context, ZoomableDraweeView zdv) {
        this.context = context;
        this.zdv = zdv;
        bp = new BitmapProcessor(context);
    }

    public void setUri(Uri uri) {
        this.fileUri = uri;
    }

    public Bitmap getBitmap(Boolean original) {
        if (original) {
            return bp.originalBMP;
        } else {
            return bp.finalBMP;
        }
    }

    public void process(String title, Integer size) {
        bp.setSize(size);
        bp.setTitle(title);

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(fileUri)
                .setAutoRotateEnabled(true)
                .setPostprocessor(bp).build();

        PipelineDraweeController controller = (PipelineDraweeController)
                Fresco.newDraweeControllerBuilder()
                        .setImageRequest(imageRequest)
                        .setOldController(zdv.getController())
                        .setTapToRetryEnabled(true)
                        .build();

        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(context.getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setProgressBarImage(new ProgressBarDrawable())
                .build();

        zdv.setController(controller);
        zdv.setHierarchy(hierarchy);
    }

    public void reprocess(String title) {
        bp.setTitle(title);
        bp.update();
    }

    public void reprocess(Integer size) {
        bp.setSize(size);
        bp.update();
    }

    public void noprocess() {
        bp.isEmpty(true);
        bp.update();
    }
}
