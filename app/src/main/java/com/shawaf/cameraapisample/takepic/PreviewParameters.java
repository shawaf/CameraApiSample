package com.shawaf.cameraapisample.takepic;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

/**
 * Created by Shawaf on 1/14/2017.
 */

public class PreviewParameters {

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;
    private static PreviewParameters previewParameters;

    public static PreviewParameters getInstance() {
        if (previewParameters == null) previewParameters = new PreviewParameters();
        return previewParameters;
    }


    public Camera.Size determineBestPreviewSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
    }

    public Camera.Size determineOptimalPreviewSize(Camera.Parameters parameters , int w,int h) {
        return getOptimalPreviewSize(parameters.getSupportedPreviewSizes(),w,h );
    }

    public Camera.Size determineBestPictureSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
    }

    public Camera.Size determineBestSize(List<Camera.Size> sizes, int widthThreshold) {
        Camera.Size bestSize = null;
        Camera.Size size;
        int numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);
            boolean isDesireRatio = (size.width / 4) == (size.height / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            Log.d("PreviewParameters", "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }
    public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
