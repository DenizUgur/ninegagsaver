package com.denizugur.dialogs;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.denizugur.ninegagsaver.R;


public class HelpDialog {

    private final Context context;

    public HelpDialog(Context context) {
        this.context = context;
    }

    public void show() {
        TextView tv = new TextView(context);
        if (Build.VERSION.SDK_INT < 23) {
            tv.setTextAppearance(context, R.style.dialog_tv_style);
        } else {
            tv.setTextAppearance(R.style.dialog_tv_style);
        }
        tv.setText(Html.fromHtml(context.getString(R.string.help_body)));
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .iconRes(R.drawable.ic_action_help_white)
                .title(context.getResources().getString(R.string.action_help))
                .customView(tv, true)
                .autoDismiss(false)
                .cancelable(false)
                .positiveText(R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build();

        dialog.show();

        final MDButton button = dialog.getActionButton(DialogAction.POSITIVE);
        final android.os.Handler mHandler = new android.os.Handler();

        Runnable runnable = new Runnable() {
            Integer second = 6;
            Boolean mStopHandler = false;

            @Override
            public void run() {
                if (second == 0) {
                    button.setEnabled(true);
                    button.setText(context.getString(R.string.ok));
                    mStopHandler = true;
                } else {
                    if (button.isEnabled()) button.setEnabled(false);
                    String text = context.getString(R.string.ok) + " (" + second.toString() + ")";
                    button.setText(text);
                }
                if (!mStopHandler) {
                    mHandler.postDelayed(this, 1000);
                    second = second - 1;
                }
            }
        };

        mHandler.post(runnable);
    }
}