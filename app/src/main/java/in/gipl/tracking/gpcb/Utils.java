package in.gipl.tracking.gpcb;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public final class Utils {

    private static AlertDialog alertDialog;
    private final String TAG;
    private final FragmentActivity activity;

    public Utils(String TAG, FragmentActivity activity) {
        this.TAG = TAG;
        this.activity = activity;
    }

    public static AlertDialog showMessage(@NonNull FragmentActivity activity, @Nullable CharSequence message
    ) {
        return showMessage(activity, null, message, activity.getString(R.string.ok), null);
    }

    public static AlertDialog showMessage(@NonNull FragmentActivity activity,
                                          @Nullable CharSequence title, @Nullable CharSequence message
    ) {
        return showMessage(activity, title, message, activity.getString(R.string.ok), null);
    }

    public static AlertDialog showMessage(@NonNull FragmentActivity activity,
                                          @Nullable CharSequence title, @Nullable CharSequence message,
                                          @Nullable CharSequence positiveButton, @Nullable DialogInterface.OnClickListener positiveButtonListener
    ) {
        return showMessage(activity, title, message, positiveButton, positiveButtonListener, null, null);
    }

    public static AlertDialog showMessage(@NonNull FragmentActivity activity,
                                          @Nullable CharSequence title, @Nullable CharSequence message,
                                          @Nullable CharSequence positiveButton, @Nullable DialogInterface.OnClickListener positiveButtonListener,
                                          @Nullable CharSequence negativeButton, @Nullable DialogInterface.OnClickListener negativeButtonListener
    ) {
        if (alertDialog != null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppAlertDialogTheme)
                .setTitle(title)
                .setMessage(message);
        if (positiveButton != null) {
            builder.setPositiveButton(positiveButton, positiveButtonListener);
        }
        if (negativeButton != null) {
            builder.setNegativeButton(negativeButton, negativeButtonListener);
        }
        builder.setCancelable(false);
        alertDialog = builder.show();
        return alertDialog;
    }

    public void hideSoftKeyboard() {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public boolean isDeviceOnline() {
        boolean checkNet = false;
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            checkNet = networkInfo != null && networkInfo.isConnected();
            if (!checkNet) {
                showMessage(activity, "Warning", "Please Check your internet connection.", "Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.finishAffinity(activity);
                    }
                });
            }
        }
        return checkNet;
    }


}

