package com.rdc.liyuzhen.audioviewplayer.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static Toast sToast;

    public static void showToast(Context context, String text) {
        if (sToast == null) {
            sToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            sToast.setDuration(Toast.LENGTH_SHORT);
            sToast.setText(text);
        }
        sToast.show();
    }

}
