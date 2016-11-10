package com.daodao.map.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.daodao.map.R;

/**
 * Created by lzd on 2016/11/8.
 */

public class CustomProgressDialog extends Dialog {
    public CustomProgressDialog(Context context) {
        super(context, R.style.DefaultDialogTheme);
    }

    public CustomProgressDialog(Context context, int themeResId) {
        super(context, R.style.DefaultDialogTheme);
    }

    public CustomProgressDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom_progress);
        Window win = getWindow();
        WindowManager.LayoutParams params = win.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(params);
        win.setGravity(Gravity.CENTER);
    }
}
