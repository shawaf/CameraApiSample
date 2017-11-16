package com.shawaf.cameraapisample.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Shawaf on 12/13/2016.
 */

public class SquareView extends FrameLayout {

    float screenWidth,screenHeight;

    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);

    }


}
