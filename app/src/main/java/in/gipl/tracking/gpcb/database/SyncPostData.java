package in.gipl.tracking.gpcb.database;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class SyncPostData {

    public static final String TAG = SyncPostData.class.getSimpleName();

    @SerializedName("status")
    private String status;

    public SyncPostData(String status) {
        this.status = status;
    }

    public static String getTAG() {
        return TAG;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SyncPostData{" +
                "status=" + status +


                '}';
    }
}
