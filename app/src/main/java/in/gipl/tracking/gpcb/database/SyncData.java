package in.gipl.tracking.gpcb.database;

import android.support.annotation.NonNull;

public class SyncData {

    public static final String TABLE_NAME = "SyncLog";

    public static final String LAST_TRACK_LOG_ID = "LastTrackLogId";
    public static final String SYNC_ID = "SyncId";
    public static final String IMEI_NO = "IMEINo";
    public static final String TOTAL_SYNC_RECORD = "TotalSyncRecord";
    public static final String CREATED_ON = "CreatedOn";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + SYNC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + LAST_TRACK_LOG_ID + " INTEGER,"
                    + IMEI_NO + " TEXT,"
                    + TOTAL_SYNC_RECORD + " INTEGER,"
                    + CREATED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";


    private long syncID;
    private long lastTrackLogId;
    private long userId;
    private int totalSync;
    private String timestamp;

    public SyncData() {
        this.syncID = 0L;
        this.lastTrackLogId = 0L;
        this.userId = 0L;
        this.totalSync = 0;
        this.timestamp = "";
    }

    public SyncData(long syncID, long lastTrackLogId, long userId, int totalSync, @NonNull String timestamp) {
        this.syncID = syncID;
        this.lastTrackLogId = lastTrackLogId;
        this.userId = userId;
        this.totalSync = totalSync;
        this.timestamp = timestamp;
    }

    public long getSyncID() {
        return syncID;
    }

    public void setSyncID(long syncID) {
        this.syncID = syncID;
    }

    public long getLastTrackLogId() {
        return lastTrackLogId;
    }

    public void setLastTrackLogId(long lastTrackLogId) {
        this.lastTrackLogId = lastTrackLogId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getTotalSync() {
        return totalSync;
    }

    public void setTotalSync(int totalSync) {
        this.totalSync = totalSync;
    }

    public String getTimestamp() {
        if (timestamp == null) {
            timestamp = "";
        }
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SyncData{" +
                "syncID=" + syncID +
                ", lastTrackLogId=" + lastTrackLogId +
                ", IMEINo=" + userId +
                ", totalSync=" + totalSync +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
