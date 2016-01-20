package com.fergus.esa.connection;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.fergus.esa.R;

/**
 * Author: svchosta (https://github.com/svchosta)
 * Date: 13.01.2016
 */
public class ConnectionErrorView {
    private static final int MOVE_IN_DP = 180;
    private View container;
    private TextView textViewErrorMessage;
    private boolean isVisible = false;

    private float height;
    private float originalY;


    public ConnectionErrorView(Context context, View view, final RetryListener listener) {
        container = view;
        textViewErrorMessage = (TextView) container.findViewById(R.id.textViewConnectionErrorMessage);
        view.findViewById(R.id.textViewButtonRetry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quickHide();
                listener.onRetry();
            }
        });

        height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MOVE_IN_DP, context.getResources().getDisplayMetrics());
    }


    public void show(String message) {
        textViewErrorMessage.setText(message);
        show();
    }


    public void show(@StringRes int resId) {
        textViewErrorMessage.setText(resId);
        show();
    }


    private void show() {
        originalY = container.getY();
        container.animate()
                .translationYBy(-0.7f * height)
                .setDuration(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        container.animate()
                                .translationYBy(height)
                                .setDuration(1000)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        super.onAnimationStart(animation);
                                        container.setVisibility(View.VISIBLE);
                                    }
                                });
                    }
                });

        isVisible = true;
    }


    public void hide() {
        container.animate()
                .alpha(0f)
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        quickHide();
                    }
                });

        isVisible = false;
    }


    public void quickHide() {
        container.setVisibility(View.GONE);
        container.setY(originalY);
    }


    public boolean isVisible() {
        return isVisible;
    }
}
