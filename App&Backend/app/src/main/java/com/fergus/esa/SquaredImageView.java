package com.fergus.esa;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/*
Picasso An image view which always remains square with respect to its width.
Available from https://github.com/square/picasso/tree/master/picasso-sample/src/main/java/com/example/picasso
*/
public class SquaredImageView extends ImageView {
    public SquaredImageView(Context context) {
        super(context);
    }


    public SquaredImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}