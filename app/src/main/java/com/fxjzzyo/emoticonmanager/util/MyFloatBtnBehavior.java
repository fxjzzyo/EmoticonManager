package com.fxjzzyo.emoticonmanager.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

/**
 * Created by fxjzzyo
 * on date 2019/7/13 0013
 */
public class MyFloatBtnBehavior extends CoordinatorLayout.Behavior<View>{

    public MyFloatBtnBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float translationY = -dependency.getY();
        int height = child.getHeight();
        int scale = 308/height;
        float needTrans = translationY * scale;
        child.setTranslationY(needTrans);
        return true;
    }

}
