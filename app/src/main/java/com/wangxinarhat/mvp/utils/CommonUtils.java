package com.wangxinarhat.mvp.utils;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by wang on 2016/8/10.
 */
public class CommonUtils {
    public static void showShortSnackbar(@NonNull View view, @NonNull CharSequence msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static void showShortSnackbar(@NonNull View view, @StringRes int resId) {
        showShortSnackbar(view, view.getResources().getText(resId));
    }

    public static void showLongSnackbar(@NonNull View view, @NonNull CharSequence msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void showLongSnackbar(@NonNull View view, @StringRes int resId) {
        showLongSnackbar(view, view.getResources().getText(resId));
    }


}
