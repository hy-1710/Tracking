package in.gipl.tracking.gpcb.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import in.gipl.tracking.gpcb.BuildConfig;
import in.gipl.tracking.gpcb.R;
import in.gipl.tracking.gpcb.Utils;
import in.gipl.tracking.gpcb.databinding.ActivityMainBinding;
import in.gipl.tracking.gpcb.prefs.AppPrefs;
import timber.log.Timber;

public final class LocationActivityHelper {

    private static final String TAG = "LocationActivityHelper";
    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    static AppPrefs appPrefs;
    private final FragmentActivity activity;
    String IMEINO;
    private OnLocationUpdatesListener listener;
    private boolean userLocationPermissionDenial;
    private boolean userLocationSettingsDenial;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient fusedLocationClient;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient settingsClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest locationRequest;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest locationSettingsRequest;
    /**
     * Callback for Location events.
     */
    private LocationCallback locationCallback;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private boolean requestingLocationUpdates;

    private static ActivityMainBinding binding;

    public LocationActivityHelper(@NonNull FragmentActivity activity, @NonNull OnLocationUpdatesListener listener, @NonNull AppPrefs appPrefs,
                                  @NonNull ActivityMainBinding binding) {
        this.userLocationPermissionDenial = false;
        this.userLocationSettingsDenial = false;
        this.requestingLocationUpdates = false;
        this.appPrefs = appPrefs;
        this.binding = binding;

        this.activity = activity;
        this.listener = listener;
        IMEINO = "000000000000000";
    }

    public static String getUniqueIMEIId(Context context) {
        Log.e(TAG, "getUniqueIMEIId: Called----");
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
         /*   if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }*/
            @SuppressLint("MissingPermission")
            String imei = telephonyManager.getDeviceId();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                @SuppressLint("MissingPermission")
                String str = telephonyManager.getImei();
                Log.e(TAG, "getUniqueIMEIId: IMEI ID------------ " + str);
            }

          /*  Log.e(TAG, "getUniqueIMEIId: value in sharepref----"+appPrefs.getIMEINO() );

            Log.e("imei  IMEI ID________", "=" + imei);*/

            if (appPrefs.getIMEINO() == null) {
                binding.tvIMEINO.setText(imei);
            } else {
                binding.tvIMEINO.setText(appPrefs.getIMEINO());

            }


            Timber.i("IMEI NO  is %s", imei);


            if (imei != null && !imei.isEmpty()) {
                //get successfull macAdd

                //865182031956444
                //000000000000000

                appPrefs.setIMEINO(imei);
                Log.d(TAG, "getUniqueIMEIId() returned: " + appPrefs.getIMEINO());


                return imei;
            } else {

                String serialNo = android.os.Build.SERIAL;
                appPrefs.setIMEINO(serialNo);

                return serialNo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "not_found";
    }

    public void create() {
        requestingLocationUpdates = false;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        settingsClient = LocationServices.getSettingsClient(activity);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
       // getUniqueIMEIId(activity);
    }

    public void pause() {
        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    public void resume() {
        if (!userLocationPermissionDenial && !userLocationSettingsDenial) {
            if (!requestingLocationUpdates) {
                requestingLocationUpdates = true;
                if (checkPermissions()) {
                    startLocationUpdates();
                } else {
                    requestPermissions();
                }
            }
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int FirstPermissionState = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int SecpndPermisionState = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.READ_PHONE_STATE);


        return FirstPermissionState == PackageManager.PERMISSION_GRANTED &&
                SecpndPermisionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Timber.i("Requesting permission");
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSIONS_REQUEST_CODE);


    }

    /**
     * Callback received when a permissions request has been completed.
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Timber.i("onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Timber.i("User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestingLocationUpdates) {
                    Timber.i("Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

                getUniqueIMEIId(activity);
                // String imei = telephonyManager.getDeviceId();
                String imei = getUniqueIMEIId(activity);
                binding.tvIMEINO.setText(imei);

                appPrefs.setIMEINO(imei);
                Timber.i("Permission granted, updates requested, starting location updates");

            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                userLocationPermissionDenial = true;
                Utils.showMessage(activity, null, activity.getString(R.string.permission_denied_explanation),
                        activity.getString(R.string.settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                            }
                        });
            }
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Location is currentLocation :D
                Location currentLocation = locationResult.getLastLocation();
                Timber.i("currentLocation is %s", currentLocation);
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    if (listener != null) {
                        listener.onLocationUpdate(currentLocation);
                    }
                    getUniqueIMEIId(activity);
                }

                stopLocationUpdates();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Timber.i("User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Timber.i("User chose not to make required location settings changes.");
                        userLocationSettingsDenial = true;
                        requestingLocationUpdates = false;
                        if (listener != null) {
                            listener.onLocationError(new Exception("User chose not to make required location settings changes"));
                        }
                        break;
                }
                break;
        }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Timber.i("All location settings are satisfied.");

                        if (!activity.isFinishing() && !activity.isDestroyed()) {
                            fusedLocationClient.requestLocationUpdates(locationRequest,
                                    locationCallback, Looper.myLooper());
                        }
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        if (!activity.isFinishing() && !activity.isDestroyed()) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    Timber.i("Location settings are not satisfied. Attempting to upgrade location settings ");
                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the
                                        // result in onActivityResult().
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Timber.i("PendingIntent unable to execute request.");
                                        if (listener != null) {
                                            listener.onLocationError(e);
                                        }
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Location Settings.";
                                    Timber.e(errorMessage);
                                    Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                                    requestingLocationUpdates = false;
                                    if (listener != null) {
                                        listener.onLocationError(new Exception(errorMessage));
                                    }
                            }
                        }
                    }
                });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!requestingLocationUpdates) {
            Timber.d("stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        requestingLocationUpdates = false;
                    }
                });
    }

    public interface OnLocationUpdatesListener {
        void onLocationUpdate(Location location);

        void onLocationError(Exception e);
    }


}
