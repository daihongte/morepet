package com.dlc.morepet.utils;

import android.app.Dialog;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Administrator on 2016/12/6.
 */

public class DialogUtils {
    public static void showDialog(Dialog dialog, WindowManager m, double height, double width) {
        /*
             * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置,
             * 可以直接调用getWindow(),表示获得这个Activity的Window
             * 对象,这样这可以以同样的方式改变这个Activity的属性.
             */
        Window dialogWindow = dialog.getWindow();
            /*
             * 将对话框的大小按屏幕大小的百分比设置
             */
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * height); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * width); // 宽度设置为屏幕的0.65
        dialogWindow.setAttributes(p);

        dialog.show();
    }
}
