package com.denizugur.ninegagsaver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.denizugur.core.Base;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Feedback dialog fragment.
 * <p/>
 * Provides an UI for sending bugs & suggestions on my email.
 */
public class FeedbackDialog extends DialogFragment {

    private Spinner mSpinner;
    private EditText mEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        assert activity != null;

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .iconRes(R.drawable.ic_action_feedback_white)
                .title(R.string.action_feedback)
                .customView(R.layout.feedback_dialog, true)
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.send)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        Context context = getActivity();
                        CharSequence message = mEditText.getText();

                        if (isMessageLongEnough(message)) {

                            int type = mSpinner.getSelectedItemPosition();
                            CharSequence title = createTitle(context, type);
                            CharSequence body = createBody(context, message);
                            send(title, body);
                        } else {
                            String toastText = getString(R.string.feedback_error_msg_too_short, 10);
                            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;
        mSpinner = (Spinner) view.findViewById(R.id.type);
        mEditText = (EditText) view.findViewById(R.id.message);

        return md;
    }

    private void send(@NonNull CharSequence title, @NonNull CharSequence body) {
        Activity context = getActivity();
        String[] recipients = {Base.SUPPORT_EMAIL};
        Intent intent = new Intent()
                .putExtra(Intent.EXTRA_EMAIL, recipients)
                .putExtra(Intent.EXTRA_SUBJECT, title)
                .putExtra(Intent.EXTRA_TEXT, body);

        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));

        startActivity(intent);
        dismiss();
    }

    /**
     * Creates the title of the email.
     *
     * @param type one of the following types:
     *             0 - issue
     *             1 - suggestion
     *             2 - other
     * @return the title of the email.
     */
    @NonNull
    private CharSequence createTitle(@NonNull Context context, int type) {
        Integer osVersion = Build.VERSION.SDK_INT;
        CharSequence[] types = new CharSequence[]{"issue", "suggestion", "other"};
        return VersionCheck.getVersionName(context) + ": " + osVersion + ", " + types[type];
    }

    /**
     * Creates the body of the email. It automatically adds some
     * info about the device.
     *
     * @param msg the message that been typed by user.
     * @return the body of the email
     */
    @NonNull
    private CharSequence createBody(@NonNull Context context, @NonNull CharSequence msg) {
        final String extra;

        do {
            PackageInfo pi;
            try {
                pi = context
                        .getPackageManager()
                        .getPackageInfo("com.denizugur.ninegagsaver", 0);
            } catch (PackageManager.NameNotFoundException e) {
                extra = "There was an exception while getting my own package info.";
                break;
            }

            JSONObject obj = new JSONObject();
            try {
                // App related stuff
                obj.put("app_version_code", pi.versionCode);
                obj.put("app_version_name", pi.versionName);

                // Device related stuff
                obj.put("language", Locale.getDefault().getLanguage());
                obj.put("android_version_release", android.os.Build.VERSION.RELEASE);
                obj.put("android_version_sdk_int", android.os.Build.VERSION.SDK_INT);
                obj.put("android_build_display", android.os.Build.DISPLAY);
                obj.put("android_build_brand", android.os.Build.BRAND);
                obj.put("android_build_model", android.os.Build.MODEL);
            } catch (JSONException ignored) {
                extra = "There was an exception while building JSON.";
                break;
            }

            extra = obj.toString().replaceAll(",\"", ", \"");
        } while (false);

        return msg + "\n\nExtras (added automatically & do not change):\n" + extra;
    }

    private boolean isMessageLongEnough(@Nullable CharSequence message) {
        return message != null && message.length() >= 10;
    }

}
